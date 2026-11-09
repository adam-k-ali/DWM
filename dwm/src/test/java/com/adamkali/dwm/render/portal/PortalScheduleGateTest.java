package com.adamkali.dwm.render.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Perf regression: a culled doorway must never reach the off-main render queue. */
class PortalScheduleGateTest {
    private static final UUID ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

    private static final PortalContent STUB = (PortalContent) java.lang.reflect.Proxy.newProxyInstance(
            PortalContent.class.getClassLoader(), new Class<?>[]{PortalContent.class},
            (proxy, method, args) -> null);

    @BeforeEach
    @AfterEach
    void reset() {
        PortalScheduler.resetForTests();
        PortalPerfStats.resetForTests();
    }

    @Test
    void culledFramesNeverGrowPendingForEitherPortalKind() {
        for (PortalKey key : new PortalKey[]{PortalKey.boti(ID), PortalKey.soto(ID)}) {
            for (int frame = 0; frame < 1000; frame++) {
                PortalScheduler.noteCulled(key);
            }
        }
        assertEquals(0, PortalScheduler.pendingCountForTest());
        assertEquals(0L, PortalScheduler.scheduledTotal());
        assertEquals(2000L, PortalScheduler.culledTotal());
    }

    @Test
    void scheduledScenesAreDeduplicatedPerKey() {
        PortalScene boti = new PortalScene(PortalKey.boti(ID), 0f, STUB);
        PortalScene soto = new PortalScene(PortalKey.soto(ID), 0f, STUB);
        for (int i = 0; i < 50; i++) {
            PortalScheduler.schedule(boti);
            PortalScheduler.schedule(soto);
        }
        assertEquals(2, PortalScheduler.pendingCountForTest());
        assertEquals(100L, PortalScheduler.scheduledTotal());
    }

    @Test
    void outcomeCulledIsDistinctFromIdle() {
        assertTrue(PortalPerfStats.Outcome.valueOf("CULLED") != PortalPerfStats.Outcome.IDLE);
        assertEquals("culled", PortalPerfStats.Outcome.CULLED.label());
    }
}
