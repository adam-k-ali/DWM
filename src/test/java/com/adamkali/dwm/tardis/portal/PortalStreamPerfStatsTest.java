package com.adamkali.dwm.tardis.portal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalStreamPerfStatsTest {
    @BeforeEach
    @AfterEach
    void reset() {
        PortalStreamPerfStats.resetForTests();
    }

    @Test
    void nsToMs_convertsNanoseconds() {
        assertEquals(2.5, PortalStreamPerfStats.nsToMs(2_500_000L), 1e-9);
    }

    @Test
    void buildSnapshotPure_sumsFlushPhasesAndPreservesCounters() {
        PortalStreamPerfStats.Snapshot snap = PortalStreamPerfStats.buildSnapshotPure(
                62.5,
                1_000_000L,
                2_000_000L,
                500_000L,
                750_000L,
                3,
                40,
                1,
                12,
                2,
                12,
                55,
                1,
                2,
                1,
                1200
        );

        assertTrue(snap.isPresent());
        assertEquals(62.5, snap.msptMs(), 1e-9);
        assertEquals(1.0, snap.flushMetaMs(), 1e-9);
        assertEquals(2.0, snap.flushSotoMs(), 1e-9);
        assertEquals(0.5, snap.flushBotiMs(), 1e-9);
        assertEquals(0.75, snap.syncEntitiesMs(), 1e-9);
        assertEquals(3.5, snap.syncFlushMs(), 1e-9);
        assertEquals(3, snap.entitySpawns());
        assertEquals(40, snap.entityUpdates());
        assertEquals(1, snap.entityRemoves());
        assertEquals(12, snap.chunkPackets());
        assertEquals(2, snap.metaPackets());
        assertEquals(12, snap.chunkSamples());
        assertEquals(55, snap.entitiesScanned());
        assertEquals(1, snap.fullResyncs());
        assertEquals(2, snap.viewers());
        assertEquals(1, snap.activeStreams());
        assertEquals(1200, snap.serverTick());
    }

    @Test
    void idleSnapshot_isNotPresent() {
        assertFalse(PortalStreamPerfStats.Snapshot.IDLE.isPresent());
    }
}
