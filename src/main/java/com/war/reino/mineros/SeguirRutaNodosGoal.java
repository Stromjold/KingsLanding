package com.war.reino.mineros;

import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;

public class SeguirRutaNodosGoal extends net.minecraft.world.entity.ai.goal.Goal {

    private static final Logger LOGGER = LoggerFactory.getLogger("reino-minero");
    private static final double DISTANCIA_LLEGADA = 1.5D;
    private static final double VELOCIDAD = 0.5D;

    private final MineroEntity minero;
    private boolean anuncioInicio = false;

    public SeguirRutaNodosGoal(MineroEntity minero) {
        this.minero = minero;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        MineroEntity.EstadoMinero estado = minero.getEstado();
        boolean enTransito = estado == MineroEntity.EstadoMinero.VIAJANDO_A_MINA
                || estado == MineroEntity.EstadoMinero.REGRESANDO_AL_COFRE;
        return enTransito && !minero.getRutaNodos().isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        if (!anuncioInicio) {
            if (minero.getEstado() == MineroEntity.EstadoMinero.VIAJANDO_A_MINA) {
                LOGGER.info("Minero iniciando ruta hacia la mina ({} nodos).", minero.getRutaNodos().size());
            } else {
                LOGGER.info("Minero iniciando ruta de regreso al cofre.");
            }
            anuncioInicio = true;
        }
    }

    @Override
    public void stop() {
        anuncioInicio = false;
        minero.getNavigation().stop();
    }

    @Override
    public void tick() {
        List<BlockPos> ruta = minero.getRutaNodos();
        if (ruta.isEmpty()) {
            return;
        }

        boolean vaHaciaAdelante = minero.getEstado() == MineroEntity.EstadoMinero.VIAJANDO_A_MINA;
        int indice = minero.getIndiceNodoActual();

        if (vaHaciaAdelante && indice >= ruta.size()) {
            llegarAMina();
            return;
        }
        if (!vaHaciaAdelante && indice < 0) {
            llegarAlCofre();
            return;
        }

        BlockPos nodoObjetivo = ruta.get(indice);
        double destinoX = nodoObjetivo.getX() + 0.5;
        double destinoY = nodoObjetivo.getY();
        double destinoZ = nodoObjetivo.getZ() + 0.5;

        if (minero.getNavigation().isDone()) {
            minero.getNavigation().moveTo(destinoX, destinoY, destinoZ, VELOCIDAD);
        }

        double distanciaCuadrada = minero.distanceToSqr(destinoX, destinoY, destinoZ);
        if (distanciaCuadrada <= DISTANCIA_LLEGADA * DISTANCIA_LLEGADA) {
            LOGGER.info("Minero ha llegado al nodo {}.", indice);

            if (vaHaciaAdelante) {
                minero.avanzarNodo();
                if (minero.getIndiceNodoActual() >= ruta.size()) {
                    llegarAMina();
                }
            } else {
                minero.retrocederNodo();
                if (minero.getIndiceNodoActual() < 0) {
                    llegarAlCofre();
                }
            }
        }
    }

    private void llegarAMina() {
        LOGGER.info("Minero ha llegado a la zona de minería. Comenzando a picar.");
        minero.setEstado(MineroEntity.EstadoMinero.MINANDO);
        // Deja el índice en el último nodo para que, al volver, el regreso
        // arranque retrocediendo desde ahí hasta el nodo 0.
        minero.setIndiceNodoActual(minero.getRutaNodos().size() - 1);
    }

    private void llegarAlCofre() {
        LOGGER.info("Minero ha llegado al cofre vinculado. Comenzando a depositar.");
        minero.setEstado(MineroEntity.EstadoMinero.DEPOSITANDO);
        minero.setIndiceNodoActual(0);
    }
}
