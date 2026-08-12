package com.war.reino.constructores;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Planos {

    /**
     * Busca y carga un archivo de estructura (.nbt) desde los archivos del juego/mod.
     *
     * @param nivel El mundo actual del servidor.
     * @param idPlano El identificador del plano (ejemplo: Identifier.parse("reino:casa_basica"))
     * @return El plano cargado (si existe).
     */
    public static Optional<StructureTemplate> obtenerPlano(ServerLevel nivel, Identifier idPlano) {
        StructureTemplateManager manager = nivel.getStructureManager();
        return manager.get(idPlano);
    }

    /**
     * Lee un plano y calcula exactamente cuántos bloques de cada tipo se necesitan.
     * Esto se conecta perfectamente con tu sistema de RegistroMineria.
     *
     * @param plano El plano cargado.
     * @return Un mapa con el nombre del material y la cantidad necesaria.
     */
    public static Map<String, Integer> calcularMaterialesNecesarios(StructureTemplate plano) {
        Map<String, Integer> materialesRequeridos = new HashMap<>();

        plano.palettes.get(0).blocks().forEach(infoBloque -> {
            BlockState estado = infoBloque.state();

            if (!estado.isAir() && !estado.is(Blocks.STRUCTURE_VOID)) {

                String nombreBloque = estado.getBlock().getName().getString();

                materialesRequeridos.put(nombreBloque, materialesRequeridos.getOrDefault(nombreBloque, 0) + 1);
            }
        });

        return materialesRequeridos;
    }
}