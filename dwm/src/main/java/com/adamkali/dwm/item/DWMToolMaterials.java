package com.adamkali.dwm.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public final class DWMToolMaterials {
    public static final ToolMaterial STEEL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            500,
            7.0F,
            2.5F,
            12,
            DWMItemTags.REPAIRS_STEEL_EQUIPMENT
    );

    public static final ToolMaterial AZBANTIUM = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1561,
            8.0F,
            3.0F,
            10,
            DWMItemTags.REPAIRS_AZBANTIUM_EQUIPMENT
    );

    private DWMToolMaterials() {
    }
}
