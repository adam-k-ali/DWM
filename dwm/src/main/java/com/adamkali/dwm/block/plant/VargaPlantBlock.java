package com.adamkali.dwm.block.plant;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rare, hazardous Skaro weed. Contact poisons living entities server-side; the effect is only
 * (re)applied once the remaining duration drops below {@link #REFRESH_THRESHOLD_TICKS}, so standing
 * in a patch cannot stack or endlessly extend it.
 */
public class VargaPlantBlock extends SkaroPlantBlock {
    public static final MapCodec<VargaPlantBlock> CODEC = simpleCodec(VargaPlantBlock::new);
    public static final int POISON_DURATION_TICKS = 100;
    public static final int REFRESH_THRESHOLD_TICKS = 40;

    public VargaPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<VargaPlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected void entityInside(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            boolean firstTick
    ) {
        if (level.isClientSide() || !(entity instanceof LivingEntity living)) {
            return;
        }
        applyPoison(living);
    }

    /** Applies the Varga poison unless a long enough one is already running. Returns whether it applied. */
    public static boolean applyPoison(LivingEntity entity) {
        MobEffectInstance current = entity.getEffect(MobEffects.POISON);
        if (current != null && current.getDuration() > REFRESH_THRESHOLD_TICKS) {
            return false;
        }
        return entity.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION_TICKS, 0));
    }
}
