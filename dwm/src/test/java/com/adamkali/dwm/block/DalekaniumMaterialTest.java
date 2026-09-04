package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.world.DWMConfiguredFeatures;
import com.adamkali.dwm.world.DWMPlacedFeatures;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekaniumMaterialTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void materialIdsAreRegistered() {
        assertEquals(id("dalekanium_ore"), BuiltInRegistries.BLOCK.getKey(DWMBlocks.DALEKANIUM_ORE));
        assertEquals(id("dalekanium_block"), BuiltInRegistries.BLOCK.getKey(DWMBlocks.DALEKANIUM_BLOCK));
        assertEquals(id("silver_dalekanium_block"), BuiltInRegistries.BLOCK.getKey(DWMBlocks.SILVER_DALEKANIUM_BLOCK));
        assertEquals(id("bronze_dalekanium_block"), BuiltInRegistries.BLOCK.getKey(DWMBlocks.BRONZE_DALEKANIUM_BLOCK));
        assertEquals(id("raw_dalekanium"), BuiltInRegistries.ITEM.getKey(DWMItems.RAW_DALEKANIUM));
        assertEquals(id("dalekanium_ingot"), BuiltInRegistries.ITEM.getKey(DWMItems.DALEKANIUM_INGOT));
        assertEquals(id("silver_dalekanium_ingot"), BuiltInRegistries.ITEM.getKey(DWMItems.SILVER_DALEKANIUM_INGOT));
        assertEquals(id("bronze_dalekanium_ingot"), BuiltInRegistries.ITEM.getKey(DWMItems.BRONZE_DALEKANIUM_INGOT));
    }

    @Test
    void oreHardnessMatchesIron() {
        assertEquals(3.0F, DWMBlocks.DALEKANIUM_ORE.defaultDestroyTime());
        assertTrue(DWMBlocks.DALEKANIUM_ORE.defaultBlockState().requiresCorrectToolForDrops());
    }

    @Test
    void featureKeysMatchIronPlacementShape() {
        assertEquals(id("dalekanium_ore"), DWMConfiguredFeatures.DALEKANIUM_ORE.identifier());
        assertEquals(id("dalekanium_ore_small"), DWMConfiguredFeatures.DALEKANIUM_ORE_SMALL.identifier());
        assertEquals(id("dalekanium_ore_upper"), DWMPlacedFeatures.DALEKANIUM_ORE_UPPER.identifier());
        assertEquals(id("dalekanium_ore_middle"), DWMPlacedFeatures.DALEKANIUM_ORE_MIDDLE.identifier());
        assertEquals(id("dalekanium_ore_small"), DWMPlacedFeatures.DALEKANIUM_ORE_SMALL.identifier());
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dwm", path);
    }
}
