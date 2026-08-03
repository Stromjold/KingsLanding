package com.war.reino;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Reino implements ModInitializer {
    public static final String MOD_ID = "reino";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("¡El mod del Reino ha despertado!");

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

                            // ¡BINGO! Encontramos a nuestro trabajador.
                            // Aquí programaremos cómo busca los árboles.

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
