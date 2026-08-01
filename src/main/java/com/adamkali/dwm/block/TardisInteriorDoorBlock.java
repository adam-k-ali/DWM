package com.adamkali.dwm.block;

import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.tardis.interior.TardisInteriorDoorShapes;
import com.adamkali.dwm.tardis.interior.TardisInteriorService;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Classic interior double-door bank (3 wide × 2 tall), modeled after vanilla {@code DoorBlock}:
 * one block id, part identity in blockstate, {@link #OPEN} synced across the bank, and a single
 * block entity on the origin cell ({@code half=lower}, {@code slot=0}).
 */
public class TardisInteriorDoorBlock extends Block implements BlockEntityProvider {
    private static final MapCodec<TardisInteriorDoorBlock> CODEC = createCodec(TardisInteriorDoorBlock::new);

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
    public static final IntProperty SLOT = IntProperty.of("slot", 0, 2);
    public static final BooleanProperty OPEN = Properties.OPEN;

    public static final int BANK_WIDTH = 3;
    public static final int BANK_HEIGHT = 2;

    public TardisInteriorDoorBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(HALF, DoubleBlockHalf.LOWER)
                .with(SLOT, 0)
                .with(OPEN, true));
    }

    @Override
    protected MapCodec<TardisInteriorDoorBlock> getCodec() {
        return CODEC;
    }

    public static boolean isOrigin(BlockState state) {
        return state.contains(HALF) && state.contains(SLOT)
                && state.get(HALF) == DoubleBlockHalf.LOWER
                && state.get(SLOT) == 0;
    }

    /**
     * Bank cells increase along {@code facing.rotateYCounterclockwise()} from the origin;
     * upper half is one block above lower.
     */
    public static BlockPos cellPos(BlockPos origin, Direction facing, DoubleBlockHalf half, int slot) {
        Direction alongBank = facing.rotateYCounterclockwise();
        BlockPos cell = origin.offset(alongBank, slot);
        if (half == DoubleBlockHalf.UPPER) {
            cell = cell.up();
        }
        return cell;
    }

    public static BlockPos originPos(BlockPos pos, BlockState state) {
        Direction facing = state.get(FACING);
        int slot = state.get(SLOT);
        BlockPos lower = state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
        return lower.offset(facing.rotateYClockwise(), slot);
    }

    public static BlockState bankCellState(Direction facing, DoubleBlockHalf half, int slot, boolean open) {
        return DWMBlocks.TARDIS_INTERIOR_DOOR.getDefaultState()
                .with(FACING, facing)
                .with(HALF, half)
                .with(SLOT, slot)
                .with(OPEN, open);
    }

    public static @Nullable TardisInteriorDoorBlockEntity getOriginEntity(BlockView world, BlockPos pos, BlockState state) {
        BlockPos origin = originPos(pos, state);
        if (world.getBlockEntity(origin) instanceof TardisInteriorDoorBlockEntity door) {
            return door;
        }
        return null;
    }

    /**
     * Sets {@link #OPEN} on every cell in the bank and drives the origin BE swing target.
     */
    public static void setOpen(World world, BlockPos pos, BlockState state, boolean open) {
        Direction facing = state.get(FACING);
        BlockPos origin = originPos(pos, state);
        TardisInteriorDoorBlockEntity originEntity = null;
        if (world.getBlockEntity(origin) instanceof TardisInteriorDoorBlockEntity door) {
            originEntity = door;
            if (door.isSwingInProgress()) {
                return;
            }
        }

        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (int slot = 0; slot < BANK_WIDTH; slot++) {
                BlockPos cell = cellPos(origin, facing, half, slot);
                BlockState cellState = world.getBlockState(cell);
                if (!cellState.isOf(DWMBlocks.TARDIS_INTERIOR_DOOR)
                        || cellState.get(FACING) != facing) {
                    continue;
                }
                world.setBlockState(cell, cellState.with(OPEN, open), Block.NOTIFY_LISTENERS);
            }
        }

        if (originEntity != null) {
            originEntity.setOpen(open);
        }
    }

    public static void toggleOpen(World world, BlockPos pos, BlockState state) {
        setOpen(world, pos, state, !state.get(OPEN));
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(HALF, DoubleBlockHalf.LOWER)
                .with(SLOT, 0)
                .with(OPEN, true);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, SLOT, OPEN);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (!isOrigin(state)) {
            return null;
        }
        return new TardisInteriorDoorBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player.isSneaking()) {
            return ActionResult.PASS;
        }
        toggleOpen(world, pos, state);
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient() || !(entity instanceof ServerPlayerEntity player)) {
            return;
        }
        TardisInteriorDoorBlockEntity doorEntity = getOriginEntity(world, pos, state);
        if (doorEntity == null || !doorEntity.isOpenEnoughForExit()) {
            return;
        }
        TardisInteriorService.tryExitToExterior(player, doorEntity);
    }

    /**
     * Open interior doors are non-solid so players can walk through and trigger exit collision.
     * Closed collision is the per-cell clip of the classic door mesh.
     */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        TardisInteriorDoorBlockEntity doorEntity = getOriginEntity(world, pos, state);
        if (doorEntity != null && doorEntity.isOpenEnoughForExit()) {
            return VoxelShapes.empty();
        }
        return TardisInteriorDoorShapes.outline(state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return TardisInteriorDoorShapes.outline(state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!isOrigin(state)) {
            return null;
        }
        return validateTicker(type, DWMBlockEntities.TARDIS_INTERIOR_DOOR_BLOCK_ENTITY,
                (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1, blockEntity));
    }

    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> validateTicker(
            BlockEntityType<A> givenType,
            BlockEntityType<E> expectedType,
            BlockEntityTicker<? super E> ticker
    ) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }
}
