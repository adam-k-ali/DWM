package com.adamkali.dwm.entity.definition;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityGoalTargetTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void playerUsesPlayerClassWithoutTypeSelector() {
        EntityGoal.TargetSpec spec = EntityGoal.resolveTarget(Identifier.withDefaultNamespace("player"));
        assertEquals(Player.class, spec.searchClass());
        assertNull(spec.typeSelector());
    }

    @Test
    void registeredEntityTypeSearchesLivingEntitiesFilteredByType() {
        EntityGoal.TargetSpec spec = EntityGoal.resolveTarget(Identifier.withDefaultNamespace("zombie"));
        assertEquals(LivingEntity.class, spec.searchClass());
        assertNotNull(spec.typeSelector());
    }

    @Test
    void unknownEntityTypeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> EntityGoal.resolveTarget(Identifier.fromNamespaceAndPath("dwm", "no_such_entity")));
    }
}
