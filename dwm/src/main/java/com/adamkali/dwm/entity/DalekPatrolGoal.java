package com.adamkali.dwm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.EnumSet;
import java.util.List;

/**
 * Patrollers walk a ring of waypoints around their post, pausing at each one. Waypoints that are
 * unreachable on foot are skipped.
 */
public class DalekPatrolGoal extends Goal {
    private static final int REPATH_INTERVAL = 20;

    private final DalekEntity dalek;
    private final double speed;
    private final int pauseMin;
    private final int pauseMax;
    private int index;
    private int pauseLeft;
    private BlockPos destination;

    public DalekPatrolGoal(DalekEntity dalek, double speed, int pauseMin, int pauseMax) {
        this.dalek = dalek;
        this.speed = speed;
        this.pauseMin = pauseMin;
        this.pauseMax = pauseMax;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = dalek.getTarget();
        return dalek.getJob() == DalekJob.PATROL && dalek.getPost() != null && !dalek.isFlying()
                && (target == null || !target.isAlive());
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        destination = null;
        pauseLeft = 0;
    }

    @Override
    public void stop() {
        dalek.setOnDuty(false);
        dalek.getNavigation().stop();
        destination = null;
    }

    @Override
    public void tick() {
        if (pauseLeft > 0) {
            pauseLeft--;
            if (pauseLeft == 0) {
                dalek.setOnDuty(false);
                destination = null;
            }
            return;
        }
        if (destination == null) {
            destination = pickDestination();
            if (destination == null) {
                return;
            }
        }
        double distSq = dalek.distanceToSqr(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5);
        if (DalekGarrisonLogic.hasArrived(distSq, DalekGarrisonLogic.ARRIVE_DISTANCE + 1.0)) {
            dalek.getNavigation().stop();
            dalek.setOnDuty(true);
            pauseLeft = DalekGarrisonLogic.pauseTicks(pauseMin, pauseMax, dalek.getRandom().nextFloat());
            return;
        }
        if (dalek.tickCount % REPATH_INTERVAL == 0 || dalek.getNavigation().isDone()) {
            dalek.getNavigation().moveTo(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5, speed);
        }
    }

    private BlockPos pickDestination() {
        BlockPos post = dalek.getPost();
        List<BlockPos> ring = DalekGarrisonLogic.patrolWaypoints(
                post, dalek.getPatrolRadius(), DalekGarrisonLogic.PATROL_WAYPOINT_COUNT);
        for (int tries = 0; tries < ring.size(); tries++) {
            BlockPos flat = ring.get(index % ring.size());
            index = DalekGarrisonLogic.nextWaypoint(index, ring.size());
            // Try the post's own level first (platforms, enclosed spaces), then the terrain surface.
            if (dalek.getNavigation().createPath(flat, 0) != null) {
                return flat;
            }
            BlockPos surface = dalek.level().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, flat);
            if (dalek.getNavigation().createPath(surface, 0) != null) {
                return surface;
            }
        }
        return null;
    }
}
