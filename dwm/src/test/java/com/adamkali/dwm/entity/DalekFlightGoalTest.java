package com.adamkali.dwm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekFlightGoalTest {
    @Test
    void landsWhenThereIsNoTarget() {
        assertFalse(DalekFlightGoal.shouldFly(false, 6.0, false, 10.0, true, false));
    }

    @Test
    void fliesWhenTargetIsSignificantlyAbove() {
        assertTrue(DalekFlightGoal.shouldFly(true, 3.0, true, 2.0, false, true));
    }

    @Test
    void staysGroundedWhenTargetIsReachableOnFoot() {
        assertFalse(DalekFlightGoal.shouldFly(true, 1.0, true, 8.0, false, true));
    }

    @Test
    void fliesWhenGroundPathIsMissingAndTargetIsFar() {
        assertTrue(DalekFlightGoal.shouldFly(true, 0.5, false, 6.0, false, true));
    }

    @Test
    void staysGroundedWhenPathIsMissingButTargetIsClose() {
        assertFalse(DalekFlightGoal.shouldFly(true, 0.5, false, 2.0, false, true));
    }

    @Test
    void staysFlyingUntilLandingOnGround() {
        assertTrue(DalekFlightGoal.shouldFly(true, 1.0, true, 3.0, true, false));
        assertFalse(DalekFlightGoal.shouldFly(true, 1.0, true, 3.0, true, true));
    }
}
