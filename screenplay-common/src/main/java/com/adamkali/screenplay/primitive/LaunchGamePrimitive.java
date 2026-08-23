package com.adamkali.screenplay.primitive;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;

import java.util.Map;

public final class LaunchGamePrimitive extends NoArgPrimitive {
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
        // Skip accessibility onboarding when it still appears despite options.txt.
        if (context.screen() instanceof AccessibilityOnboardingScreen) {
            client.gui.setScreen(new TitleScreen());
            return false;
        }
        // Title screen is enough; LoadingOverlay may still be fading out on top.
        return context.screen() instanceof TitleScreen;
    }
}
