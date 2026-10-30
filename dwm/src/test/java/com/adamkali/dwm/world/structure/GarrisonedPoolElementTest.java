package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.MinecraftTestBootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GarrisonedPoolElementTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void unsunkPiecesKeepVanillaTerrainMatchingHeights() {
        // Template layer 0 sits on the top ground block (surface - 1), so layer 1 stands at the surface.
        assertEquals(63, GarrisonedPoolElement.terrainY(64, 0, 0));
        assertEquals(64, GarrisonedPoolElement.terrainY(64, 1, 0));
    }

    @Test
    void sunkPiecesDropEveryLayerByTheSinkDepth() {
        assertEquals(61, GarrisonedPoolElement.terrainY(64, 0, 2));
        assertEquals(62, GarrisonedPoolElement.terrainY(64, 1, 2));
        // Layer 3 is the ground-level parapet: the first block above the surface.
        assertEquals(64, GarrisonedPoolElement.terrainY(64, 3, 2));
    }
}
