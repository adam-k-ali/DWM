package com.adamkali.dwm.render.portal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalPerfDebugLogTest {
    @BeforeEach
    @AfterEach
    void reset() {
        PortalPerfDebugLog.resetForTests();
        PortalPerfStats.resetForTests();
    }

    @Test
    void formatLine_includesAveragesAndIdentity() {
        UUID id = UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789");
        EnumMap<PortalPerfStats.Stage, Double> avg = new EnumMap<>(PortalPerfStats.Stage.class);
        avg.put(PortalPerfStats.Stage.TERRAIN_OPAQUE, 1.25);
        avg.put(PortalPerfStats.Stage.MESH_BAKE, 0.5);

        PortalPerfStats.DisplaySnapshot snap = new PortalPerfStats.DisplaySnapshot(
                PortalKey.soto(id),
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
                0.25,
                1.5,
                PortalPerfStats.Stage.TERRAIN_OPAQUE
        );

        String line = PortalPerfDebugLog.formatLine(1_700_000_000_000L, snap);
        assertTrue(line.startsWith("{"));
        assertTrue(line.endsWith("}"));
        assertTrue(line.contains("\"kind\":\"SOTO\""));
        assertTrue(line.contains("\"tardisId\":\"abcdef01-2345-6789-abcd-ef0123456789\""));
        assertTrue(line.contains("\"outcome\":\"rendered\""));
        assertTrue(line.contains("\"avgTotalMs\":2.5000"));
        assertTrue(line.contains("\"emaTotalMs\":2.1000"));
        assertTrue(line.contains("\"window\":60"));
        assertTrue(line.contains("\"opaque\":1.2500"));
        assertTrue(line.contains("\"chunks\":25"));
        assertTrue(line.contains("\"cullKept\":8"));
        assertTrue(line.contains("\"cullCulled\":17"));
        assertTrue(line.contains("\"avgBakeCount\":0.2500"));
        assertTrue(line.contains("\"avgBakeSkipCount\":1.5000"));
    }

    @Test
    void resolveLogPathForTest_usesLogsSubdir() {
        Path path = PortalPerfDebugLog.resolveLogPathForTest(Path.of("/tmp/game"));
        assertEquals(Path.of("/tmp/game/logs/dwm-portal-perf.jsonl"), path);
    }
}
