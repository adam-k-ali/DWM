package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Weapons-grade Cyberman: the sleek Cyberiad design that upgrades itself mid-fight. Repeated hits of
 * one damage type do less and less ({@link CyberAdaptation}), and it dashes at distant targets with
 * super-speed ({@link CyberSpeedBurst}). Stats and AI come from {@code weapons_grade_cyberman.json}.
 */
public class WeaponsGradeCybermanEntity extends CybermanEntity {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "weapons_grade_cyberman");
    public static final Identifier SPEED_BURST_MODIFIER =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cyberman_speed_burst");

    private final CyberAdaptation adaptation = new CyberAdaptation();
    private int burstTicksRemaining;
    private int burstCooldown;

    public WeaponsGradeCybermanEntity(EntityType<? extends WeaponsGradeCybermanEntity> type, Level level) {
        super(type, level);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityDefinitions.createAttributes(ID);
    }

    /** Current reduction against {@code source}'s damage type (for tests and debugging). */
    public float adaptationAgainst(DamageSource source) {
        return adaptation.reductionFor(adaptationKey(source), this.level().getGameTime());
    }

    public boolean isBursting() {
        return burstTicksRemaining > 0;
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource source, float damage) {
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            Object key = adaptationKey(source);
            long now = level.getGameTime();
            float before = adaptation.reductionFor(key, now);
            damage = adaptation.adapt(key, now, damage);
            if (adaptation.reductionFor(key, now) > before) {
                this.playSound(DWMSounds.WEAPONS_GRADE_CYBERMAN_UPGRADE, 0.8F, 1.0F);
            }
        }
        super.actuallyHurt(level, source, damage);
    }

    private static Object adaptationKey(DamageSource source) {
        return source.typeHolder().unwrapKey().<Object>map(key -> key).orElse(source.type());
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (burstTicksRemaining > 0) {
            burstTicksRemaining--;
            if (burstTicksRemaining == 0) {
                endBurst();
            }
            return;
        }
        if (burstCooldown > 0) {
            burstCooldown--;
        }
        LivingEntity target = this.getTarget();
        boolean hasTarget = target != null && target.isAlive();
        double distance = hasTarget ? this.distanceTo(target) : 0.0;
        if (CyberSpeedBurst.shouldBurst(hasTarget, distance, burstCooldown)) {
            startBurst(level);
        }
    }

    private void startBurst(ServerLevel level) {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(SPEED_BURST_MODIFIER);
        speed.addTransientModifier(new AttributeModifier(
                SPEED_BURST_MODIFIER,
                CyberSpeedBurst.SPEED_MULTIPLIER,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
        burstTicksRemaining = CyberSpeedBurst.BURST_TICKS;
        level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(0.2), this.getZ(), 6, 0.3, 0.1, 0.3, 0.02);
    }

    private void endBurst() {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPEED_BURST_MODIFIER);
        }
        burstCooldown = CyberSpeedBurst.COOLDOWN_TICKS;
    }
}
