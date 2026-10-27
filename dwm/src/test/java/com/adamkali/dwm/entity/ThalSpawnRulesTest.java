package com.adamkali.dwm.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.world.entity.EntitySpawnReason;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThalSpawnRulesTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void naturalAndChunkGenerationSpawnsAreRejectedOutsideSettlements() {
        assertFalse(ThalSpawnRules.allowsNaturalSpawn(EntitySpawnReason.NATURAL, false));
        assertFalse(ThalSpawnRules.allowsNaturalSpawn(EntitySpawnReason.CHUNK_GENERATION, false));
    }

    @Test
    void naturalAndChunkGenerationSpawnsAreAllowedInsideSettlements() {
        assertTrue(ThalSpawnRules.allowsNaturalSpawn(EntitySpawnReason.NATURAL, true));
        assertTrue(ThalSpawnRules.allowsNaturalSpawn(EntitySpawnReason.CHUNK_GENERATION, true));
    }

    @Test
    void deliberateSpawnReasonsAreAlwaysAllowed() {
        for (EntitySpawnReason reason : EntitySpawnReason.values()) {
            if (reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION) {
                continue;
            }
            assertTrue(ThalSpawnRules.allowsNaturalSpawn(reason, false), reason.name());
            assertTrue(ThalSpawnRules.allowsNaturalSpawn(reason, true), reason.name());
        }
    }
}
