package com.adamkali.screenplay.primitive;

import com.adamkali.screenplay.ScenarioCoordinates;
import com.adamkali.screenplay.ScenarioException;
import com.adamkali.screenplay.ScenarioIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class InteractWithEntityPrimitive implements ScenarioPrimitive {
    private static final Set<String> KEYS = Set.of("type", "mode", "maxDistance", "hand", "near", "index");
    private static final Set<String> MODES = Set.of("crosshair", "nearest");
    private static final Set<String> HANDS = Set.of("main", "off");
    private static final Set<String> NEAR_KEYS = Set.of("x", "y", "z");
    private static final double DEFAULT_MAX_DISTANCE = 6.0D;

    @Override
    public String name() {
        return "interactWithEntity";
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
        MultiPlayerGameMode gameMode = client.gameMode;
        if (player == null || gameMode == null || client.level == null || context.screen() != null) {
            return false;
        }

        Map<String, Object> arguments = context.arguments();
        Identifier typeId = Identifier.parse((String) arguments.get("type"));
        String mode = (String) arguments.get("mode");
        InteractionHand hand = parseHand((String) arguments.get("hand"));
        Vec3 anchor = anchorPoint(player, arguments);

        Entity target = switch (mode) {
            case "crosshair" -> entityUnderCrosshair(client, typeId);
            case "nearest" -> nearestEntity(
                    client,
                    player,
                    typeId,
                    (Double) arguments.get("maxDistance"),
                    anchor,
                    (Integer) arguments.get("index")
            );
            default -> null;
        };
        if (target == null) {
            return false;
        }

        EntityHitResult hit = hitForTarget(client, target, mode);
        if (hit == null) {
            return false;
        }

        context.logger().info(
                "Interacting with {} ({}) using {}",
                BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()),
                target.getId(),
                hand
        );
        gameMode.interact(player, target, hit, hand);
        return true;
    }

    public static Map<String, Object> normalize(Map<String, Object> arguments) {
        for (String key : arguments.keySet()) {
            if (!KEYS.contains(key)) {
                throw new ScenarioException("interactWithEntity does not accept '" + key + "'");
            }
        }
        if (!arguments.containsKey("type")) {
            throw new ScenarioException("interactWithEntity requires 'type'");
        }

        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("type", ScenarioIds.normalize(arguments.get("type"), "interactWithEntity type"));

        String mode = "crosshair";
        if (arguments.containsKey("mode")) {
            if (!(arguments.get("mode") instanceof String string) || string.isBlank()) {
                throw new ScenarioException("interactWithEntity mode must be 'crosshair' or 'nearest'");
            }
            mode = string.trim().toLowerCase(Locale.ROOT);
            if (!MODES.contains(mode)) {
                throw new ScenarioException("interactWithEntity mode must be 'crosshair' or 'nearest'");
            }
        }
        normalized.put("mode", mode);

        double maxDistance = DEFAULT_MAX_DISTANCE;
        if (arguments.containsKey("maxDistance")) {
            Object value = arguments.get("maxDistance");
            if (!(value instanceof Number number)) {
                throw new ScenarioException("interactWithEntity maxDistance must be a number");
            }
            maxDistance = number.doubleValue();
            if (maxDistance <= 0.0D) {
                throw new ScenarioException("interactWithEntity maxDistance must be positive");
            }
        }
        normalized.put("maxDistance", maxDistance);

        String hand = "main";
        if (arguments.containsKey("hand")) {
            if (!(arguments.get("hand") instanceof String string) || string.isBlank()) {
                throw new ScenarioException("interactWithEntity hand must be 'main' or 'off'");
            }
            hand = string.trim().toLowerCase(Locale.ROOT);
            if (!HANDS.contains(hand)) {
                throw new ScenarioException("interactWithEntity hand must be 'main' or 'off'");
            }
        }
        normalized.put("hand", hand);

        if (arguments.containsKey("near")) {
            normalized.put("near", Map.copyOf(normalizeNear(arguments.get("near"))));
        }

        int index = 0;
        if (arguments.containsKey("index")) {
            Object value = arguments.get("index");
            if (!(value instanceof Number number)) {
                throw new ScenarioException("interactWithEntity index must be a non-negative integer");
            }
            double raw = number.doubleValue();
            if (raw != Math.rint(raw) || raw < 0.0D) {
                throw new ScenarioException("interactWithEntity index must be a non-negative integer");
            }
            index = number.intValue();
        }
        normalized.put("index", index);
        return normalized;
    }

    private static Map<String, Object> normalizeNear(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            throw new ScenarioException("interactWithEntity near must be an object with x, y, and z");
        }
        Map<String, Object> near = new LinkedHashMap<>();
        raw.forEach((key, entryValue) -> {
            if (!(key instanceof String stringKey)) {
                throw new ScenarioException("interactWithEntity near object keys must be strings");
            }
            near.put(stringKey, entryValue);
        });
        for (String key : near.keySet()) {
            if (!NEAR_KEYS.contains(key)) {
                throw new ScenarioException("interactWithEntity near does not accept '" + key + "'");
            }
        }
        for (String axis : NEAR_KEYS) {
            if (!near.containsKey(axis)) {
                throw new ScenarioException("interactWithEntity near requires '" + axis + "'");
            }
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (String axis : NEAR_KEYS) {
            ScenarioCoordinates.Component component = ScenarioCoordinates.parse(
                    near.get(axis), "interactWithEntity near " + axis);
            normalized.put(axis, component.authored());
        }
        return normalized;
    }

    private static Vec3 anchorPoint(LocalPlayer player, Map<String, Object> arguments) {
        if (arguments.get("near") instanceof Map<?, ?> near) {
            BlockPos origin = player.blockPosition();
            double x = ScenarioCoordinates.parse(near.get("x"), "interactWithEntity near x")
                    .resolve(origin.getX()) + 0.5D;
            double y = ScenarioCoordinates.parse(near.get("y"), "interactWithEntity near y")
                    .resolve(origin.getY()) + 0.5D;
            double z = ScenarioCoordinates.parse(near.get("z"), "interactWithEntity near z")
                    .resolve(origin.getZ()) + 0.5D;
            return new Vec3(x, y, z);
        }
        return player.getEyePosition();
    }

    private static InteractionHand parseHand(String hand) {
        return "off".equals(hand) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private static Entity entityUnderCrosshair(Minecraft client, Identifier typeId) {
        HitResult hit = client.hitResult;
        if (!(hit instanceof EntityHitResult entityHit) || entityHit.getType() != HitResult.Type.ENTITY) {
            return null;
        }
        Entity entity = entityHit.getEntity();
        return matchesType(entity, typeId) ? entity : null;
    }

    private static EntityHitResult hitForTarget(Minecraft client, Entity target, String mode) {
        if ("crosshair".equals(mode)) {
            HitResult hit = client.hitResult;
            if (hit instanceof EntityHitResult entityHit
                    && entityHit.getType() == HitResult.Type.ENTITY
                    && entityHit.getEntity() == target) {
                return entityHit;
            }
        }
        return new EntityHitResult(target, target.getBoundingBox().getCenter());
    }

    private static Entity nearestEntity(
            Minecraft client,
            LocalPlayer player,
            Identifier typeId,
            double maxDistance,
            Vec3 anchor,
            int index
    ) {
        AABB search = player.getBoundingBox().inflate(maxDistance);
        List<Entity> matches = client.level.getEntities(player, search, entity -> matchesType(entity, typeId));
        return matches.stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(anchor)))
                .skip(index)
                .findFirst()
                .orElse(null);
    }

    private static boolean matchesType(Entity entity, Identifier typeId) {
        return typeId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }
}
