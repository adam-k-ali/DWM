package com.adamkali.dwm.scenariotest.primitive;

import com.adamkali.dwm.scenariotest.ScenarioException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class SelectorPrimitive implements ScenarioPrimitive {
    private static final List<String> SELECTOR_TYPE_LIST = List.of(
            "button", "cycle", "tab", "editbox", "label", "screen");
    private static final Set<String> SELECTOR_TYPES = Set.copyOf(SELECTOR_TYPE_LIST);
    private static final Set<String> SELECTOR_FIELDS = Set.of("type", "name");

    protected Map<String, Object> requireSelector(Map<String, Object> arguments, String source) {
        return validateSelector(arguments, source, name());
    }

    static Map<String, Object> validateSelector(Map<String, Object> arguments, String source, String step) {
        for (String key : arguments.keySet()) {
            if (!SELECTOR_FIELDS.contains(key)) {
                throw new ScenarioException(source + ": step '" + step + "' has unknown selector field '" + key + "'");
            }
        }
        requireSelectorString(arguments, "type", step, source);
        requireSelectorString(arguments, "name", step, source);
        if (!SELECTOR_TYPES.contains(arguments.get("type"))) {
            throw new ScenarioException(source + ": unsupported element type '" + arguments.get("type")
                    + "'; supported types: " + SELECTOR_TYPE_LIST);
        }
        return arguments;
    }

    private static void requireSelectorString(
            Map<String, Object> arguments,
            String key,
            String step,
            String source
    ) {
        Object value = arguments.get(key);
        if (!(value instanceof String string) || string.isBlank()) {
            throw new ScenarioException(source + ": step '" + step + "' requires a non-empty string '" + key + "'");
        }
    }
}
