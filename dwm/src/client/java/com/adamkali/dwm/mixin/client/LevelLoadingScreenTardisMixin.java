package com.adamkali.dwm.mixin.client;

import com.adamkali.dwm.render.TardisTransitionFade;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces the "Downloading terrain" screen with plain black during TARDIS enter/exit, then fades in. */
@Mixin(LevelLoadingScreen.class)
public class LevelLoadingScreenTardisMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void dwm$blackScreen(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (TardisTransitionFade.isActive()) {
            graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0xFF000000);
            ci.cancel();
        }
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void dwm$startFade(CallbackInfo ci) {
        TardisTransitionFade.loadingScreenClosed();
    }
}
