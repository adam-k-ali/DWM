package com.adamkali.dwm.entity.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Optional;

/**
 * Priority + optional {@code can_use} wrapper around a typed {@link EntityGoal}.
 */
public record EntityGoalBinding(int priority, Optional<GoalCondition> canUse, EntityGoal goal) {
    public static final Codec<EntityGoalBinding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("priority").forGetter(EntityGoalBinding::priority),
            GoalCondition.CODEC.optionalFieldOf("can_use").forGetter(EntityGoalBinding::canUse),
            EntityGoal.MAP_CODEC.forGetter(EntityGoalBinding::goal)
    ).apply(instance, EntityGoalBinding::new));

    public Goal create(Mob mob) {
        Goal created = goal.create(mob);
        return canUse.map(condition -> (Goal) new ConditionalGoal(mob, created, condition)).orElse(created);
    }
}
