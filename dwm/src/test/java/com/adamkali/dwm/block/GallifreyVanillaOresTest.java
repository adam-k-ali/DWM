package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.world.DWMConfiguredFeatures;
import com.adamkali.dwm.world.DWMPlacedFeatures;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GallifreyVanillaOresTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void blocksListContainsAllFourOres() {
        assertEquals(4, DWMBlocks.GALLIFREY_VANILLA_ORES.size());
        assertTrue(DWMBlocks.GALLIFREY_VANILLA_ORES.contains(DWMBlocks.GALLIFREY_COAL_ORE));
        assertTrue(DWMBlocks.GALLIFREY_VANILLA_ORES.contains(DWMBlocks.GALLIFREY_IRON_ORE));
        assertTrue(DWMBlocks.GALLIFREY_VANILLA_ORES.contains(DWMBlocks.GALLIFREY_GOLD_ORE));
        assertTrue(DWMBlocks.GALLIFREY_VANILLA_ORES.contains(DWMBlocks.GALLIFREY_DIAMOND_ORE));
    }

    @Test
    void oresMatchVanillaOreHardness() {
        for (var ore : DWMBlocks.GALLIFREY_VANILLA_ORES) {
            assertEquals(3.0F, ore.defaultDestroyTime(), "Expected hardness 3.0 for " + ore);
        }
    }

    @Test
    void featureKeysUseExpectedIds() {
        assertEquals(Identifier.fromNamespaceAndPath("dwm", "gallifrey_coal_ore"), DWMConfiguredFeatures.GALLIFREY_COAL_ORE.identifier());
        assertEquals(Identifier.fromNamespaceAndPath("dwm", "gallifrey_iron_ore_upper"), DWMPlacedFeatures.GALLIFREY_IRON_ORE_UPPER.identifier());
        assertEquals(Identifier.fromNamespaceAndPath("dwm", "gallifrey_gold_ore"), DWMPlacedFeatures.GALLIFREY_GOLD_ORE.identifier());
        assertEquals(Identifier.fromNamespaceAndPath("dwm", "gallifrey_diamond_ore_buried"), DWMPlacedFeatures.GALLIFREY_DIAMOND_ORE_BURIED.identifier());
    }
}
