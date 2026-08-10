package com.adamkali.dwm.tardis.logic;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

import static org.junit.jupiter.api.Assertions.*;

class LandingSiteLogicTest {
    @Test
    void parseBiome_acceptsValidId() {
        Optional<ResourceKey<Biome>> key = LandingSiteLogic.parseBiome("minecraft:plains");
        assertTrue(key.isPresent());
        assertEquals(ResourceKey.create(Registries.BIOME, Identifier.parse("minecraft:plains")), key.get());
    }

    @Test
    void parseBiome_rejectsNullBlankAndInvalid() {
        assertTrue(LandingSiteLogic.parseBiome(null).isEmpty());
        assertTrue(LandingSiteLogic.parseBiome("").isEmpty());
        assertTrue(LandingSiteLogic.parseBiome("   ").isEmpty());
        assertTrue(LandingSiteLogic.parseBiome("not a biome").isEmpty());
    }
}
