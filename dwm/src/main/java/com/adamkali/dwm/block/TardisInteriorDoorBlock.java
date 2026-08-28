package com.adamkali.dwm.block;

import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.item.SonicTardisLogic;
import com.adamkali.dwm.sound.DWMSounds;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.TardisInteriorDoorShapes;
import com.adamkali.dwm.tardis.interior.TardisInteriorService;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Classic interior double-door bank (3 wide × 2 tall), modeled after vanilla {@code DoorBlock}:
 * one block id, part identity in blockstate, {@link #OPEN} synced across the bank, and a single
 * block entity on the origin cell ({@code half=lower}, {@code slot=0}).
 */
public class TardisInteriorDoorBlock extends Block implements EntityBlock {
    private static final MapCodec<TardisInteriorDoorBlock> CODEC = simpleCodec(TardisInteriorDoorBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty SLOT = IntegerProperty.create("slot", 0, 2);
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public static final int BANK_WIDTH = 3;
    public static final int BANK_HEIGHT = 2;

    public TardisInteriorDoorBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(SLOT, 0)
                .setValue(OPEN, true));
    }

    @Override
    protected MapCodec<TardisInteriorDoorBlock> codec() {
        return CODEC;
    }

    public static boolean isOrigin(BlockState state) {
        return state.hasProperty(HALF) && state.hasProperty(SLOT)
                && state.getValue(HALF) == DoubleBlockHalf.LOWER
                && state.getValue(SLOT) == 0;
    }

    /**
     * Bank cells increase along {@code facing.rotateYCounterclockwise()} from the origin;
     * upper half is one block above lower.
     */
    public static BlockPos cellPos(BlockPos origin, Direction facing, DoubleBlockHalf half, int slot) {
        Direction alongBank = facing.getCounterClockWise();
        BlockPos cell = origin.relative(alongBank, slot);
        if (half == DoubleBlockHalf.UPPER) {
            cell = cell.above();
        }
        return cell;
    }

    public static BlockPos originPos(BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        int slot = state.getValue(SLOT);
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        return lower.relative(facing.getClockWise(), slot);
    }

    public static BlockState bankCellState(Direction facing, DoubleBlockHalf half, int slot, boolean open) {
        return DWMBlocks.TARDIS_INTERIOR_DOOR.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(HALF, half)
                .setValue(SLOT, slot)
                .setValue(OPEN, open);
    }

    public static @Nullable TardisInteriorDoorBlockEntity getOriginEntity(BlockGetter world, BlockPos pos, BlockState state) {
        BlockPos origin = originPos(pos, state);
        if (world.getBlockEntity(origin) instanceof TardisInteriorDoorBlockEntity door) {
            return door;
        }
        return null;
    }

    /**
     * Sets {@link #OPEN} on every cell in the bank and drives the origin BE swing target.
     * Mid-swing clicks are ignored unless {@code ignoreSwing} is true (canonical model sync).
     */
    public static void setOpen(Level world, BlockPos pos, BlockState state, boolean open) {
        setOpen(world, pos, state, open, false);
    }

    public static void setOpen(Level world, BlockPos pos, BlockState state, boolean open, boolean ignoreSwing) {
        Direction facing = state.getValue(FACING);
        BlockPos origin = originPos(pos, state);
        TardisInteriorDoorBlockEntity originEntity = null;
        if (world.getBlockEntity(origin) instanceof TardisInteriorDoorBlockEntity door) {
            originEntity = door;
            if (!ignoreSwing && door.isSwingInProgress()) {
                return;
            }
        }

        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (int slot = 0; slot < BANK_WIDTH; slot++) {
                BlockPos cell = cellPos(origin, facing, half, slot);
                BlockState cellState = world.getBlockState(cell);
                if (!cellState.is(DWMBlocks.TARDIS_INTERIOR_DOOR)
                        || cellState.getValue(FACING) != facing) {
                    continue;
                }
                world.setBlock(cell, cellState.setValue(OPEN, open), Block.UPDATE_CLIENTS);
            }
        }

        if (originEntity != null) {
            originEntity.setOpen(open);
        }
    }

    public static void toggleOpen(Level world, BlockPos pos, BlockState state) {
        setOpen(world, pos, state, !state.getValue(OPEN));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(SLOT, 0)
                .setValue(OPEN, true);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, SLOT, OPEN);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (!isOrigin(state)) {
            return null;
        }
        return new TardisInteriorDoorBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (SonicTardisLogic.shouldYieldToSonic(player)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        TardisInteriorDoorBlockEntity origin = getOriginEntity(world, pos, state);
        UUID tardisId = origin == null ? null : origin.getTardisId();
        TardisDataModel model = tardisId == null ? null : TardisDataLoader.get(tardisId);
        if (model != null) {
            InteractionResult result = TardisLogic.toggleDoor(tardisId);
            if (result == InteractionResult.FAIL && TardisLogic.areDoorsLocked(tardisId)) {
                player.sendOverlayMessage(Component.translatable("dwm.console.doors_are_locked"));
                return InteractionResult.CONSUME;
            }
            if (result != InteractionResult.SUCCESS) {
                return InteractionResult.CONSUME;
            }
            boolean open = model.doorState.isOpen;
            setOpen(world, pos, state, open, true);
            SoundEvent sound = open ? DWMSounds.TARDIS_DOOR_OPEN : DWMSounds.TARDIS_DOOR_CLOSE;
            world.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        toggleOpen(world, pos, state);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void entityInside(
            BlockState state,
            Level world,
            BlockPos pos,
            Entity entity,
            net.minecraft.world.entity.InsideBlockEffectApplier effectApplier,
            boolean firstTick
    ) {
        if (world.isClientSide() || !(entity instanceof ServerPlayer player)) {
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
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        TardisInteriorDoorBlockEntity doorEntity = getOriginEntity(world, pos, state);
        if (doorEntity != null && doorEntity.isOpenEnoughForExit()) {
            return Shapes.empty();
        }
        return TardisInteriorDoorShapes.outline(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return TardisInteriorDoorShapes.outline(state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
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
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
