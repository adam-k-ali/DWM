package com.adamkali.dwm.block;

import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TardisBlock extends BlockWithEntity {
    public static final IntProperty FACING_ROTATION = Properties.ROTATION;

    protected static final VoxelShape SHAPE = Block.createCuboidShape(-10.0, 0.0, -10.0, 26.0, 48.0, 26.0);

    public TardisBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING_ROTATION, 0));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(TardisBlock::new);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING_ROTATION, RotationPropertyHelper.fromYaw(ctx.getPlayerYaw()));
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
            return super.onUse(state, world, pos, player, hit);
        }

        tardisBlockEntity.toggleDoor();
        if (tardisBlockEntity.getDoorState().isOpen()) {
            player.sendMessage(Text.literal("The door is now open!"), true);
        } else {
            player.sendMessage(Text.literal("The door is now closed!"), true);
        }
        tardisBlockEntity.markDirty();

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
