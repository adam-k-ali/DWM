package com.adamkali.screenplay;

import com.adamkali.screenplay.primitive.UseItemPrimitive;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UseItemPrimitiveTest {
    @Test
    void normalizePreservesBlockUseAsDefault() {
        assertEquals("block", UseItemPrimitive.normalize(Map.of()).get("target"));
    }

    @Test
    void normalizeAcceptsAirTarget() {
        assertEquals("air", UseItemPrimitive.normalize(Map.of("text", "air")).get("target"));
    }

    @Test
    void normalizeRejectsUnknownTarget() {
        assertThrows(
                ScenarioException.class,
                () -> UseItemPrimitive.normalize(Map.of("target", "entity"))
        );
    }
}
