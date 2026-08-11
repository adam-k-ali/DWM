package com.adamkali.dwm.tardis.data.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DestinationModeTest {
    @Test
    void fromString_defaultsBlankAndUnknownToBiome() {
        assertEquals(DestinationMode.BIOME, DestinationMode.fromString(null));
        assertEquals(DestinationMode.BIOME, DestinationMode.fromString(""));
        assertEquals(DestinationMode.BIOME, DestinationMode.fromString("   "));
        assertEquals(DestinationMode.BIOME, DestinationMode.fromString("not-a-mode"));
    }

    @Test
    void fromString_parsesKnownModesCaseInsensitive() {
        assertEquals(DestinationMode.BIOME, DestinationMode.fromString("biome"));
        assertEquals(DestinationMode.WAYPOINT, DestinationMode.fromString("WAYPOINT"));
        assertEquals(DestinationMode.PLAYER, DestinationMode.fromString("Player"));
    }
}
