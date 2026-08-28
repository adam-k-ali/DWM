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

    @Test
    void normalizeAcceptsNearAnchorAndIndex() {
        Map<String, Object> normalized = InteractWithEntityPrimitive.normalize(Map.of(
                "type", "dwm:console_control",
                "mode", "nearest",
                "maxDistance", 4,
                "near", Map.of("x", "~", "y", "~1", "z", "~1"),
                "index", 1
        ));

        assertEquals("nearest", normalized.get("mode"));
        assertEquals(1, normalized.get("index"));
        @SuppressWarnings("unchecked")
        Map<String, Object> near = (Map<String, Object>) normalized.get("near");
        assertEquals("~", near.get("x"));
        assertEquals("~1", near.get("y"));
        assertEquals("~1", near.get("z"));
    }

    @Test
    void normalizeRejectsInvalidNearAndIndex() {
        ScenarioException missingAxis = assertThrows(
                ScenarioException.class,
                () -> InteractWithEntityPrimitive.normalize(Map.of(
                        "type", "dwm:console_control",
                        "near", Map.of("x", "~", "y", "~1")
                ))
        );
        ScenarioException negativeIndex = assertThrows(
                ScenarioException.class,
                () -> InteractWithEntityPrimitive.normalize(Map.of(
                        "type", "dwm:console_control",
                        "index", -1
                ))
        );

        assertTrue(missingAxis.getMessage().contains("near requires 'z'"));
        assertTrue(negativeIndex.getMessage().contains("index must be a non-negative integer"));
    }
}
