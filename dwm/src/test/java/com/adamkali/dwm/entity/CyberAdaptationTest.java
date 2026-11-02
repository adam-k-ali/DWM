package com.adamkali.dwm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CyberAdaptationTest {
    private static final String ARROW = "arrow";
    private static final String MELEE = "player_attack";

    @Test
    void firstHitOfATypeLandsInFull() {
        CyberAdaptation adaptation = new CyberAdaptation();
        assertEquals(0.0F, adaptation.reductionFor(ARROW, 0L));
        assertEquals(10.0F, adaptation.adapt(ARROW, 0L, 10.0F), 1.0E-4F);
    }

    @Test
    void repeatedHitsStackOneStepEach() {
        CyberAdaptation adaptation = new CyberAdaptation();
        assertEquals(10.0F, adaptation.adapt(ARROW, 0L, 10.0F), 1.0E-4F);
        assertEquals(8.0F, adaptation.adapt(ARROW, 10L, 10.0F), 1.0E-4F);
        assertEquals(6.0F, adaptation.adapt(ARROW, 20L, 10.0F), 1.0E-4F);
    }

    @Test
    void reductionIsCapped() {
        CyberAdaptation adaptation = new CyberAdaptation();
        for (int i = 0; i < 10; i++) {
            adaptation.recordHit(ARROW, i);
        }
        assertEquals(CyberAdaptation.MAX_REDUCTION, adaptation.reductionFor(ARROW, 10L), 1.0E-4F);
        assertEquals(4.0F, adaptation.adapt(ARROW, 11L, 10.0F), 1.0E-4F);
    }

    @Test
    void typesAreTrackedIndependently() {
        CyberAdaptation adaptation = new CyberAdaptation();
        adaptation.recordHit(ARROW, 0L);
        adaptation.recordHit(ARROW, 1L);
        assertEquals(0.4F, adaptation.reductionFor(ARROW, 2L), 1.0E-4F);
        assertEquals(0.0F, adaptation.reductionFor(MELEE, 2L));
    }

    @Test
    void adaptationDecaysAfterIdleTicks() {
        CyberAdaptation adaptation = new CyberAdaptation();
        adaptation.recordHit(ARROW, 0L);
        assertEquals(CyberAdaptation.STEP, adaptation.reductionFor(ARROW, CyberAdaptation.DECAY_TICKS), 1.0E-4F);
        assertEquals(0.0F, adaptation.reductionFor(ARROW, CyberAdaptation.DECAY_TICKS + 1));
        adaptation.recordHit(ARROW, CyberAdaptation.DECAY_TICKS + 1);
        assertEquals(CyberAdaptation.STEP, adaptation.reductionFor(ARROW, CyberAdaptation.DECAY_TICKS + 2), 1.0E-4F);
    }
}
