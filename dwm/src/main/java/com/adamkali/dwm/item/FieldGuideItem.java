package com.adamkali.dwm.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * Client-only guidebook. Use in air to open the Field Guide screen.
 */
public class FieldGuideItem extends Item {
    /**
     * Client registers this to open the Field Guide without pulling client classes into common.
     */
    public static @Nullable Consumer<Player> openGuide = null;

    public FieldGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(
            @NonNull Level level,
            @NonNull Player player,
            @NonNull InteractionHand hand
    ) {
        if (level.isClientSide()) {
            Consumer<Player> opener = openGuide;
            if (opener != null) {
                opener.accept(player);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(
            @NonNull ItemStack stack,
            @NonNull TooltipContext context,
            @NonNull TooltipDisplay display,
            @NonNull Consumer<Component> tooltip,
            @NonNull TooltipFlag flag
    ) {
        tooltip.accept(Component.translatable("dwm.guide.item.tooltip"));
    }
}
