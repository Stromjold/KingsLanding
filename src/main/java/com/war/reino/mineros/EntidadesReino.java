package com.war.reino.mineros;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import com.war.reino.guardia.Espadachin;
import com.war.reino.Constructores.Constructor;
import com.war.reino.guardia.Arquero;
import com.war.reino.guardia.Caballeria;

public class EntidadesReino {

    public static final Identifier ID_MINERO = Identifier.parse("reino:minero");
    public static final ResourceKey<EntityType<?>> LLAVE_MINERO = ResourceKey.create(Registries.ENTITY_TYPE, ID_MINERO);
    public static final EntityType<MineroEntity> MINERO = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ID_MINERO,
            EntityType.Builder.of(MineroEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .build(LLAVE_MINERO)
    );

    public static final Identifier ID_CONSTRUCTOR = Identifier.parse("reino:constructor");
    public static final ResourceKey<EntityType<?>> LLAVE_CONSTRUCTOR = ResourceKey.create(Registries.ENTITY_TYPE, ID_CONSTRUCTOR);
    public static final EntityType<Constructor> CONSTRUCTOR = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ID_CONSTRUCTOR,
            EntityType.Builder.of(Constructor::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .build(LLAVE_CONSTRUCTOR)
    );

    public static final Identifier ID_ARQUERO = Identifier.parse("reino:arquero");
    public static final ResourceKey<EntityType<?>> LLAVE_ARQUERO = ResourceKey.create(Registries.ENTITY_TYPE, ID_ARQUERO);
    public static final EntityType<Arquero> ARQUERO = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, ID_ARQUERO,
            EntityType.Builder.of(Arquero::new, MobCategory.MISC).sized(0.6F, 1.95F).build(LLAVE_ARQUERO)
    );

    public static final Identifier ID_CABALLERIA = Identifier.parse("reino:caballeria");
    public static final ResourceKey<EntityType<?>> LLAVE_CABALLERIA = ResourceKey.create(Registries.ENTITY_TYPE, ID_CABALLERIA);
    public static final EntityType<Caballeria> CABALLERIA = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, ID_CABALLERIA,
            EntityType.Builder.of(Caballeria::new, MobCategory.MISC).sized(0.6F, 1.95F).build(LLAVE_CABALLERIA)
    );

    public static void registrarEntidades() {
    }
}