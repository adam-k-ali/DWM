package com.adamkali.dwm.scenariotest.primitive;

import com.adamkali.dwm.scenariotest.ScenarioException;

import java.util.Map;
import java.util.Set;

public abstract class SelectorPrimitive implements ScenarioPrimitive {
    private static final Set<String> SELECTOR_TYPES = Set.of("button", "cycle", "tab", "editbox");
    private static final Set<String> SELECTOR_FIELDS = Set.of("type", "name");

    protected Map<String, Object> requireSelector(Map<String, Object> arguments, String source) {
        String step = name();
        for (String key : arguments.keySet()) {
            if (!SELECTOR_FIELDS.contains(key)) {
                throw new ScenarioException(source + ": step '" + step + "' has unknown selector field '" + key + "'");
            }
        }
        requireSelectorString(arguments, "type", step, source);
        requireSelectorString(arguments, "name", step, source);
        if (!SELECTOR_TYPES.contains(arguments.get("type"))) {
            throw new ScenarioException(source + ": unsupported element type '" + arguments.get("type")
                    + "'; supported types: [button, cycle, tab, editbox]");
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
