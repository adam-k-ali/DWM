package com.adamkali.dwm.entity;

/**
 * Pure flight-mode decision helper. No world access — unit-tested in isolation.
 */
public final class DalekFlight {
    public static final double FLY_Y_DELTA = 2.5;
    public static final double MIN_DISTANCE_FOR_MISSING_PATH = 4.0;

    private DalekFlight() {
    }

    /**
     * @param hasTarget whether a living attack target exists
     * @param targetYDelta target Y minus self Y
     * @param hasGroundPath whether ground navigation currently has a path to the target
     * @param horizontalDistance planar distance to the target
     * @param currentlyFlying whether the Dalek is already in flying mode
     * @param onGround whether the Dalek is standing on a block
     */
    public static boolean shouldFly(
            boolean hasTarget,
            double targetYDelta,
            boolean hasGroundPath,
            double horizontalDistance,
            boolean currentlyFlying,
            boolean onGround
    ) {
        if (!hasTarget) {
            return false;
        }
        if (targetYDelta > FLY_Y_DELTA) {
            return true;
        }
        if (!hasGroundPath && horizontalDistance > MIN_DISTANCE_FOR_MISSING_PATH) {
            return true;
        }
        return currentlyFlying && !onGround;
    }
}
