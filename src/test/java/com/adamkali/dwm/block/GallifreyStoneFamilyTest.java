package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GallifreyStoneFamilyTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void stoneFamilyContainsBuildingAndTerrainBlocks() {
        assertEquals(15, DWMBlocks.GALLIFREY_STONE_FAMILY.size());
        assertEquals(11, DWMBlocks.GALLIFREY_STONE_BUILDING_BLOCKS.size());
        assertTrue(DWMBlocks.GALLIFREY_STONE_FAMILY.containsAll(DWMBlocks.GALLIFREY_STONE_BUILDING_BLOCKS));
        assertTrue(DWMBlocks.GALLIFREY_STONE_FAMILY.contains(DWMBlocks.GALLIFREY_SAND));
        assertTrue(DWMBlocks.GALLIFREY_STONE_FAMILY.contains(DWMBlocks.GALLIFREY_DIRT));
        assertTrue(DWMBlocks.GALLIFREY_STONE_FAMILY.contains(DWMBlocks.GALLIFREY_COARSE_DIRT));
        assertTrue(DWMBlocks.GALLIFREY_STONE_FAMILY.contains(DWMBlocks.GALLIFREY_GRASS_BLOCK));
    }

    @Test
    void buildingBlocksExcludeTerrainOnlyBlocks() {
        assertFalse(DWMBlocks.GALLIFREY_STONE_BUILDING_BLOCKS.contains(DWMBlocks.GALLIFREY_SAND));
        assertFalse(DWMBlocks.GALLIFREY_STONE_BUILDING_BLOCKS.contains(DWMBlocks.GALLIFREY_DIRT));
        assertFalse(DWMBlocks.GALLIFREY_STONE_BUILDING_BLOCKS.contains(DWMBlocks.GALLIFREY_COARSE_DIRT));
        assertFalse(DWMBlocks.GALLIFREY_STONE_BUILDING_BLOCKS.contains(DWMBlocks.GALLIFREY_GRASS_BLOCK));
        assertTrue(DWMBlocks.GALLIFREY_STONE_BUILDING_BLOCKS.contains(DWMBlocks.GALLIFREY_SANDSTONE));
    }
}
