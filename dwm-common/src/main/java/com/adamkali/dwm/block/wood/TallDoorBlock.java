package com.adamkali.dwm.block.wood;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Three-block-tall wood door (one block id, {@link #SEGMENT} identity).
 * Open/powered state is synced across the column, matching vanilla {@code DoorBlock} semantics.
 */
public class TallDoorBlock extends Block {
    public static final MapCodec<TallDoorBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BlockSetType.CODEC.fieldOf("block_set_type").forGetter(TallDoorBlock::getBlockSetType),
                            propertiesCodec()
                    )
                    .apply(instance, TallDoorBlock::new)
    );

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<TallDoorSegment> SEGMENT = EnumProperty.create("segment", TallDoorSegment.class);

    public static final int HEIGHT = 3;

    protected static final VoxelShape NORTH_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EAST_SHAPE = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape WEST_SHAPE = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);

    private final BlockSetType blockSetType;

    public TallDoorBlock(BlockSetType type, BlockBehaviour.Properties settings) {
        super(settings.sound(type.soundType()));
        this.blockSetType = type;
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(OPEN, false)
                        .setValue(HINGE, DoorHingeSide.LEFT)
                        .setValue(POWERED, false)
                        .setValue(SEGMENT, TallDoorSegment.BOTTOM)
        );
    }

    @Override
    public MapCodec<? extends TallDoorBlock> codec() {
        return CODEC;
    }

    public BlockSetType getBlockSetType() {
        return this.blockSetType;
    }

    public static boolean isOrigin(BlockState state) {
        return state.hasProperty(SEGMENT) && state.getValue(SEGMENT) == TallDoorSegment.BOTTOM;
    }

    public static BlockPos cellPos(BlockPos origin, TallDoorSegment segment) {
        return origin.above(segment.index());
    }

    public static BlockPos originPos(BlockPos pos, BlockState state) {
        return pos.below(state.getValue(SEGMENT).index());
    }

    /**
     * Pure helper: whether a redstone edge should rewrite {@link #POWERED}/{@link #OPEN}.
     */
    public static boolean shouldApplyPowerEdge(boolean poweredNow, boolean currentlyPowered) {
        return poweredNow != currentlyPowered;
    }

    /**
     * Whether creative / non-harvest breaks of this segment must destroy the origin first
     * with {@link Block#UPDATE_SUPPRESS_DROPS} (vanilla {@code DoorBlock} / {@code TallPlantBlock} pattern).
     */
    public static boolean shouldPreventCreativeDropFromOrigin(TallDoorSegment segment) {
        return segment != TallDoorSegment.BOTTOM;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        boolean closed = !state.getValue(OPEN);
        boolean right = state.getValue(HINGE) == DoorHingeSide.RIGHT;
        return switch (direction) {
            case SOUTH -> closed ? NORTH_SHAPE : (right ? WEST_SHAPE : EAST_SHAPE);
            case WEST -> closed ? EAST_SHAPE : (right ? NORTH_SHAPE : SOUTH_SHAPE);
            case NORTH -> closed ? SOUTH_SHAPE : (right ? EAST_SHAPE : WEST_SHAPE);
            default -> closed ? WEST_SHAPE : (right ? SOUTH_SHAPE : NORTH_SHAPE);
        };
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader world,
            ScheduledTickAccess tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        TallDoorSegment segment = state.getValue(SEGMENT);
        if (direction.getAxis() == Direction.Axis.Y) {
            boolean expectsNeighborBelow = direction == Direction.DOWN && segment != TallDoorSegment.BOTTOM;
            boolean expectsNeighborAbove = direction == Direction.UP && segment != TallDoorSegment.TOP;
            if (expectsNeighborBelow || expectsNeighborAbove) {
                if (neighborState.is(this) && neighborState.getValue(SEGMENT) != segment) {
                    return neighborState.setValue(SEGMENT, segment);
                }
                return Blocks.AIR.defaultBlockState();
            }
        }
        if (segment == TallDoorSegment.BOTTOM && direction == Direction.DOWN && !state.canSurvive(world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide() && (player.isCreative() || !player.hasCorrectToolForDrops(state))) {
            preventCreativeDropFromOrigin(world, pos, state, player);
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    /**
     * Destroys the bottom segment without dropping when a non-bottom segment is broken in creative
     * (or when the player cannot harvest). Neighbor updates then clear the remaining column;
     * loot only drops from {@link TallDoorSegment#BOTTOM}.
     */
    private void preventCreativeDropFromOrigin(Level world, BlockPos pos, BlockState state, Player player) {
        if (!shouldPreventCreativeDropFromOrigin(state.getValue(SEGMENT))) {
            return;
        }
        BlockPos origin = originPos(pos, state);
        BlockState bottomState = world.getBlockState(origin);
        if (bottomState.is(this) && bottomState.getValue(SEGMENT) == TallDoorSegment.BOTTOM) {
            world.setBlock(origin, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            world.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, origin, Block.getId(bottomState));
        }
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return switch (type) {
            case LAND, AIR -> state.getValue(OPEN);
            case WATER -> false;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        Level world = ctx.getLevel();
        if (pos.getY() + HEIGHT - 1 > world.getMaxY()) {
            return null;
        }
        for (int i = 1; i < HEIGHT; i++) {
            if (!world.getBlockState(pos.above(i)).canBeReplaced(ctx)) {
                return null;
            }
        }
        boolean powered = isColumnPowered(world, pos);
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection())
                .setValue(HINGE, getHinge(ctx))
                .setValue(POWERED, powered)
                .setValue(OPEN, powered)
                .setValue(SEGMENT, TallDoorSegment.BOTTOM);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        for (TallDoorSegment segment : TallDoorSegment.values()) {
            if (segment == TallDoorSegment.BOTTOM) {
                continue;
            }
            world.setBlock(pos.above(segment.index()), state.setValue(SEGMENT, segment), Block.UPDATE_ALL);
        }
    }

    private DoorHingeSide getHinge(BlockPlaceContext ctx) {
        BlockGetter world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction facing = ctx.getHorizontalDirection();
        Direction ccw = facing.getCounterClockWise();
        Direction cw = facing.getClockWise();

        int score = 0;
        for (int i = 0; i < HEIGHT; i++) {
            BlockPos cell = pos.above(i);
            BlockPos left = cell.relative(ccw);
            BlockPos right = cell.relative(cw);
            if (world.getBlockState(left).isCollisionShapeFullBlock(world, left)) {
                score--;
            }
            if (world.getBlockState(right).isCollisionShapeFullBlock(world, right)) {
                score++;
            }
        }
        if (score > 0) {
            return DoorHingeSide.LEFT;
        }
        if (score < 0) {
            return DoorHingeSide.RIGHT;
        }

        int ox = facing.getStepX();
        int oz = facing.getStepZ();
        Vec3 hit = ctx.getClickLocation();
        double dx = hit.x - (double) pos.getX();
        double dz = hit.z - (double) pos.getZ();
        return (ox >= 0 || !(dz < 0.5))
                        && (ox <= 0 || !(dz > 0.5))
                        && (oz >= 0 || !(dx > 0.5))
                        && (oz <= 0 || !(dx < 0.5))
                ? DoorHingeSide.LEFT
                : DoorHingeSide.RIGHT;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!this.blockSetType.canOpenByHand()) {
            return InteractionResult.PASS;
        }
        boolean open = !state.getValue(OPEN);
        setOpen(player, world, state, pos, open);
        return InteractionResult.SUCCESS;
    }

    public boolean isOpen(BlockState state) {
        return state.getValue(OPEN);
    }

    public void setOpen(@Nullable Entity entity, Level world, BlockState state, BlockPos pos, boolean open) {
        if (!state.is(this) || state.getValue(OPEN) == open) {
            return;
        }
        setColumnOpenAndPowered(world, pos, state, open, state.getValue(POWERED), true, entity);
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level world,
            BlockPos pos,
            Block sourceBlock,
            @Nullable Orientation wireOrientation,
            boolean notify
    ) {
        BlockPos origin = originPos(pos, state);
        boolean poweredNow = isColumnPowered(world, origin);
        if (!this.defaultBlockState().is(sourceBlock) && shouldApplyPowerEdge(poweredNow, state.getValue(POWERED))) {
            setColumnOpenAndPowered(world, pos, state, poweredNow, poweredNow, state.getValue(OPEN) != poweredNow, null);
        }
    }

    private void setColumnOpenAndPowered(
            Level world,
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
            if (!cellState.is(this)) {
                continue;
            }
            world.setBlock(
                    cell,
                    cellState.setValue(OPEN, open).setValue(POWERED, powered),
                    Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE
            );
        }
        if (playSound) {
            playOpenCloseSound(entity, world, origin, open);
            world.gameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, origin);
        }
    }

    public static boolean isColumnPowered(Level world, BlockPos origin) {
        for (TallDoorSegment segment : TallDoorSegment.values()) {
            if (world.hasNeighborSignal(cellPos(origin, segment))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        TallDoorSegment segment = state.getValue(SEGMENT);
        if (segment == TallDoorSegment.BOTTOM) {
            BlockPos below = pos.below();
            return world.getBlockState(below).isFaceSturdy(world, below, Direction.UP);
        }
        BlockState below = world.getBlockState(pos.below());
        return below.is(this) && below.getValue(SEGMENT).index() == segment.index() - 1;
    }

    private void playOpenCloseSound(@Nullable Entity entity, Level world, BlockPos pos, boolean open) {
        world.playSound(
                entity,
                pos,
                open ? this.blockSetType.doorOpen() : this.blockSetType.doorClose(),
                SoundSource.BLOCKS,
                1.0F,
                world.getRandom().nextFloat() * 0.1F + 0.9F
        );
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return mirror == Mirror.NONE
                ? state
                : state.rotate(mirror.getRotation(state.getValue(FACING))).cycle(HINGE);
    }

    @Override
    protected long getSeed(BlockState state, BlockPos pos) {
        return Mth.getSeed(pos.getX(), originPos(pos, state).getY(), pos.getZ());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SEGMENT, FACING, OPEN, HINGE, POWERED);
    }
}
