package com.adamkali.sightline;

import com.adamkali.sightline.primitive.WaitUntilPrimitive;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaitUntilPrimitiveTest {
    @Test
    void normalizeHoldingAcceptsBareAndNamespacedIds() {
        Map<String, Object> dirt = WaitUntilPrimitive.normalize(Map.of("holding", "dirt"));
        Map<String, Object> nested = WaitUntilPrimitive.normalize(Map.of(
                "holding", Map.of("id", "minecraft:oak_log")));

        assertEquals("minecraft:dirt", dirt.get("holding"));
        assertEquals("minecraft:oak_log", nested.get("holding"));
    }

    @Test
    void normalizeBlockStoresAuthoredCoordinates() {
        Map<String, Object> normalized = WaitUntilPrimitive.normalize(Map.of(
                "block", Map.of(
                        "id", "dirt",
                        "x", "~1",
                        "y", "~-1",
                        "z", "~"
                )
        ));

        @SuppressWarnings("unchecked")
        Map<String, Object> block = (Map<String, Object>) normalized.get("block");
        assertEquals("minecraft:dirt", block.get("id"));
        assertEquals("~1", block.get("x"));
        assertEquals("~-1", block.get("y"));
        assertEquals("~", block.get("z"));
    }

    @Test
    void normalizeRejectsMissingBlockFieldsAndMixedConditions() {
        ScenarioException missing = assertThrows(
                ScenarioException.class,
                () -> WaitUntilPrimitive.normalize(Map.of(
                        "block", Map.of("id", "dirt", "x", 1, "y", 2)))
        );
        ScenarioException mixed = assertThrows(
                ScenarioException.class,
                () -> WaitUntilPrimitive.normalize(Map.of(
                        "holding", "dirt",
                        "visible", Map.of("type", "button", "name", "Singleplayer")))
        );

        assertTrue(missing.getMessage().contains("waitUntil block requires 'z'"));
        assertTrue(mixed.getMessage().contains("waitUntil requires exactly one of"));
    }
}
