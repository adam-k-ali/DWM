package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.item.DWMItems;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ZeitonFamilyTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void itemsAndOreAreRegistered() {
        assertNotNull(DWMBlocks.ZEITON_ORE);
        assertNotNull(DWMItems.ZEITON_CRYSTALS);
        assertNotNull(DWMItems.ZEITON_POWDER);
        assertNotNull(DWMItems.FERRITE_POWDER);
    }

    @Test
    void oreMatchesGallifreyVanillaHardness() {
        assertEquals(3.0F, DWMBlocks.ZEITON_ORE.defaultDestroyTime());
    }
}
