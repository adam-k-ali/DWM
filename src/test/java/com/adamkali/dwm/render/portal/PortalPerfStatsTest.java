package com.adamkali.dwm.render.portal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalPerfStatsTest {
    @BeforeEach
    @AfterEach
    void reset() {
        PortalPerfStats.resetForTests();
    }

    @Test
    void updateEma_firstSample_returnsSample() {
        assertEquals(4.0, PortalPerfStats.updateEma(0.0, 4.0, 0.2), 1e-9);
    }

    @Test
    void updateEma_blendsTowardSample() {
        double ema = PortalPerfStats.updateEma(10.0, 0.0, 0.2);
        assertEquals(8.0, ema, 1e-9);
    }

    @Test
    void nsToMs_convertsNanoseconds() {
        assertEquals(1.5, PortalPerfStats.nsToMs(1_500_000L), 1e-9);
    }

    @Test
    void findMaxStage_prefersLargestTimedStage() {
        EnumMap<PortalPerfStats.Stage, Long> stages = new EnumMap<>(PortalPerfStats.Stage.class);
        stages.put(PortalPerfStats.Stage.SKY_FOG, 100_000L);
        stages.put(PortalPerfStats.Stage.TERRAIN_OPAQUE, 5_000_000L);
        stages.put(PortalPerfStats.Stage.GHOST_FEATURES, 2_000_000L);
        assertEquals(PortalPerfStats.Stage.TERRAIN_OPAQUE, PortalPerfStats.findMaxStage(stages));
    }

    @Test
    void findMaxAvgStage_prefersLargestAverage() {
        EnumMap<PortalPerfStats.Stage, Double> avg = new EnumMap<>(PortalPerfStats.Stage.class);
        avg.put(PortalPerfStats.Stage.SKY_FOG, 0.1);
        avg.put(PortalPerfStats.Stage.TERRAIN_OPAQUE, 1.5);
        avg.put(PortalPerfStats.Stage.MESH_BAKE, 0.8);
        assertEquals(PortalPerfStats.Stage.TERRAIN_OPAQUE, PortalPerfStats.findMaxAvgStage(avg));
    }

    @Test
    void windowAverage_meansSamples() {
        assertEquals(2.0, PortalPerfStats.windowAverage(new double[]{1.0, 2.0, 3.0}, 3), 1e-9);
        assertEquals(0.0, PortalPerfStats.windowAverage(new double[]{}, 0), 1e-9);
    }

    @Test
    void shortId_usesFirstEightChars() {
        UUID id = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        assertEquals("12345678", PortalPerfStats.shortId(id));
    }

    @Test
    void formatLinesPure_idleWhenNoKey() {
        List<String> lines = PortalPerfStats.formatLinesPure(PortalPerfStats.DisplaySnapshot.IDLE);
        assertEquals(1, lines.size());
        assertEquals("Portal Perf [idle]", lines.getFirst());
    }

    @Test
    void formatLinesPure_showsAveragesNotRawFrame() {
        UUID id = UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789");
        PortalKey key = PortalKey.soto(id);
        EnumMap<PortalPerfStats.Stage, Double> avg = new EnumMap<>(PortalPerfStats.Stage.class);
        avg.put(PortalPerfStats.Stage.FLUSH_TOTAL, 3.0);
        avg.put(PortalPerfStats.Stage.OFF_MAIN_TOTAL, 2.5);
        avg.put(PortalPerfStats.Stage.TERRAIN_OPAQUE, 1.2);
        avg.put(PortalPerfStats.Stage.PASS_BATCH_REBUILD, 0.8);

        PortalPerfStats.DisplaySnapshot snap = new PortalPerfStats.DisplaySnapshot(
                key,
                PortalPerfStats.Outcome.RENDERED,
                avg,
                25,
                24,
                3,
                8,
                17,
                2.5,
                2.1,
                60,
                0.0,
                0.0,
                PortalPerfStats.Stage.TERRAIN_OPAQUE
        );

        List<String> lines = PortalPerfStats.formatLinesPure(snap);
        assertTrue(lines.get(0).contains("SOTO"));
        assertTrue(lines.get(0).contains("abcdef01"));
        assertTrue(lines.get(1).contains("avg: 2.50ms"));
        assertTrue(lines.get(1).contains("ema 2.10"));
        assertTrue(lines.get(1).contains("n=60"));
        assertTrue(lines.stream().anyMatch(line -> line.contains("*opaque:")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("cull: 8/17")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("maxAvg: opaque")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("bake this frame")));
    }

    @Test
    void ringBuffer_computesWindowAverageAndHistory() {
        PortalKey key = PortalKey.soto(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        PortalPerfStats.setIdentityForTest(key, PortalPerfStats.Outcome.RENDERED);

        for (int i = 0; i < 3; i++) {
            EnumMap<PortalPerfStats.Stage, Double> stages = new EnumMap<>(PortalPerfStats.Stage.class);
            stages.put(PortalPerfStats.Stage.TERRAIN_OPAQUE, 1.0 + i);
            stages.put(PortalPerfStats.Stage.OFF_MAIN_TOTAL, 2.0 + i);
            PortalPerfStats.pushSampleForTest(stages, 2.0 + i, 0);
        }

        assertEquals(3, PortalPerfStats.historySize());
        float[] totals = PortalPerfStats.historyTotalsMs();
        assertEquals(3, totals.length);
        assertEquals(2.0f, totals[0], 1e-4f);
        assertEquals(4.0f, totals[2], 1e-4f);

        PortalPerfStats.DisplaySnapshot display = PortalPerfStats.displaySnapshot();
        assertEquals(3, display.windowCount());
        assertEquals(3.0, display.avgTotalMs(), 1e-9);
        assertEquals(2.0, display.avgStageMs().get(PortalPerfStats.Stage.TERRAIN_OPAQUE), 1e-9);
    }

    @Test
    void ringBuffer_evictsOldestBeyondHistoryCap() {
        PortalKey key = PortalKey.boti(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        PortalPerfStats.setIdentityForTest(key, PortalPerfStats.Outcome.RENDERED);

        for (int i = 0; i < PortalPerfStats.HISTORY_FRAMES + 5; i++) {
            EnumMap<PortalPerfStats.Stage, Double> stages = new EnumMap<>(PortalPerfStats.Stage.class);
            stages.put(PortalPerfStats.Stage.OFF_MAIN_TOTAL, (double) i);
            PortalPerfStats.pushSampleForTest(stages, i, 0);
        }

        assertEquals(PortalPerfStats.HISTORY_FRAMES, PortalPerfStats.historySize());
        float[] totals = PortalPerfStats.historyTotalsMs();
        assertEquals(5.0f, totals[0], 1e-4f);
        assertEquals(PortalPerfStats.HISTORY_FRAMES + 4.0f, totals[totals.length - 1], 1e-4f);
    }
}
