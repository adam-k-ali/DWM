package com.adamkali.dwm.tardis.data.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TardisTravelPhaseTest {
    @Test
    void fromString_defaultsUnknownAndNullToIdle() {
        assertEquals(TardisTravelPhase.IDLE, TardisTravelPhase.fromString(null));
        assertEquals(TardisTravelPhase.IDLE, TardisTravelPhase.fromString(""));
        assertEquals(TardisTravelPhase.IDLE, TardisTravelPhase.fromString("NOPE"));
    }

    @Test
    void fromString_parsesKnownPhases() {
        assertEquals(TardisTravelPhase.DEMATERIALISING, TardisTravelPhase.fromString("DEMATERIALISING"));
        assertEquals(TardisTravelPhase.IN_FLIGHT, TardisTravelPhase.fromString("IN_FLIGHT"));
        assertEquals(TardisTravelPhase.MATERIALISING, TardisTravelPhase.fromString("MATERIALISING"));
    }

    @Test
    void isTraveling_falseOnlyForIdle() {
        assertFalse(TardisTravelPhase.IDLE.isTraveling());
        assertTrue(TardisTravelPhase.DEMATERIALISING.isTraveling());
        assertTrue(TardisTravelPhase.IN_FLIGHT.isTraveling());
        assertTrue(TardisTravelPhase.MATERIALISING.isTraveling());
    }

    @Test
    void awaitsMaterialise_onlyInFlight() {
        assertFalse(TardisTravelPhase.IDLE.awaitsMaterialise());
        assertFalse(TardisTravelPhase.DEMATERIALISING.awaitsMaterialise());
        assertTrue(TardisTravelPhase.IN_FLIGHT.awaitsMaterialise());
        assertFalse(TardisTravelPhase.MATERIALISING.awaitsMaterialise());
    }
}
