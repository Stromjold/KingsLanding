package com.war.reino.aldeanosrecolectoresdemadera;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Silencia las advertencias estrictas del IDE sobre anotaciones @NonNull de Minecraft
@SuppressWarnings("null")
public class AldeanosRecolectoresMadera {

    private static final int CAPACIDAD_INVENTARIO = 16;
    private static final int USOS_HACHA = 50;
    private static final int RADIO_BUSQUEDA = 30;

    private final Map<UUID, WorkerState> workers = new HashMap<>();
    private final Map<UUID, BaseState> bases = new HashMap<>();

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("talar").executes(context -> {
                if (context.getSource().getEntity() instanceof ServerPlayer jugador) {
                    activarLenadores(jugador);
                    return Command.SINGLE_SUCCESS;
                }
                return 0;
            }));

            dispatcher.register(Commands.literal("guardar").executes(context -> {
                if (context.getSource().getEntity() instanceof ServerPlayer jugador) {
                    ordenarGuardar(jugador);
                    return Command.SINGLE_SUCCESS;
                }
                return 0;
            }));
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            // Eliminado el "if (sender == null)" para solucionar el Dead Code
            String texto = message.signedContent().trim().toLowerCase();
            if (texto.equals("talar")) {
                activarLenadores(sender);
            } else if (texto.equals("guardar")) {
                ordenarGuardar(sender);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer jugador : server.getPlayerList().getPlayers()) {
                BaseState base = bases.computeIfAbsent(
                        jugador.getUUID(),
                        id -> crearBaseVirtual(jugador)
                );

                List<WorkerState> estados = new ArrayList<>(workers.values());

                for (WorkerState worker : estados) {
                    if (!worker.active || !jugador.getUUID().equals(worker.playerId)) {
                        continue;
                    }

                    ServerLevel level = (ServerLevel) jugador.level();
                    Entity entity = level.getEntity(worker.villagerId);
                    
                    if (!(entity instanceof Villager aldeano)) {
                        continue; 
                    }
                    
                    if (aldeano.isDeadOrDying()) {
                        worker.active = false;
                        continue;
                    }

                    procesarLogicaAldeano(aldeano, level, base, worker);
                }
            }
        });
    }

    private void ordenarGuardar(ServerPlayer jugador) {
        int count = 0;
        ServerLevel level = (ServerLevel) jugador.level();
        BaseState base = bases.get(jugador.getUUID());

        for (WorkerState worker : workers.values()) {
            if (worker.active && jugador.getUUID().equals(worker.playerId)) {
                if (!worker.inventario.isEmpty()) {
                    
                    if (worker.currentTarget != null) {
                        Entity entity = level.getEntity(worker.villagerId);
                        if (entity instanceof Villager aldeano) {
                            limpiarProgreso(level, aldeano, worker.currentTarget);
                        }
                        if (base != null) {
                            base.claimedBlocks.remove(worker.currentTarget);
                        }
                        worker.currentTarget = null;
                    }

                    if (base != null && !base.construida) {
                        construirBaseFisica(level, base);
                    }

                    worker.miningTick = 0;
                    worker.tiempoTala = 0;
                    worker.estadoActual = EstadoAldeano.IR_A_COFRE;
                    count++;
                }
            }
        }

        if (count > 0) {
            jugador.sendSystemMessage(Component.literal("§a[Sistema] §f" + count + " aldeano(s) van a guardar la madera."), false);
        } else {
            jugador.sendSystemMessage(Component.literal("§e[Sistema] §fNo hay aldeanos activos con madera en este momento."), false);
        }
    }

    private void activarLenadores(ServerPlayer jugador) {
        ServerLevel level = (ServerLevel) jugador.level();
        BaseState base = bases.computeIfAbsent(
                jugador.getUUID(),
                id -> crearBaseVirtual(jugador)
        );

        int count = 0;

        for (Villager aldeano : level.getEntitiesOfClass(
                Villager.class,
                jugador.getBoundingBox().inflate(40.0D)
        )) {
            if (!aldeano.hasCustomName()) continue;

            Component nombreComponent = aldeano.getCustomName();
            if (nombreComponent == null) continue;

            String nombre = nombreComponent
                    .getString()
                    .replaceAll("§.", "")
                    .trim()
                    .toLowerCase();

            if (!nombre.contains("leñador") && !nombre.contains("lenador")) {
                continue;
            }

            WorkerState worker = workers.computeIfAbsent(
                    aldeano.getUUID(),
                    id -> new WorkerState()
            );

            worker.active = true;
            worker.playerId = jugador.getUUID();
            worker.villagerId = aldeano.getUUID();
            worker.currentTarget = null;
            worker.bloquesIgnorados.clear();
            worker.miningTick = 0;
            worker.tiempoTala = 0;

            if (!base.construida) {
                worker.estadoSiguiente = EstadoAldeano.CONSTRUIR_BASE;
                worker.estadoActual = EstadoAldeano.BUSCAR_RECURSOS_INICIALES;
            } else if (aldeano.getMainHandItem().getItem() instanceof AxeItem) {
                worker.estadoSiguiente = EstadoAldeano.BUSCAR_ARBOL;
                worker.estadoActual = EstadoAldeano.BUSCAR_ARBOL;
            } else {
                worker.estadoSiguiente = EstadoAldeano.BUSCAR_ARBOL;
                worker.estadoActual = EstadoAldeano.IR_A_MESA_CRAFTEO;
            }

            count++;
        }

        if (count > 0) {
            jugador.sendSystemMessage(Component.literal("§a[Sistema] §f" + count + " aldeano(s) leñador(es) activado(s)."), false);
        } else {
            jugador.sendSystemMessage(Component.literal("§c[Error] §fNo se encontró ningún aldeano con la etiqueta 'Leñador' cerca."), false);
        }
    }

    private void procesarLogicaAldeano(Villager aldeano, ServerLevel level, BaseState base, WorkerState worker) {
        worker.tickCounter++;

        switch (worker.estadoActual) {
            case BUSCAR_RECURSOS_INICIALES -> {
                if (worker.currentTarget == null) {
                    worker.currentTarget = encontrarTroncoMasBajo(level, aldeano.blockPosition(), RADIO_BUSQUEDA, base, worker);
                }
                if (worker.currentTarget != null) {
                    worker.estadoSiguiente = EstadoAldeano.CONSTRUIR_BASE;
                    worker.estadoActual = EstadoAldeano.IR_A_ARBOL;
                }
            }

            case CONSTRUIR_BASE -> {
                if (acercarseA(aldeano, base.basePos, 0.9D)) {
                    construirBaseFisica(level, base);
                    worker.estadoActual = EstadoAldeano.IR_A_MESA_CRAFTEO;
                }
            }

            case IR_A_MESA_CRAFTEO -> {
                if (acercarseA(aldeano, base.craftingTablePos, 0.9D)) {
                    aldeano.swing(InteractionHand.MAIN_HAND);
                    aldeano.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WOODEN_AXE));
                    worker.usosHacha = 0;
                    worker.estadoActual = EstadoAldeano.BUSCAR_ARBOL;
                }
            }

            case BUSCAR_ARBOL -> {
                if (inventarioLleno(worker)) {
                    worker.estadoActual = EstadoAldeano.IR_A_COFRE;
                    break;
                }
                if (!(aldeano.getMainHandItem().getItem() instanceof AxeItem)) {
                    worker.estadoActual = EstadoAldeano.IR_A_MESA_CRAFTEO;
                    break;
                }

                worker.currentTarget = encontrarTroncoMasBajo(level, aldeano.blockPosition(), RADIO_BUSQUEDA, base, worker);

                if (worker.currentTarget != null) {
                    worker.estadoSiguiente = EstadoAldeano.BUSCAR_ARBOL;
                    worker.estadoActual = EstadoAldeano.IR_A_ARBOL;
                }
            }

            case IR_A_ARBOL -> {
                if (worker.currentTarget == null || !esBloqueMadera(level.getBlockState(worker.currentTarget))) {
                    reiniciarObjetivo(worker, base);
                    break;
                }

                if (acercarseA(aldeano, worker.currentTarget, 0.9D)) {
                    worker.miningTick = 0;
                    worker.tiempoTala = 0;
                    worker.estadoActual = EstadoAldeano.TALAR;
                } else if (worker.tickCounter % 100 == 0 && aldeano.getNavigation().isDone()) {
                    worker.bloquesIgnorados.add(worker.currentTarget);
                    reiniciarObjetivo(worker, base);
                }
            }

            case TALAR -> talarBloque(level, aldeano, base, worker);

            case IR_A_COFRE -> {
                if (base.cofres.isEmpty()) {
                    construirBaseFisica(level, base);
                }

                if (acercarseA(aldeano, base.cofres.get(0), 0.9D)) {
                    worker.estadoActual = EstadoAldeano.GUARDAR_MADERA;
                }
            }

            case GUARDAR_MADERA -> {
                almacenarInventarioEnCofres(level, base, worker);
                worker.estadoActual = worker.inventario.isEmpty()
                        ? EstadoAldeano.BUSCAR_ARBOL
                        : EstadoAldeano.IR_A_COFRE;
            }
        }
    }

    private void talarBloque(ServerLevel level, Villager aldeano, BaseState base, WorkerState worker) {
        if (worker.currentTarget == null) {
            reiniciarObjetivo(worker, base);
            return;
        }

        BlockPos objetivo = worker.currentTarget;
        BlockState estado = level.getBlockState(objetivo);

        if (!esBloqueMadera(estado)) {
            limpiarProgreso(level, aldeano, objetivo);
            reiniciarObjetivo(worker, base);
            return;
        }

        aldeano.getLookControl().setLookAt(
                objetivo.getX() + 0.5D,
                objetivo.getY() + 0.5D,
                objetivo.getZ() + 0.5D
        );

        if (worker.tiempoTala == 0) {
            boolean tieneHacha = aldeano.getMainHandItem().getItem() instanceof AxeItem;
            worker.tiempoTala = tieneHacha ? 30 : 80; 
        }

        worker.miningTick++;

        if (worker.tickCounter % 6 == 0) {
            aldeano.swing(InteractionHand.MAIN_HAND);
        }

        int progreso = Math.min(9, (int) ((worker.miningTick / (float) worker.tiempoTala) * 10.0F));
        level.destroyBlockProgress(aldeano.getId(), objetivo, progreso);

        if (worker.miningTick < worker.tiempoTala) {
            return;
        }

        recogerBloqueSinDrop(level, aldeano, worker, objetivo, estado);
        limpiarProgreso(level, aldeano, objetivo);
        base.claimedBlocks.remove(objetivo);
        worker.currentTarget = null;
        worker.miningTick = 0;
        worker.tiempoTala = 0;

        if (aldeano.getMainHandItem().getItem() instanceof AxeItem) {
            worker.usosHacha++;
            if (worker.usosHacha >= USOS_HACHA) {
                aldeano.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                worker.usosHacha = 0;
            }
        }

        if (!base.construida) {
            worker.estadoActual = EstadoAldeano.CONSTRUIR_BASE;
        } else if (inventarioLleno(worker)) {
            worker.estadoActual = EstadoAldeano.IR_A_COFRE;
        } else if (!(aldeano.getMainHandItem().getItem() instanceof AxeItem)) {
            worker.estadoActual = EstadoAldeano.IR_A_MESA_CRAFTEO;
        } else {
            worker.estadoActual = worker.estadoSiguiente;
        }
    }

    private void recogerBloqueSinDrop(ServerLevel level, Villager aldeano, WorkerState worker, BlockPos pos, BlockState state) {
        level.removeBlockEntity(pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        
        agregarAInventario(worker, new ItemStack(state.getBlock().asItem(), 1));
    }

    private void limpiarProgreso(ServerLevel level, Villager aldeano, BlockPos pos) {
        level.destroyBlockProgress(aldeano.getId(), pos, -1);
    }

    private boolean acercarseA(Villager aldeano, BlockPos target, double speed) {
        double distancia = aldeano.distanceToSqr(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);

        if (distancia <= 5.0D) {
            aldeano.getNavigation().stop();
            return true;
        }

        if (aldeano.tickCount % 20 == 0) {
            aldeano.getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, speed);
        }
        return false;
    }

    private void reiniciarObjetivo(WorkerState worker, BaseState base) {
        if (worker.currentTarget != null) base.claimedBlocks.remove(worker.currentTarget);
        worker.currentTarget = null;
        worker.miningTick = 0;
        worker.tiempoTala = 0;
        worker.estadoActual = worker.estadoSiguiente == EstadoAldeano.CONSTRUIR_BASE
                ? EstadoAldeano.BUSCAR_RECURSOS_INICIALES
                : EstadoAldeano.BUSCAR_ARBOL;
    }

    private BlockPos encontrarTroncoMasBajo(ServerLevel level, BlockPos origen, int radio, BaseState base, WorkerState worker) {
        BlockPos masCercano = null;
        double minDistancia = Double.MAX_VALUE;

        for (int dx = -radio; dx <= radio; dx++) {
            for (int dy = -10; dy <= 10; dy++) {
                for (int dz = -radio; dz <= radio; dz++) {
                    BlockPos pos = origen.offset(dx, dy, dz);
                    if (!esBloqueMadera(level.getBlockState(pos))) continue;

                    BlockPos troncoBajo = pos;
                    while (esBloqueMadera(level.getBlockState(troncoBajo.below()))) {
                        troncoBajo = troncoBajo.below();
                    }

                    if (base.claimedBlocks.contains(troncoBajo) || worker.bloquesIgnorados.contains(troncoBajo)) continue;

                    double distancia = origen.distSqr(troncoBajo);
                    if (distancia < minDistancia) {
                        minDistancia = distancia;
                        masCercano = troncoBajo;
                    }
                }
            }
        }

        if (masCercano != null) base.claimedBlocks.add(masCercano);
        return masCercano;
    }

    private boolean esBloqueMadera(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    private int contarMadera(WorkerState worker) {
        int total = 0;
        for (ItemStack stack : worker.inventario) total += stack.getCount();
        return total;
    }

    private boolean inventarioLleno(WorkerState worker) {
        return contarMadera(worker) >= CAPACIDAD_INVENTARIO;
    }

    private boolean agregarAInventario(WorkerState worker, ItemStack stack) {
        if (stack.isEmpty()) return true;

        int espacioTotal = CAPACIDAD_INVENTARIO - contarMadera(worker);
        if (espacioTotal <= 0) return false;

        ItemStack restante = stack.copy();
        restante.setCount(Math.min(restante.getCount(), espacioTotal));

        for (ItemStack existente : worker.inventario) {
            if (existente.getItem() != restante.getItem()) continue;

            int espacio = existente.getMaxStackSize() - existente.getCount();
            int cantidad = Math.min(espacio, restante.getCount());
            existente.grow(cantidad);
            restante.shrink(cantidad);
            if (restante.isEmpty()) return true;
        }

        while (!restante.isEmpty()) {
            int cantidad = Math.min(restante.getMaxStackSize(), restante.getCount());
            worker.inventario.add(restante.split(cantidad));
        }
        return true;
    }

    private void almacenarInventarioEnCofres(ServerLevel level, BaseState base, WorkerState worker) {
        Iterator<ItemStack> iterator = worker.inventario.iterator();

        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            for (BlockPos chestPos : base.cofres) {
                if (stack.isEmpty()) break;
                insertarEnCofre(level, chestPos, stack);
            }
            if (stack.isEmpty()) iterator.remove();
        }

        if (!worker.inventario.isEmpty()) {
            BlockPos nuevoCofre = crearNuevoCofre(level, base);
            Iterator<ItemStack> restantes = worker.inventario.iterator();
            while (restantes.hasNext()) {
                ItemStack stack = restantes.next();
                insertarEnCofre(level, nuevoCofre, stack);
                if (stack.isEmpty()) restantes.remove();
            }
        }
    }

    private boolean insertarEnCofre(ServerLevel level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) return true;

        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof ChestBlockEntity chest)) return false;

        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack actual = chest.getItem(i);
            
            if (actual.isEmpty() || actual.getItem() != stack.getItem()) continue;

            int espacio = Math.min(actual.getMaxStackSize(), chest.getMaxStackSize()) - actual.getCount();
            if (espacio <= 0) continue;

            int cantidad = Math.min(espacio, stack.getCount());
            actual.grow(cantidad);
            stack.shrink(cantidad);
            if (stack.isEmpty()) {
                chest.setChanged();
                return true;
            }
        }

        for (int i = 0; i < chest.getContainerSize(); i++) {
            if (!chest.getItem(i).isEmpty()) continue;
            int cantidad = Math.min(stack.getCount(), chest.getMaxStackSize());
            chest.setItem(i, stack.split(cantidad));
            if (stack.isEmpty()) {
                chest.setChanged();
                return true;
            }
        }

        chest.setChanged();
        return stack.isEmpty();
    }

    private BaseState crearBaseVirtual(ServerPlayer jugador) {
        BaseState base = new BaseState();
        ServerLevel level = (ServerLevel) jugador.level();
        
        BlockPos pos = jugador.blockPosition().offset(3, 0, 3);
        
        while (pos.getY() > level.getMinY() && level.isEmptyBlock(pos.below())) {
            pos = pos.below();
        }
        while (!level.isEmptyBlock(pos) && pos.getY() < level.getMaxY()) {
            pos = pos.above();
        }

        base.basePos = pos;
        base.craftingTablePos = base.basePos;
        base.cofres.add(base.basePos.east());
        base.construida = false;
        return base;
    }

    private void construirBaseFisica(ServerLevel level, BaseState base) {
        if (base.construida) return;

        if (level.isEmptyBlock(base.craftingTablePos)) {
            level.setBlockAndUpdate(base.craftingTablePos, Blocks.CRAFTING_TABLE.defaultBlockState());
        }

        if (!base.cofres.isEmpty()) {
            BlockPos cofre = base.cofres.get(0);
            if (level.isEmptyBlock(cofre)) {
                level.setBlockAndUpdate(cofre, Blocks.CHEST.defaultBlockState());
            }
        }

        base.construida = true;
    }

    private BlockPos crearNuevoCofre(ServerLevel level, BaseState base) {
        int index = base.cofres.size();
        BlockPos pos = base.basePos.offset(2 + index * 2, 0, 0);

        for (int i = 0; i < 10 && !level.isEmptyBlock(pos); i++) {
            pos = pos.above();
        }

        if (!level.isEmptyBlock(pos)) return base.cofres.get(0);

        level.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
        base.cofres.add(pos);
        return pos;
    }

    private enum EstadoAldeano {
        BUSCAR_RECURSOS_INICIALES,
        CONSTRUIR_BASE,
        IR_A_MESA_CRAFTEO,
        BUSCAR_ARBOL,
        IR_A_ARBOL,
        TALAR,
        IR_A_COFRE,
        GUARDAR_MADERA
    }

    private static class WorkerState {
        private boolean active;
        private UUID playerId;
        private UUID villagerId;
        private EstadoAldeano estadoActual;
        private EstadoAldeano estadoSiguiente;
        private BlockPos currentTarget;
        private final Set<BlockPos> bloquesIgnorados = new HashSet<>();
        private int tickCounter;
        private int miningTick;
        private int tiempoTala;
        private int usosHacha;
        private final List<ItemStack> inventario = new ArrayList<>();
    }

    private static class BaseState {
        private boolean construida;
        private BlockPos basePos;
        private BlockPos craftingTablePos;
        private final List<BlockPos> cofres = new ArrayList<>();
        private final Set<BlockPos> claimedBlocks = new HashSet<>();
    }
}