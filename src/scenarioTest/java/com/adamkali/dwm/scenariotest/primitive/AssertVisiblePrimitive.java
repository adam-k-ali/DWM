package com.adamkali.dwm.scenariotest.primitive;

import java.util.Map;

public final class AssertVisiblePrimitive extends SelectorPrimitive {
    @Override
    public String name() {
        return "assertVisible";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        return requireSelector(arguments, source);
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        return context.widgetFinder().find(context.screen(), context.arguments()).isPresent();
    }
}
