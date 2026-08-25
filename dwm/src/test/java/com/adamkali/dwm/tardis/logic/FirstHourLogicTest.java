package com.adamkali.dwm.tardis.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FirstHourLogicTest {
    @Test
    void claimedOverlayKey_matchesLang() {
        assertEquals("dwm.tardis.claimed", FirstHourLogic.CLAIMED_OVERLAY_KEY);
    }

    @Test
    void isSameWorldHop_trueWhenSameDimensionNotSummon() {
        assertTrue(FirstHourLogic.isSameWorldHop(
                "minecraft:overworld",
                "minecraft:overworld",
                false
        ));
    }

    @Test
    void isSameWorldHop_falseForCrossDimension() {
        assertFalse(FirstHourLogic.isSameWorldHop(
                "minecraft:overworld",
                "minecraft:the_nether",
                false
        ));
        assertFalse(FirstHourLogic.isSameWorldHop(
                "minecraft:overworld",
                "dwm:gallifrey",
                false
        ));
    }

    @Test
    void isSameWorldHop_falseForSummonEvenSameWorld() {
        assertFalse(FirstHourLogic.isSameWorldHop(
                "minecraft:overworld",
                "minecraft:overworld",
                true
        ));
    }

    @Test
    void isSameWorldHop_falseForBlankOrNull() {
        assertFalse(FirstHourLogic.isSameWorldHop(null, "minecraft:overworld", false));
        assertFalse(FirstHourLogic.isSameWorldHop("minecraft:overworld", null, false));
        assertFalse(FirstHourLogic.isSameWorldHop("", "minecraft:overworld", false));
        assertFalse(FirstHourLogic.isSameWorldHop("minecraft:overworld", "  ", false));
    }
}
