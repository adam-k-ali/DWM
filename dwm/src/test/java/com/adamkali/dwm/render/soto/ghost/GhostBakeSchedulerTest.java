package com.adamkali.dwm.render.soto.ghost;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GhostBakeSchedulerTest {

    private static final long NO_TIME_LIMIT = Long.MAX_VALUE;

    private static List<Long> drainAll(GhostBakeScheduler s, int maxChunks, long maxNanos, AtomicLong clock, long costPerBake) {
        List<Long> baked = new ArrayList<>();
        s.drain(maxChunks, maxNanos, clock::get, k -> k, k -> {
            baked.add(k);
            clock.addAndGet(costPerBake);
        });
        return baked;
    }

    @Test
    void enqueue_dedupes() {
        GhostBakeScheduler s = new GhostBakeScheduler();
        s.enqueue(1, GhostBakeScheduler.Priority.PRIMARY);
        s.enqueue(1, GhostBakeScheduler.Priority.PRIMARY);
        s.enqueue(1, GhostBakeScheduler.Priority.REFRESH);
        assertEquals(1, s.size());
    }

    @Test
    void primary_drainsBeforeRefresh_thenNearestFirst() {
        GhostBakeScheduler s = new GhostBakeScheduler();
        s.enqueue(5, GhostBakeScheduler.Priority.REFRESH);
        s.enqueue(9, GhostBakeScheduler.Priority.PRIMARY);
        s.enqueue(2, GhostBakeScheduler.Priority.PRIMARY);
        s.enqueue(1, GhostBakeScheduler.Priority.REFRESH);
        List<Long> order = drainAll(s, 10, NO_TIME_LIMIT, new AtomicLong(), 0);
        assertEquals(List.of(2L, 9L, 1L, 5L), order);
    }

    @Test
    void refreshNeverDowngrades_primaryUpgradesRefresh() {
        GhostBakeScheduler s = new GhostBakeScheduler();
        s.enqueue(7, GhostBakeScheduler.Priority.PRIMARY);
        s.enqueue(7, GhostBakeScheduler.Priority.REFRESH);
        s.enqueue(3, GhostBakeScheduler.Priority.REFRESH);
        s.enqueue(3, GhostBakeScheduler.Priority.PRIMARY);
        s.enqueue(1, GhostBakeScheduler.Priority.REFRESH);
        // 7 and 3 are PRIMARY (nearest first among them), 1 is REFRESH.
        assertEquals(List.of(3L, 7L, 1L), drainAll(s, 10, NO_TIME_LIMIT, new AtomicLong(), 0));
    }

    @Test
    void drain_respectsChunkBudget() {
        GhostBakeScheduler s = new GhostBakeScheduler();
        for (long k = 0; k < 10; k++) {
            s.enqueue(k, GhostBakeScheduler.Priority.PRIMARY);
        }
        assertEquals(4, drainAll(s, 4, NO_TIME_LIMIT, new AtomicLong(), 0).size());
        assertEquals(6, s.size());
    }

    @Test
    void drain_respectsTimeBudget_butAlwaysBakesOne() {
        GhostBakeScheduler s = new GhostBakeScheduler();
        for (long k = 0; k < 10; k++) {
            s.enqueue(k, GhostBakeScheduler.Priority.PRIMARY);
        }
        // Each bake costs 3 ms against a 4 ms budget: bake, bake (3 < 4), then stop at 6 >= 4.
        assertEquals(2, drainAll(s, 10, 4_000_000L, new AtomicLong(), 3_000_000L).size());

        GhostBakeScheduler slow = new GhostBakeScheduler();
        slow.enqueue(1, GhostBakeScheduler.Priority.PRIMARY);
        slow.enqueue(2, GhostBakeScheduler.Priority.PRIMARY);
        // Budget already blown by the first bake: still exactly one handled.
        assertEquals(1, drainAll(slow, 10, 1L, new AtomicLong(), 50_000_000L).size());
    }

    @Test
    void drain_emptyQueue_handlesNothing() {
        assertEquals(0, drainAll(new GhostBakeScheduler(), 4, NO_TIME_LIMIT, new AtomicLong(), 0).size());
    }

    @Test
    void removeAndClear() {
        GhostBakeScheduler s = new GhostBakeScheduler();
        s.enqueue(1, GhostBakeScheduler.Priority.PRIMARY);
        s.enqueue(2, GhostBakeScheduler.Priority.REFRESH);
        assertTrue(s.remove(1));
        assertFalse(s.remove(1));
        assertFalse(s.contains(1));
        assertTrue(s.contains(2));
        s.clear();
        assertEquals(0, s.size());
    }

    @Test
    void manyRefreshes_coalesceIntoOneBake() {
        GhostBakeScheduler s = new GhostBakeScheduler();
        for (int i = 0; i < 9; i++) {
            s.enqueue(42, GhostBakeScheduler.Priority.REFRESH);
        }
        assertEquals(List.of(42L), drainAll(s, 10, NO_TIME_LIMIT, new AtomicLong(), 0));
    }
}
