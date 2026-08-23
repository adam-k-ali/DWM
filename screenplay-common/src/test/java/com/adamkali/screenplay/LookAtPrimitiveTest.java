package com.adamkali.screenplay;

import com.adamkali.screenplay.primitive.LookAtPrimitive;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LookAtPrimitiveTest {
    @Test
    void normalizeAcceptsYawAndPitch() {
        Map<String, Object> normalized = LookAtPrimitive.normalize(Map.of(
                "yaw", 90,
                "pitch", -45
        ));

        assertEquals(90.0F, normalized.get("yaw"));
        assertEquals(-45.0F, normalized.get("pitch"));
        assertFalse(normalized.containsKey("x"));
    }

    @Test
    void normalizeAcceptsRelativeCoordinates() {
        Map<String, Object> normalized = LookAtPrimitive.normalize(Map.of(
                "x", "~1",
                "y", "~-1",
                "z", "~"
        ));

        assertEquals("~1", normalized.get("x"));
        assertEquals("~-1", normalized.get("y"));
        assertEquals("~", normalized.get("z"));
    }

    @Test
    void normalizeRejectsMixedAndIncompleteTargets() {
        ScenarioException mixed = assertThrows(
                ScenarioException.class,
                () -> LookAtPrimitive.normalize(Map.of("yaw", 90, "x", "~1"))
        );
        ScenarioException incompleteRotation = assertThrows(
                ScenarioException.class,
                () -> LookAtPrimitive.normalize(Map.of("yaw", 90))
        );
        ScenarioException incompletePosition = assertThrows(
                ScenarioException.class,
                () -> LookAtPrimitive.normalize(Map.of("x", 1, "y", 2))
        );
        ScenarioException unknown = assertThrows(
                ScenarioException.class,
                () -> LookAtPrimitive.normalize(Map.of(
                        "x", 1,
                        "y", 2,
                        "z", 3,
                        "anchor", "eyes"))
        );
        ScenarioException pitch = assertThrows(
                ScenarioException.class,
                () -> LookAtPrimitive.normalize(Map.of("yaw", 0, "pitch", 91))
        );

        assertTrue(mixed.getMessage().contains("lookAt requires yaw and pitch, or x, y, and z"));
        assertTrue(incompleteRotation.getMessage().contains("lookAt requires yaw and pitch, or x, y, and z"));
        assertTrue(incompletePosition.getMessage().contains("lookAt requires yaw and pitch, or x, y, and z"));
        assertTrue(unknown.getMessage().contains("lookAt does not accept 'anchor'"));
        assertTrue(pitch.getMessage().contains("lookAt pitch must be between -90 and 90"));
    }
}
