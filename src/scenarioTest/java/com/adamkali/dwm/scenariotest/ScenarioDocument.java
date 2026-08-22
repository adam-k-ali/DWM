package com.adamkali.dwm.scenariotest;

import java.util.List;
import java.util.Map;

public record ScenarioDocument(
        String id,
        String name,
        Type type,
        List<Parameter> parameters,
        List<Invocation> steps,
        String source
) {
    public enum Type {
        TEST,
        COMMAND
    }

    public record Parameter(String name, String type) {
    }

    public record Invocation(String name, Map<String, Object> arguments) {
        public Invocation {
            arguments = Map.copyOf(arguments);
        }
    }
}
