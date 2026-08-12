package com.war.reino.constructores;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedList;
import java.util.Queue;

public class Operadores {

    public static class TareaBloque {
        public final BlockPos posicion;
        public final BlockState estadoBloque;

        public TareaBloque(BlockPos posicion, BlockState estadoBloque) {
            this.posicion = posicion;
            this.estadoBloque = estadoBloque;
        }
    }

    private static final Queue<TareaBloque> tareasPendientes = new LinkedList<>();

    /**
     * Se llama cuando el proyecto tiene los materiales listos para empezar.
     * Desglosa el plano en pequeñas tareas individuales.
     */
    public static void iniciarProyecto(Queue<TareaBloque> nuevasTareas) {
        tareasPendientes.clear(); // Limpiamos proyectos anteriores
        tareasPendientes.addAll(nuevasTareas);
    }

    /**
     * Un constructor llama a este método cuando está libre.
     *
     * @return La siguiente tarea a realizar, o 'null' si ya terminaron la casa.
     */
    public static TareaBloque pedirTrabajo() {

        return tareasPendientes.poll();
    }

    /**
     * Para saber cuánto falta para terminar la obra.
     */
    public static int bloquesRestantes() {
        return tareasPendientes.size();
    }
}