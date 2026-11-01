package com.adamkali.dwm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Technicians cycle between their post and work spots, pausing at each to tend the station.
 */
public class DalekTendStationGoal extends Goal {
    private static final int REPATH_INTERVAL = 20;

    private final DalekEntity dalek;
    private final double speed;
    private final int pauseMin;
    private final int pauseMax;
    private int index;
    private int pauseLeft;
    private boolean working;

    public DalekTendStationGoal(DalekEntity dalek, double speed, int pauseMin, int pauseMax) {
        this.dalek = dalek;
        this.speed = speed;
        this.pauseMin = pauseMin;
        this.pauseMax = pauseMax;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = dalek.getTarget();
        return dalek.getJob() == DalekJob.TECHNICIAN && dalek.getPost() != null && !dalek.isFlying()
                && (target == null || !target.isAlive());
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        working = false;
        pauseLeft = 0;
    }

    @Override
    public void stop() {
        dalek.setOnDuty(false);
        dalek.getNavigation().stop();
        working = false;
    }

    @Override
    public void tick() {
        List<BlockPos> spots = new ArrayList<>();
        spots.add(dalek.getPost());
        spots.addAll(dalek.getWorkSpots());
        BlockPos spot = spots.get(index % spots.size());
        double x = spot.getX() + 0.5;
        double z = spot.getZ() + 0.5;

        if (working) {
            dalek.getLookControl().setLookAt(x, spot.getY() + 1.0, z);
            if (--pauseLeft <= 0) {
                working = false;
                dalek.setOnDuty(false);
                index = DalekGarrisonLogic.nextWorkSpot(index % spots.size(), spots.size(), dalek.getRandom().nextInt(spots.size()));
            }
            return;
        }
        double distSq = dalek.distanceToSqr(x, spot.getY(), z);
        if (DalekGarrisonLogic.hasArrived(distSq, DalekGarrisonLogic.ARRIVE_DISTANCE + 1.0)) {
            dalek.getNavigation().stop();
            dalek.setOnDuty(true);
            working = true;
            pauseLeft = DalekGarrisonLogic.pauseTicks(pauseMin, pauseMax, dalek.getRandom().nextFloat());
            return;
        }
        if (dalek.tickCount % REPATH_INTERVAL == 0 || dalek.getNavigation().isDone()) {
            dalek.getNavigation().moveTo(x, spot.getY(), z, speed);
        }
    }
}
