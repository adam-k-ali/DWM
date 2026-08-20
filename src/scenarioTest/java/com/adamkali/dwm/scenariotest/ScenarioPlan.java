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
            return detail == null ? name : name + " \"" + detail + "\"";
        }
    }
}
