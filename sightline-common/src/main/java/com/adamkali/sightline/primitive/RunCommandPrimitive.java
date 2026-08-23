package com.adamkali.sightline.primitive;

import com.adamkali.sightline.ScenarioException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class RunCommandPrimitive implements ScenarioPrimitive {
    public static final int MAX_COMMAND_LENGTH = 256;
    private static final Set<String> KEYS = Set.of("command", "text");

    @Override
    public String name() {
        return "runCommand";
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
        ClientPacketListener connection = client.getConnection();
        if (client.player == null || connection == null) {
            return false;
        }
        String authored = (String) context.arguments().get("command");
        String payload = normalizeCommand(authored);
        context.logger().info("Running command \"{}\"", authored);
        connection.sendCommand(payload);
        return true;
    }

    public static Map<String, Object> normalize(Map<String, Object> arguments) {
        for (String key : arguments.keySet()) {
            if (!KEYS.contains(key)) {
                throw new ScenarioException("runCommand does not accept '" + key + "'");
            }
        }
        boolean hasCommand = arguments.containsKey("command");
        boolean hasText = arguments.containsKey("text");
        if (hasCommand == hasText) {
            throw new ScenarioException("runCommand requires a non-empty string 'command'");
        }
        Object value = hasCommand ? arguments.get("command") : arguments.get("text");
        normalizeCommand(value);
        String authored = ((String) value).trim();
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("command", authored);
        return normalized;
    }

    public static String normalizeCommand(Object value) {
        if (!(value instanceof String string) || string.isBlank()) {
            throw new ScenarioException("runCommand requires a non-empty string 'command'");
        }
        String trimmed = string.trim();
        String stripped = trimmed.startsWith("/") ? trimmed.substring(1).trim() : trimmed;
        if (stripped.isBlank()) {
            throw new ScenarioException("runCommand requires a non-empty string 'command'");
        }
        if (stripped.length() > MAX_COMMAND_LENGTH) {
            throw new ScenarioException("runCommand must be at most " + MAX_COMMAND_LENGTH + " characters");
        }
        return stripped;
    }
}
