package com.war.reino;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.war.reino.mineros.MineroEntity;
import com.war.reino.mineros.EntidadesReino;
import com.war.reino.mineros.EventosReino;
import com.war.reino.mineros.ComandosReino;

import com.war.reino.Constructores.Constructor;

public class Reino implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("reino");

    @Override
    public void onInitialize() {
        LOGGER.info("¡El mod del Reino se está inicializando!");

        EntidadesReino.registrarEntidades();
        EventosReino.registrarEventos();
        ComandosReino.registrarComandos();

        FabricDefaultAttributeRegistry.register(EntidadesReino.MINERO, MineroEntity.crearAtributos());
        FabricDefaultAttributeRegistry.register(EntidadesReino.CONSTRUCTOR, Constructor.crearAtributos());
        FabricDefaultAttributeRegistry.register(EntidadesReino.ESPADACHIN, Espadachin.crearAtributos());
        FabricDefaultAttributeRegistry.register(EntidadesReino.ARQUERO, Arquero.crearAtributos());
        FabricDefaultAttributeRegistry.register(EntidadesReino.CABALLERIA, Caballeria.crearAtributos());
    }
}