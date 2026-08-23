package com.adamkali.sightline.primitive;

import com.adamkali.sightline.ScenarioCoordinates;
import com.adamkali.sightline.ScenarioException;
import com.adamkali.sightline.ScenarioIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WalkUntilPrimitive implements ScenarioPrimitive {
    private static final Set<String> POSITION_KEYS = Set.of("x", "y", "z");
    private static final Set<String> KEYS = Set.of("x", "y", "z", "dimension");

    private boolean holdingForward;
    private BlockPos absoluteTarget;

    @Override
    public String name() {
        return "walkUntil";
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
        if (player == null || client.level == null || context.screen() != null) {
            return false;
        }

        Map<String, Object> arguments = context.arguments();
        if (!holdingForward) {
            if (!arguments.containsKey("dimension")) {
                BlockPos origin = player.blockPosition();
                absoluteTarget = new BlockPos(
                        ScenarioCoordinates.parse(arguments.get("x"), "walkUntil x").resolve(origin.getX()),
                        ScenarioCoordinates.parse(arguments.get("y"), "walkUntil y").resolve(origin.getY()),
                        ScenarioCoordinates.parse(arguments.get("z"), "walkUntil z").resolve(origin.getZ())
                );
                context.logger().info("Walking toward absolute {}", absoluteTarget);
            }
            client.options.keyUp.setDown(true);
            holdingForward = true;
            context.logger().info("Holding forward for walkUntil");
        }

        boolean done;
        if (arguments.containsKey("dimension")) {
            String expected = (String) arguments.get("dimension");
            String actual = client.level.dimension().identifier().toString();
            done = expected.equals(actual);
            if (!done) {
                context.logger().debug("Waiting for dimension {} (current {})", expected, actual);
            }
        } else {
            player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(absoluteTarget));
            done = player.blockPosition().equals(absoluteTarget);
            if (!done) {
                context.logger().debug("Walking toward {} from {}", absoluteTarget, player.blockPosition());
            }
        }

        if (!done) {
            return false;
        }

        releaseForward(client);
        context.logger().info("walkUntil condition met");
        return true;
    }

    private void releaseForward(Minecraft client) {
        client.options.keyUp.setDown(false);
        holdingForward = false;
        absoluteTarget = null;
    }

    public static Map<String, Object> normalize(Map<String, Object> arguments) {
        for (String key : arguments.keySet()) {
            if (!KEYS.contains(key)) {
                throw new ScenarioException("walkUntil does not accept '" + key + "'");
            }
        }
        boolean hasDimension = arguments.containsKey("dimension");
        boolean hasPosition = arguments.keySet().stream().anyMatch(POSITION_KEYS::contains);
        if (hasDimension == hasPosition) {
            throw new ScenarioException("walkUntil requires dimension, or x, y, and z");
        }
        if (hasDimension) {
            return normalizeDimension(arguments);
        }
        return normalizePosition(arguments);
    }

    private static Map<String, Object> normalizeDimension(Map<String, Object> arguments) {
        if (arguments.size() != 1) {
            throw new ScenarioException("walkUntil requires dimension, or x, y, and z");
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("dimension", ScenarioIds.normalize(arguments.get("dimension"), "walkUntil dimension"));
        return normalized;
    }

    private static Map<String, Object> normalizePosition(Map<String, Object> arguments) {
        if (!arguments.containsKey("x") || !arguments.containsKey("y") || !arguments.containsKey("z")) {
            throw new ScenarioException("walkUntil requires dimension, or x, y, and z");
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (String axis : List.of("x", "y", "z")) {
            ScenarioCoordinates.Component component = ScenarioCoordinates.parse(
                    arguments.get(axis), "walkUntil " + axis);
            normalized.put(axis, component.authored());
        }
        return normalized;
    }
}
