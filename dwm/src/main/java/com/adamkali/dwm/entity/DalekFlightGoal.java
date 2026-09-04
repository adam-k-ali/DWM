package com.adamkali.dwm.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Switches the Dalek between ground glide and flying navigation when the
 * attack target is above the chassis or otherwise unreachable on foot.
 * Does not take MOVE/LOOK flags so ranged attack can run while airborne.
 */
public class DalekFlightGoal extends Goal {
    public static final double FLY_Y_DELTA = 2.5;
    public static final double MIN_DISTANCE_FOR_MISSING_PATH = 4.0;

    private final DalekEntity dalek;

    public DalekFlightGoal(DalekEntity dalek) {
        this.dalek = dalek;
    }

    /**
     * @param hasTarget whether a living attack target exists
     * @param targetYDelta target Y minus self Y
     * @param hasGroundPath whether ground navigation currently has a path to the target
     * @param horizontalDistance planar distance to the target
     * @param currentlyFlying whether the Dalek is already in flying mode
     * @param onGround whether the Dalek is standing on a block
     */
    static boolean shouldFly(
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

    @Override
    public boolean canUse() {
        LivingEntity target = dalek.getTarget();
        boolean hasTarget = target != null && target.isAlive();
        double yDelta = hasTarget ? target.getY() - dalek.getY() : 0.0;
        double distance = hasTarget ? dalek.distanceTo(target) : 0.0;
        boolean hasGroundPath = !hasTarget || dalek.hasGroundPathTo(target);
        return shouldFly(
                hasTarget,
                yDelta,
                hasGroundPath,
                distance,
                dalek.isFlying(),
                dalek.onGround()
        );
    }

    @Override
    public void start() {
        dalek.setFlying(true);
    }

    @Override
    public void stop() {
        dalek.setFlying(false);
    }
}
