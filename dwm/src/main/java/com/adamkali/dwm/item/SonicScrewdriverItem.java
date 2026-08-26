package com.adamkali.dwm.item;

import com.adamkali.dwm.actions.SonicActions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class SonicScrewdriverItem extends Item {
    /**
     * Client registers this to open the field-mode HUD carousel without pulling client classes into common.
     */
    public static @Nullable Consumer<ItemStack> openFieldModeSelector = null;

    public SonicScrewdriverItem(Item.Properties itemProperties) {
        super(itemProperties);
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        SonicActions.getInstance().interactWithBlock(context);
        // Consume so sneak-use-on-block does not fall through to air-use (field-mode HUD).
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public @NonNull InteractionResult interactLivingEntity(
            @NonNull ItemStack stack,
            @NonNull Player user,
            @NonNull LivingEntity entity,
            @NonNull InteractionHand hand
    ) {
        SonicActions.getInstance().interactWithEntity(stack, entity, user, hand);
        if (user.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public @NonNull InteractionResult use(
            @NonNull Level level,
            @NonNull Player player,
            @NonNull InteractionHand hand
    ) {
        if (!player.isShiftKeyDown()) {
            // Un-sneak use-in-air reserved for DWM-061 ping.
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            Consumer<ItemStack> opener = openFieldModeSelector;
            if (opener != null) {
                opener.accept(player.getItemInHand(hand));
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
        SonicState state = SonicStateLogic.effective(stack);
        SonicFieldMode selected = state.selected();
        for (SonicFieldMode mode : SonicFieldMode.cycleOrder()) {
            if (!state.isUnlocked(mode)) {
                continue;
            }
            Component name = Component.translatable(mode.translationKey());
            if (mode == selected) {
                tooltip.accept(Component.translatable("dwm.sonic.tooltip.selected", name));
            } else {
                tooltip.accept(Component.translatable("dwm.sonic.tooltip.unlocked", name));
            }
        }
    }
}
