package com.adamkali.screenplay;

import java.util.List;
import java.util.Map;

public record ScenarioDocument(
        String id,
        String name,
        Type type,
        boolean record,
        List<Parameter> parameters,
        List<Invocation> steps,
        List<Invocation> beforeAll,
        List<Invocation> beforeEach,
        List<Invocation> afterEach,
        List<Invocation> afterAll,
        List<String> testIds,
        String source
) {
    public ScenarioDocument {
        parameters = List.copyOf(parameters);
        steps = List.copyOf(steps);
        beforeAll = List.copyOf(beforeAll);
        beforeEach = List.copyOf(beforeEach);
        afterEach = List.copyOf(afterEach);
        afterAll = List.copyOf(afterAll);
        testIds = List.copyOf(testIds);
    }

    public enum Type {
        TEST,
        COMMAND,
        SUITE
    }

    public record Parameter(String name, String type) {
    }

    public record Invocation(String name, Map<String, Object> arguments) {
        public Invocation {
            arguments = Map.copyOf(arguments);
        }
    }
}
