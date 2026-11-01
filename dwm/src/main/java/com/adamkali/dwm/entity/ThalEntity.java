package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.item.LaserGunLogic;
import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Skaro Thal NPC. Peaceful wanderer with four cosmetic variants: opens doors, looks at players,
 * flees anything in {@code #dwm:thal_fears} (the Daleks) and never initiates combat. No trades,
 * dialogue, or breeding. Residents come from settlements only (see {@link ThalSpawnRules}).
 * <p>
 * During a Dalek/Thal battle a Thal is <em>armed</em>: it holds a Dalek Laser Gun, stops fleeing and
 * shoots Daleks (see the {@code dwm:armed} / {@code dwm:not_armed} goal conditions in {@code thal.json}).
 */
public class ThalEntity extends PathfinderMob implements RangedAttackMob {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "thal");

    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(ThalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ARMED =
            SynchedEntityData.defineId(ThalEntity.class, EntityDataSerializers.BOOLEAN);

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
        builder.define(DATA_ARMED, false);
    }

    public boolean isArmed() {
        return this.entityData.get(DATA_ARMED);
    }

    /** Arms or disarms this Thal, giving or removing the Dalek Laser Gun in the main hand. */
    public void setArmed(boolean armed) {
        this.entityData.set(DATA_ARMED, armed);
        if (this.level().isClientSide()) {
            return;
        }
        if (armed) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(DWMItems.LASER_GUN));
            this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        } else {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
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
        this.playSound(DWMSounds.DALEK_SHOOT, 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F);
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
        output.putBoolean("Armed", isArmed());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(ThalVariant.byId(input.getStringOr("Variant", ThalVariant.THAL_1.getSerializedName())));
        this.entityData.set(DATA_ARMED, input.getBooleanOr("Armed", false));
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
