package com.adamkali.dwm.render;

import com.adamkali.dwm.item.SonicFieldMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SonicCarouselLayoutTest {
    @Test
    void slot_CentersFullSizeSelectionOnRequestedPoint() {
        SonicCarouselLayout.SlotGeometry slot = SonicCarouselLayout.slot(0.0f, 160, 100, 0.0f);
        assertEquals(144, slot.slotX());
        assertEquals(100, slot.slotY());
        assertEquals(32, slot.slotSize());
        assertEquals(152, slot.iconX());
        assertEquals(108, slot.iconY());
        assertEquals(16, slot.iconSize());
    }

    @Test
    void slot_UsesContinuousPositionAndDepthScale() {
        SonicCarouselLayout.SlotGeometry halfway = SonicCarouselLayout.slot(0.5f, 160, 100, 0.0f);
        SonicCarouselLayout.SlotGeometry far = SonicCarouselLayout.slot(2.0f, 160, 100, 0.0f);
        assertTrue(halfway.slotX() > 144);
        assertTrue(halfway.slotSize() < 32);
        assertTrue(halfway.slotSize() > far.slotSize());
    }

    @Test
    void slot_KeepsScaledIconCenteredAndContainedAtEveryDepth() {
        for (float offset : new float[] {0.0f, 0.5f, 1.0f, 2.0f}) {
            SonicCarouselLayout.SlotGeometry slot =
                    SonicCarouselLayout.slot(offset, 160, 100, offset == 0.0f ? 0.04f : 0.0f);

            assertTrue(slot.iconX() >= slot.slotX());
            assertTrue(slot.iconY() >= slot.slotY());
            assertTrue(slot.iconX() + slot.iconSize() <= slot.slotX() + slot.slotSize());
            assertTrue(slot.iconY() + slot.iconSize() <= slot.slotY() + slot.slotSize());
            assertTrue(Math.abs(
                    (slot.iconX() * 2 + slot.iconSize())
                            - (slot.slotX() * 2 + slot.slotSize())
            ) <= 1);
            assertTrue(Math.abs(
                    (slot.iconY() * 2 + slot.iconSize())
                            - (slot.slotY() * 2 + slot.slotSize())
            ) <= 1);
        }
    }

    @Test
    void visualOffset_KeepsOutgoingWrappedModeCenteredAtTransitionStart() {
        float offset = SonicCarouselLayout.visualOffset(
                SonicFieldMode.SHATTER,
                SonicFieldMode.OPEN,
                0.0f,
                1.0f
        );
        assertEquals(0.0f, offset, 0.0f);
    }

    @Test
    void visualOffset_PreservesForwardPathAcrossRapidThreeStepRetarget() {
        assertEquals(
                0.0f,
                SonicCarouselLayout.visualOffset(
                        SonicFieldMode.DISRUPT,
                        SonicFieldMode.OPEN,
                        0.0f,
                        3.0f
                ),
                0.0f
        );
        assertEquals(
                3.0f,
                SonicCarouselLayout.visualOffset(
                        SonicFieldMode.DISRUPT,
                        SonicFieldMode.DISRUPT,
                        0.0f,
                        3.0f
                ),
                0.0f
        );
    }

    @Test
    void indicator_AnchorsInsideTopRightEdge() {
        assertEquals(
                204,
                SonicCarouselLayout.indicatorX(320)
        );
        assertEquals(SonicCarouselLayout.INDICATOR_MARGIN, SonicCarouselLayout.indicatorY(false));
        assertEquals(34, SonicCarouselLayout.indicatorY(true));
        assertEquals(SonicCarouselLayout.INDICATOR_MARGIN, SonicCarouselLayout.indicatorX(80));
    }

    @Test
    void indicator_ShowsOnlyForHeldSonicOutsideCarousel() {
        assertTrue(SonicCarouselLayout.shouldShowIndicator(true, false));
        assertFalse(SonicCarouselLayout.shouldShowIndicator(false, false));
        assertFalse(SonicCarouselLayout.shouldShowIndicator(true, true));
    }
}
