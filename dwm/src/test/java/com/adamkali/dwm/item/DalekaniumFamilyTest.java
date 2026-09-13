package com.adamkali.dwm.item;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.world.DWMConfiguredFeatures;
import com.adamkali.dwm.world.DWMPlacedFeatures;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekaniumFamilyTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void oreIngotsAndToolsAreRegistered() {
        assertNotNull(DWMBlocks.DALEKANIUM_ORE);
        assertNotNull(DWMBlocks.SILVER_DALEKANIUM_BLOCK);
        assertNotNull(DWMBlocks.BRONZE_DALEKANIUM_BLOCK);
        assertEquals(2, DWMBlocks.DALEKANIUM_STORAGE_BLOCKS.size());
        assertTrue(DWMBlocks.DALEKANIUM_STORAGE_BLOCKS.contains(DWMBlocks.SILVER_DALEKANIUM_BLOCK));
        assertTrue(DWMBlocks.DALEKANIUM_STORAGE_BLOCKS.contains(DWMBlocks.BRONZE_DALEKANIUM_BLOCK));
        assertNotNull(DWMItems.SILVER_DALEKANIUM_INGOT);
        assertNotNull(DWMItems.BRONZE_DALEKANIUM_INGOT);
        assertNotNull(DWMItems.SILVER_DALEKANIUM_SWORD);
        assertNotNull(DWMItems.SILVER_DALEKANIUM_SHOVEL);
        assertNotNull(DWMItems.SILVER_DALEKANIUM_PICKAXE);
        assertNotNull(DWMItems.SILVER_DALEKANIUM_AXE);
        assertNotNull(DWMItems.SILVER_DALEKANIUM_HOE);
        assertNotNull(DWMItems.BRONZE_DALEKANIUM_SWORD);
        assertNotNull(DWMItems.BRONZE_DALEKANIUM_SHOVEL);
        assertNotNull(DWMItems.BRONZE_DALEKANIUM_PICKAXE);
        assertNotNull(DWMItems.BRONZE_DALEKANIUM_AXE);
        assertNotNull(DWMItems.BRONZE_DALEKANIUM_HOE);
    }

    @Test
    void oreMatchesIronHardness() {
        assertEquals(3.0F, DWMBlocks.DALEKANIUM_ORE.defaultDestroyTime());
    }

    @Test
    void storageBlocksMatchIronBlockHardness() {
        assertEquals(5.0F, DWMBlocks.SILVER_DALEKANIUM_BLOCK.defaultDestroyTime());
        assertEquals(5.0F, DWMBlocks.BRONZE_DALEKANIUM_BLOCK.defaultDestroyTime());
    }

    @Test
    void silverSitsBetweenSteelAndBronze() {
        ToolMaterial steel = DWMToolMaterials.STEEL;
        ToolMaterial silver = DWMToolMaterials.SILVER_DALEKANIUM;
        ToolMaterial bronze = DWMToolMaterials.BRONZE_DALEKANIUM;

        assertEquals(BlockTags.INCORRECT_FOR_IRON_TOOL, silver.incorrectBlocksForDrops());
        assertEquals(BlockTags.INCORRECT_FOR_IRON_TOOL, bronze.incorrectBlocksForDrops());
        assertEquals(DWMItemTags.REPAIRS_SILVER_DALEKANIUM_EQUIPMENT, silver.repairItems());
        assertEquals(DWMItemTags.REPAIRS_BRONZE_DALEKANIUM_EQUIPMENT, bronze.repairItems());

        assertEquals(550, silver.durability());
        assertEquals(7.2F, silver.speed());
        assertEquals(2.6F, silver.attackDamageBonus());
        assertEquals(13, silver.enchantmentValue());

        assertEquals(625, bronze.durability());
        assertEquals(7.5F, bronze.speed());
        assertEquals(2.75F, bronze.attackDamageBonus());
        assertEquals(14, bronze.enchantmentValue());

        assertTrue(silver.durability() > steel.durability());
        assertTrue(silver.speed() > steel.speed());
        assertTrue(silver.attackDamageBonus() > steel.attackDamageBonus());
        assertTrue(bronze.durability() > silver.durability());
        assertTrue(bronze.speed() > silver.speed());
        assertTrue(bronze.attackDamageBonus() > silver.attackDamageBonus());
        assertTrue(bronze.durability() < 1561);
        assertTrue(bronze.speed() < 8.0F);
        assertTrue(bronze.attackDamageBonus() < 3.0F);
    }

    @Test
    void featureKeysUseExpectedIds() {
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "dalekanium_ore"),
                DWMConfiguredFeatures.DALEKANIUM_ORE.identifier()
        );
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "dalekanium_ore"),
                DWMPlacedFeatures.DALEKANIUM_ORE.identifier()
        );
    }
}
