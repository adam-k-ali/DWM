package com.adamkali.dwm.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Idle Daleks copy a nearby ally's living player target within the patrol radius.
 * Server-authoritative; uses vanilla {@code setTarget} via {@link TargetGoal}.
 */
public class DalekShareTargetGoal extends TargetGoal {
    private final DalekEntity dalek;

    public DalekShareTargetGoal(DalekEntity dalek) {
        super(dalek, false);
        this.dalek = dalek;
    }

    @Override
    public boolean canUse() {
        if (dalek.level().isClientSide()) {
            return false;
        }
        LivingEntity current = dalek.getTarget();
        boolean selfIdle = current == null || !current.isAlive();
        if (!selfIdle) {
            return false;
        }

        AABB search = dalek.getBoundingBox().inflate(DalekPatrolLogic.TARGET_SHARE_RADIUS);
        List<DalekEntity> allies = dalek.level().getEntitiesOfClass(
                DalekEntity.class,
                search,
                ally -> ally != dalek && ally.isAlive()
        );
        for (DalekEntity ally : allies) {
            LivingEntity allyTarget = ally.getTarget();
            boolean allyHasLivingPlayerTarget = allyTarget instanceof Player && allyTarget.isAlive();
            if (DalekPatrolLogic.shouldShareTarget(
                    true,
                    dalek.level().dimension().equals(ally.level().dimension()),
                    dalek.distanceToSqr(ally),
                    allyHasLivingPlayerTarget
            )) {
                this.targetMob = allyTarget;
                return true;
            }
        }
        return false;
    }
}
