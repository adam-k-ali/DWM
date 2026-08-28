package com.adamkali.screenplay.primitive;

import com.adamkali.screenplay.ScenarioException;
import com.adamkali.screenplay.ScenarioIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
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
    private static final Set<String> KEYS = Set.of("type", "mode", "maxDistance", "hand");
    private static final Set<String> MODES = Set.of("crosshair", "nearest");
    private static final Set<String> HANDS = Set.of("main", "off");
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

        Entity target = switch (mode) {
            case "crosshair" -> entityUnderCrosshair(client, typeId);
            case "nearest" -> nearestEntity(client, player, typeId, (Double) arguments.get("maxDistance"));
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
        return normalized;
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

    private static Entity nearestEntity(Minecraft client, LocalPlayer player, Identifier typeId, double maxDistance) {
        Vec3 eyes = player.getEyePosition();
        AABB search = player.getBoundingBox().inflate(maxDistance);
        List<Entity> matches = client.level.getEntities(player, search, entity -> matchesType(entity, typeId));
        return matches.stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(eyes)))
                .orElse(null);
    }

    private static boolean matchesType(Entity entity, Identifier typeId) {
        return typeId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }
}
