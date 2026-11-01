package com.adamkali.dwm.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.List;

/**
 * A garrison Dalek with a living target hands it to idle garrison Daleks nearby. Lookouts shout
 * further. Wild Daleks ({@link DalekJob#NONE}) are never recruited.
 */
public class DalekRaiseAlarmGoal extends TargetGoal {
    /** Target selectors only evaluate canUse when (tickCount + id) is even, so the phase includes the id. */
    private static final int CHECK_INTERVAL = 20;

    private final DalekEntity dalek;
    private final double radius;
    private final double lookoutRadius;
    private List<DalekEntity> recipients = List.of();

    public DalekRaiseAlarmGoal(DalekEntity dalek, double radius, double lookoutRadius) {
        super(dalek, false);
        this.dalek = dalek;
        this.radius = radius;
        this.lookoutRadius = lookoutRadius;
    }

    @Override
    public boolean canUse() {
        if (dalek.level().isClientSide() || dalek.getJob() == DalekJob.NONE) {
            return false;
        }
        LivingEntity current = dalek.getTarget();
        if (current == null || !current.isAlive() || (dalek.tickCount + dalek.getId()) % CHECK_INTERVAL != 0) {
            return false;
        }
        double alarm = DalekGarrisonLogic.alarmRadius(dalek.getJob(), radius, lookoutRadius);
        recipients = dalek.level().getEntitiesOfClass(
                DalekEntity.class,
                dalek.getBoundingBox().inflate(alarm),
                ally -> ally != dalek && ally.isAlive() && ally.getJob() != DalekJob.NONE
                        && (ally.getTarget() == null || !ally.getTarget().isAlive())
                        && ally.distanceToSqr(dalek) <= alarm * alarm
        );
        this.targetMob = current;
        return !recipients.isEmpty();
    }

    @Override
    public void start() {
        LivingEntity current = dalek.getTarget();
        if (current != null) {
            for (DalekEntity ally : recipients) {
                ally.setTarget(current);
            }
        }
        recipients = List.of();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    /** Vanilla's {@link TargetGoal#stop()} clears the target; the alarm must not drop the caller's own. */
    @Override
    public void stop() {
        recipients = List.of();
    }
}
