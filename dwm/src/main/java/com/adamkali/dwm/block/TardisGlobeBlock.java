package com.adamkali.dwm.block;

import com.adamkali.dwm.block.entities.TardisDecorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * TARDIS globe: decor BER prop that spins up on empty-hand use. The server sends a block event
 * (like the vanilla bell) so every nearby client starts the same {@code TardisGlobeSpin}.
 */
public class TardisGlobeBlock extends TardisDecorEntityBlock {
    public static final MapCodec<TardisGlobeBlock> CODEC = simpleCodec(TardisGlobeBlock::new);

    public TardisGlobeBlock(Properties settings) {
        super(settings, TardisDecorShapes.GLOBE);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!world.isClientSide()) {
            world.blockEvent(pos, this, TardisDecorBlockEntity.EVENT_SPIN, 0);
            world.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.3F);
            world.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
