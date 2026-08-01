package com.adamkali.dwm.tardis.soto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SotoExteriorSyncServiceTest {

    @Test
    void snapshotFlushInterval_ignoresEntityActiveFlag() {
        assertEquals(3, SotoExteriorSyncService.snapshotFlushIntervalTicks(false));
        assertEquals(3, SotoExteriorSyncService.snapshotFlushIntervalTicks(true));
    }
}
