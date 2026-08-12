package com.war.reino;

import com.war.reino.aldeanosrecolectoresdemadera.AldeanosRecolectoresMadera;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Reino implements ModInitializer {
    public static final String MOD_ID = "reino";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("¡El mod del Reino ha despertado!");

        Set<UUID> jugadoresQueYaVieronElMensaje = new HashSet<>();
        AldeanosRecolectoresMadera recolectores = new AldeanosRecolectoresMadera();
        recolectores.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("¡El mundo del Reino ha cargado correctamente!");
            for (ServerPlayer jugador : server.getPlayerList().getPlayers()) {
                jugador.sendSystemMessage(Component.literal("¡El mundo del Reino ha cargado correctamente!"), false);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer jugador = handler.player;
            jugador.sendSystemMessage(Component.literal("¡Bienvenido al Reino! Escribe talar para activar a los leñadores."), false);
            jugadoresQueYaVieronElMensaje.add(jugador.getUUID());
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            String texto = message.signedContent().trim();
            if (texto.equalsIgnoreCase("reino")) {
                sender.sendSystemMessage(Component.literal("¡El mod del Reino ha despertado!"), false);
            }
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
