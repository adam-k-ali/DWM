package com.adamkali.sightline.primitive;

import com.adamkali.sightline.CreateWorldProcess;
import com.adamkali.sightline.ScenarioException;

import java.time.Duration;
import java.util.Map;

public final class CreateWorldPrimitive implements ScenarioPrimitive {
    private static final Duration TIMEOUT_FLOOR = Duration.ofSeconds(120);

    @Override
    public String name() {
        return "createWorld";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        try {
            return CreateWorldProcess.normalize(arguments);
        } catch (ScenarioException exception) {
            throw new ScenarioException(source + ": " + exception.getMessage());
        }
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        return context.createWorld().tick(context.client(), context.arguments());
    }

    @Override
    public Duration timeout(Duration stepTimeout) {
        return stepTimeout.compareTo(TIMEOUT_FLOOR) >= 0 ? stepTimeout : TIMEOUT_FLOOR;
    }
}
