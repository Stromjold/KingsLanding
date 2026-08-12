package com.war.reino.mineros;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;

import java.util.HashMap;
import java.util.Map;

public class ComandosReino {

    private static int contadorMinerosLibres = 1;
    private static final Map<String, Integer> contadorPelotones = new HashMap<>();

    public static void registrarComandos() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("reino")

                    .then(Commands.literal("etiqueta")
                            .then(Commands.literal("minero").executes(context -> {
                                ServerPlayer p = context.getSource().getPlayerOrException();
                                ItemStack etiqueta = new ItemStack(Items.NAME_TAG);
                                etiqueta.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("Minero"));
                                p.getInventory().add(etiqueta);
                                return 1;
                            }))
                            .then(Commands.literal("nodo").executes(context -> {
                                ServerPlayer p = context.getSource().getPlayerOrException();
                                ItemStack etiqueta = new ItemStack(Items.NAME_TAG);
                                etiqueta.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("Nodo"));
                                p.getInventory().add(etiqueta);
                                return 1;
                            }))
                    )

                    .then(Commands.literal("reclutar")
                            .then(Commands.argument("objetivo", EntityArgument.entity())
                                    .executes(context -> {
                                        Entity entidad = EntityArgument.getEntity(context, "objetivo");

                                        if (!(entidad instanceof MineroEntity)) {
                                            MineroEntity nuevoMinero = EntidadesReino.MINERO.create(entidad.level(), EntitySpawnReason.CONVERSION);
                                            if (nuevoMinero != null) {

                                                nuevoMinero.setPos(entidad.getX(), entidad.getY(), entidad.getZ());
                                                nuevoMinero.setYRot(entidad.getYRot());
                                                nuevoMinero.setXRot(entidad.getXRot());

                                                String nombreAsignado = "minero_" + contadorMinerosLibres;
                                                nuevoMinero.setCustomName(Component.literal(nombreAsignado));
                                                nuevoMinero.setCustomNameVisible(false);

                                                entidad.level().addFreshEntity(nuevoMinero);
                                                entidad.discard();

                                                contadorMinerosLibres++;

                                                context.getSource().sendSystemMessage(Component.literal(" ¡Reclutado! Identificación: " + nombreAsignado).withStyle(ChatFormatting.GREEN));
                                            }
                                        } else {
                                            context.getSource().sendSystemMessage(Component.literal(" Esa entidad ya es un trabajador.").withStyle(ChatFormatting.RED));
                                        }
                                        return 1;
                                    })
                            )
                    )

                    .then(Commands.literal("peloton")
                            .then(Commands.literal("crear")
                                    .then(Commands.argument("nombre_escuadron", StringArgumentType.word())
                                            .executes(context -> {
                                                String nombre = StringArgumentType.getString(context, "nombre_escuadron");
                                                contadorPelotones.putIfAbsent(nombre, 1);
                                                context.getSource().sendSystemMessage(Component.literal("🚩 Pelotón '" + nombre + "' inicializado.").withStyle(ChatFormatting.GOLD));
                                                return 1;
                                            })
                                    )
                            )
                            .then(Commands.literal("asignar")
                                    .then(Commands.argument("nombre_escuadron", StringArgumentType.word())
                                            .then(Commands.argument("identificacion", StringArgumentType.word())
                                                    .executes(context -> {
                                                        String escuadron = StringArgumentType.getString(context, "nombre_escuadron");
                                                        String idBusqueda = StringArgumentType.getString(context, "identificacion");
                                                        ServerLevel nivel = context.getSource().getLevel();

                                                        MineroEntity mineroEncontrado = null;
                                                        for (Entity e : nivel.getAllEntities()) {
                                                            if (e instanceof MineroEntity m && m.hasCustomName() && m.getCustomName().getString().equals(idBusqueda)) {
                                                                mineroEncontrado = m;
                                                                break;
                                                            }
                                                        }

                                                        if (mineroEncontrado != null) {
                                                            int numeroEnPeloton = contadorPelotones.getOrDefault(escuadron, 1);
                                                            String nuevoNombre = escuadron + "_" + numeroEnPeloton;

                                                            mineroEncontrado.setCustomName(Component.literal(nuevoNombre));
                                                            contadorPelotones.put(escuadron, numeroEnPeloton + 1);

                                                            context.getSource().sendSystemMessage(Component.literal("🎖️ " + idBusqueda + " ha sido reasignado. Nuevo nombre: " + nuevoNombre).withStyle(ChatFormatting.GREEN));
                                                        } else {
                                                            context.getSource().sendSystemMessage(Component.literal("❌ No se encontró ningún trabajador llamado '" + idBusqueda + "'.").withStyle(ChatFormatting.RED));
                                                        }
                                                        return 1;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("enviar")
                                    .then(Commands.argument("nombre_escuadron", StringArgumentType.word())
                                            .then(Commands.argument("destino", StringArgumentType.word())
                                                    .executes(context -> {
                                                        String nombre = StringArgumentType.getString(context, "nombre_escuadron");
                                                        String destino = StringArgumentType.getString(context, "destino");
                                                        context.getSource().sendSystemMessage(Component.literal("⚔️ El pelotón '" + nombre + "' ha sido movilizado a: " + destino).withStyle(ChatFormatting.AQUA));
                                                        return 1;
                                                    })
                                            )
                                    )
                            )
                    )

                    .then(Commands.literal("menu")
                            .then(Commands.argument("mineral", StringArgumentType.string())
                                    .then(Commands.argument("uuid", StringArgumentType.string())
                                            .executes(context -> {
                                                String mineral = StringArgumentType.getString(context, "mineral");
                                                String uuid = StringArgumentType.getString(context, "uuid");
                                                ServerPlayer p = context.getSource().getPlayerOrException();

                                                MutableComponent btnEscribir = Component.literal("[✍️ Hacer clic para ingresar cantidad]")
                                                        .withStyle(style -> style
                                                                .withColor(ChatFormatting.YELLOW)
                                                                .withUnderlined(true)
                                                                .withClickEvent(new ClickEvent.SuggestCommand("/reino iniciar " + uuid + " " + mineral + " ")));

                                                p.sendSystemMessage(Component.literal("Has seleccionado " + mineral + ".").withStyle(ChatFormatting.AQUA));
                                                p.sendSystemMessage(btnEscribir);
                                                return 1;
                                            })
                                    )
                            )
                    )

                    .then(Commands.literal("iniciar")
                            .then(Commands.argument("uuid", StringArgumentType.string())
                                    .then(Commands.argument("mineral", StringArgumentType.string())
                                            .then(Commands.argument("cantidad", IntegerArgumentType.integer(1))
                                                    .executes(context -> {
                                                        String mineral = StringArgumentType.getString(context, "mineral");
                                                        int cantidad = IntegerArgumentType.getInteger(context, "cantidad");

                                                        context.getSource().sendSystemMessage(
                                                                Component.literal("✅ ¡Contrato Confirmado! El minero extraerá " + cantidad + " de " + mineral + " automáticamente.").withStyle(ChatFormatting.GREEN)
                                                        );

                                                        return 1;
                                                    })
                                            )
                                    )
                            )
                    )
            );
        });
    }
}