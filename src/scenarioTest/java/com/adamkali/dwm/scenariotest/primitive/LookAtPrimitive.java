package com.adamkali.dwm.scenariotest.primitive;

import com.adamkali.dwm.scenariotest.ScenarioCoordinates;
import com.adamkali.dwm.scenariotest.ScenarioException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LookAtPrimitive implements ScenarioPrimitive {
    private static final Set<String> ROTATION_KEYS = Set.of("yaw", "pitch");
    private static final Set<String> POSITION_KEYS = Set.of("x", "y", "z");
    private static final Set<String> KEYS = Set.of("yaw", "pitch", "x", "y", "z");

    @Override
    public String name() {
        return "lookAt";
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
        LocalPlayer player = client.player;
        if (player == null) {
            return false;
        }
        Map<String, Object> arguments = context.arguments();
        if (arguments.containsKey("yaw")) {
            float yaw = (Float) arguments.get("yaw");
            float pitch = (Float) arguments.get("pitch");
            context.logger().info("Looking yaw {} pitch {}", yaw, pitch);
            player.setYRot(yaw);
            player.setXRot(pitch);
            return true;
        }
        BlockPos origin = player.blockPosition();
        BlockPos target = new BlockPos(
                ScenarioCoordinates.parse(arguments.get("x"), "lookAt x").resolve(origin.getX()),
                ScenarioCoordinates.parse(arguments.get("y"), "lookAt y").resolve(origin.getY()),
                ScenarioCoordinates.parse(arguments.get("z"), "lookAt z").resolve(origin.getZ())
        );
        context.logger().info("Looking at {} from {}", target, origin);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(target));
        return true;
    }

    public static Map<String, Object> normalize(Map<String, Object> arguments) {
        for (String key : arguments.keySet()) {
            if (!KEYS.contains(key)) {
                throw new ScenarioException("lookAt does not accept '" + key + "'");
            }
        }
        boolean hasRotation = arguments.keySet().stream().anyMatch(ROTATION_KEYS::contains);
        boolean hasPosition = arguments.keySet().stream().anyMatch(POSITION_KEYS::contains);
        if (hasRotation == hasPosition) {
            throw new ScenarioException("lookAt requires yaw and pitch, or x, y, and z");
        }
        if (hasRotation) {
            return normalizeRotation(arguments);
        }
        return normalizePosition(arguments);
    }

    private static Map<String, Object> normalizeRotation(Map<String, Object> arguments) {
        if (!arguments.containsKey("yaw") || !arguments.containsKey("pitch")) {
            throw new ScenarioException("lookAt requires yaw and pitch, or x, y, and z");
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("yaw", parseDegrees(arguments.get("yaw"), "yaw"));
        normalized.put("pitch", parsePitch(arguments.get("pitch")));
        return normalized;
    }

    private static Map<String, Object> normalizePosition(Map<String, Object> arguments) {
        if (!arguments.containsKey("x") || !arguments.containsKey("y") || !arguments.containsKey("z")) {
            throw new ScenarioException("lookAt requires yaw and pitch, or x, y, and z");
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (String axis : List.of("x", "y", "z")) {
            ScenarioCoordinates.Component component = ScenarioCoordinates.parse(arguments.get(axis), "lookAt " + axis);
            normalized.put(axis, component.authored());
        }
        return normalized;
    }

    static float parseDegrees(Object value, String field) {
        if (value instanceof Number number) {
            return number.floatValue();
        }
        if (value instanceof String string && !string.isBlank()) {
            try {
                return Float.parseFloat(string.trim());
            } catch (NumberFormatException exception) {
                throw new ScenarioException("lookAt " + field + " must be a number");
            }
        }
        throw new ScenarioException("lookAt " + field + " must be a number");
    }

    static float parsePitch(Object value) {
        float pitch = parseDegrees(value, "pitch");
        if (pitch < -90.0F || pitch > 90.0F) {
            throw new ScenarioException("lookAt pitch must be between -90 and 90");
        }
        return pitch;
    }
}
