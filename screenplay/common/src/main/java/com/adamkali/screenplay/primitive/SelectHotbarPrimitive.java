package com.adamkali.screenplay.primitive;

import com.adamkali.screenplay.ScenarioException;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class SelectHotbarPrimitive implements ScenarioPrimitive {
    public static final int MIN_SLOT = 0;
    public static final int MAX_SLOT = 8;
    private static final Set<String> KEYS = Set.of("slot", "text");

    @Override
    public String name() {
        return "selectHotbar";
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
        if (client.player == null || client.player.connection == null) {
            return false;
        }
        int slot = (Integer) context.arguments().get("slot");
        context.logger().info("Selecting hotbar slot {}", slot);
        client.player.getInventory().setSelectedSlot(slot);
        client.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
        return true;
    }

    public static Map<String, Object> normalize(Map<String, Object> arguments) {
        for (String key : arguments.keySet()) {
            if (!KEYS.contains(key)) {
                throw new ScenarioException("selectHotbar does not accept '" + key + "'");
            }
        }
        boolean hasSlot = arguments.containsKey("slot");
        boolean hasText = arguments.containsKey("text");
        if (hasSlot == hasText) {
            throw new ScenarioException("selectHotbar requires an integer 'slot' from "
                    + MIN_SLOT + " to " + MAX_SLOT);
        }
        int slot = parseSlot(hasSlot ? arguments.get("slot") : arguments.get("text"));
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("slot", slot);
        return normalized;
    }

    public static int parseSlot(Object value) {
        int slot;
        if (value instanceof Integer integer) {
            slot = integer;
        } else if (value instanceof Long longValue) {
            if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
                throw invalidSlot();
            }
            slot = longValue.intValue();
        } else if (value instanceof String string) {
            if (string.isBlank()) {
                throw invalidSlot();
            }
            try {
                slot = Integer.parseInt(string.trim());
            } catch (NumberFormatException exception) {
                throw invalidSlot();
            }
        } else {
            throw invalidSlot();
        }
        if (slot < MIN_SLOT || slot > MAX_SLOT) {
            throw invalidSlot();
        }
        return slot;
    }

    private static ScenarioException invalidSlot() {
        return new ScenarioException("selectHotbar requires an integer 'slot' from "
                + MIN_SLOT + " to " + MAX_SLOT);
    }
}
