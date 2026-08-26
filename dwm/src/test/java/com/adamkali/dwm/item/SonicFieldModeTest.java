package com.adamkali.dwm.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SonicFieldModeTest {
    @Test
    void step_wrapsForwardThroughAllModes() {
        assertEquals(SonicFieldMode.SHATTER, SonicFieldMode.OPEN.step(1));
        assertEquals(SonicFieldMode.PRIME, SonicFieldMode.SHATTER.step(1));
        assertEquals(SonicFieldMode.DISRUPT, SonicFieldMode.PRIME.step(1));
        assertEquals(SonicFieldMode.SHEAR, SonicFieldMode.DISRUPT.step(1));
        assertEquals(SonicFieldMode.OPEN, SonicFieldMode.SHEAR.step(1));
    }

    @Test
    void step_wrapsBackwardThroughAllModes() {
        assertEquals(SonicFieldMode.SHEAR, SonicFieldMode.OPEN.step(-1));
        assertEquals(SonicFieldMode.DISRUPT, SonicFieldMode.SHEAR.step(-1));
        assertEquals(SonicFieldMode.PRIME, SonicFieldMode.DISRUPT.step(-1));
        assertEquals(SonicFieldMode.SHATTER, SonicFieldMode.PRIME.step(-1));
        assertEquals(SonicFieldMode.OPEN, SonicFieldMode.SHATTER.step(-1));
    }

    @Test
    void signedOffset_wrapsAcrossCycleEnds() {
        assertEquals(-1, SonicFieldMode.signedOffset(SonicFieldMode.OPEN, SonicFieldMode.SHEAR));
        assertEquals(1, SonicFieldMode.signedOffset(SonicFieldMode.SHEAR, SonicFieldMode.OPEN));
    }

    @Test
    void signedOffset_isZeroForSameMode() {
        assertEquals(0, SonicFieldMode.signedOffset(SonicFieldMode.PRIME, SonicFieldMode.PRIME));
    }

    @Test
    void signedOffset_usesShortestPath() {
        assertEquals(1, SonicFieldMode.signedOffset(SonicFieldMode.OPEN, SonicFieldMode.SHATTER));
        assertEquals(-1, SonicFieldMode.signedOffset(SonicFieldMode.SHATTER, SonicFieldMode.OPEN));
        assertEquals(2, SonicFieldMode.signedOffset(SonicFieldMode.OPEN, SonicFieldMode.PRIME));
    }
}
