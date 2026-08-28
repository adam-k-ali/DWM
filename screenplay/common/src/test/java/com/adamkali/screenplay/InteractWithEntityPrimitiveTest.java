package com.adamkali.screenplay;

import com.adamkali.screenplay.primitive.InteractWithEntityPrimitive;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractWithEntityPrimitiveTest {
    @Test
    void normalizeAppliesDefaults() {
        Map<String, Object> normalized = InteractWithEntityPrimitive.normalize(Map.of(
                "type", "dwm:console_control"
        ));

        assertEquals("dwm:console_control", normalized.get("type"));
        assertEquals("crosshair", normalized.get("mode"));
        assertEquals(6.0D, normalized.get("maxDistance"));
        assertEquals("main", normalized.get("hand"));
    }

    @Test
    void normalizeAcceptsNearestModeAndOffHand() {
        Map<String, Object> normalized = InteractWithEntityPrimitive.normalize(Map.of(
                "type", "minecraft:villager",
                "mode", "nearest",
                "maxDistance", 12,
                "hand", "off"
        ));

        assertEquals("minecraft:villager", normalized.get("type"));
        assertEquals("nearest", normalized.get("mode"));
        assertEquals(12.0D, normalized.get("maxDistance"));
        assertEquals("off", normalized.get("hand"));
    }

    @Test
    void normalizeRejectsMissingTypeAndInvalidValues() {
        ScenarioException missingType = assertThrows(
                ScenarioException.class,
                () -> InteractWithEntityPrimitive.normalize(Map.of("mode", "nearest"))
        );
        ScenarioException invalidMode = assertThrows(
                ScenarioException.class,
                () -> InteractWithEntityPrimitive.normalize(Map.of(
                        "type", "minecraft:cow",
                        "mode", "look"
                ))
        );
        ScenarioException invalidDistance = assertThrows(
                ScenarioException.class,
                () -> InteractWithEntityPrimitive.normalize(Map.of(
                        "type", "minecraft:cow",
                        "maxDistance", 0
                ))
        );
        ScenarioException unknownField = assertThrows(
                ScenarioException.class,
                () -> InteractWithEntityPrimitive.normalize(Map.of(
                        "type", "minecraft:cow",
                        "radius", 4
                ))
        );

        assertTrue(missingType.getMessage().contains("requires 'type'"));
        assertTrue(invalidMode.getMessage().contains("mode must be 'crosshair' or 'nearest'"));
        assertTrue(invalidDistance.getMessage().contains("maxDistance must be positive"));
        assertTrue(unknownField.getMessage().contains("does not accept 'radius'"));
    }

    @Test
    void normalizeAddsDefaultNamespace() {
        Map<String, Object> normalized = InteractWithEntityPrimitive.normalize(Map.of(
                "type", "villager"
        ));

        assertEquals("minecraft:villager", normalized.get("type"));
        assertFalse(normalized.containsKey("text"));
    }
}
