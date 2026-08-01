package com.adamkali.dwm.tardis.logic;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BiomeSelectorLogicTest {
    private static RegistryKey<Biome> biome(String path) {
        return RegistryKey.of(RegistryKeys.BIOME, Identifier.of("minecraft", path));
    }

    @Test
    void tagForDimension_mapsVanillaDimensions() {
        assertEquals(Optional.of(BiomeTags.IS_OVERWORLD), BiomeSelectorLogic.tagForDimension("minecraft:overworld"));
        assertEquals(Optional.of(BiomeTags.IS_NETHER), BiomeSelectorLogic.tagForDimension("minecraft:the_nether"));
        assertEquals(Optional.of(BiomeTags.IS_END), BiomeSelectorLogic.tagForDimension("minecraft:the_end"));
        assertTrue(BiomeSelectorLogic.tagForDimension("dwm:tardis").isEmpty());
        assertTrue(BiomeSelectorLogic.tagForDimension(null).isEmpty());
        assertTrue(BiomeSelectorLogic.tagForDimension("").isEmpty());
    }

    @Test
    void nextBiome_wrapsAndHandlesMissingCurrent() {
        List<RegistryKey<Biome>> biomes = List.of(biome("plains"), biome("forest"), biome("desert"));

        assertEquals(
                Optional.of(Identifier.of("minecraft", "plains")),
                BiomeSelectorLogic.nextBiome(null, biomes)
        );
        assertEquals(
                Optional.of(Identifier.of("minecraft", "forest")),
                BiomeSelectorLogic.nextBiome("minecraft:plains", biomes)
        );
        assertEquals(
                Optional.of(Identifier.of("minecraft", "desert")),
                BiomeSelectorLogic.nextBiome("minecraft:forest", biomes)
        );
        assertEquals(
                Optional.of(Identifier.of("minecraft", "plains")),
                BiomeSelectorLogic.nextBiome("minecraft:desert", biomes)
        );
        assertEquals(
                Optional.of(Identifier.of("minecraft", "plains")),
                BiomeSelectorLogic.nextBiome("minecraft:jungle", biomes)
        );
    }

    @Test
    void nextBiome_emptyList_returnsEmpty() {
        assertTrue(BiomeSelectorLogic.nextBiome("minecraft:plains", List.of()).isEmpty());
        assertTrue(BiomeSelectorLogic.nextBiome(null, List.of()).isEmpty());
    }
}
