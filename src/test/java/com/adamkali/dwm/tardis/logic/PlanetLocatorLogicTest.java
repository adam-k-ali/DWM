package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.interior.TardisDimensions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import static org.junit.jupiter.api.Assertions.*;

class PlanetLocatorLogicTest {
    private static ResourceKey<Level> world(String namespace, String path) {
        return ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(namespace, path));
    }

    @Test
    void filterTravelDimensions_excludesTardisAndSorts() {
        List<ResourceKey<Level>> input = List.of(
                world("minecraft", "the_end"),
                TardisDimensions.TARDIS_WORLD_KEY,
                world("minecraft", "overworld"),
                world("minecraft", "the_nether")
        );

        List<ResourceKey<Level>> filtered = PlanetLocatorLogic.filterTravelDimensions(input);

        assertEquals(3, filtered.size());
        assertEquals("minecraft:overworld", filtered.get(0).identifier().toString());
        assertEquals("minecraft:the_end", filtered.get(1).identifier().toString());
        assertEquals("minecraft:the_nether", filtered.get(2).identifier().toString());
        assertFalse(filtered.contains(TardisDimensions.TARDIS_WORLD_KEY));
    }

    @Test
    void filterTravelDimensions_emptyOrNull() {
        assertTrue(PlanetLocatorLogic.filterTravelDimensions(null).isEmpty());
        assertTrue(PlanetLocatorLogic.filterTravelDimensions(List.of()).isEmpty());
        assertTrue(PlanetLocatorLogic.filterTravelDimensions(List.of(TardisDimensions.TARDIS_WORLD_KEY)).isEmpty());
    }

    @Test
    void nextDimension_wrapsAndHandlesMissingCurrent() {
        List<ResourceKey<Level>> dims = List.of(
                Level.OVERWORLD,
                Level.NETHER,
                Level.END
        );

        assertEquals(
                Optional.of(Level.OVERWORLD.identifier()),
                PlanetLocatorLogic.nextDimension(null, dims)
        );
        assertEquals(
                Optional.of(Level.NETHER.identifier()),
                PlanetLocatorLogic.nextDimension("minecraft:overworld", dims)
        );
        assertEquals(
                Optional.of(Level.END.identifier()),
                PlanetLocatorLogic.nextDimension("minecraft:the_nether", dims)
        );
        assertEquals(
                Optional.of(Level.OVERWORLD.identifier()),
                PlanetLocatorLogic.nextDimension("minecraft:the_end", dims)
        );
        assertEquals(
                Optional.of(Level.OVERWORLD.identifier()),
                PlanetLocatorLogic.nextDimension("minecraft:unknown", dims)
        );
    }

    @Test
    void nextDimension_emptyList_returnsEmpty() {
        assertTrue(PlanetLocatorLogic.nextDimension("minecraft:overworld", List.of()).isEmpty());
        assertTrue(PlanetLocatorLogic.nextDimension(null, List.of()).isEmpty());
        assertTrue(PlanetLocatorLogic.nextDimension(null, null).isEmpty());
    }

    @Test
    void dimensions_nullServer_returnsEmpty() {
        assertTrue(PlanetLocatorLogic.dimensions(null).isEmpty());
    }
}
