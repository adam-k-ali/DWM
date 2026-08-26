package com.adamkali.screenplay.primitive;

import com.adamkali.screenplay.ScenarioException;

import java.util.Map;
import java.util.Set;

public final class SetSneakingPrimitive implements ScenarioPrimitive {
    private static final Set<String> KEYS = Set.of("enabled");

    @Override
    public String name() {
        return "setSneaking";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        for (String key : arguments.keySet()) {
            if (!KEYS.contains(key)) {
                throw new ScenarioException(source + ": setSneaking does not accept '" + key + "'");
            }
        }
        Object enabled = arguments.get("enabled");
        if (!(enabled instanceof Boolean booleanValue) || arguments.size() != 1) {
            throw new ScenarioException(source + ": setSneaking requires a boolean 'enabled'");
        }
        return Map.of("enabled", booleanValue);
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        if (context.client().player == null) {
            return false;
        }
        boolean enabled = (Boolean) context.arguments().get("enabled");
        context.logger().info("Setting sneak key state to {}", enabled);
        context.client().options.keyShift.setDown(enabled);
        return true;
    }
}
