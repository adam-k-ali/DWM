package com.adamkali.dwm.scenariotest.primitive;

import com.adamkali.dwm.scenariotest.ScenarioException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WaitUntilPrimitive implements ScenarioPrimitive {
    private static final Set<String> CONDITIONS = Set.of("visible", "notVisible");

    @Override
    public String name() {
        return "waitUntil";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        for (String key : arguments.keySet()) {
            if (!CONDITIONS.contains(key)) {
                throw new ScenarioException(source + ": waitUntil does not accept '" + key + "'");
            }
        }
        boolean hasVisible = arguments.containsKey("visible");
        boolean hasNotVisible = arguments.containsKey("notVisible");
        if (hasVisible == hasNotVisible) {
            throw new ScenarioException(source + ": waitUntil requires exactly one of 'visible' or 'notVisible'");
        }
        String condition = hasVisible ? "visible" : "notVisible";
        Map<String, Object> selector = nestedSelector(arguments.get(condition), source, condition);
        Map<String, Object> validated = new LinkedHashMap<>();
        validated.put(condition, Map.copyOf(SelectorPrimitive.validateSelector(
                selector, source, name() + " " + condition)));
        return validated;
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        boolean expectVisible = context.arguments().containsKey("visible");
        @SuppressWarnings("unchecked")
        Map<String, Object> selector = (Map<String, Object>) context.arguments().get(
                expectVisible ? "visible" : "notVisible");
        boolean matches = context.widgetFinder().matches(context.screen(), selector);
        return expectVisible == matches;
    }

    private static Map<String, Object> nestedSelector(Object value, String source, String condition) {
        if (value instanceof List<?> list) {
            if (list.size() != 1) {
                throw new ScenarioException(source + ": step 'waitUntil' "
                        + condition + " expects one argument object, but received " + list.size());
            }
            value = list.getFirst();
        }
        if (!(value instanceof Map<?, ?> raw)) {
            throw new ScenarioException(source + ": waitUntil '" + condition + "' must be a selector object");
        }
        Map<String, Object> selector = new LinkedHashMap<>();
        raw.forEach((key, entryValue) -> {
            if (!(key instanceof String stringKey)) {
                throw new ScenarioException(source + ": waitUntil '" + condition + "' object keys must be strings");
            }
            selector.put(stringKey, entryValue);
        });
        return selector;
    }
}
