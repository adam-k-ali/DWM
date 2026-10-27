package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Skaro Thal NPC. Peaceful wanderer with four cosmetic variants: opens doors, looks at players,
 * flees anything in {@code #dwm:thal_fears} (the Daleks) and never initiates combat. No trades,
 * dialogue, or breeding. Residents come from settlements only (see {@link ThalSpawnRules}).
 */
public class ThalEntity extends PathfinderMob {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "thal");

    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(ThalEntity.class, EntityDataSerializers.INT);

    public ThalEntity(EntityType<? extends ThalEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityDefinitions.createAttributes(ID);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanOpenDoors(true);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        EntityDefinitions.registerGoals(this, this.goalSelector, this.targetSelector);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, ThalVariant.THAL_1.ordinal());
    }

    public ThalVariant getVariant() {
        return ThalVariant.byOrdinal(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(ThalVariant variant) {
        this.entityData.set(DATA_VARIANT, variant.ordinal());
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason reason,
            SpawnGroupData spawnData
    ) {
        this.setVariant(ThalVariant.getRandom(this.getRandom()));
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
        setVariant(ThalVariant.byId(input.getStringOr("Variant", ThalVariant.THAL_1.getSerializedName())));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return DWMSounds.THAL_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return DWMSounds.THAL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DWMSounds.THAL_DEATH;
    }
}
