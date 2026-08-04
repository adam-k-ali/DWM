package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.interior.TardisDimensions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PlanetLocatorLogicTest {
    private static RegistryKey<World> world(String namespace, String path) {
        return RegistryKey.of(RegistryKeys.WORLD, Identifier.of(namespace, path));
    }

    @Test
    void filterTravelDimensions_excludesTardisAndSorts() {
        List<RegistryKey<World>> input = List.of(
                world("minecraft", "the_end"),
                TardisDimensions.TARDIS_WORLD_KEY,
                world("minecraft", "overworld"),
                world("minecraft", "the_nether")
        );

        List<RegistryKey<World>> filtered = PlanetLocatorLogic.filterTravelDimensions(input);

        assertEquals(3, filtered.size());
        assertEquals("minecraft:overworld", filtered.get(0).getValue().toString());
        assertEquals("minecraft:the_end", filtered.get(1).getValue().toString());
        assertEquals("minecraft:the_nether", filtered.get(2).getValue().toString());
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
        List<RegistryKey<World>> dims = List.of(
                World.OVERWORLD,
                World.NETHER,
                World.END
        );

        assertEquals(
                Optional.of(World.OVERWORLD.getValue()),
                PlanetLocatorLogic.nextDimension(null, dims)
        );
        assertEquals(
                Optional.of(World.NETHER.getValue()),
                PlanetLocatorLogic.nextDimension("minecraft:overworld", dims)
        );
        assertEquals(
                Optional.of(World.END.getValue()),
                PlanetLocatorLogic.nextDimension("minecraft:the_nether", dims)
        );
        assertEquals(
                Optional.of(World.OVERWORLD.getValue()),
                PlanetLocatorLogic.nextDimension("minecraft:the_end", dims)
        );
        assertEquals(
                Optional.of(World.OVERWORLD.getValue()),
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
