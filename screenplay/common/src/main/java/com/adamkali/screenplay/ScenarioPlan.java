package com.adamkali.screenplay;

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
                detail = arguments.get("command");
            }
            if (detail == null) {
                detail = arguments.get("slot");
            }
            if (detail == null) {
                detail = arguments.get("ticks");
            }
            if (arguments.containsKey("holding")) {
                return name + " holding \"" + arguments.get("holding") + "\"";
            }
            if (arguments.containsKey("notHolding")) {
                return name + " notHolding \"" + arguments.get("notHolding") + "\"";
            }
            if (arguments.containsKey("overlay")) {
                return name + " overlay \"" + arguments.get("overlay") + "\"";
            }
            if (arguments.get("toast") instanceof Map<?, ?> toast) {
                Object toastDetail = toast.get("contains");
                if (toastDetail == null) {
                    toastDetail = toast.get("id");
                }
                return name + " toast " + toast.get("type") + " \"" + toastDetail + "\"";
            }
            if (arguments.containsKey("dimension")) {
                return name + " dimension \"" + arguments.get("dimension") + "\"";
            }
            if (arguments.get("block") instanceof Map<?, ?> block && block.get("id") != null) {
                return name + " block \"" + block.get("id") + "\"";
            }
            if (arguments.containsKey("x")) {
                return name + " \"" + arguments.get("x") + " " + arguments.get("y") + " "
                        + arguments.get("z") + "\"";
            }
            if (arguments.containsKey("yaw")) {
                return name + " \"" + arguments.get("yaw") + " " + arguments.get("pitch") + "\"";
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
