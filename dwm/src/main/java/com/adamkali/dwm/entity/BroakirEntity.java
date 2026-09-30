package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Gallifrey forest animal. Passive wanderer; not rideable or breedable in this pass.
 */
public class BroakirEntity extends Animal {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "broakir");

    public BroakirEntity(EntityType<? extends BroakirEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityDefinitions.createAttributes(ID);
    }

    @Override
    protected void registerGoals() {
        EntityDefinitions.registerGoals(this, this.goalSelector, this.targetSelector);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public BroakirEntity getBreedOffspring(ServerLevel level, AgeableMob other) {
        return DWMEntityTypes.BROAKIR.create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return DWMSounds.BROAKIR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return DWMSounds.BROAKIR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DWMSounds.BROAKIR_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(DWMSounds.BROAKIR_STEP, 0.15F, 1.0F);
    }
}
