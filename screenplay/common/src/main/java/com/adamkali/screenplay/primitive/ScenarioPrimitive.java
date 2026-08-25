package com.adamkali.screenplay.primitive;

import java.time.Duration;
import java.util.Map;

public interface ScenarioPrimitive {
    String name();

    Map<String, Object> validate(Map<String, Object> arguments, String source);

    boolean execute(ScenarioPrimitiveContext context);

    default Duration timeout(Duration stepTimeout) {
        return stepTimeout;
    }
}
