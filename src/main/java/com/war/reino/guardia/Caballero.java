package com.war.reino.guardia;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Caballero extends PathfinderMob {

    public enum OrdenMilitar {
        VIGILAR,    // Se queda cuidando una posición
        PROTEGER,   // Sigue y protege al jugador
        PATRULLAR   // Busca enemigos activamente por todo el mapa
    }

    private OrdenMilitar ordenActual = OrdenMilitar.VIGILAR;
    private BlockPos puntoDeGuardia = BlockPos.ZERO;
    private Player comandante = null;

    public Caballero(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);

        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        this.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
    }

    public static AttributeSupplier.Builder crearAtributos() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));

        this.goalSelector.addGoal(2, new Goal() {
            @Override
            public boolean canUse() {
                return ordenActual == OrdenMilitar.PROTEGER && comandante != null && comandante.isAlive()
                        && Caballero.this.distanceToSqr(comandante) > 25.0D; // 5 bloques al cuadrado
            }
            @Override
            public void tick() {
                Caballero.this.getNavigation().moveTo(comandante, 1.1D);
            }
        });

        this.goalSelector.addGoal(3, new Goal() {
            @Override
            public boolean canUse() {
                return ordenActual == OrdenMilitar.VIGILAR && puntoDeGuardia != BlockPos.ZERO
                        && Caballero.this.blockPosition().distSqr(puntoDeGuardia) > 100.0D; // 10 bloques al cuadrado
            }
            @Override
            public void tick() {
                Caballero.this.getNavigation().moveTo(puntoDeGuardia.getX(), puntoDeGuardia.getY(), puntoDeGuardia.getZ(), 1.0D);
            }
        });

        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                if (ordenActual == OrdenMilitar.PROTEGER) return false;
                return super.canUse();
            }
        });

        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {

        if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).isEmpty()) {

            if (!this.level().isClientSide) {

                this.comandante = player;

                if (this.ordenActual == OrdenMilitar.VIGILAR) {
                    this.ordenActual = OrdenMilitar.PROTEGER;
                    player.sendSystemMessage(Component.literal("🛡️ Caballero: ¡Mi espada es tuya! Te seguiré y protegeré.").withStyle(ChatFormatting.AQUA));
                }
                else if (this.ordenActual == OrdenMilitar.PROTEGER) {
                    this.ordenActual = OrdenMilitar.PATRULLAR;
                    player.sendSystemMessage(Component.literal("⚔️ Caballero: ¡Saliendo de cacería! Buscaré y destruiré enemigos en la zona.").withStyle(ChatFormatting.RED));
                }
                else {
                    this.ordenActual = OrdenMilitar.VIGILAR;
                    this.puntoDeGuardia = this.blockPosition(); // Guarda la coordenada exacta donde está parado
                    player.sendSystemMessage(Component.literal("🏰 Caballero: Mantendré esta posición y vigilaré los alrededores.").withStyle(ChatFormatting.GOLD));
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }
}