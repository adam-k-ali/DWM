package com.adamkali.sightline.primitive;

import com.adamkali.sightline.ScenarioException;
import com.adamkali.sightline.ScreenshotCapture;

import java.util.Map;

public final class CaptureScreenshotPrimitive implements ScenarioPrimitive {
    @Override
    public String name() {
        return "captureScreenshot";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        if (arguments.isEmpty()) {
            return arguments;
        }
        for (String key : arguments.keySet()) {
            if (!"name".equals(key)) {
                throw new ScenarioException(source + ": captureScreenshot does not accept '" + key + "'");
            }
        }
        Object value = arguments.get("name");
        if (!(value instanceof String string) || string.isBlank()) {
            throw new ScenarioException(source + ": captureScreenshot requires a non-empty string 'name'");
        }
        try {
            arguments.put("name", ScreenshotCapture.normalizeFileName(string));
        } catch (ScenarioException exception) {
            throw new ScenarioException(source + ": " + exception.getMessage());
        }
        return arguments;
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        return context.screenshotCapture().tick(context.client(), (String) context.arguments().get("name"));
    }
}
