package com.adamkali.screenplay;

import com.adamkali.screenplay.primitive.WaitTicksPrimitive;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaitTicksPrimitiveTest {
    @Test
    void normalizeAcceptsTicksAndRemapsText() {
        Map<String, Object> fromTicks = WaitTicksPrimitive.normalize(Map.of("ticks", 25));
        Map<String, Object> fromText = WaitTicksPrimitive.normalize(Map.of("text", "25"));

        assertEquals(25, fromTicks.get("ticks"));
        assertEquals(25, fromText.get("ticks"));
        assertFalse(fromText.containsKey("text"));
    }

    @Test
    void normalizeRejectsMissingZeroAndUnknownFields() {
        ScenarioException missing = assertThrows(
                ScenarioException.class,
                () -> WaitTicksPrimitive.normalize(Map.of())
        );
        ScenarioException both = assertThrows(
                ScenarioException.class,
                () -> WaitTicksPrimitive.normalize(Map.of("ticks", 25, "text", "25"))
        );
        ScenarioException zero = assertThrows(
                ScenarioException.class,
                () -> WaitTicksPrimitive.normalize(Map.of("ticks", 0))
        );
        ScenarioException negative = assertThrows(
                ScenarioException.class,
                () -> WaitTicksPrimitive.normalize(Map.of("ticks", -1))
        );
        ScenarioException unknown = assertThrows(
                ScenarioException.class,
                () -> WaitTicksPrimitive.normalize(Map.of("duration", 25))
        );

        assertTrue(missing.getMessage().contains("waitTicks requires a positive integer 'ticks'"));
        assertTrue(both.getMessage().contains("waitTicks requires a positive integer 'ticks'"));
        assertTrue(zero.getMessage().contains("waitTicks requires a positive integer 'ticks'"));
        assertTrue(negative.getMessage().contains("waitTicks requires a positive integer 'ticks'"));
        assertTrue(unknown.getMessage().contains("waitTicks does not accept 'duration'"));
    }
}
