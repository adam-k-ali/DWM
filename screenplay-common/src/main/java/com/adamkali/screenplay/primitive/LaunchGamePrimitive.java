package com.adamkali.screenplay.primitive;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.server.packs.resources.ReloadInstance;

import java.lang.reflect.Field;
import java.util.Map;

public final class LaunchGamePrimitive extends NoArgPrimitive {
    private static Field loadingOverlayReloadField;
    private static boolean loadingOverlayReloadFieldResolved;

    @Override
    public String name() {
        return "launchGame";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        return requireNoArguments(arguments, source);
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        Minecraft client = context.client();
        if (client == null || client.gui == null) {
            return false;
        }

        // LoadingOverlay only advances fade/finish from Overlay.tick(), which is gated on
        // Minecraft client ticks. Under Forge/xvfb those ticks can stall while frames still
        // render — drive the overlay from Screenplay so the title screen can appear.
        Overlay overlay = client.gui.overlay();
        if (overlay != null) {
            overlay.tick();
        }

        if (context.screen() instanceof AccessibilityOnboardingScreen) {
            client.gui.setScreen(new TitleScreen());
            return false;
        }
        return context.screen() instanceof TitleScreen;
    }
}
