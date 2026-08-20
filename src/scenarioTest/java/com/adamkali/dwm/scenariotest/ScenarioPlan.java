package com.adamkali.dwm.scenariotest;

import java.util.List;
import java.util.Map;

public record ScenarioPlan(String id, String name, List<Step> steps) {
    public ScenarioPlan {
        steps = List.copyOf(steps);
    }

    public record Step(String name, Map<String, Object> arguments, String source) {
        public Step {
            arguments = Map.copyOf(arguments);
        }

        public String displayName() {
            Object detail = arguments.get("name");
            if (detail == null) {
                detail = arguments.get("text");
            }
            if (detail == null) {
                for (String condition : List.of("visible", "notVisible")) {
                    Object nested = arguments.get(condition);
                    if (nested instanceof Map<?, ?> selector) {
                        Object nestedName = selector.get("name");
                        if (nestedName != null) {
                            return name + " " + condition + " \"" + nestedName + "\"";
                        }
                    }
                }
            }
            return detail == null ? name : name + " \"" + detail + "\"";
        }
    }
}
