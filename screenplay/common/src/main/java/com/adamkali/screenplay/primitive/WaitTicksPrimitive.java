package com.adamkali.screenplay.primitive;

import com.adamkali.screenplay.ScenarioException;
import net.minecraft.client.Minecraft;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class WaitTicksPrimitive implements ScenarioPrimitive {
    public static final int MIN_TICKS = 1;
    private static final Set<String> KEYS = Set.of("ticks", "text");

    private Long deadlineGameTime;

    @Override
    public String name() {
        return "waitTicks";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        try {
            return normalize(arguments);
        } catch (ScenarioException exception) {
            throw new ScenarioException(source + ": " + exception.getMessage());
        }
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        Minecraft client = context.client();
        if (client.level == null) {
            return false;
        }
        int ticks = (Integer) context.arguments().get("ticks");
        long gameTime = client.level.getGameTime();
        if (deadlineGameTime == null) {
            deadlineGameTime = gameTime + ticks;
            context.logger().info("Waiting {} ticks until gameTime {}", ticks, deadlineGameTime);
            return false;
        }
        if (gameTime >= deadlineGameTime) {
            deadlineGameTime = null;
            return true;
        }
        return false;
    }

    public static Map<String, Object> normalize(Map<String, Object> arguments) {
        for (String key : arguments.keySet()) {
            if (!KEYS.contains(key)) {
                throw new ScenarioException("waitTicks does not accept '" + key + "'");
            }
        }
        boolean hasTicks = arguments.containsKey("ticks");
        boolean hasText = arguments.containsKey("text");
        if (hasTicks == hasText) {
            throw new ScenarioException("waitTicks requires a positive integer 'ticks'");
        }
        int ticks = parseTicks(hasTicks ? arguments.get("ticks") : arguments.get("text"));
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("ticks", ticks);
        return normalized;
    }

    public static int parseTicks(Object value) {
        int ticks;
        if (value instanceof Integer integer) {
            ticks = integer;
        } else if (value instanceof Long longValue) {
            if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
                throw invalidTicks();
            }
            ticks = longValue.intValue();
        } else if (value instanceof String string) {
            if (string.isBlank()) {
                throw invalidTicks();
            }
            try {
                ticks = Integer.parseInt(string.trim());
            } catch (NumberFormatException exception) {
                throw invalidTicks();
            }
        } else {
            throw invalidTicks();
        }
        if (ticks < MIN_TICKS) {
            throw invalidTicks();
        }
        return ticks;
    }

    private static ScenarioException invalidTicks() {
        return new ScenarioException("waitTicks requires a positive integer 'ticks'");
    }
}
