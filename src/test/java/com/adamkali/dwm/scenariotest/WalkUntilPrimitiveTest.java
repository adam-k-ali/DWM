package com.adamkali.dwm.scenariotest;

import com.adamkali.dwm.scenariotest.primitive.WalkUntilPrimitive;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalkUntilPrimitiveTest {
    @Test
    void normalizeAcceptsDimensionId() {
        Map<String, Object> normalized = WalkUntilPrimitive.normalize(Map.of(
                "dimension", "dwm:tardis"
        ));

        assertEquals("dwm:tardis", normalized.get("dimension"));
        assertFalse(normalized.containsKey("x"));
    }

    @Test
    void normalizeAcceptsRelativeCoordinates() {
        Map<String, Object> normalized = WalkUntilPrimitive.normalize(Map.of(
                "x", "~3",
                "y", "~",
                "z", "~-1"
        ));

        assertEquals("~3", normalized.get("x"));
        assertEquals("~", normalized.get("y"));
        assertEquals("~-1", normalized.get("z"));
        assertFalse(normalized.containsKey("dimension"));
    }

    @Test
    void normalizeRejectsMixedIncompleteAndUnknown() {
        ScenarioException mixed = assertThrows(
                ScenarioException.class,
                () -> WalkUntilPrimitive.normalize(Map.of(
                        "dimension", "dwm:tardis",
                        "x", "~1"))
        );
        ScenarioException incomplete = assertThrows(
                ScenarioException.class,
                () -> WalkUntilPrimitive.normalize(Map.of("x", 1, "y", 2))
        );
        ScenarioException empty = assertThrows(
                ScenarioException.class,
                () -> WalkUntilPrimitive.normalize(Map.of())
        );
        ScenarioException unknown = assertThrows(
                ScenarioException.class,
                () -> WalkUntilPrimitive.normalize(Map.of(
                        "x", 1,
                        "y", 2,
                        "z", 3,
                        "sprint", true))
        );
        ScenarioException blankDimension = assertThrows(
                ScenarioException.class,
                () -> WalkUntilPrimitive.normalize(Map.of("dimension", "  "))
        );

        assertTrue(mixed.getMessage().contains("walkUntil requires dimension, or x, y, and z"));
        assertTrue(incomplete.getMessage().contains("walkUntil requires dimension, or x, y, and z"));
        assertTrue(empty.getMessage().contains("walkUntil requires dimension, or x, y, and z"));
        assertTrue(unknown.getMessage().contains("walkUntil does not accept 'sprint'"));
        assertTrue(blankDimension.getMessage().contains("walkUntil dimension"));
    }
}
