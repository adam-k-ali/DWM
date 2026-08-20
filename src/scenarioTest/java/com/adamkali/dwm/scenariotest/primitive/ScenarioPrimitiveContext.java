package com.adamkali.dwm.scenariotest.primitive;

import com.adamkali.dwm.scenariotest.ScenarioPlan;
import com.adamkali.dwm.scenariotest.ScreenshotCapture;
import com.adamkali.dwm.scenariotest.VanillaServerProcess;
import com.adamkali.dwm.scenariotest.WidgetFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;

import java.util.Map;

public record ScenarioPrimitiveContext(
        Minecraft client,
        ScenarioPlan.Step step,
        WidgetFinder widgetFinder,
        ScreenshotCapture screenshotCapture,
        VanillaServerProcess vanillaServer,
        Logger logger
) {
    public Screen screen() {
        return client.gui.screen();
    }

    public Map<String, Object> arguments() {
        return step.arguments();
    }

    public String source() {
        return step.source();
    }
}
