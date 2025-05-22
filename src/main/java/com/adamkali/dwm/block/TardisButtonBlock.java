package com.adamkali.dwm.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class TardisButtonBlock extends HorizontalFacingBlock {
    public static final MapCodec<TardisButtonBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(createSettingsCodec()).apply(instance, TardisButtonBlock::new));
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final VoxelShape NORTH_SOUTH_SHAPE_A = Block.createCuboidShape(5.0, 0, 1.0, 11.0, 2, 7.0);
    public static final VoxelShape NORTH_SOUTH_SHAPE_B = Block.createCuboidShape(5.0, 0, 9.0, 11.0, 2, 15.0);
    public static final VoxelShape EAST_WEST_SHAPE_A = Block.createCuboidShape(9.0, 0, 5.0, 15.0, 2, 11.0);
    public static final VoxelShape EAST_WEST_SHAPE_B = Block.createCuboidShape(1.0, 0, 5.0, 7.0, 2, 11.0);

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    public TardisButtonBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWERED, false).with(FACING, Direction.NORTH));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH, SOUTH -> VoxelShapes.union(NORTH_SOUTH_SHAPE_A, NORTH_SOUTH_SHAPE_B);
            case EAST, WEST -> VoxelShapes.union(EAST_WEST_SHAPE_A, EAST_WEST_SHAPE_B);
            default -> VoxelShapes.fullCube();
        };
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        for (Direction direction : ctx.getPlacementDirections()) {
            BlockState blockState;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockState = this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
            } else {
                blockState = this.getDefaultState().with(FACING, direction.getOpposite());
            }

            if (blockState.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
                return blockState;
            }
        }
        return null;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // Work out which shape the player is hitting
        Vec3d hitPos = hit.getPos();
        Vec3d blockPos = Vec3d.ofCenter(pos);
        Vec3d distFromCenter = hitPos.subtract(blockPos);
        double distX = distFromCenter.x;
        double distZ = distFromCenter.z;
        boolean buttonIsHit;
        switch (state.get(FACING)) {
            case NORTH:
                buttonIsHit = distZ > 0;
                break;
            case SOUTH:
                buttonIsHit = distZ < 0;
                break;
            case EAST:
                buttonIsHit = distX < 0;
                break;
            case WEST:
                buttonIsHit = distX > 0;
                break;
            default:
                buttonIsHit = true;
        }
        if (!buttonIsHit) {
            return ActionResult.PASS;
        }
        if (state.get(POWERED)) {
            return ActionResult.CONSUME;
        }
        activate(player, world, pos, state);
        return ActionResult.SUCCESS;
    }

    private void activate(PlayerEntity player, World world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state.with(POWERED, true), Block.NOTIFY_ALL);
        this.playClickSound(player, world, pos, true);
        world.scheduleBlockTick(pos, this, 20);
    }

    private void playClickSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos, boolean powered) {
        world.playSound(powered ? player : null, pos, this.getClickSound(powered), SoundCategory.BLOCKS);
    }

    protected SoundEvent getClickSound(boolean powered) {
        return powered ? BlockSetType.STONE.buttonClickOn() : BlockSetType.STONE.buttonClickOff();
    }


    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(POWERED)) {
            world.setBlockState(pos, state.with(POWERED, false), Block.NOTIFY_ALL);
            this.playClickSound(null, world, pos, false);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED, FACING);
    }

}
