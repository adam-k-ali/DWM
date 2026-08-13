package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.item.DWMItems;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzbantiumFamilyTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void blocksListContainsOreAndStorage() {
        assertEquals(2, DWMBlocks.AZBANTIUM_BLOCKS.size());
        assertTrue(DWMBlocks.AZBANTIUM_BLOCKS.contains(DWMBlocks.AZBANTIUM_ORE));
        assertTrue(DWMBlocks.AZBANTIUM_BLOCKS.contains(DWMBlocks.AZBANTIUM_BLOCK));
    }

    @Test
    void gemAndGearAreRegistered() {
        assertNotNull(DWMItems.AZBANTIUM);
        assertNotNull(DWMItems.AZBANTIUM_PICKAXE);
        assertNotNull(DWMItems.AZBANTIUM_SWORD);
        assertNotNull(DWMItems.AZBANTIUM_HELMET);
        assertNotNull(DWMItems.AZBANTIUM_CHESTPLATE);
        assertNotNull(DWMItems.AZBANTIUM_LEGGINGS);
        assertNotNull(DWMItems.AZBANTIUM_BOOTS);
    }

    @Test
    void oreAndBlockAreHarderThanObsidian() {
        assertTrue(DWMBlocks.AZBANTIUM_ORE.defaultDestroyTime() > 50.0F);
        assertTrue(DWMBlocks.AZBANTIUM_BLOCK.defaultDestroyTime() > 50.0F);
    }
}
