package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Kaled mutant: the creature inside a Dalek casing. A small, slow hostile crawler that lashes at
 * players in melee. It has no natural spawns; it crawls out of destroyed Daleks
 * ({@link DalekMutantEmergence}). Stats and AI come from {@code dalek_mutant.json}.
 */
public class DalekMutantEntity extends Monster {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dalek_mutant");

    public DalekMutantEntity(EntityType<? extends DalekMutantEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityDefinitions.createAttributes(ID);
    }

    @Override
    protected void registerGoals() {
        EntityDefinitions.registerGoals(this, this.goalSelector, this.targetSelector);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return DWMSounds.DALEK_MUTANT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return DWMSounds.DALEK_MUTANT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DWMSounds.DALEK_MUTANT_DEATH;
    }
}
