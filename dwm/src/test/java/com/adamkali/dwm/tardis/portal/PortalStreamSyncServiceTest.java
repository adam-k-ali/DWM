package com.adamkali.dwm.tardis.portal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalStreamSyncServiceTest {
    private static final UUID TARDIS_ID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @AfterEach
    void tearDown() {
        PortalStreamSyncService.clear();
    }

    @Test
    void botiLightGate_passesAsSoonAsLightingIsReady() {
        assertTrue(PortalStreamSyncService.shouldDeferBotiStreamForTest(TARDIS_ID, false, 10));
        assertFalse(PortalStreamSyncService.shouldDeferBotiStreamForTest(TARDIS_ID, true, 11));
        assertFalse(PortalStreamSyncService.shouldDeferBotiStreamForTest(TARDIS_ID, false, 12));
    }

    @Test
    void botiLightGate_timesOutAndStaysOpen() {
        assertTrue(PortalStreamSyncService.shouldDeferBotiStreamForTest(TARDIS_ID, false, 10));
        assertTrue(PortalStreamSyncService.shouldDeferBotiStreamForTest(
                TARDIS_ID, false, 10 + PortalStreamSyncService.MAX_BOTI_LIGHT_DEFER_TICKS - 1));
        assertFalse(PortalStreamSyncService.shouldDeferBotiStreamForTest(
                TARDIS_ID, false, 10 + PortalStreamSyncService.MAX_BOTI_LIGHT_DEFER_TICKS));
        assertFalse(PortalStreamSyncService.shouldDeferBotiStreamForTest(TARDIS_ID, false, 1000));
    }
}
