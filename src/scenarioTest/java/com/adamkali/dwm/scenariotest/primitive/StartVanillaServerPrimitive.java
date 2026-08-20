package com.adamkali.dwm.scenariotest.primitive;

import com.adamkali.dwm.scenariotest.ScenarioException;
import com.adamkali.dwm.scenariotest.VanillaServerProcess;

import java.time.Duration;
import java.util.Map;

public final class StartVanillaServerPrimitive implements ScenarioPrimitive {
    private static final Duration TIMEOUT_FLOOR = Duration.ofSeconds(120);

    @Override
    public String name() {
        return "startVanillaServer";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        if (arguments.isEmpty()) {
            return arguments;
        }
        for (String key : arguments.keySet()) {
            if (!"port".equals(key)) {
                throw new ScenarioException(source + ": startVanillaServer does not accept '" + key + "'");
            }
        }
        try {
            arguments.put("port", VanillaServerProcess.parsePort(arguments.get("port")));
        } catch (ScenarioException exception) {
            throw new ScenarioException(source + ": " + exception.getMessage());
        }
        return arguments;
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        return context.vanillaServer().tick(VanillaServerProcess.parsePort(context.arguments().get("port")));
    }

    @Override
    public Duration timeout(Duration stepTimeout) {
        return stepTimeout.compareTo(TIMEOUT_FLOOR) >= 0 ? stepTimeout : TIMEOUT_FLOOR;
    }
}
