package com.adamkali.sightline;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioCoordinatesTest {
    @Test
    void parseRelativeOffsets() {
        ScenarioCoordinates.Component origin = ScenarioCoordinates.parse("~", "x");
        ScenarioCoordinates.Component plus = ScenarioCoordinates.parse("~1", "x");
        ScenarioCoordinates.Component minus = ScenarioCoordinates.parse("~-1", "x");
        ScenarioCoordinates.Component plusSigned = ScenarioCoordinates.parse("~+2", "x");

        assertTrue(origin.relative());
        assertEquals(0, origin.value());
        assertEquals("~", origin.authored());
        assertEquals(10, origin.resolve(10));
        assertEquals(11, plus.resolve(10));
        assertEquals(9, minus.resolve(10));
        assertEquals(12, plusSigned.resolve(10));
        assertEquals("~1", plus.authored());
        assertEquals("~-1", minus.authored());
        assertEquals("~2", plusSigned.authored());
    }

    @Test
    void parseAbsoluteIntegersFromNumbersAndStrings() {
        ScenarioCoordinates.Component fromInt = ScenarioCoordinates.parse(4, "y");
        ScenarioCoordinates.Component fromString = ScenarioCoordinates.parse(" -8 ", "y");

        assertFalse(fromInt.relative());
        assertEquals(4, fromInt.value());
        assertEquals("4", fromInt.authored());
        assertEquals(4, fromInt.resolve(100));
        assertEquals(-8, fromString.resolve(0));
    }

    @Test
    void parseRejectsBlankNullAndFractionalValues() {
        ScenarioException missing = assertThrows(
                ScenarioException.class,
                () -> ScenarioCoordinates.parse(null, "z")
        );
        ScenarioException blank = assertThrows(
                ScenarioException.class,
                () -> ScenarioCoordinates.parse("  ", "z")
        );
        ScenarioException fraction = assertThrows(
                ScenarioException.class,
                () -> ScenarioCoordinates.parse(1.5, "z")
        );
        ScenarioException invalid = assertThrows(
                ScenarioException.class,
                () -> ScenarioCoordinates.parse("~~", "z")
        );

        assertTrue(missing.getMessage().contains("quote \"~\" in YAML"));
        assertTrue(blank.getMessage().contains("must be a relative"));
        assertTrue(fraction.getMessage().contains("must be a relative"));
        assertTrue(invalid.getMessage().contains("must be a relative"));
    }
}
