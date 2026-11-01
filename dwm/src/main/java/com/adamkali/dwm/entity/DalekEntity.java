package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import com.adamkali.dwm.sound.DWMSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Hostile classic Dalek (1963 and Time War casings); subclasses swap the line via {@link #family()}.
 * Ground-glides by default, shoots a laser bolt at nearby players, and switches to
 * flying navigation when the target is above or otherwise unreachable on the ground.
 */
public class DalekEntity extends Monster implements RangedAttackMob {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dalek");

    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(DalekEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_FLYING =
            SynchedEntityData.defineId(DalekEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> DATA_JOB =
            SynchedEntityData.defineId(DalekEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ON_DUTY =
            SynchedEntityData.defineId(DalekEntity.class, EntityDataSerializers.BOOLEAN);

    private static final float LASER_SPEED = 1.6F;

    private final MoveControl groundMoveControl;
    private final FlyingMoveControl<DalekEntity> flyingMoveControl;
    private PathNavigation groundNavigation;
    private FlyingPathNavigation flyingNavigation;
    private boolean wasFlying;
    private int lastExterminateTick = -DalekVoice.EXTERMINATE_COOLDOWN_TICKS;
    private @Nullable BlockPos post;
    private float postYaw;
    private int patrolRadius = DalekGarrisonLogic.DEFAULT_PATROL_RADIUS;
    private List<BlockPos> workSpots = List.of();

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
        return EntityDefinitions.createAttributes(ID);
    }

    /**
     * Dalek line this entity type spawns from. Called while the superclass constructor defines
     * synched data, so overrides must return a constant.
     */
    public DalekFamily family() {
        return DalekFamily.CLASSIC;
    }

    /** Casing the current variant is painted for; decides the mesh and the gunstick muzzle. */
    public DalekChassis chassis() {
        return getVariant().chassis();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        EntityDefinitions.registerGoals(this, this.goalSelector, this.targetSelector);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, family().defaultVariant().ordinal());
        builder.define(DATA_FLYING, false);
        builder.define(DATA_JOB, DalekJob.NONE.ordinal());
        builder.define(DATA_ON_DUTY, false);
    }

    public DalekJob getJob() {
        return DalekJob.byOrdinal(this.entityData.get(DATA_JOB));
    }

    public void setJob(DalekJob job) {
        this.entityData.set(DATA_JOB, job.ordinal());
    }

    /** True while a job goal is holding, pausing or working; drives the job animations. */
    public boolean isOnDuty() {
        return this.entityData.get(DATA_ON_DUTY);
    }

    public void setOnDuty(boolean onDuty) {
        if (this.isOnDuty() != onDuty) {
            this.entityData.set(DATA_ON_DUTY, onDuty);
        }
    }

    public @Nullable BlockPos getPost() {
        return post;
    }

    public float getPostYaw() {
        return postYaw;
    }

    public int getPatrolRadius() {
        return patrolRadius;
    }

    public List<BlockPos> getWorkSpots() {
        return workSpots;
    }

    /** Binds this Dalek to a post and job; used by garrison placement, commands and tests. */
    public void assignJob(DalekJob job, BlockPos post, float yaw, int patrolRadius, List<BlockPos> workSpots) {
        setJob(job);
        this.post = post.immutable();
        this.postYaw = yaw;
        this.patrolRadius = patrolRadius;
        this.workSpots = List.copyOf(workSpots);
        this.setOnDuty(false);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (post != null && getTarget() != null) {
            double distance = Math.sqrt(this.blockPosition().distSqr(post));
            if (DalekGarrisonLogic.shouldDropTarget(distance, DalekGarrisonLogic.LEASH_DISTANCE)) {
                setTarget(null);
            }
        }
    }

    public DalekVariant getVariant() {
        return DalekVariant.byOrdinal(this.entityData.get(DATA_VARIANT), family());
    }

    public void setVariant(DalekVariant variant) {
        this.entityData.set(DATA_VARIANT, DalekVariant.fitting(variant, family()).ordinal());
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

    boolean hasGroundPathTo(BlockPos pos) {
        return this.groundNavigation.createPath(pos, 0) != null;
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

    /** Shouts "Exterminate!" when a fresh target is acquired, at most once per cooldown. */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        boolean hadTarget = this.getTarget() != null;
        super.setTarget(target);
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }
        int ticksSinceShout = this.tickCount - this.lastExterminateTick;
        if (DalekVoice.shouldExterminate(hadTarget, target != null, ticksSinceShout)) {
            this.lastExterminateTick = this.tickCount;
            this.playSound(DWMSounds.DALEK_EXTERMINATE, 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        }
    }

    public Vec3 gunMuzzleWorldPos() {
        Vec3 look = this.getLookAngle();
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0));
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1.0, 0.0, 0.0);
        } else {
            right = right.normalize();
        }
        DalekChassis chassis = chassis();
        return this.position()
                .add(0.0, chassis.gunHeight(), 0.0)
                .add(right.scale(chassis.gunSide()))
                .add(look.scale(chassis.gunForward()));
    }

    /**
     * Like a slime splitting, a destroyed casing may release its mutant once the death animation ends.
     * Only the death-animation removal ({@code KILLED}) counts: a dying Dalek that is unloaded or
     * discarded mid-animation releases nothing.
     */
    @Override
    public void remove(Entity.RemovalReason reason) {
        if (reason == Entity.RemovalReason.KILLED
                && this.level() instanceof ServerLevel serverLevel
                && this.isDeadOrDying()
                && DalekMutantEmergence.shouldEmerge(this.random.nextFloat())) {
            DalekMutantEmergence.release(serverLevel, this);
        }
        super.remove(reason);
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason reason,
            SpawnGroupData spawnData
    ) {
        this.setVariant(DalekVariant.pick(this.random, family()));
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Variant", getVariant().getSerializedName());
        output.putBoolean("Flying", isFlying());
        output.putString("Job", getJob().getSerializedName());
        if (post != null) {
            output.store("Post", BlockPos.CODEC, post);
            output.putFloat("PostYaw", postYaw);
            output.putInt("PatrolRadius", patrolRadius);
            output.store("WorkSpots", BlockPos.CODEC.listOf(), workSpots);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(DalekVariant.byId(input.getStringOr("Variant", ""), family()));
        setFlying(input.getBooleanOr("Flying", false));
        setJob(DalekJob.byId(input.getStringOr("Job", "")));
        this.post = input.read("Post", BlockPos.CODEC).orElse(null);
        this.postYaw = input.getFloatOr("PostYaw", 0.0F);
        this.patrolRadius = input.getIntOr("PatrolRadius", DalekGarrisonLogic.DEFAULT_PATROL_RADIUS);
        this.workSpots = new ArrayList<>(input.read("WorkSpots", BlockPos.CODEC.listOf()).orElse(List.of()));
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
