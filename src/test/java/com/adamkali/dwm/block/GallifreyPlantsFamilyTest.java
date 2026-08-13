package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GallifreyPlantsFamilyTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void plantsListContainsPlaceableItems() {
        assertEquals(3, DWMBlocks.GALLIFREY_PLANTS.size());
        assertTrue(DWMBlocks.GALLIFREY_PLANTS.contains(DWMBlocks.FLOWER_OF_REMEMBRANCE));
        assertTrue(DWMBlocks.GALLIFREY_PLANTS.contains(DWMBlocks.MOONLIGHT_BLOOM));
        assertTrue(DWMBlocks.GALLIFREY_PLANTS.contains(DWMBlocks.SACCHARINE_CANE));
    }

    @Test
    void crossPlantsHavePottedVariants() {
        assertEquals(2, DWMBlocks.GALLIFREY_CROSS_PLANTS.size());
        assertEquals(2, DWMBlocks.GALLIFREY_POTTED_PLANTS.size());
        assertTrue(DWMBlocks.GALLIFREY_CROSS_PLANTS.contains(DWMBlocks.FLOWER_OF_REMEMBRANCE));
        assertTrue(DWMBlocks.GALLIFREY_CROSS_PLANTS.contains(DWMBlocks.MOONLIGHT_BLOOM));
        assertTrue(DWMBlocks.GALLIFREY_POTTED_PLANTS.contains(DWMBlocks.POTTED_FLOWER_OF_REMEMBRANCE));
        assertTrue(DWMBlocks.GALLIFREY_POTTED_PLANTS.contains(DWMBlocks.POTTED_MOONLIGHT_BLOOM));
        assertFalse(DWMBlocks.GALLIFREY_PLANTS.contains(DWMBlocks.POTTED_FLOWER_OF_REMEMBRANCE));
        assertFalse(DWMBlocks.GALLIFREY_CROSS_PLANTS.contains(DWMBlocks.SACCHARINE_CANE));
    }
}
