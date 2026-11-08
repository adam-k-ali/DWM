package com.adamkali.dwm.tardis.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

/**
 * Perf regression: locks in the per-tick work budgets shared by the BOTI and SOTO streams so
 * a change that raises them (or removes the cap) fails loudly.
 */
class PortalStreamPerfBudgetTest {

    @Test
    void budgetConstantsDoNotRegress() {
        assertTrue(PortalStreamSyncService.CHUNK_SEND_BUDGET_PER_VIEWER_TICK <= 8);
        assertTrue(PortalStreamSyncService.GLOBAL_SAMPLE_BUDGET_PER_TICK <= 16);
        assertTrue(PortalStreamSyncService.SAMPLE_TIME_BUDGET_NS <= 2_000_000L);
    }

    @Test
    void sampleBudgetStopsAfterGlobalCapEvenWithTimeRemaining() {
        PortalStreamSyncService.SampleBudget budget = PortalStreamSyncService.SampleBudget.start();
        int samples = 0;
        // Generous loop bound: a missing cap would blow past it.
        for (int i = 0; i < 10_000 && budget.canSample(); i++) {
            budget.noteSample();
            samples++;
        }
        assertTrue(samples <= PortalStreamSyncService.GLOBAL_SAMPLE_BUDGET_PER_TICK, "samples=" + samples);
        assertFalse(budget.canSample() && samples >= PortalStreamSyncService.GLOBAL_SAMPLE_BUDGET_PER_TICK);
    }

    @Test
    void freshBudgetPerTickResetsTheCap() {
        PortalStreamSyncService.SampleBudget a = PortalStreamSyncService.SampleBudget.start();
        a.remainingSamples = 0;
        assertFalse(a.canSample());
        assertTrue(PortalStreamSyncService.SampleBudget.start().canSample());
    }

    @Test
    void leftoverDirtyCarriesUnsentChunksToNextTick() {
        Map<Long, Integer> need = new HashMap<>();
        Map<Long, Integer> got = new HashMap<>();
        int total = PortalStreamSyncService.CHUNK_SEND_BUDGET_PER_VIEWER_TICK + 5;
        for (int i = 0; i < total; i++) {
            need.put(ChunkPos.pack(i, 0), 1);
        }
        for (int i = 0; i < PortalStreamSyncService.CHUNK_SEND_BUDGET_PER_VIEWER_TICK; i++) {
            got.put(ChunkPos.pack(i, 0), 1);
        }
        Set<Long> leftover = PortalStreamSyncService.leftoverDirty(need, got);
        assertEquals(5, leftover.size());
        // Draining at the per-tick budget finishes in a bounded number of ticks.
        int ticks = (total + PortalStreamSyncService.CHUNK_SEND_BUDGET_PER_VIEWER_TICK - 1)
                / PortalStreamSyncService.CHUNK_SEND_BUDGET_PER_VIEWER_TICK;
        assertEquals(2, ticks);
    }

    @Test
    void metaIsClaimedOncePerViewer() {
        Set<String> sent = new HashSet<>();
        assertTrue(PortalStreamSyncService.claimMetaSend(sent, "viewer"));
        for (int i = 0; i < 100; i++) {
            assertFalse(PortalStreamSyncService.claimMetaSend(sent, "viewer"));
        }
    }

    @Test
    void chunksAreOrderedNearestFirstByChebyshevDistance() {
        long near = ChunkPos.pack(1, 1);
        long mid = ChunkPos.pack(-2, 0);
        long far = ChunkPos.pack(3, -3);
        assertTrue(PortalStreamSyncService.chebyshevDistance(near, 0, 0)
                < PortalStreamSyncService.chebyshevDistance(mid, 0, 0));
        assertTrue(PortalStreamSyncService.chebyshevDistance(mid, 0, 0)
                < PortalStreamSyncService.chebyshevDistance(far, 0, 0));
    }
}
