package com.adamkali.dwm.mixin.client;

import com.adamkali.dwm.render.SonicFieldModeHudController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Scroll navigates the sonic field-mode HUD preview and swallows hotbar scroll while the HUD is open.
 */
@Mixin(MouseHandler.class)
public class SonicFieldModeScrollMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void dwm$navigateSonicFieldModeHud(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!SonicFieldModeHudController.isActive() || minecraft.player == null || minecraft.level == null) {
            return;
        }
        int direction = vertical > 0 ? -1 : (vertical < 0 ? 1 : 0);
        if (direction != 0) {
            SonicFieldModeHudController.navigatePreview(direction);
        }
        ci.cancel();
    }
}
