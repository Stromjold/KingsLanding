package com.war.reino.Constructores;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;

public class Constructor extends PathfinderMob {

    public enum EstadoConstructor {
        ESPERANDO_PLANOS,
        BUSCANDO_MATERIAL,
        CONSTRUYENDO,
        OBRA_TERMINADA
    }
    private EstadoConstructor estado = EstadoConstructor.ESPERANDO_PLANOS;

    public Constructor(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder crearAtributos() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D) // Vida de aldeano
                .add(Attributes.MOVEMENT_SPEED, 0.5D); // Velocidad al caminar
    }

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public EstadoConstructor getEstado() {
        return estado;
    }

    public void setEstado(EstadoConstructor nuevoEstado) {
        this.estado = nuevoEstado;
    }
}