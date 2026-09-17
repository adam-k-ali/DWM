package com.adamkali.dwm.entity;

import net.minecraft.world.entity.EntitySpawnReason;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekPatrolLogicTest {
    @Test
    void spawnConstantsMatchPatrolContract() {
        assertEquals(100, DalekPatrolLogic.STANDARD_SPAWN_WEIGHT);
        assertEquals(25, DalekPatrolLogic.SPARSE_SPAWN_WEIGHT);
        assertEquals(1, DalekPatrolLogic.STANDARD_MIN_COUNT);
        assertEquals(3, DalekPatrolLogic.STANDARD_MAX_COUNT);
        assertEquals(1, DalekPatrolLogic.SPARSE_MIN_COUNT);
        assertEquals(2, DalekPatrolLogic.SPARSE_MAX_COUNT);
        assertEquals(0.7, DalekPatrolLogic.STANDARD_SPAWN_CHARGE, 1e-9);
        assertEquals(0.15, DalekPatrolLogic.STANDARD_SPAWN_ENERGY_BUDGET, 1e-9);
        assertEquals(1.0, DalekPatrolLogic.SPARSE_SPAWN_CHARGE, 1e-9);
        assertEquals(0.12, DalekPatrolLogic.SPARSE_SPAWN_ENERGY_BUDGET, 1e-9);
        assertTrue(DalekPatrolLogic.SPARSE_SPAWN_WEIGHT < DalekPatrolLogic.STANDARD_SPAWN_WEIGHT);
        assertTrue(DalekPatrolLogic.SPARSE_MAX_COUNT < DalekPatrolLogic.STANDARD_MAX_COUNT);
        assertTrue(DalekPatrolLogic.STANDARD_MIN_COUNT <= DalekPatrolLogic.STANDARD_MAX_COUNT);
        assertTrue(DalekPatrolLogic.SPARSE_MIN_COUNT <= DalekPatrolLogic.SPARSE_MAX_COUNT);
    }

    @Test
    void worldPopulationIsNaturalOrChunkGenerationOnly() {
        assertTrue(DalekPatrolLogic.isWorldPopulationReason(EntitySpawnReason.NATURAL));
        assertTrue(DalekPatrolLogic.isWorldPopulationReason(EntitySpawnReason.CHUNK_GENERATION));
        assertFalse(DalekPatrolLogic.isWorldPopulationReason(EntitySpawnReason.SPAWN_ITEM_USE));
        assertFalse(DalekPatrolLogic.isWorldPopulationReason(EntitySpawnReason.COMMAND));
        assertFalse(DalekPatrolLogic.isWorldPopulationReason(EntitySpawnReason.STRUCTURE));
    }

    @Test
    void naturalSpawnIsSkaroOnlyWhileEggsRemainDimensionAgnostic() {
        assertTrue(DalekPatrolLogic.allowsWorldPopulation(EntitySpawnReason.NATURAL, true));
        assertFalse(DalekPatrolLogic.allowsWorldPopulation(EntitySpawnReason.NATURAL, false));
        assertTrue(DalekPatrolLogic.allowsWorldPopulation(EntitySpawnReason.CHUNK_GENERATION, true));
        assertFalse(DalekPatrolLogic.allowsWorldPopulation(EntitySpawnReason.CHUNK_GENERATION, false));
        assertTrue(DalekPatrolLogic.allowsWorldPopulation(EntitySpawnReason.SPAWN_ITEM_USE, false));
        assertTrue(DalekPatrolLogic.allowsWorldPopulation(EntitySpawnReason.COMMAND, false));
    }

    @Test
    void sharesPlayerTargetWhenIdleSameDimensionAndInRadius() {
        double inRadius = DalekPatrolLogic.TARGET_SHARE_RADIUS * DalekPatrolLogic.TARGET_SHARE_RADIUS;
        assertTrue(DalekPatrolLogic.shouldShareTarget(true, true, inRadius, true));
        assertTrue(DalekPatrolLogic.shouldShareTarget(true, true, 0.0, true));
    }

    @Test
    void doesNotShareWhenBusyDifferentDimensionOutOfRadiusOrNonPlayer() {
        double justOutside = DalekPatrolLogic.TARGET_SHARE_RADIUS * DalekPatrolLogic.TARGET_SHARE_RADIUS + 0.01;
        assertFalse(DalekPatrolLogic.shouldShareTarget(false, true, 1.0, true));
        assertFalse(DalekPatrolLogic.shouldShareTarget(true, false, 1.0, true));
        assertFalse(DalekPatrolLogic.shouldShareTarget(true, true, justOutside, true));
        assertFalse(DalekPatrolLogic.shouldShareTarget(true, true, 1.0, false));
    }
}
