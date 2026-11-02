package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import com.adamkali.dwm.item.LaserGunLogic;
import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Cybermaster: a Cyberman built from dead Time Lords. It fires a wrist laser (a {@link DalekLaserEntity}
 * bolt dealing its attack damage) and survives its first killing blow once by regenerating
 * ({@link CybermasterRegeneration}). Stats and AI come from {@code cybermaster.json}.
 */
public class CybermasterEntity extends CybermanEntity implements RangedAttackMob {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cybermaster");
    public static final String REGENERATED_TAG = "Regenerated";

    private boolean regenerated;

    public CybermasterEntity(EntityType<? extends CybermasterEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityDefinitions.createAttributes(ID);
    }

    public boolean hasRegenerated() {
        return regenerated;
    }

    public void setRegenerated(boolean regenerated) {
        this.regenerated = regenerated;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }
        Vec3 muzzle = LaserGunLogic.muzzlePosition(this.getEyePosition(), this.getLookAngle());
        Vec3 aim = target.getEyePosition().subtract(muzzle);
        DalekLaserEntity laser = new DalekLaserEntity(this.level(), this);
        laser.setPos(muzzle.x, muzzle.y, muzzle.z);
        laser.shoot(aim.x, aim.y, aim.z, LaserGunLogic.LASER_SPEED, 0.0F);
        this.level().addFreshEntity(laser);
        this.playSound(DWMSounds.CYBERMASTER_SHOOT, 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F);
    }

    /**
     * Runs inside vanilla {@code hurtServer} before its death check (the totem hook is private), so a
     * regeneration here skips the death sound, loot and {@code die}.
     */
    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource source, float damage) {
        super.actuallyHurt(level, source, damage);
        if (CybermasterRegeneration.shouldRegenerate(
                this.isDeadOrDying(), this.regenerated, source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))) {
            regenerate(level);
        }
    }

    private void regenerate(ServerLevel level) {
        this.regenerated = true;
        this.setHealth(this.getMaxHealth());
        this.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE,
                CybermasterRegeneration.RESISTANCE_TICKS,
                CybermasterRegeneration.RESISTANCE_AMPLIFIER
        ));
        level.sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                this.getX(), this.getY(0.6), this.getZ(),
                CybermasterRegeneration.PARTICLE_COUNT,
                0.4, 0.6, 0.4,
                0.3
        );
        this.playSound(DWMSounds.CYBERMASTER_REGENERATE, 1.0F, 1.0F);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(REGENERATED_TAG, regenerated);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        regenerated = input.getBooleanOr(REGENERATED_TAG, false);
    }
}
