package com.adamkali.dwm.scenariotest;

import com.adamkali.dwm.scenariotest.primitive.SelectHotbarPrimitive;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectHotbarPrimitiveTest {
    @Test
    void normalizeAcceptsSlotAndRemapsText() {
        Map<String, Object> fromSlot = SelectHotbarPrimitive.normalize(Map.of("slot", 3));
        Map<String, Object> fromText = SelectHotbarPrimitive.normalize(Map.of("text", "8"));

        assertEquals(3, fromSlot.get("slot"));
        assertEquals(8, fromText.get("slot"));
        assertFalse(fromText.containsKey("text"));
    }

    @Test
    void normalizeRejectsOutOfRangeMissingAndUnknownFields() {
        ScenarioException missing = assertThrows(
                ScenarioException.class,
                () -> SelectHotbarPrimitive.normalize(Map.of())
        );
        ScenarioException both = assertThrows(
                ScenarioException.class,
                () -> SelectHotbarPrimitive.normalize(Map.of("slot", 0, "text", "0"))
        );
        ScenarioException range = assertThrows(
                ScenarioException.class,
                () -> SelectHotbarPrimitive.normalize(Map.of("slot", 9))
        );
        ScenarioException unknown = assertThrows(
                ScenarioException.class,
                () -> SelectHotbarPrimitive.normalize(Map.of("index", 1))
        );

        assertTrue(missing.getMessage().contains("selectHotbar requires an integer 'slot'"));
        assertTrue(both.getMessage().contains("selectHotbar requires an integer 'slot'"));
        assertTrue(range.getMessage().contains("from 0 to 8"));
        assertTrue(unknown.getMessage().contains("selectHotbar does not accept 'index'"));
    }
}
