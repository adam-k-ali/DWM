package com.adamkali.dwm.entity;

import com.adamkali.dwm.item.LaserGunLogic;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DalekLaserEntity extends Projectile {
    private static final int MAX_LIFE_TICKS = 40;
    private static final EntityDataAccessor<Boolean> DATA_HEAVY =
            SynchedEntityData.defineId(DalekLaserEntity.class, EntityDataSerializers.BOOLEAN);

    public DalekLaserEntity(EntityType<? extends DalekLaserEntity> type, Level level) {
        super(type, level);
    }

    public DalekLaserEntity(Level level, LivingEntity owner) {
        this(DWMEntityTypes.DALEK_LASER, level);
        this.setOwner(owner);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    protected float getAirDrag() {
        return 0.99F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > MAX_LIFE_TICKS) {
            this.discard();
            return;
        }
        Vec3 movement = this.getDeltaMovement();
        if (this.level().isClientSide() && profile().explodes()) {
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        this.hitTargetOrDeflectSelf(hitResult);
        if (this.isRemoved()) {
            return;
        }
        this.updateRotation();
        this.setDeltaMovement(movement.scale(this.getAirDrag()));
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (isDalekKin(entity) && isDalekKin(this.getOwner())) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    private static boolean isDalekKin(Entity entity) {
        return entity instanceof DalekEntity || entity instanceof DalekMutantEntity;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (this.getOwner() instanceof LivingEntity livingOwner && this.level() instanceof ServerLevel serverLevel) {
            Entity target = hitResult.getEntity();
            boolean playerFired = livingOwner instanceof Player;
            float damage = playerFired
                    ? LaserGunLogic.PLAYER_DAMAGE
                    : (float) livingOwner.getAttributeValue(Attributes.ATTACK_DAMAGE);
            DamageSource damageSource = playerFired
                    ? this.damageSources().indirectMagic(this, livingOwner)
                    : this.damageSources().mobProjectile(this, livingOwner);
            if (target.hurtServer(serverLevel, damageSource, damage)) {
                EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
            }
        }
        if (!this.level().isClientSide()) {
            detonate(hitResult.getLocation());
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (!this.level().isClientSide()) {
            detonate(hitResult.getLocation());
            this.discard();
        }
    }

    public void setProfile(DalekLaserProfile profile) {
        this.entityData.set(DATA_HEAVY, profile.explodes());
    }

    public DalekLaserProfile profile() {
        return this.entityData.get(DATA_HEAVY) ? DalekLaserProfile.HEAVY : DalekLaserProfile.STANDARD;
    }

    /** Detonates the heavy round at {@code pos}; Dalek kin and the shooter are spared the blast. */
    private void detonate(Vec3 pos) {
        DalekLaserProfile profile = profile();
        if (!profile.explodes() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ExplosionDamageCalculator calculator = new ExplosionDamageCalculator() {
            @Override
            public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
                return !isDalekKin(entity);
            }
        };
        serverLevel.explode(this, null, calculator, pos, profile.blastRadius(), false, Level.ExplosionInteraction.MOB);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_HEAVY, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Heavy", this.entityData.get(DATA_HEAVY));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_HEAVY, input.getBooleanOr("Heavy", false));
    }
}
