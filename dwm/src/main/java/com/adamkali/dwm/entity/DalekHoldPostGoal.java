package com.adamkali.dwm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Sentries and lookouts walk (or fly, if the post is above them or unreachable on foot) back to
 * their post when idle, then stand watch: facing the post yaw and sweeping their eyestalk.
 */
public class DalekHoldPostGoal extends Goal {
    private static final int REPATH_INTERVAL = 10;
    private static final double CLOSE_DISTANCE_SQ = 36.0;

    private final DalekEntity dalek;
    private final double speed;
    private int scanTick;

    public DalekHoldPostGoal(DalekEntity dalek, double speed) {
        this.dalek = dalek;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = dalek.getTarget();
        return dalek.getJob().holdsPost() && dalek.getPost() != null && (target == null || !target.isAlive());
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void stop() {
        dalek.setOnDuty(false);
        dalek.getNavigation().stop();
        if (dalek.isFlying()) {
            dalek.setFlying(false);
        }
    }

    @Override
    public void tick() {
        BlockPos post = dalek.getPost();
        if (post == null) {
            return;
        }
        double x = post.getX() + 0.5;
        double z = post.getZ() + 0.5;
        double distSq = dalek.distanceToSqr(x, post.getY(), z);
        if (DalekGarrisonLogic.hasArrived(distSq, DalekGarrisonLogic.ARRIVE_DISTANCE)) {
            dalek.getNavigation().stop();
            if (dalek.isFlying()) {
                dalek.setFlying(false);
            }
            dalek.setOnDuty(true);
            float yaw = dalek.getPostYaw() + DalekGarrisonLogic.scanYawOffset(scanTick++);
            dalek.setYRot(yaw);
            dalek.setYHeadRot(yaw);
            dalek.yBodyRot = yaw;
            return;
        }
        dalek.setOnDuty(false);
        scanTick = 0;
        if (!dalek.isFlying()
                && DalekGarrisonLogic.shouldFlyToPost(post.getY() - dalek.getY(), dalek.hasGroundPathTo(post))) {
            dalek.setFlying(true);
        }
        if (dalek.isFlying() && distSq < CLOSE_DISTANCE_SQ) {
            // Flying path nodes stall a few blocks short; steer straight in for the last stretch.
            dalek.getMoveControl().setWantedPosition(x, post.getY(), z, speed);
        } else if (dalek.tickCount % REPATH_INTERVAL == 0 || dalek.getNavigation().isDone()) {
            dalek.getNavigation().moveTo(x, post.getY(), z, speed);
        }
    }
}
