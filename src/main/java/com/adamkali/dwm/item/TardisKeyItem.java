package com.adamkali.dwm.item;

import com.adamkali.dwm.block.TardisBlock;
import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.DoorLockLogic;
import com.adamkali.dwm.tardis.logic.TardisKeyLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class TardisKeyItem extends Item {
    public TardisKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof TardisBlock)
                && !(state.getBlock() instanceof TardisInteriorDoorBlock)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        if (!(level instanceof ServerLevel serverLevel) || player == null) {
            return InteractionResult.CONSUME;
        }

        UUID tardisId = resolveTardisId(level, pos, state);
        TardisDataModel model = tardisId == null ? null : TardisDataLoader.get(tardisId);
        TardisKeyLogic.UseResult result = TardisKeyLogic.useOnTardis(
                context.getItemInHand().get(DWMDataComponents.BOUND_TARDIS_ID),
                player.getUUID(),
                tardisId,
                model
        );

        switch (result) {
            case BOUND -> {
                context.getItemInHand().set(DWMDataComponents.BOUND_TARDIS_ID, tardisId);
                player.sendOverlayMessage(Component.translatable("dwm.key.bound"));
            }
            case TOGGLE_READY -> DoorLockLogic.toggleForPlayer(
                    model,
                    player,
                    serverLevel.getServer(),
                    tardisId
            );
            case NOT_OWNER -> player.sendOverlayMessage(Component.translatable("dwm.key.bind_not_owner"));
            case WRONG_TARDIS -> player.sendOverlayMessage(Component.translatable("dwm.key.wrong_tardis"));
            case UNAVAILABLE -> player.sendOverlayMessage(Component.translatable("dwm.console.door_lock_unavailable"));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
            @NonNull ItemStack stack,
            @NonNull TooltipContext context,
            @NonNull TooltipDisplay display,
            @NonNull Consumer<Component> tooltip,
            @NonNull TooltipFlag flag
    ) {
        tooltip.accept(Component.translatable(
                stack.has(DWMDataComponents.BOUND_TARDIS_ID)
                        ? "dwm.key.tooltip.bound"
                        : "dwm.key.tooltip.unbound"
        ));
    }

    private static @Nullable UUID resolveTardisId(
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull BlockState state
    ) {
        if (level.getBlockEntity(pos) instanceof TardisBlockEntity exterior) {
            return exterior.getTardisId();
        }
        TardisInteriorDoorBlockEntity interior = TardisInteriorDoorBlock.getOriginEntity(level, pos, state);
        return interior == null ? null : interior.getTardisId();
    }
}
