package com.adamkali.dwm.tardis.logic;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LandingSiteLogicTest {
    @Test
    void parseBiome_acceptsValidId() {
        Optional<RegistryKey<Biome>> key = LandingSiteLogic.parseBiome("minecraft:plains");
        assertTrue(key.isPresent());
        assertEquals(RegistryKey.of(RegistryKeys.BIOME, Identifier.of("minecraft:plains")), key.get());
    }

    @Test
    void parseBiome_rejectsNullBlankAndInvalid() {
        assertTrue(LandingSiteLogic.parseBiome(null).isEmpty());
        assertTrue(LandingSiteLogic.parseBiome("").isEmpty());
        assertTrue(LandingSiteLogic.parseBiome("   ").isEmpty());
        assertTrue(LandingSiteLogic.parseBiome("not a biome").isEmpty());
    }
}
