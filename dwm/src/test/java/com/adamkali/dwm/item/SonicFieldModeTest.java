package com.adamkali.dwm.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SonicFieldModeTest {
    @Test
    void step_wrapsForwardThroughAllModes() {
        assertEquals(SonicFieldMode.SHATTER, SonicFieldMode.OPEN.step(1));
        assertEquals(SonicFieldMode.PRIME, SonicFieldMode.SHATTER.step(1));
        assertEquals(SonicFieldMode.DISRUPT, SonicFieldMode.PRIME.step(1));
        assertEquals(SonicFieldMode.SHEAR, SonicFieldMode.DISRUPT.step(1));
        assertEquals(SonicFieldMode.SEAL, SonicFieldMode.SHEAR.step(1));
        assertEquals(SonicFieldMode.SCAN, SonicFieldMode.SEAL.step(1));
        assertEquals(SonicFieldMode.PING, SonicFieldMode.SCAN.step(1));
        assertEquals(SonicFieldMode.OPEN, SonicFieldMode.PING.step(1));
    }

    @Test
    void step_wrapsBackwardThroughAllModes() {
        assertEquals(SonicFieldMode.PING, SonicFieldMode.OPEN.step(-1));
        assertEquals(SonicFieldMode.SCAN, SonicFieldMode.PING.step(-1));
        assertEquals(SonicFieldMode.SEAL, SonicFieldMode.SCAN.step(-1));
        assertEquals(SonicFieldMode.SHEAR, SonicFieldMode.SEAL.step(-1));
        assertEquals(SonicFieldMode.DISRUPT, SonicFieldMode.SHEAR.step(-1));
        assertEquals(SonicFieldMode.PRIME, SonicFieldMode.DISRUPT.step(-1));
        assertEquals(SonicFieldMode.SHATTER, SonicFieldMode.PRIME.step(-1));
        assertEquals(SonicFieldMode.OPEN, SonicFieldMode.SHATTER.step(-1));
    }

    @Test
    void signedOffset_wrapsAcrossCycleEnds() {
        assertEquals(-1, SonicFieldMode.signedOffset(SonicFieldMode.OPEN, SonicFieldMode.PING));
        assertEquals(1, SonicFieldMode.signedOffset(SonicFieldMode.PING, SonicFieldMode.OPEN));
        assertEquals(4, SonicFieldMode.signedOffset(SonicFieldMode.OPEN, SonicFieldMode.SHEAR));
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
        assertEquals(-3, SonicFieldMode.signedOffset(SonicFieldMode.OPEN, SonicFieldMode.SEAL));
        assertEquals(-2, SonicFieldMode.signedOffset(SonicFieldMode.OPEN, SonicFieldMode.SCAN));
    }

    @Test
    void cycleOrder_isDeclarationOrderWithEightModes() {
        SonicFieldMode[] order = SonicFieldMode.cycleOrder();
        assertEquals(8, order.length);
        assertEquals(SonicFieldMode.OPEN, order[0]);
        assertEquals(SonicFieldMode.SEAL, order[5]);
        assertEquals(SonicFieldMode.SCAN, order[6]);
        assertEquals(SonicFieldMode.PING, order[7]);
    }

    @Test
    void tardisModes_areSealScanPing() {
        assertTrue(SonicFieldMode.SEAL.isTardisMode());
        assertTrue(SonicFieldMode.SCAN.isTardisMode());
        assertTrue(SonicFieldMode.PING.isTardisMode());
        assertTrue(!SonicFieldMode.OPEN.isTardisMode());
        assertEquals("dwm.sonic.recipe_hint.tardis_pair", SonicFieldMode.SEAL.recipeHintKey());
        assertEquals("dwm.sonic.recipe_hint.tardis_pair", SonicFieldMode.SCAN.recipeHintKey());
        assertEquals("dwm.sonic.recipe_hint.tardis_pair", SonicFieldMode.PING.recipeHintKey());
    }
}
