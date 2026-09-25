package com.adamkali.dwm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Horizontally facing interior decor with JSON block models (chairs, column, ceiling vent).
 */
public class TardisDecorBlock extends HorizontalDirectionalBlock {
    private final Map<Direction, VoxelShape> shapesByFacing;

    public TardisDecorBlock(Properties settings, VoxelShape northShape) {
        this(settings, facing -> TardisDecorShapes.rotateHorizontal(northShape, facing));
    }

    public TardisDecorBlock(Properties settings, Function<Direction, VoxelShape> shapeFactory) {
        super(settings);
        EnumMap<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            shapes.put(facing, shapeFactory.apply(facing));
        }
        this.shapesByFacing = Map.copyOf(shapes);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, TardisDecorShapes.facingForPlacement(ctx.getHorizontalDirection()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return shapesByFacing.getOrDefault(state.getValue(FACING), shapesByFacing.get(Direction.NORTH));
    }
}
