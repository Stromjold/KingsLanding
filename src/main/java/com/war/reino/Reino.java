package com.war.reino;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Reino implements ModInitializer {
    public static final String MOD_ID = "reino";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("¡El mod del Reino ha despertado!");

        Set<UUID> jugadoresQueYaVieronElMensaje = new HashSet<>();
        Set<UUID> jugadoresQueYaVieronALeñador = new HashSet<>();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("¡El mundo del Reino ha cargado correctamente!");
            for (ServerPlayer jugador : server.getPlayerList().getPlayers()) {
                jugador.sendSystemMessage(Component.literal("¡El mundo del Reino ha cargado correctamente!"), false);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer jugador = handler.player;
            jugador.sendSystemMessage(Component.literal("¡Bienvenido al Reino! El mundo ya está listo."), false);
            jugadoresQueYaVieronElMensaje.add(jugador.getUUID());
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            String texto = message.signedContent().trim();
            if (texto.equalsIgnoreCase("reino")) {
                sender.sendSystemMessage(Component.literal("¡El mod del Reino ha despertado!"), false);
            }
        });

        // El bucle de tiempo (20 Ticks = 1 Segundo)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // 1. Recorremos a todos los jugadores conectados
            for (ServerPlayer jugador : server.getPlayerList().getPlayers()) {

                // 2. Creamos un "cubo" invisible de 50 bloques de radio alrededor del jugador
                AABB areaDeBusqueda = jugador.getBoundingBox().inflate(50.0);

                // 3. Obtenemos una Lista de TODOS los aldeanos que estén dentro de ese cubo
                List<Villager> aldeanosCerca = jugador.level().getEntitiesOfClass(
                        Villager.class,
                        areaDeBusqueda,
                        entidad -> true // No filtramos por nada más, tomamos a todos
                );

                // 4. Recorremos esa lista de aldeanos uno por uno
                for (Villager aldeano : aldeanosCerca) {

                    // Revisamos si el aldeano fue nombrado con una Etiqueta de Nombre
                    if (aldeano.hasCustomName()) {

                        // Extraemos el texto exacto de la etiqueta (String)
                        String nombre = aldeano.getCustomName().getString();

                        // Comparamos el texto (Ojo: distingue mayúsculas y minúsculas)
                        if (nombre.equals("Leñador")) {
                            if (jugadoresQueYaVieronALeñador.add(jugador.getUUID())) {
                                LOGGER.info("Se ha detectado al aldeano Leñador cerca de {}", jugador.getName().getString());
                                jugador.sendSystemMessage(Component.literal("Se ha detectado al aldeano Leñador cerca de ti."), false);
                            }
                        }
                    }
                }
            }
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
