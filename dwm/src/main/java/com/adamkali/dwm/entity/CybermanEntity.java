package com.adamkali.dwm.entity;

import com.adamkali.dwm.entity.definition.EntityDefinitions;
import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Shared base for every Cyberman type: a hostile humanoid that marches on the ground, shares the
 * Cyberman sound set and never hurts another Cyberman. Stats and AI come from each type's
 * {@code data/dwm/entity/definition/<id>.json}; subclasses add their signature ability.
 */
public abstract class CybermanEntity extends Monster {
    private static final float STEP_VOLUME = 0.3F;

    protected CybermanEntity(EntityType<? extends CybermanEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    /** True for any entity in {@code #dwm:cybermen}. */
    public static boolean isCyberman(@Nullable Entity entity) {
        return entity != null && entity.is(DWMEntityTypeTags.CYBERMEN);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        EntityDefinitions.registerGoals(this, this.goalSelector, this.targetSelector);
    }

    /** Cybermen ignore damage dealt by other Cybermen (melee or wrist laser). */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (isCyberman(source.getEntity())) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return DWMSounds.CYBERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return DWMSounds.CYBERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DWMSounds.CYBERMAN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(DWMSounds.CYBERMAN_STEP, STEP_VOLUME, 1.0F);
    }
}
