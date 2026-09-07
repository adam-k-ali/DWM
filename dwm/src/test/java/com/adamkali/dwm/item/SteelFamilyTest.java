package com.adamkali.dwm.item;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SteelFamilyTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void ingotAndToolsAreRegistered() {
        assertNotNull(DWMItems.STEEL_INGOT);
        assertNotNull(DWMItems.STEEL_SWORD);
        assertNotNull(DWMItems.STEEL_SHOVEL);
        assertNotNull(DWMItems.STEEL_PICKAXE);
        assertNotNull(DWMItems.STEEL_AXE);
        assertNotNull(DWMItems.STEEL_HOE);
    }

    @Test
    void materialStatsSitBetweenIronAndDiamond() {
        ToolMaterial steel = DWMToolMaterials.STEEL;
        assertEquals(BlockTags.INCORRECT_FOR_IRON_TOOL, steel.incorrectBlocksForDrops());
        assertEquals(500, steel.durability());
        assertEquals(7.0F, steel.speed());
        assertEquals(2.5F, steel.attackDamageBonus());
        assertEquals(12, steel.enchantmentValue());
        assertEquals(DWMItemTags.REPAIRS_STEEL_EQUIPMENT, steel.repairItems());

        assertTrue(steel.durability() > 250);
        assertTrue(steel.durability() < 1561);
        assertTrue(steel.speed() > 6.0F);
        assertTrue(steel.speed() < 8.0F);
        assertTrue(steel.attackDamageBonus() > 2.0F);
        assertTrue(steel.attackDamageBonus() < 3.0F);
    }
}
