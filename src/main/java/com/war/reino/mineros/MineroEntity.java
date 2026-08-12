package com.war.reino.mineros;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public class MineroEntity extends PathfinderMob {

    public enum EstadoMinero {
        ESPERANDO_INSTRUCCION, 
        VIAJANDO_A_MINA,       
        MINANDO,               
        REGRESANDO_AL_COFRE,   
        DEPOSITANDO,           
        REPORTE_FINAL          
    }

    private EstadoMinero estado = EstadoMinero.ESPERANDO_INSTRUCCION;

    private final Map<String, Integer> objetivoMinerales = new HashMap<>();

    private final Map<String, Integer> recolectado = new HashMap<>();

    @Nullable
    private BlockPos cofreVinculado;

    private final List<BlockPos> rutaNodos = new ArrayList<>();

    private int indiceNodoActual = 0;

    public MineroEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder crearAtributos() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D) // Vida como la de un jugador/aldeano
                .add(Attributes.MOVEMENT_SPEED, 0.5D); // Velocidad al caminar
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this)); 
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));

        this.goalSelector.addGoal(1, new SeguirRutaNodosGoal(this));

    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason spawnReason,
                                        @Nullable SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    public EstadoMinero getEstado() {
        return estado;
    }

    public void setEstado(EstadoMinero nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public void asignarMision(String idMineral, int cantidad) {
        objetivoMinerales.put(idMineral, cantidad);
        recolectado.putIfAbsent(idMineral, 0);
        if (estado == EstadoMinero.ESPERANDO_INSTRUCCION) {
            estado = EstadoMinero.VIAJANDO_A_MINA;
        }
    }

    public Map<String, Integer> getObjetivoMinerales() {
        return objetivoMinerales;
    }

    public Map<String, Integer> getRecolectado() {
        return recolectado;
    }

    public boolean cuotaCumplida() {
        for (Map.Entry<String, Integer> entry : objetivoMinerales.entrySet()) {
            int tenido = recolectado.getOrDefault(entry.getKey(), 0);
            if (tenido < entry.getValue()) {
                return false;
            }
        }
        return !objetivoMinerales.isEmpty();
    }

    public void registrarRecoleccion(String idMineral, int cantidad) {
        recolectado.merge(idMineral, cantidad, Integer::sum);
    }

    @Nullable
    public BlockPos getCofreVinculado() {
        return cofreVinculado;
    }

    public void setCofreVinculado(BlockPos pos) {
        this.cofreVinculado = pos;
    }

    public List<BlockPos> getRutaNodos() {
        return rutaNodos;
    }

    public void setRutaNodos(List<BlockPos> nodos) {
        this.rutaNodos.clear();
        this.rutaNodos.addAll(nodos);
        this.indiceNodoActual = 0;
    }

    public int getIndiceNodoActual() {
        return indiceNodoActual;
    }

    public void avanzarNodo() {
        indiceNodoActual++;
    }

    public void retrocederNodo() {
        indiceNodoActual--;
    }

    public void setIndiceNodoActual(int indice) {
        this.indiceNodoActual = indice;
    }

    public void iniciarViajeHaciaMina(List<BlockPos> nodosRuta) {
        setRutaNodos(nodosRuta);
        setIndiceNodoActual(0);
        estado = EstadoMinero.VIAJANDO_A_MINA;
    }

    public void reiniciarMision() {
        objetivoMinerales.clear();
        recolectado.clear();
        rutaNodos.clear();
        indiceNodoActual = 0;
        estado = EstadoMinero.ESPERANDO_INSTRUCCION;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        if (cofreVinculado != null) {
            tag.putLong("CofreVinculado", cofreVinculado.asLong());
        }
        tag.putString("EstadoMinero", estado.name());
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);

        estado = tag.getString("EstadoMinero")
                .map(EstadoMinero::valueOf)
                .orElse(EstadoMinero.ESPERANDO_INSTRUCCION);

        cofreVinculado = tag.getLong("CofreVinculado")
                .map(BlockPos::of)
                .orElse(null);
    }
}
