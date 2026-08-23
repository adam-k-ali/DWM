package com.adamkali.sightline.primitive;

import java.util.Map;

public final class DebugScreenPrimitive extends NoArgPrimitive {
    @Override
    public String name() {
        return "debugScreen";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        return requireNoArguments(arguments, source);
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        context.logger().info("{}", context.widgetFinder().describeVisibleWidgets(context.screen()));
        return true;
    }
}
