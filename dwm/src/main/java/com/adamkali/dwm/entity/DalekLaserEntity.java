package com.adamkali.dwm.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
        if (entity instanceof DalekEntity) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (this.getOwner() instanceof LivingEntity livingOwner && this.level() instanceof ServerLevel serverLevel) {
            Entity target = hitResult.getEntity();
            float damage = (float) livingOwner.getAttributeValue(Attributes.ATTACK_DAMAGE);
            DamageSource damageSource = this.damageSources().mobProjectile(this, livingOwner);
            if (target.hurtServer(serverLevel, damageSource, damage)) {
                EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
            }
        }
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    }
}
