package com.adamkali.dwm.item;

import com.adamkali.dwm.advancement.DWMCriteria;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

/**
 * Craftable field-mode setting. Use while a sonic is in the other hand to install.
 */
public class SonicSettingItem extends Item {
    private final SonicFieldMode mode;

    public SonicSettingItem(SonicFieldMode mode, Properties properties) {
        super(properties);
        this.mode = mode;
    }

    public SonicFieldMode mode() {
        return mode;
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        ItemStack settingStack = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack sonicStack = player.getItemInHand(otherHand);
        if (!(sonicStack.getItem() instanceof SonicScrewdriverItem)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (SonicStateLogic.isUnlocked(sonicStack, mode)) {
            player.sendOverlayMessage(Component.translatable(
                    SonicStateLogic.SETTING_ALREADY_INSTALLED_KEY,
                    Component.translatable(mode.translationKey())
            ));
            return InteractionResult.CONSUME;
        }

        boolean installed = SonicStateLogic.install(sonicStack, mode);
        if (!installed) {
            return InteractionResult.CONSUME;
        }

        if (!player.getAbilities().instabuild) {
            settingStack.shrink(1);
        }

        player.sendOverlayMessage(Component.translatable(
                SonicStateLogic.SETTING_INSTALLED_KEY,
                Component.translatable(mode.translationKey())
        ));

        if (player instanceof ServerPlayer serverPlayer) {
            triggerInstall(serverPlayer);
        }
        return InteractionResult.CONSUME;
    }

    private void triggerInstall(ServerPlayer player) {
        switch (mode) {
            case SHATTER -> DWMCriteria.SONIC_INSTALL_SHATTER.trigger(player);
            case PRIME -> DWMCriteria.SONIC_INSTALL_PRIME.trigger(player);
            case DISRUPT -> DWMCriteria.SONIC_INSTALL_DISRUPT.trigger(player);
            case SHEAR -> DWMCriteria.SONIC_INSTALL_SHEAR.trigger(player);
            default -> {
            }
        }
    }
}
