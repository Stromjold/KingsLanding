package com.war.reino.mineros;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;

import com.war.reino.guardia.Espadachin;
import com.war.reino.guardia.Arquero;
import com.war.reino.guardia.Caballero;
import com.war.reino.guardia.Caballeria;
import com.war.reino.constructores.Constructor;

public class EventosReino {

    public static void registrarEventos() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand == InteractionHand.MAIN_HAND && !world.isClientSide()) {

                ItemStack itemEnMano = player.getItemInHand(hand);
                ServerLevel serverWorld = (ServerLevel) world;

                if (entity instanceof MineroEntity || entity instanceof Constructor ||
                        entity instanceof Espadachin || entity instanceof Arquero ||
                        entity instanceof Caballero || entity instanceof Caballeria) {
                    return InteractionResult.PASS;
                }

                if (itemEnMano.getItem() == Items.STICK) {
                    itemEnMano.shrink(1);
                    MineroEntity nuevoMinero = EntidadesReino.MINERO.create(serverWorld, EntitySpawnReason.CONVERSION);
                    if (nuevoMinero != null) {
                        transformarEntidad(entity, nuevoMinero, "Minero", serverWorld);
                        player.sendSystemMessage(Component.literal(" ¡Minero reclutado con éxito!").withStyle(ChatFormatting.GREEN));
                        return InteractionResult.CONSUME;
                    }
                }

                if (itemEnMano.getItem() == Items.WOODEN_AXE) {
                    if (tieneYConsumeItems(player, Items.CRAFTING_TABLE)) {
                        itemEnMano.shrink(1);
                        Constructor nuevoConstructor = EntidadesReino.CONSTRUCTOR.create(serverWorld, EntitySpawnReason.CONVERSION);
                        if (nuevoConstructor != null) {
                            transformarEntidad(entity, nuevoConstructor, "Constructor", serverWorld);
                            player.sendSystemMessage(Component.literal(" ¡Constructor contratado!").withStyle(ChatFormatting.YELLOW));
                            return InteractionResult.CONSUME;
                        }
                    } else {
                        player.sendSystemMessage(Component.literal(" Te falta una Mesa de Trabajo en el inventario.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }
                }

                if (itemEnMano.getItem() == Items.IRON_SWORD) {
                    if (tieneYConsumeItems(player, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS)) {
                        itemEnMano.shrink(1);
                        Espadachin nuevoEspadachin = EntidadesReino.ESPADACHIN.create(serverWorld, EntitySpawnReason.CONVERSION);
                        if (nuevoEspadachin != null) {
                            transformarEntidad(entity, nuevoEspadachin, "Espadachín", serverWorld);
                            player.sendSystemMessage(Component.literal(" ¡Espadachín reclutado!").withStyle(ChatFormatting.GOLD));
                            return InteractionResult.CONSUME;
                        }
                    } else {
                        player.sendSystemMessage(Component.literal(" Faltan piezas de Armadura de Hierro en tu inventario.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }
                }

                if (itemEnMano.getItem() == Items.BOW) {
                    if (tieneYConsumeItems(player, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.ARROW)) {
                        itemEnMano.shrink(1);
                        Arquero nuevoArquero = EntidadesReino.ARQUERO.create(serverWorld, EntitySpawnReason.CONVERSION);
                        if (nuevoArquero != null) {
                            transformarEntidad(entity, nuevoArquero, "Arquero", serverWorld);
                            player.sendSystemMessage(Component.literal(" ¡Arquero reclutado y listo!").withStyle(ChatFormatting.GREEN));
                            return InteractionResult.CONSUME;
                        }
                    } else {
                        player.sendSystemMessage(Component.literal(" Te falta la Armadura de Cuero completa o Flechas en tu inventario.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }
                }

                if (itemEnMano.getItem() == Items.SHIELD) {
                    if (tieneYConsumeItems(player, Items.IRON_SWORD, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)) {
                        itemEnMano.shrink(1);
                        Caballero nuevoCaballero = EntidadesReino.CABALLERO.create(serverWorld, EntitySpawnReason.CONVERSION);
                        if (nuevoCaballero != null) {
                            transformarEntidad(entity, nuevoCaballero, "Caballero", serverWorld);
                            player.sendSystemMessage(Component.literal(" ¡Caballero pesado reclutado! El tanque está listo.").withStyle(ChatFormatting.DARK_AQUA));
                            return InteractionResult.CONSUME;
                        }
                    } else {
                        player.sendSystemMessage(Component.literal(" Faltan piezas de Diamante o una Espada de Hierro en tu inventario para el Caballero.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }
                }


                if (itemEnMano.getItem() == Items.SADDLE) {
                    if (tieneYConsumeItems(player, Items.IRON_SWORD, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS)) {
                        itemEnMano.shrink(1);
                        Caballeria nuevaCaballeria = EntidadesReino.CABALLERIA.create(serverWorld, EntitySpawnReason.CONVERSION);
                        if (nuevaCaballeria != null) {
                            transformarEntidad(entity, nuevaCaballeria, "Caballería", serverWorld);
                            player.sendSystemMessage(Component.literal(" ¡Caballería desplegada!").withStyle(ChatFormatting.GOLD));
                            return InteractionResult.CONSUME;
                        }
                    } else {
                        player.sendSystemMessage(Component.literal(" Faltan piezas de Hierro o la Espada para el jinete.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }

    private static void transformarEntidad(net.minecraft.world.entity.Entity vieja, net.minecraft.world.entity.PathfinderMob nueva, String nombre, ServerLevel nivel) {
        nueva.setPos(vieja.getX(), vieja.getY(), vieja.getZ());
        nueva.setYRot(vieja.getYRot());
        nueva.setXRot(vieja.getXRot());
        nueva.setCustomName(Component.literal(nombre));
        nueva.setCustomNameVisible(true);
        nivel.addFreshEntity(nueva);
        vieja.discard();
    }

    private static boolean tieneYConsumeItems(Player player, Item... itemsRequeridos) {

        for (Item item : itemsRequeridos) {
            if (player.getInventory().countItem(item) < 1) {
                return false; // Falta un ítem, cancelamos todo
            }
        }

        for (Item item : itemsRequeridos) {
            removerItem(player, item);
        }
        return true;
    }

    private static void removerItem(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                stack.shrink(1);
                break;
            }
        }
    }
}