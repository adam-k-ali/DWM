package com.adamkali.dwm.scenariotest.primitive;

import net.minecraft.client.Minecraft;

import java.util.Map;

public final class CloseScreenPrimitive extends NoArgPrimitive {
    @Override
    public String name() {
        return "closeScreen";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        return requireNoArguments(arguments, source);
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        Minecraft client = context.client();
        if (client.player == null) {
            return false;
        }
        if (context.screen() == null) {
            return true;
        }
        context.logger().info("Closing {}", context.screen().getClass().getName());
        client.player.closeContainer();
        client.gui.setScreen(null);
        return context.screen() == null;
    }
}
