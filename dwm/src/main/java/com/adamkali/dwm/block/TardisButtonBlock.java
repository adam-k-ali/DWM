package com.adamkali.dwm.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TardisButtonBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TardisButtonBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(propertiesCodec()).apply(instance, TardisButtonBlock::new));
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final VoxelShape NORTH_SOUTH_SHAPE_A = Block.box(5.0, 0, 1.0, 11.0, 2, 7.0);
    public static final VoxelShape NORTH_SOUTH_SHAPE_B = Block.box(5.0, 0, 9.0, 11.0, 2, 15.0);
    public static final VoxelShape EAST_WEST_SHAPE_A = Block.box(9.0, 0, 5.0, 15.0, 2, 11.0);
    public static final VoxelShape EAST_WEST_SHAPE_B = Block.box(1.0, 0, 5.0, 7.0, 2, 11.0);

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    public TardisButtonBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH, SOUTH -> Shapes.or(NORTH_SOUTH_SHAPE_A, NORTH_SOUTH_SHAPE_B);
            case EAST, WEST -> Shapes.or(EAST_WEST_SHAPE_A, EAST_WEST_SHAPE_B);
            default -> Shapes.block();
        };
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        for (Direction direction : ctx.getNearestLookingDirections()) {
            BlockState blockState;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockState = this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
            } else {
                blockState = this.defaultBlockState().setValue(FACING, direction.getOpposite());
            }

            if (blockState.canSurvive(ctx.getLevel(), ctx.getClickedPos())) {
                return blockState;
            }
        }
        return null;
    }

    private String getShapeHit(BlockHitResult hit, Map<String, VoxelShape> shapes) {
        BlockPos blockPos = hit.getBlockPos();
        Vec3 hitPos = hit.getLocation().subtract(new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()));

        final Vec3 translatedHitPos = new Vec3(hitPos.x, 0, hitPos.z); // Flatten the hit position to 2D

        for (Map.Entry<String, VoxelShape> entry : shapes.entrySet()) {
            String name = entry.getKey();
            VoxelShape shape = entry.getValue();
            if (shape.bounds().contains(translatedHitPos)) {
                return name;
            }
        }

        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        boolean buttonIsHit;
        String shapeHit;
        if (state.getValue(FACING) == Direction.NORTH || state.getValue(FACING) == Direction.SOUTH) {
            shapeHit = getShapeHit(hit, Map.of("NORTH_SOUTH_SHAPE_A", NORTH_SOUTH_SHAPE_A, "NORTH_SOUTH_SHAPE_B", NORTH_SOUTH_SHAPE_B));
        } else {
            shapeHit = getShapeHit(hit, Map.of("EAST_WEST_SHAPE_A", EAST_WEST_SHAPE_A, "EAST_WEST_SHAPE_B", EAST_WEST_SHAPE_B));
        }

        if (shapeHit == null) {
            return InteractionResult.PASS;
        }

        switch (state.getValue(FACING)) {
            case NORTH:
                buttonIsHit = shapeHit.equals("NORTH_SOUTH_SHAPE_B");
                break;
            case SOUTH:
                buttonIsHit = shapeHit.equals("NORTH_SOUTH_SHAPE_A");
                break;
            case EAST:
                buttonIsHit = shapeHit.equals("EAST_WEST_SHAPE_B");
                break;
            case WEST:
                buttonIsHit = shapeHit.equals("EAST_WEST_SHAPE_A");
                break;
            default:
                buttonIsHit = true;
        }
        if (!buttonIsHit) {
            return InteractionResult.PASS;
        }
        if (state.getValue(POWERED)) {
            return InteractionResult.CONSUME;
        }
        activate(player, world, pos, state);
        return InteractionResult.SUCCESS;
    }

    private void activate(Player player, Level world, BlockPos pos, BlockState state) {
        world.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_ALL);
        this.playClickSound(player, world, pos, true);
        world.scheduleTick(pos, this, 20);
    }

    private void playClickSound(@Nullable Player player, LevelAccessor world, BlockPos pos, boolean powered) {
        world.playSound(powered ? player : null, pos, this.getClickSound(powered), SoundSource.BLOCKS);
    }

    protected SoundEvent getClickSound(boolean powered) {
        return powered ? BlockSetType.STONE.buttonClickOn() : BlockSetType.STONE.buttonClickOff();
    }


    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            world.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
            this.playClickSound(null, world, pos, false);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, FACING);
    }

}
