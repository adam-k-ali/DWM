package com.adamkali.dwm.block.plant;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Decorative stacked cane for Gallifrey builds. Survives on dirt/sand (including Gallifrey
 * and orange sand via tags) or another cane below — no water adjacency and no random growth.
 */
public class SaccharineCaneBlock extends Block {
    public static final MapCodec<SaccharineCaneBlock> CODEC = simpleCodec(SaccharineCaneBlock::new);
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 16.0);

    public SaccharineCaneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<SaccharineCaneBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random
    ) {
        if (!state.canSurvive(level, pos)) {
            ticks.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(this)) {
            return true;
        }
        return below.is(BlockTags.DIRT) || below.is(BlockTags.SAND);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return state.getFluidState().isEmpty();
    }
}
