package com.adamkali.dwm.tardis.portal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.level.ChunkPos;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void chunkSendBudget_isSmallerThanDefaultViewBox() {
        int radius2Columns = 5 * 5;
        assertTrue(PortalStreamSyncService.CHUNK_SEND_BUDGET_PER_VIEWER_TICK < radius2Columns);
        assertTrue(PortalStreamSyncService.CHUNK_SEND_BUDGET_PER_VIEWER_TICK > 0);
        assertTrue(PortalStreamSyncService.GLOBAL_SAMPLE_BUDGET_PER_TICK >= PortalStreamSyncService.CHUNK_SEND_BUDGET_PER_VIEWER_TICK);
        assertTrue(PortalStreamSyncService.SAMPLE_TIME_BUDGET_NS > 0);
    }

    @Test
    void leftoverDirty_keepsChunksUntilEveryViewerIsUpdated() {
        long packed = ChunkPos.pack(4, -2);
        Map<Long, Integer> need = new HashMap<>();
        Map<Long, Integer> got = new HashMap<>();
        need.put(packed, 2);
        got.put(packed, 1);
        assertTrue(PortalStreamSyncService.leftoverDirty(need, got).contains(packed));
        got.put(packed, 2);
        assertTrue(PortalStreamSyncService.leftoverDirty(need, got).isEmpty());
    }

    @Test
    void chebyshevDistance_isMaxAxis() {
        assertEquals(3, PortalStreamSyncService.chebyshevDistance(ChunkPos.pack(3, -2), 0, 0));
        assertEquals(0, PortalStreamSyncService.chebyshevDistance(ChunkPos.pack(5, 9), 5, 9));
    }

    @Test
    void sampleBudget_stopsAfterGlobalCap() {
        PortalStreamSyncService.SampleBudget budget = PortalStreamSyncService.SampleBudget.start();
        assertTrue(budget.canSample());
        for (int i = 0; i < PortalStreamSyncService.GLOBAL_SAMPLE_BUDGET_PER_TICK; i++) {
            assertTrue(budget.canSample());
            budget.noteSample();
        }
        assertFalse(budget.canSample());
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
