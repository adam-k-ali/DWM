package com.adamkali.dwm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekFlightTest {
    @Test
    void landsWhenThereIsNoTarget() {
        assertFalse(DalekFlight.shouldFly(false, 6.0, false, 10.0, true, false));
    }

    @Test
    void fliesWhenTargetIsSignificantlyAbove() {
        assertTrue(DalekFlight.shouldFly(true, 3.0, true, 2.0, false, true));
    }

    @Test
    void staysGroundedWhenTargetIsReachableOnFoot() {
        assertFalse(DalekFlight.shouldFly(true, 1.0, true, 8.0, false, true));
    }

    @Test
    void fliesWhenGroundPathIsMissingAndTargetIsFar() {
        assertTrue(DalekFlight.shouldFly(true, 0.5, false, 6.0, false, true));
    }

    @Test
    void staysGroundedWhenPathIsMissingButTargetIsClose() {
        assertFalse(DalekFlight.shouldFly(true, 0.5, false, 2.0, false, true));
    }

    @Test
    void staysFlyingUntilLandingOnGround() {
        assertTrue(DalekFlight.shouldFly(true, 1.0, true, 3.0, true, false));
        assertFalse(DalekFlight.shouldFly(true, 1.0, true, 3.0, true, true));
    }
}
