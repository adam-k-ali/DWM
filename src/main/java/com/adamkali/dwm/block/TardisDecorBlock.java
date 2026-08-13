package com.adamkali.dwm.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
 * Horizontally facing interior decor with JSON block models (chairs, column).
 */
public class TardisDecorBlock extends HorizontalDirectionalBlock {
    private final MapCodec<TardisDecorBlock> codec;
    private final Map<Direction, VoxelShape> shapesByFacing;

    public TardisDecorBlock(Properties settings, VoxelShape northShape) {
        this(settings, facing -> TardisDecorShapes.rotateHorizontal(northShape, facing));
    }

    public TardisDecorBlock(Properties settings, Function<Direction, VoxelShape> shapeFactory) {
        super(settings);
        this.codec = RecordCodecBuilder.mapCodec(instance ->
                instance.group(propertiesCodec()).apply(instance, props -> new TardisDecorBlock(props, shapeFactory)));
        EnumMap<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            shapes.put(facing, shapeFactory.apply(facing));
        }
        this.shapesByFacing = Map.copyOf(shapes);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return codec;
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
