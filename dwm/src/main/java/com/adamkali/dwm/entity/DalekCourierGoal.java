package com.adamkali.dwm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Couriers run a line: the post followed by the work spots, walking to the far end and back and
 * pausing briefly at each stop. Used to link trench checkpoints.
 */
public class DalekCourierGoal extends Goal {
    private static final int REPATH_INTERVAL = 20;

    private final DalekEntity dalek;
    private final double speed;
    private final int pauseMin;
    private final int pauseMax;
    private int index;
    private int direction = 1;
    private int pauseLeft;

    public DalekCourierGoal(DalekEntity dalek, double speed, int pauseMin, int pauseMax) {
        this.dalek = dalek;
        this.speed = speed;
        this.pauseMin = pauseMin;
        this.pauseMax = pauseMax;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = dalek.getTarget();
        return dalek.getJob() == DalekJob.COURIER && dalek.getPost() != null && !dalek.isFlying()
                && (target == null || !target.isAlive());
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        pauseLeft = 0;
    }

    @Override
    public void stop() {
        dalek.setOnDuty(false);
        dalek.getNavigation().stop();
    }

    @Override
    public void tick() {
        List<BlockPos> stops = new ArrayList<>();
        stops.add(dalek.getPost());
        stops.addAll(dalek.getWorkSpots());
        index = Math.min(index, stops.size() - 1);
        BlockPos stop = stops.get(index);
        if (pauseLeft > 0) {
            if (--pauseLeft == 0) {
                dalek.setOnDuty(false);
                int[] leg = DalekGarrisonLogic.nextCourierLeg(index, direction, stops.size());
                index = leg[0];
                direction = leg[1];
            }
            return;
        }
        double x = stop.getX() + 0.5;
        double z = stop.getZ() + 0.5;
        double distSq = dalek.distanceToSqr(x, stop.getY(), z);
        if (DalekGarrisonLogic.hasArrived(distSq, DalekGarrisonLogic.ARRIVE_DISTANCE + 1.0)) {
            dalek.getNavigation().stop();
            dalek.setOnDuty(true);
            pauseLeft = DalekGarrisonLogic.pauseTicks(pauseMin, pauseMax, dalek.getRandom().nextFloat());
            return;
        }
        if (dalek.tickCount % REPATH_INTERVAL == 0 || dalek.getNavigation().isDone()) {
            dalek.getNavigation().moveTo(x, stop.getY(), z, speed);
        }
    }
}
