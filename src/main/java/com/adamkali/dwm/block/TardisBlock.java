package com.adamkali.dwm.block;

import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.network.OpenTardisChameleonScreen;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TardisBlock extends BlockWithEntity {
    private static final MapCodec<TardisBlock> CODEC = createCodec(TardisBlock::new);
    public static final IntProperty FACING_ROTATION = Properties.ROTATION;
    private static final Logger LOGGER = LogUtils.getLogger();


    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 32.0, 16.0);

    public TardisBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING_ROTATION, 0));
    }

    @Override
    protected MapCodec<TardisBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (!(world.getBlockEntity(pos) instanceof TardisBlockEntity tardisBlockEntity)) {
            return SHAPE;
        }

        if (tardisBlockEntity.isDoorOpen()) {
            return VoxelShapes.empty();
        }

        return SHAPE;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        LOGGER.info("Collision with entity: {} at pos: {}", entity.getType().toString(), pos);

        if (world.isClient) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        if (!(world.getBlockEntity(pos) instanceof TardisBlockEntity tardisBlockEntity)) {
            return;
        }

        LOGGER.info("Player identified...");

        if (tardisBlockEntity.isDoorOpen()) {
            LOGGER.info("Door is open, teleporting to interior...");
            tardisBlockEntity.teleportToInterior(player);
        }
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING_ROTATION, RotationPropertyHelper.fromYaw(ctx.getPlayerYaw()));
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING_ROTATION, rotation.rotate(state.get(FACING_ROTATION), RotationPropertyHelper.getMax()));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.with(FACING_ROTATION, mirror.mirror(state.get(FACING_ROTATION), RotationPropertyHelper.getMax()));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING_ROTATION);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TardisBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof TardisBlockEntity tardisBlockEntity)) {
            LOGGER.warn("Tried to interact with a TARDIS block entity at {} but it was missing!", pos);
            return ActionResult.PASS;
        }

        // Client: do not mutate anything. Let server be authoritative.
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (!player.isSneaking()) {
            tardisBlockEntity.toggleDoor();
            tardisBlockEntity.markDirty();
        } else if (!world.isClient && DWMConfig.getBoolean(DWMConfig.ENABLE_CHAMELEON_GUI)) {
            ServerPlayNetworking.send((ServerPlayerEntity) player, new OpenTardisChameleonScreen(tardisBlockEntity.getTardisId()));
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, DWMBlockEntities.TARDIS_BLOCK_ENTITY, (world1, pos, state1, blockEntity) -> {
            blockEntity.tick(world1, pos, state1, blockEntity);
        });
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }
}
