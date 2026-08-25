package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrangeSandFamilyTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void familyContainsBuildingAndTerrainBlocks() {
        assertEquals(11, DWMBlocks.ORANGE_SAND_FAMILY.size());
        assertEquals(10, DWMBlocks.ORANGE_SAND_BUILDING_BLOCKS.size());
        assertTrue(DWMBlocks.ORANGE_SAND_FAMILY.containsAll(DWMBlocks.ORANGE_SAND_BUILDING_BLOCKS));
        assertTrue(DWMBlocks.ORANGE_SAND_FAMILY.contains(DWMBlocks.ORANGE_SAND));
        assertTrue(DWMBlocks.ORANGE_SAND_FAMILY.contains(DWMBlocks.ORANGE_SANDSTONE_STAIRS));
        assertTrue(DWMBlocks.ORANGE_SAND_FAMILY.contains(DWMBlocks.ORANGE_SANDSTONE_SLAB));
        assertTrue(DWMBlocks.ORANGE_SAND_FAMILY.contains(DWMBlocks.ORANGE_SANDSTONE_WALL));
        assertTrue(DWMBlocks.ORANGE_SAND_FAMILY.contains(DWMBlocks.SMOOTH_ORANGE_SANDSTONE));
    }

    @Test
    void buildingBlocksExcludeTerrainSand() {
        assertFalse(DWMBlocks.ORANGE_SAND_BUILDING_BLOCKS.contains(DWMBlocks.ORANGE_SAND));
        assertTrue(DWMBlocks.ORANGE_SAND_BUILDING_BLOCKS.contains(DWMBlocks.ORANGE_SANDSTONE));
        assertTrue(DWMBlocks.ORANGE_SAND_BUILDING_BLOCKS.contains(DWMBlocks.CUT_ORANGE_SANDSTONE_SLAB));
        assertTrue(DWMBlocks.ORANGE_SAND_BUILDING_BLOCKS.contains(DWMBlocks.CHISELED_ORANGE_SANDSTONE));
    }
}
