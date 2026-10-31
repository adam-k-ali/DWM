package com.adamkali.dwm.block;

import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.block.entities.TardisMonitorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * TARDIS monitor (compact / full scanner): a decor mesh whose screen shows live ship status for the
 * TARDIS interior it sits in. Empty-hand use cycles the page (sneak to go back); anyone may read it.
 */
public class TardisMonitorBlock extends TardisDecorEntityBlock {
    private final MapCodec<TardisMonitorBlock> codec;

    public TardisMonitorBlock(Properties settings, VoxelShape northShape) {
        super(settings, northShape);
        this.codec = simpleCodec(props -> new TardisMonitorBlock(props, northShape));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TardisMonitorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide()) {
            return null;
        }
        return createTickerHelper(
                type,
                DWMBlockEntities.TARDIS_DECOR_BLOCK_ENTITY,
                (tickWorld, pos, tickState, entity) -> {
                    if (entity instanceof TardisMonitorBlockEntity monitor) {
                        TardisMonitorBlockEntity.serverTick(tickWorld, pos, tickState, monitor);
                    }
                }
        );
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (world instanceof ServerLevel serverWorld
                && world.getBlockEntity(pos) instanceof TardisMonitorBlockEntity monitor) {
            monitor.linkToInteriorPlot(serverWorld);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(world.getBlockEntity(pos) instanceof TardisMonitorBlockEntity monitor)) {
            return InteractionResult.PASS;
        }
        monitor.cyclePage(player.isSecondaryUseActive());
        world.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3F, 1.6F);
        return InteractionResult.SUCCESS;
    }
}
