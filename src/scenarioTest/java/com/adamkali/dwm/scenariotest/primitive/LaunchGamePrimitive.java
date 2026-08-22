package com.adamkali.dwm.scenariotest.primitive;

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
        return context.screen() instanceof TitleScreen;
    }
}
