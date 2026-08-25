package com.adamkali.screenplay.primitive;

import com.adamkali.screenplay.ScenarioException;
import com.adamkali.screenplay.ScreenshotCapture;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class CaptureScreenshotPrimitive implements ScenarioPrimitive {
    private static final Set<String> KEYS = Set.of("name", "compare", "maxDiffPixels");

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
            if (!KEYS.contains(key)) {
                throw new ScenarioException(source + ": captureScreenshot does not accept '" + key + "'");
            }
        }

        Map<String, Object> normalized = new LinkedHashMap<>();
        boolean compare = parseCompare(arguments.get("compare"), source);
        if (arguments.containsKey("name")) {
            Object value = arguments.get("name");
            if (!(value instanceof String string) || string.isBlank()) {
                throw new ScenarioException(source + ": captureScreenshot requires a non-empty string 'name'");
            }
            try {
                normalized.put("name", ScreenshotCapture.normalizeFileName(string));
            } catch (ScenarioException exception) {
                throw new ScenarioException(source + ": " + exception.getMessage());
            }
        } else if (compare) {
            throw new ScenarioException(source + ": captureScreenshot compare requires a non-empty string 'name'");
        }

        if (arguments.containsKey("compare") || compare) {
            normalized.put("compare", compare);
        }
        if (compare || arguments.containsKey("maxDiffPixels")) {
            normalized.put("maxDiffPixels", parseMaxDiffPixels(arguments.get("maxDiffPixels"), source));
        }
        return normalized;
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        Map<String, Object> arguments = context.arguments();
        String filename = (String) arguments.get("name");
        boolean compare = Boolean.TRUE.equals(arguments.get("compare"));
        long maxDiffPixels = arguments.containsKey("maxDiffPixels")
                ? ((Number) arguments.get("maxDiffPixels")).longValue()
                : 0L;
        return context.screenshotCapture().tick(context.client(), filename, compare, maxDiffPixels);
    }

    private static boolean parseCompare(Object value, String source) {
        if (value == null) {
            return false;
        }
        if (!(value instanceof Boolean compare)) {
            throw new ScenarioException(source + ": captureScreenshot compare must be a boolean");
        }
        return compare;
    }

    private static long parseMaxDiffPixels(Object value, String source) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Integer integer) {
            if (integer < 0) {
                throw new ScenarioException(source + ": captureScreenshot maxDiffPixels must be >= 0");
            }
            return integer.longValue();
        }
        if (value instanceof Long longValue) {
            if (longValue < 0) {
                throw new ScenarioException(source + ": captureScreenshot maxDiffPixels must be >= 0");
            }
            return longValue;
        }
        throw new ScenarioException(source + ": captureScreenshot maxDiffPixels must be an integer >= 0");
    }
}
