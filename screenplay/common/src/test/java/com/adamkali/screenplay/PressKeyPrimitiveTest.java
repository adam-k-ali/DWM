package com.adamkali.screenplay;

import com.adamkali.screenplay.primitive.PressKeyPrimitive;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PressKeyPrimitiveTest {
    @Test
    void resolveKeySupportsCommonNames() {
        assertEquals("key.keyboard.g", PressKeyPrimitive.resolveKey("g").getName());
        assertEquals("key.keyboard.escape", PressKeyPrimitive.resolveKey("escape").getName());
        assertEquals("key.keyboard.escape", PressKeyPrimitive.resolveKey("esc").getName());
        assertEquals("key.keyboard.f1", PressKeyPrimitive.resolveKey("f1").getName());
        assertEquals("key.keyboard.space", PressKeyPrimitive.resolveKey("space").getName());
        assertEquals("key.keyboard.left", PressKeyPrimitive.resolveKey("left").getName());
        assertEquals("key.keyboard.right", PressKeyPrimitive.resolveKey("right").getName());
    }

    @Test
    void resolveKeyRejectsUnknownNames() {
        assertThrows(ScenarioException.class, () -> PressKeyPrimitive.resolveKey("f9"));
    }

    @Test
    void validateAcceptsScalarKey() {
        PressKeyPrimitive primitive = new PressKeyPrimitive();
        assertEquals("g", primitive.validate(Map.of("key", "g"), "test").get("key"));
        assertEquals("f1", primitive.validate(Map.of("key", "F1"), "test").get("key"));
        assertEquals("left", primitive.validate(Map.of("key", "Left"), "test").get("key"));
        assertEquals("right", primitive.validate(Map.of("key", "RIGHT"), "test").get("key"));
    }
}
