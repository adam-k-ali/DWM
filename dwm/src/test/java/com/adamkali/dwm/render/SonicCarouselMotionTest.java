package com.adamkali.dwm.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SonicCarouselMotionTest {
    @Test
    void value_UsesStableEndpointsAndMidpoint() {
        assertEquals(0.0f, SonicCarouselMotion.value(0.0f, 1.0f, 100L, 100L), 0.0f);
        assertEquals(0.5f, SonicCarouselMotion.value(0.0f, 1.0f, 100L, 180L), 1.0e-4f);
        assertEquals(1.0f, SonicCarouselMotion.value(0.0f, 1.0f, 100L, 300L), 0.0f);
    }

    @Test
    void retarget_SamplesCurrentMotionWithoutJumping() {
        SonicCarouselMotion.Transition transition =
                SonicCarouselMotion.retarget(0.0f, 1.0f, 100L, 2.0f, 180L);
        assertEquals(0.5f, transition.from(), 1.0e-4f);
        assertEquals(2.0f, transition.target(), 0.0f);
        assertEquals(180L, transition.startedAtMs());
    }

    @Test
    void selectedScaleBoost_IsBriefAndSettlesToZero() {
        assertEquals(0.0f, SonicCarouselMotion.selectedScaleBoost(100L, 180L), 0.0f);
        assertTrue(SonicCarouselMotion.selectedScaleBoost(100L, 232L) > 0.0f);
        assertEquals(0.0f, SonicCarouselMotion.selectedScaleBoost(100L, 260L), 0.0f);
    }

    @Test
    void closeDecision_ReleaseCommitsOnlyUnlockedChanges() {
        assertEquals(
                SonicCarouselMotion.CloseDecision.COMMIT,
                SonicCarouselMotion.closeDecision(SonicCarouselMotion.CloseReason.RELEASE, true, true)
        );
        assertEquals(
                SonicCarouselMotion.CloseDecision.CLOSE_UNCHANGED,
                SonicCarouselMotion.closeDecision(SonicCarouselMotion.CloseReason.RELEASE, true, false)
        );
        assertEquals(
                SonicCarouselMotion.CloseDecision.REJECT_LOCKED,
                SonicCarouselMotion.closeDecision(SonicCarouselMotion.CloseReason.RELEASE, false, true)
        );
    }

    @Test
    void closeDecision_EscapeAndForcedCloseAlwaysCancel() {
        assertEquals(
                SonicCarouselMotion.CloseDecision.CANCEL,
                SonicCarouselMotion.closeDecision(SonicCarouselMotion.CloseReason.ESCAPE, true, true)
        );
        assertEquals(
                SonicCarouselMotion.CloseDecision.CANCEL,
                SonicCarouselMotion.closeDecision(SonicCarouselMotion.CloseReason.FORCED, true, true)
        );
    }
}
