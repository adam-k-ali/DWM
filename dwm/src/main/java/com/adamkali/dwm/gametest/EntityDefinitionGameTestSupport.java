package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.definition.EntityDefinition;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import com.adamkali.dwm.entity.definition.EntityGoalBinding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shared assertions proving JSON entity definitions reach live mobs.
 */
final class EntityDefinitionGameTestSupport {
    private EntityDefinitionGameTestSupport() {
    }

    static <T extends Mob> T spawnOnGrass(GameTestHelper context, EntityType<T> type) {
        // A wide floor so path-finding goals (panic, follow) have somewhere to walk.
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                context.setBlock(new BlockPos(x, 1, z), DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());
            }
        }
        BlockPos grassRel = new BlockPos(4, 1, 4);
        T mob = context.spawn(type, grassRel.above());
        if (mob == null || !mob.isAlive()) {
            throw new AssertionError("Expected a living " + type + " after spawn");
        }
        return mob;
    }

    static EntityDefinition definitionOf(Mob mob) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        return EntityDefinitions.get(id)
                .orElseThrow(() -> new AssertionError("Missing entity definition for " + id));
    }

    static void assertAttributesMatch(Mob mob, EntityDefinition definition) {
        definition.attributes().forEach((attributeId, expected) -> {
            var holder = BuiltInRegistries.ATTRIBUTE.get(attributeId)
                    .orElseThrow(() -> new AssertionError("Unknown attribute " + attributeId));
            double actual = mob.getAttributeBaseValue(holder);
            if (Math.abs(actual - expected) > 1.0E-6) {
                throw new AssertionError(definition.entity() + " " + attributeId + ": expected " + expected + " but was " + actual);
            }
        });
    }

    static void assertGoalsMatch(Mob mob, EntityDefinition definition) {
        assertSelectorMatches(definition.entity(), "goals", mob, mob.goalSelector, definition.goals());
        assertSelectorMatches(definition.entity(), "targets", mob, mob.targetSelector, definition.targets());
    }

    static boolean isRunning(GoalSelector selector, Class<?> goalType) {
        return selector.getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.isRunning() && goalType.isInstance(wrapped.getGoal()));
    }

    private static void assertSelectorMatches(Identifier entity, String label, Mob mob, GoalSelector selector,
                                              List<EntityGoalBinding> expectedBindings) {
        List<String> expected = new ArrayList<>();
        for (EntityGoalBinding binding : expectedBindings) {
            expected.add(binding.priority() + ":" + binding.create(mob).getClass().getName());
        }
        List<String> actual = new ArrayList<>();
        for (WrappedGoal wrapped : selector.getAvailableGoals()) {
            actual.add(wrapped.getPriority() + ":" + wrapped.getGoal().getClass().getName());
        }
        expected.sort(Comparator.naturalOrder());
        actual.sort(Comparator.naturalOrder());
        if (!expected.equals(actual)) {
            throw new AssertionError(entity + " " + label + " mismatch: expected " + expected + " but was " + actual);
        }
    }
}
