package com.adamkali.dwm.entity;

import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Gallifrey flying insect. Passive wanderer with four species variants; not rideable or breedable.
 */
public class FlutterwingEntity extends Animal {
    /** Collision and render scale relative to the Blockbench mesh (20% smaller). */
    public static final float SCALE = 0.8F;

    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(FlutterwingEntity.class, EntityDataSerializers.INT);

    public FlutterwingEntity(EntityType<? extends FlutterwingEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl<>(this, 20, true);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(false);
        navigation.setRequiredPathLength(48.0F);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, FlutterwingVariant.BLUE_CRYSTAL.ordinal());
    }

    public FlutterwingVariant getVariant() {
        return FlutterwingVariant.byOrdinal(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(FlutterwingVariant variant) {
        this.entityData.set(DATA_VARIANT, variant.ordinal());
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason reason,
            SpawnGroupData spawnData
    ) {
        this.setVariant(FlutterwingVariant.getRandom(this.getRandom()));
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Variant", getVariant().getSerializedName());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(FlutterwingVariant.byId(input.getStringOr("Variant", FlutterwingVariant.BLUE_CRYSTAL.getSerializedName())));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public FlutterwingEntity getBreedOffspring(ServerLevel level, AgeableMob other) {
        FlutterwingEntity child = DWMEntityTypes.FLUTTERWING.create(level, EntitySpawnReason.BREEDING);
        if (child != null) {
            child.setVariant(getVariant());
        }
        return child;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return DWMSounds.FLUTTERWING_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return DWMSounds.FLUTTERWING_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DWMSounds.FLUTTERWING_DEATH;
    }
}
