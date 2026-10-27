package com.adamkali.dwm.block.plant;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Mire shoreline reeds: the base must sit on soil next to water; further segments stack on reeds. */
public class MutatedReedsBlock extends SkaroPlantBlock {
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 16.0);
    public static final MapCodec<MutatedReedsBlock> CODEC = simpleCodec(MutatedReedsBlock::new);

    public MutatedReedsBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<MutatedReedsBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(this)) {
            return true;
        }
        if (!(below.is(BlockTags.DIRT) || below.is(BlockTags.SAND))) {
            return false;
        }
        BlockPos base = pos.below();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getFluidState(base.relative(direction)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }
}
