package com.adamkali.dwm.item;

import com.adamkali.dwm.tardis.logic.CircuitFittedLogic;
import com.adamkali.dwm.tardis.logic.TardisSummonLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class StattenheimRemoteItem extends Item {
    public StattenheimRemoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }

        TardisSummonLogic.Result result = TardisSummonLogic.summon(
                player,
                serverLevel,
                context.getClickedPos(),
                context.getClickedFace()
        );
        if (result == TardisSummonLogic.Result.CIRCUIT_BROKEN) {
            CircuitFittedLogic.refuseBrokenAtBlock(player, serverLevel, context.getClickedPos());
            return InteractionResult.CONSUME;
        }
        player.sendOverlayMessage(Component.translatable(TardisSummonLogic.overlayKey(result)));
        return InteractionResult.SUCCESS;
    }
}
