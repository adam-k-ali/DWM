package com.adamkali.sightline.primitive;

import com.adamkali.sightline.CreateWorldProcess;
import com.adamkali.sightline.ScenarioPlan;
import com.adamkali.sightline.ScreenshotCapture;
import com.adamkali.sightline.VanillaServerProcess;
import com.adamkali.sightline.WidgetFinder;
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
