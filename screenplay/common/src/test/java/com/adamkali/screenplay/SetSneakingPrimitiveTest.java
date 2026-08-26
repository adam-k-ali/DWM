package com.adamkali.screenplay;

import com.adamkali.screenplay.primitive.SetSneakingPrimitive;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SetSneakingPrimitiveTest {
    @Test
    void validateAcceptsBooleanState() {
        SetSneakingPrimitive primitive = new SetSneakingPrimitive();

        assertEquals(true, primitive.validate(Map.of("enabled", true), "test").get("enabled"));
    }

    @Test
    void validateRejectsNonBooleanState() {
        SetSneakingPrimitive primitive = new SetSneakingPrimitive();

        assertThrows(
                ScenarioException.class,
                () -> primitive.validate(Map.of("enabled", "true"), "test")
        );
    }
}
