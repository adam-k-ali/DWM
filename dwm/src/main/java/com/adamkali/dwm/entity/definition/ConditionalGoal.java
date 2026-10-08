package com.adamkali.dwm.entity.definition;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Forwards a vanilla goal while gating {@code canUse}/{@code canContinueToUse} on a JSON condition.
 */
final class ConditionalGoal extends Goal {
    private final Goal delegate;
    private final GoalCondition condition;
    private final Mob mob;

    ConditionalGoal(Mob mob, Goal delegate, GoalCondition condition) {
        this.mob = mob;
        this.delegate = delegate;
        this.condition = condition;
        this.setFlags(EnumSet.copyOf(delegate.getFlags()));
    }

    @Override
    public boolean canUse() {
        return condition.test(mob) && delegate.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return condition.test(mob) && delegate.canContinueToUse();
    }

    @Override
    public boolean isInterruptable() {
        return delegate.isInterruptable();
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return delegate.requiresUpdateEveryTick();
    }

    @Override
    public void tick() {
        delegate.tick();
    }
}
