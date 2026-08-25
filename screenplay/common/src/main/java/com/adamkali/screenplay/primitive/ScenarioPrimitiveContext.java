package com.adamkali.screenplay.primitive;

import com.adamkali.screenplay.CreateWorldProcess;
import com.adamkali.screenplay.ScenarioPlan;
import com.adamkali.screenplay.ScreenshotCapture;
import com.adamkali.screenplay.VanillaServerProcess;
import com.adamkali.screenplay.WidgetFinder;
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
        CreateWorldProcess createWorld,
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
