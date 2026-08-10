package com.adamkali.dwm.block;

import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.network.OpenTardisChameleonScreen;
import com.adamkali.dwm.tardis.interior.TardisEntryGate;
import com.adamkali.dwm.tardis.interior.TardisInteriorService;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TardisBlock extends BaseEntityBlock {
    private static final MapCodec<TardisBlock> CODEC = simpleCodec(TardisBlock::new);
    public static final IntegerProperty FACING_ROTATION = BlockStateProperties.ROTATION_16;

    public TardisBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING_ROTATION, 0));
    }

    @Override
    protected MapCodec<TardisBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING_ROTATION, RotationSegment.convertToSegment(ctx.getRotation()));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING_ROTATION, rotation.rotate(state.getValue(FACING_ROTATION), RotationSegment.getMaxSegmentIndex()));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING_ROTATION, mirror.mirror(state.getValue(FACING_ROTATION), RotationSegment.getMaxSegmentIndex()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING_ROTATION);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TardisBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof TardisBlockEntity tardisBlockEntity)) {
            return InteractionResult.PASS;
        }
        if (!player.isShiftKeyDown()) {
            // Door state lives in a shared TardisDataLoader cache; toggling on both sides in
            // integrated singleplayer immediately undoes the client open. Server is authoritative.
            if (!world.isClientSide()) {
                tardisBlockEntity.toggleDoor();
                tardisBlockEntity.setChanged();
            }
        } else if (!world.isClientSide() && DWMConfig.getBoolean(DWMConfig.ENABLE_CHAMELEON_GUI)) {
            ServerPlayNetworking.send((ServerPlayer) player, new OpenTardisChameleonScreen(tardisBlockEntity.getTardisId()));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (world.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }
        if (!(world instanceof ServerLevel serverWorld)) {
            return;
        }
        if (!(world.getBlockEntity(pos) instanceof TardisBlockEntity tardisBlockEntity)) {
            return;
        }
        TardisInteriorService.tryEnterFromExterior(player, serverWorld, tardisBlockEntity);
    }

    /**
     * When the door is open enough for entry, disable collision so players can walk into the
     * block volume and trigger {@link #entityInside}. Outline stays full for interaction.
     */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (world.getBlockEntity(pos) instanceof TardisBlockEntity tardisBlockEntity) {
            UUID tardisId = tardisBlockEntity.getTardisIdOrNull();
            if (tardisId != null && TardisEntryGate.canEnter(TardisLogic.getDoorState(tardisId))) {
                return Shapes.empty();
            }
        }
        return Shapes.block();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, DWMBlockEntities.TARDIS_BLOCK_ENTITY, (world1, pos, state1, blockEntity) -> {
            blockEntity.tick(world1, pos, state1, blockEntity);
        });
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
