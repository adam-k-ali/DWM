package com.adamkali.screenplay.primitive;

import com.adamkali.screenplay.ScenarioException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class UseItemPrimitive implements ScenarioPrimitive {
    private static final Set<String> KEYS = Set.of("target", "text");

    @Override
    public String name() {
        return "useItem";
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
        if (player == null || gameMode == null || context.screen() != null) {
            return false;
        }
        String target = (String) context.arguments().get("target");
        if ("air".equals(target)) {
            context.logger().info("Using main-hand item in air");
            gameMode.useItem(player, InteractionHand.MAIN_HAND);
            return true;
        }
        HitResult hit = client.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        context.logger().info("Using item on {} face {}", blockHit.getBlockPos(), blockHit.getDirection());
        gameMode.useItemOn(player, InteractionHand.MAIN_HAND, blockHit);
        return true;
    }

    public static Map<String, Object> normalize(Map<String, Object> arguments) {
        if (arguments.isEmpty()) {
            return Map.of("target", "block");
        }
        for (String key : arguments.keySet()) {
            if (!KEYS.contains(key)) {
                throw new ScenarioException("useItem does not accept '" + key + "'");
            }
        }
        boolean hasTarget = arguments.containsKey("target");
        boolean hasText = arguments.containsKey("text");
        if (hasTarget == hasText) {
            throw invalidTarget();
        }
        Object value = hasTarget ? arguments.get("target") : arguments.get("text");
        if (!(value instanceof String string)) {
            throw invalidTarget();
        }
        String target = string.trim().toLowerCase(Locale.ROOT);
        if (!target.equals("air") && !target.equals("block")) {
            throw invalidTarget();
        }
        return Map.of("target", target);
    }

    private static ScenarioException invalidTarget() {
        return new ScenarioException("useItem target must be 'block' or 'air'");
    }
}
