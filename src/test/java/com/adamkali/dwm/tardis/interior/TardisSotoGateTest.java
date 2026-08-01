package com.adamkali.dwm.tardis.interior;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisSotoGateTest {

    @Test
    void shouldShow_requiresBotiSwingThreshold() {
        assertFalse(TardisSotoGate.shouldShow(0.0f));
        assertFalse(TardisSotoGate.shouldShow(TardisDimensions.BOTI_DOOR_SWING_THRESHOLD - 0.01f));
        assertTrue(TardisSotoGate.shouldShow(TardisDimensions.BOTI_DOOR_SWING_THRESHOLD));
        assertTrue(TardisSotoGate.shouldShow(1.0f));
    }
}
