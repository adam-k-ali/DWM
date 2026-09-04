package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.world.DWMBiomeTags;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

import static org.junit.jupiter.api.Assertions.*;

class BiomeSelectorLogicTest {
    private static ResourceKey<Biome> biome(String path) {
        return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("minecraft", path));
    }

    @Test
    void tagForDimension_mapsVanillaDimensions() {
        assertEquals(Optional.of(BiomeTags.IS_OVERWORLD), BiomeSelectorLogic.tagForDimension("minecraft:overworld"));
        assertEquals(Optional.of(BiomeTags.IS_NETHER), BiomeSelectorLogic.tagForDimension("minecraft:the_nether"));
        assertEquals(Optional.of(BiomeTags.IS_END), BiomeSelectorLogic.tagForDimension("minecraft:the_end"));
        assertEquals(Optional.of(DWMBiomeTags.IS_GALLIFREY), BiomeSelectorLogic.tagForDimension("dwm:gallifrey"));
        assertEquals(Optional.of(DWMBiomeTags.IS_SKARO), BiomeSelectorLogic.tagForDimension("dwm:skaro"));
        assertTrue(BiomeSelectorLogic.tagForDimension("dwm:tardis").isEmpty());
        assertTrue(BiomeSelectorLogic.tagForDimension(null).isEmpty());
        assertTrue(BiomeSelectorLogic.tagForDimension("").isEmpty());
    }

    @Test
    void nextBiome_wrapsAndHandlesMissingCurrent() {
        List<ResourceKey<Biome>> biomes = List.of(biome("plains"), biome("forest"), biome("desert"));

        assertEquals(
                Optional.of(Identifier.fromNamespaceAndPath("minecraft", "plains")),
                BiomeSelectorLogic.nextBiome(null, biomes)
        );
        assertEquals(
                Optional.of(Identifier.fromNamespaceAndPath("minecraft", "forest")),
                BiomeSelectorLogic.nextBiome("minecraft:plains", biomes)
        );
        assertEquals(
                Optional.of(Identifier.fromNamespaceAndPath("minecraft", "desert")),
                BiomeSelectorLogic.nextBiome("minecraft:forest", biomes)
        );
        assertEquals(
                Optional.of(Identifier.fromNamespaceAndPath("minecraft", "plains")),
                BiomeSelectorLogic.nextBiome("minecraft:desert", biomes)
        );
        assertEquals(
                Optional.of(Identifier.fromNamespaceAndPath("minecraft", "plains")),
                BiomeSelectorLogic.nextBiome("minecraft:jungle", biomes)
        );
    }

    @Test
    void nextBiome_emptyList_returnsEmpty() {
        assertTrue(BiomeSelectorLogic.nextBiome("minecraft:plains", List.of()).isEmpty());
        assertTrue(BiomeSelectorLogic.nextBiome(null, List.of()).isEmpty());
    }
}
