package com.adamkali.dwm.entity;

import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Hostile 1963 Dalek. Ground-glides by default, shoots a laser bolt at nearby
 * players, and switches to flying navigation when the target is above or
 * otherwise unreachable on the ground.
 */
public class DalekEntity extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(DalekEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_FLYING =
            SynchedEntityData.defineId(DalekEntity.class, EntityDataSerializers.BOOLEAN);

    private static final float LASER_SPEED = 1.6F;
    private static final float GUN_HEIGHT = 18.0F / 16.0F;
    private static final float GUN_FORWARD = 10.0F / 16.0F;
    private static final float GUN_SIDE = -2.5F / 16.0F;

    private final MoveControl groundMoveControl;
    private final FlyingMoveControl<DalekEntity> flyingMoveControl;
    private PathNavigation groundNavigation;
    private FlyingPathNavigation flyingNavigation;
    private boolean wasFlying;

    public DalekEntity(EntityType<? extends DalekEntity> type, Level level) {
        super(type, level);
        this.groundMoveControl = this.moveControl;
        this.flyingMoveControl = new FlyingMoveControl<>(this, 20, true);
        this.groundNavigation = this.navigation;
        this.flyingNavigation = createFlyingNavigation(level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        this.xpReward = 10;
    }

    private FlyingPathNavigation createFlyingNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(false);
        navigation.setRequiredPathLength(48.0F);
        return navigation;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.FLYING_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.ARMOR, 6.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DalekFlightGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 40, 16.0F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0) {
            @Override
            public boolean canUse() {
                return !DalekEntity.this.isFlying() && super.canUse();
            }
        });
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, DalekVariant.CLASSIC_1963.ordinal());
        builder.define(DATA_FLYING, false);
    }

    public DalekVariant getVariant() {
        return DalekVariant.byOrdinal(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(DalekVariant variant) {
        this.entityData.set(DATA_VARIANT, variant.ordinal());
    }

    public boolean isFlying() {
        return this.entityData.get(DATA_FLYING);
    }

    public void setFlying(boolean flying) {
        if (this.isFlying() == flying) {
            return;
        }
        this.entityData.set(DATA_FLYING, flying);
        this.navigation.stop();
        if (flying) {
            this.moveControl = this.flyingMoveControl;
            this.navigation = this.flyingNavigation;
            this.setNoGravity(true);
        } else {
            this.moveControl = this.groundMoveControl;
            this.navigation = this.groundNavigation;
            this.setNoGravity(false);
        }
    }

    boolean hasGroundPathTo(LivingEntity target) {
        Path path = this.groundNavigation.createPath(target, 0);
        return path != null;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            tickFlightParticles();
        }
    }

    private void tickFlightParticles() {
        boolean flying = this.isFlying();
        if (flying && !this.wasFlying) {
            spawnTakeoffBurst();
        }
        if (flying) {
            spawnCruiseExhaust();
        }
        this.wasFlying = flying;
    }

    private void spawnCruiseExhaust() {
        Vec3 movement = this.getDeltaMovement();
        double speed = movement.length();
        boolean climbing = movement.y > 0.02;
        int count = DalekFlightFx.cruiseParticleCount(speed, climbing);
        for (int i = 0; i < count; i++) {
            spawnExhaustParticle(ParticleTypes.SMOKE, movement);
        }
        if (DalekFlightFx.shouldSpawnClimbCloud(climbing, this.random)) {
            spawnExhaustParticle(ParticleTypes.CLOUD, movement);
        }
    }

    private void spawnTakeoffBurst() {
        Vec3 movement = this.getDeltaMovement();
        int count = DalekFlightFx.takeoffBurstCount();
        for (int i = 0; i < count; i++) {
            spawnExhaustParticle(i % 2 == 0 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CLOUD, movement);
        }
    }

    private void spawnExhaustParticle(ParticleOptions particle, Vec3 movement) {
        Vec3 pos = DalekFlightFx.exhaustPos(this.position(), this.random);
        Vec3 velocity = DalekFlightFx.exhaustVelocity(movement, this.random);
        this.level().addParticle(particle, pos.x, pos.y, pos.z, velocity.x, velocity.y, velocity.z);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }
        Vec3 muzzle = gunMuzzleWorldPos();
        Vec3 aim = target.getEyePosition().subtract(muzzle);
        DalekLaserEntity laser = new DalekLaserEntity(this.level(), this);
        laser.setPos(muzzle.x, muzzle.y, muzzle.z);
        laser.shoot(aim.x, aim.y, aim.z, LASER_SPEED, 0.0F);
        this.level().addFreshEntity(laser);
        this.playSound(DWMSounds.DALEK_SHOOT, 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F);
    }

    public Vec3 gunMuzzleWorldPos() {
        Vec3 look = this.getLookAngle();
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0));
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1.0, 0.0, 0.0);
        } else {
            right = right.normalize();
        }
        return this.position()
                .add(0.0, GUN_HEIGHT, 0.0)
                .add(right.scale(GUN_SIDE))
                .add(look.scale(GUN_FORWARD));
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason reason,
            SpawnGroupData spawnData
    ) {
        this.setVariant(DalekVariant.CLASSIC_1963);
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Variant", getVariant().getSerializedName());
        output.putBoolean("Flying", isFlying());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(DalekVariant.byId(input.getStringOr("Variant", DalekVariant.CLASSIC_1963.getSerializedName())));
        setFlying(input.getBooleanOr("Flying", false));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return DWMSounds.DALEK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return DWMSounds.DALEK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DWMSounds.DALEK_DEATH;
    }
}
