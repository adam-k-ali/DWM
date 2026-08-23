package com.adamkali.sightline.primitive;

import com.adamkali.sightline.ScenarioException;

import java.util.Map;

public abstract class NoArgPrimitive implements ScenarioPrimitive {
    protected Map<String, Object> requireNoArguments(Map<String, Object> arguments, String source) {
        if (!arguments.isEmpty()) {
            throw new ScenarioException(source + ": " + name() + " does not accept arguments");
        }
        return arguments;
    }
}
