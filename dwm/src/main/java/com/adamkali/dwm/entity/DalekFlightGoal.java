package com.adamkali.dwm.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Switches the Dalek between ground glide and flying navigation when the
 * attack target is above the chassis or otherwise unreachable on foot.
 * Does not take MOVE/LOOK flags so ranged attack can run while airborne.
 */
public class DalekFlightGoal extends Goal {
    private final DalekEntity dalek;
    private final double flyYDelta;
    private final double minDistanceForMissingPath;

    public DalekFlightGoal(DalekEntity dalek, double flyYDelta, double minDistanceForMissingPath) {
        this.dalek = dalek;
        this.flyYDelta = flyYDelta;
        this.minDistanceForMissingPath = minDistanceForMissingPath;
    }

    /**
     * @param hasTarget whether a living attack target exists
     * @param targetYDelta target Y minus self Y
     * @param hasGroundPath whether ground navigation currently has a path to the target
     * @param horizontalDistance planar distance to the target
     * @param currentlyFlying whether the Dalek is already in flying mode
     * @param onGround whether the Dalek is standing on a block
     * @param flyYDelta Y delta above which the Dalek takes off
     * @param minDistanceForMissingPath distance beyond which a missing ground path triggers flight
     */
    static boolean shouldFly(
            boolean hasTarget,
            double targetYDelta,
            boolean hasGroundPath,
            double horizontalDistance,
            boolean currentlyFlying,
            boolean onGround,
            double flyYDelta,
            double minDistanceForMissingPath
    ) {
        if (!hasTarget) {
            return false;
        }
        if (targetYDelta > flyYDelta) {
            return true;
        }
        if (!hasGroundPath && horizontalDistance > minDistanceForMissingPath) {
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
                dalek.onGround(),
                flyYDelta,
                minDistanceForMissingPath
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
