package com.adamkali.dwm.block.wood;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

/**
 * Three-block-tall wood door (one block id, {@link #SEGMENT} identity).
 * Open/powered state is synced across the column, matching vanilla {@code DoorBlock} semantics.
 */
public class TallDoorBlock extends Block {
    public static final MapCodec<TallDoorBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BlockSetType.CODEC.fieldOf("block_set_type").forGetter(TallDoorBlock::getBlockSetType),
                            createSettingsCodec()
                    )
                    .apply(instance, TallDoorBlock::new)
    );

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = Properties.OPEN;
    public static final EnumProperty<DoorHinge> HINGE = Properties.DOOR_HINGE;
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final EnumProperty<TallDoorSegment> SEGMENT = EnumProperty.of("segment", TallDoorSegment.class);

    public static final int HEIGHT = 3;

    protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
    protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);

    private final BlockSetType blockSetType;

    public TallDoorBlock(BlockSetType type, AbstractBlock.Settings settings) {
        super(settings.sounds(type.soundType()));
        this.blockSetType = type;
        this.setDefaultState(
                this.stateManager
                        .getDefaultState()
                        .with(FACING, Direction.NORTH)
                        .with(OPEN, false)
                        .with(HINGE, DoorHinge.LEFT)
                        .with(POWERED, false)
                        .with(SEGMENT, TallDoorSegment.BOTTOM)
        );
    }

    @Override
    public MapCodec<? extends TallDoorBlock> getCodec() {
        return CODEC;
    }

    public BlockSetType getBlockSetType() {
        return this.blockSetType;
    }

    public static boolean isOrigin(BlockState state) {
        return state.contains(SEGMENT) && state.get(SEGMENT) == TallDoorSegment.BOTTOM;
    }

    public static BlockPos cellPos(BlockPos origin, TallDoorSegment segment) {
        return origin.up(segment.index());
    }

    public static BlockPos originPos(BlockPos pos, BlockState state) {
        return pos.down(state.get(SEGMENT).index());
    }

    /**
     * Pure helper: whether a redstone edge should rewrite {@link #POWERED}/{@link #OPEN}.
     */
    public static boolean shouldApplyPowerEdge(boolean poweredNow, boolean currentlyPowered) {
        return poweredNow != currentlyPowered;
    }

    /**
     * Whether creative / non-harvest breaks of this segment must destroy the origin first
     * with {@link Block#SKIP_DROPS} (vanilla {@code DoorBlock} / {@code TallPlantBlock} pattern).
     */
    public static boolean shouldPreventCreativeDropFromOrigin(TallDoorSegment segment) {
        return segment != TallDoorSegment.BOTTOM;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction direction = state.get(FACING);
        boolean closed = !state.get(OPEN);
        boolean right = state.get(HINGE) == DoorHinge.RIGHT;
        return switch (direction) {
            case SOUTH -> closed ? NORTH_SHAPE : (right ? WEST_SHAPE : EAST_SHAPE);
            case WEST -> closed ? EAST_SHAPE : (right ? NORTH_SHAPE : SOUTH_SHAPE);
            case NORTH -> closed ? SOUTH_SHAPE : (right ? EAST_SHAPE : WEST_SHAPE);
            default -> closed ? WEST_SHAPE : (right ? SOUTH_SHAPE : NORTH_SHAPE);
        };
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state,
            WorldView world,
            ScheduledTickView tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            Random random
    ) {
        TallDoorSegment segment = state.get(SEGMENT);
        if (direction.getAxis() == Direction.Axis.Y) {
            boolean expectsNeighborBelow = direction == Direction.DOWN && segment != TallDoorSegment.BOTTOM;
            boolean expectsNeighborAbove = direction == Direction.UP && segment != TallDoorSegment.TOP;
            if (expectsNeighborBelow || expectsNeighborAbove) {
                if (neighborState.isOf(this) && neighborState.get(SEGMENT) != segment) {
                    return neighborState.with(SEGMENT, segment);
                }
                return Blocks.AIR.getDefaultState();
            }
        }
        if (segment == TallDoorSegment.BOTTOM && direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && (player.isCreative() || !player.canHarvest(state))) {
            preventCreativeDropFromOrigin(world, pos, state, player);
        }
        return super.onBreak(world, pos, state, player);
    }

    /**
     * Destroys the bottom segment without dropping when a non-bottom segment is broken in creative
     * (or when the player cannot harvest). Neighbor updates then clear the remaining column;
     * loot only drops from {@link TallDoorSegment#BOTTOM}.
     */
    private void preventCreativeDropFromOrigin(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!shouldPreventCreativeDropFromOrigin(state.get(SEGMENT))) {
            return;
        }
        BlockPos origin = originPos(pos, state);
        BlockState bottomState = world.getBlockState(origin);
        if (bottomState.isOf(this) && bottomState.get(SEGMENT) == TallDoorSegment.BOTTOM) {
            world.setBlockState(origin, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
            world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, origin, Block.getRawIdFromState(bottomState));
        }
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return switch (type) {
            case LAND, AIR -> state.get(OPEN);
            case WATER -> false;
        };
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();
        if (pos.getY() + HEIGHT - 1 > world.getTopYInclusive()) {
            return null;
        }
        for (int i = 1; i < HEIGHT; i++) {
            if (!world.getBlockState(pos.up(i)).canReplace(ctx)) {
                return null;
            }
        }
        boolean powered = isColumnPowered(world, pos);
        return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing())
                .with(HINGE, getHinge(ctx))
                .with(POWERED, powered)
                .with(OPEN, powered)
                .with(SEGMENT, TallDoorSegment.BOTTOM);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        for (TallDoorSegment segment : TallDoorSegment.values()) {
            if (segment == TallDoorSegment.BOTTOM) {
                continue;
            }
            world.setBlockState(pos.up(segment.index()), state.with(SEGMENT, segment), Block.NOTIFY_ALL);
        }
    }

    private DoorHinge getHinge(ItemPlacementContext ctx) {
        BlockView world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        Direction facing = ctx.getHorizontalPlayerFacing();
        Direction ccw = facing.rotateYCounterclockwise();
        Direction cw = facing.rotateYClockwise();

        int score = 0;
        for (int i = 0; i < HEIGHT; i++) {
            BlockPos cell = pos.up(i);
            BlockPos left = cell.offset(ccw);
            BlockPos right = cell.offset(cw);
            if (world.getBlockState(left).isFullCube(world, left)) {
                score--;
            }
            if (world.getBlockState(right).isFullCube(world, right)) {
                score++;
            }
        }
        if (score > 0) {
            return DoorHinge.LEFT;
        }
        if (score < 0) {
            return DoorHinge.RIGHT;
        }

        int ox = facing.getOffsetX();
        int oz = facing.getOffsetZ();
        Vec3d hit = ctx.getHitPos();
        double dx = hit.x - (double) pos.getX();
        double dz = hit.z - (double) pos.getZ();
        return (ox >= 0 || !(dz < 0.5))
                        && (ox <= 0 || !(dz > 0.5))
                        && (oz >= 0 || !(dx > 0.5))
                        && (oz <= 0 || !(dx < 0.5))
                ? DoorHinge.LEFT
                : DoorHinge.RIGHT;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!this.blockSetType.canOpenByHand()) {
            return ActionResult.PASS;
        }
        boolean open = !state.get(OPEN);
        setOpen(player, world, state, pos, open);
        return ActionResult.SUCCESS;
    }

    public boolean isOpen(BlockState state) {
        return state.get(OPEN);
    }

    public void setOpen(@Nullable Entity entity, World world, BlockState state, BlockPos pos, boolean open) {
        if (!state.isOf(this) || state.get(OPEN) == open) {
            return;
        }
        setColumnOpenAndPowered(world, pos, state, open, state.get(POWERED), true, entity);
    }

    @Override
    protected void neighborUpdate(
            BlockState state,
            World world,
            BlockPos pos,
            Block sourceBlock,
            @Nullable WireOrientation wireOrientation,
            boolean notify
    ) {
        BlockPos origin = originPos(pos, state);
        boolean poweredNow = isColumnPowered(world, origin);
        if (!this.getDefaultState().isOf(sourceBlock) && shouldApplyPowerEdge(poweredNow, state.get(POWERED))) {
            setColumnOpenAndPowered(world, pos, state, poweredNow, poweredNow, state.get(OPEN) != poweredNow, null);
        }
    }

    private void setColumnOpenAndPowered(
            World world,
            BlockPos pos,
            BlockState state,
            boolean open,
            boolean powered,
            boolean playSound,
            @Nullable Entity entity
    ) {
        BlockPos origin = originPos(pos, state);
        for (TallDoorSegment segment : TallDoorSegment.values()) {
            BlockPos cell = cellPos(origin, segment);
            BlockState cellState = world.getBlockState(cell);
            if (!cellState.isOf(this)) {
                continue;
            }
            world.setBlockState(
                    cell,
                    cellState.with(OPEN, open).with(POWERED, powered),
                    Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD
            );
        }
        if (playSound) {
            playOpenCloseSound(entity, world, origin, open);
            world.emitGameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, origin);
        }
    }

    public static boolean isColumnPowered(World world, BlockPos origin) {
        for (TallDoorSegment segment : TallDoorSegment.values()) {
            if (world.isReceivingRedstonePower(cellPos(origin, segment))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        TallDoorSegment segment = state.get(SEGMENT);
        if (segment == TallDoorSegment.BOTTOM) {
            BlockPos below = pos.down();
            return world.getBlockState(below).isSideSolidFullSquare(world, below, Direction.UP);
        }
        BlockState below = world.getBlockState(pos.down());
        return below.isOf(this) && below.get(SEGMENT).index() == segment.index() - 1;
    }

    private void playOpenCloseSound(@Nullable Entity entity, World world, BlockPos pos, boolean open) {
        world.playSound(
                entity,
                pos,
                open ? this.blockSetType.doorOpen() : this.blockSetType.doorClose(),
                SoundCategory.BLOCKS,
                1.0F,
                world.getRandom().nextFloat() * 0.1F + 0.9F
        );
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return mirror == BlockMirror.NONE
                ? state
                : state.rotate(mirror.getRotation(state.get(FACING))).cycle(HINGE);
    }

    @Override
    protected long getRenderingSeed(BlockState state, BlockPos pos) {
        return MathHelper.hashCode(pos.getX(), originPos(pos, state).getY(), pos.getZ());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SEGMENT, FACING, OPEN, HINGE, POWERED);
    }
}
