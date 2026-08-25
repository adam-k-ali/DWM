package com.adamkali.dwm.tardis.interior;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisPortalGateTest {

    @Test
    void shouldShow_requiresBotiSwingThreshold() {
        assertFalse(TardisPortalGate.shouldShow(0.0f));
        assertFalse(TardisPortalGate.shouldShow(TardisDimensions.BOTI_DOOR_SWING_THRESHOLD - 0.01f));
        assertTrue(TardisPortalGate.shouldShow(TardisDimensions.BOTI_DOOR_SWING_THRESHOLD));
        assertTrue(TardisPortalGate.shouldShow(1.0f));
    }
}
