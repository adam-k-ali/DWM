package com.adamkali.dwm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekFlightGoalTest {
    private static final double FLY_Y_DELTA = 2.5;
    private static final double MIN_DISTANCE_FOR_MISSING_PATH = 4.0;

    @Test
    void landsWhenThereIsNoTarget() {
        assertFalse(DalekFlightGoal.shouldFly(false, 6.0, false, 10.0, true, false, FLY_Y_DELTA, MIN_DISTANCE_FOR_MISSING_PATH));
    }

    @Test
    void fliesWhenTargetIsSignificantlyAbove() {
        assertTrue(DalekFlightGoal.shouldFly(true, 3.0, true, 2.0, false, true, FLY_Y_DELTA, MIN_DISTANCE_FOR_MISSING_PATH));
    }

    @Test
    void staysGroundedWhenTargetIsReachableOnFoot() {
        assertFalse(DalekFlightGoal.shouldFly(true, 1.0, true, 8.0, false, true, FLY_Y_DELTA, MIN_DISTANCE_FOR_MISSING_PATH));
    }

    @Test
    void fliesWhenGroundPathIsMissingAndTargetIsFar() {
        assertTrue(DalekFlightGoal.shouldFly(true, 0.5, false, 6.0, false, true, FLY_Y_DELTA, MIN_DISTANCE_FOR_MISSING_PATH));
    }

    @Test
    void staysGroundedWhenPathIsMissingButTargetIsClose() {
        assertFalse(DalekFlightGoal.shouldFly(true, 0.5, false, 2.0, false, true, FLY_Y_DELTA, MIN_DISTANCE_FOR_MISSING_PATH));
    }

    @Test
    void staysFlyingUntilLandingOnGround() {
        assertTrue(DalekFlightGoal.shouldFly(true, 1.0, true, 3.0, true, false, FLY_Y_DELTA, MIN_DISTANCE_FOR_MISSING_PATH));
        assertFalse(DalekFlightGoal.shouldFly(true, 1.0, true, 3.0, true, true, FLY_Y_DELTA, MIN_DISTANCE_FOR_MISSING_PATH));
    }
}
