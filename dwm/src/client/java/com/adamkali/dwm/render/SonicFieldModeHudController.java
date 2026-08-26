package com.adamkali.dwm.render;

import com.adamkali.dwm.item.DWMItemTags;
import com.adamkali.dwm.item.SonicFieldMode;
import com.adamkali.dwm.item.SonicStateLogic;
import com.adamkali.dwm.network.SelectSonicFieldModeC2SPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Client state for the sonic field-mode HUD carousel (sneak-use in air).
 */
public final class SonicFieldModeHudController {
    private static final SonicFieldModeHudController INSTANCE = new SonicFieldModeHudController();

    private boolean active;
    private SonicFieldMode previewMode;
    private boolean escWasDown;
    private boolean leftWasDown;
    private boolean rightWasDown;
    private float motionFrom;
    private float motionTarget;
    private long motionStartedAtMs;

    private SonicFieldModeHudController() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(INSTANCE::tick);
        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> INSTANCE.active);
    }

    public static void open(ItemStack sonic) {
        INSTANCE.active = true;
        INSTANCE.previewMode = SonicStateLogic.selected(sonic);
        INSTANCE.escWasDown = false;
        INSTANCE.leftWasDown = false;
        INSTANCE.rightWasDown = false;
        INSTANCE.motionFrom = 0.0f;
        INSTANCE.motionTarget = 0.0f;
        INSTANCE.motionStartedAtMs = Util.getMillis();
    }

    public static boolean isActive() {
        return INSTANCE.active;
    }

    public static SonicFieldMode previewMode() {
        return INSTANCE.previewMode;
    }

    public static float visualScroll(long nowMs) {
        return SonicCarouselMotion.value(
                INSTANCE.motionFrom,
                INSTANCE.motionTarget,
                INSTANCE.motionStartedAtMs,
                nowMs
        );
    }

    public static float targetScroll() {
        return INSTANCE.motionTarget;
    }

    public static int selectionPhase(long nowMs) {
        return SonicCarouselMotion.selectionPhase(INSTANCE.motionStartedAtMs, nowMs);
    }

    public static float selectedScaleBoost(long nowMs) {
        return SonicCarouselMotion.selectedScaleBoost(INSTANCE.motionStartedAtMs, nowMs);
    }

    public static ItemStack heldSonic(Player player) {
        return findHeldSonic(player);
    }

    public static boolean isUnlocked(Player player, SonicFieldMode mode) {
        ItemStack sonic = findHeldSonic(player);
        return !sonic.isEmpty() && SonicStateLogic.isUnlocked(sonic, mode);
    }

    public static SonicFieldMode selectedMode(Player player) {
        ItemStack sonic = findHeldSonic(player);
        return sonic.isEmpty() ? SonicFieldMode.OPEN : SonicStateLogic.selected(sonic);
    }

    public void close() {
        active = false;
    }

    public static void navigatePreview(int direction) {
        INSTANCE.navigate(direction);
    }

    public void navigate(int direction) {
        if (!active || direction == 0) {
            return;
        }
        int step = direction < 0 ? -1 : 1;
        long nowMs = Util.getMillis();
        SonicCarouselMotion.Transition transition = SonicCarouselMotion.retarget(
                motionFrom,
                motionTarget,
                motionStartedAtMs,
                motionTarget + step,
                nowMs
        );
        motionFrom = transition.from();
        motionTarget = transition.target();
        motionStartedAtMs = transition.startedAtMs();
        previewMode = previewMode.step(step);
    }

    private void tick(Minecraft client) {
        if (!active) {
            return;
        }
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            finish(client, SonicCarouselMotion.CloseReason.FORCED);
            return;
        }
        if (findHeldSonic(player).isEmpty()) {
            finish(client, SonicCarouselMotion.CloseReason.FORCED);
            return;
        }
        if (!player.isShiftKeyDown()) {
            finish(client, SonicCarouselMotion.CloseReason.RELEASE);
            return;
        }

        var window = client.getWindow();
        boolean escDown = InputConstants.isKeyDown(window, InputConstants.KEY_ESCAPE);
        if (escDown && !escWasDown) {
            finish(client, SonicCarouselMotion.CloseReason.ESCAPE);
        }
        escWasDown = escDown;
        if (!active) {
            return;
        }

        boolean leftDown = InputConstants.isKeyDown(window, InputConstants.KEY_LEFT);
        if (leftDown && !leftWasDown) {
            navigate(-1);
        }
        leftWasDown = leftDown;

        boolean rightDown = InputConstants.isKeyDown(window, InputConstants.KEY_RIGHT);
        if (rightDown && !rightWasDown) {
            navigate(1);
        }
        rightWasDown = rightDown;
    }

    private void finish(Minecraft client, SonicCarouselMotion.CloseReason reason) {
        LocalPlayer player = client.player;
        if (player == null) {
            close();
            return;
        }
        ItemStack sonic = findHeldSonic(player);
        if (sonic.isEmpty()) {
            close();
            return;
        }

        SonicFieldMode selectedMode = SonicStateLogic.selected(sonic);
        SonicCarouselMotion.CloseDecision decision = SonicCarouselMotion.closeDecision(
                reason,
                SonicStateLogic.isUnlocked(sonic, previewMode),
                selectedMode != previewMode
        );
        if (decision == SonicCarouselMotion.CloseDecision.COMMIT) {
            ClientPlayNetworking.send(new SelectSonicFieldModeC2SPayload(previewMode));
        } else if (decision == SonicCarouselMotion.CloseDecision.REJECT_LOCKED) {
            player.sendOverlayMessage(Component.translatable(
                    SonicStateLogic.SETTING_NOT_INSTALLED_DETAIL_KEY,
                    Component.translatable(previewMode.translationKey())
            ));
        }
        close();
    }

    private static ItemStack findHeldSonic(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.is(DWMItemTags.SONIC_SCREWDRIVERS)) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.is(DWMItemTags.SONIC_SCREWDRIVERS)) {
            return off;
        }
        return ItemStack.EMPTY;
    }
}
