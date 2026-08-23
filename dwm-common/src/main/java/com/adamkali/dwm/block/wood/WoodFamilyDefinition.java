package com.adamkali.dwm.block.wood;

import java.util.EnumSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

public record WoodFamilyDefinition(
        String id,
        String displayName,
        MapColor planksColor,
        MapColor barkColor,
        WoodType woodType,
        BlockSetType blockSetType,
        TreeGrower saplingGenerator,
        TagKey<Block> logBlockTag,
        TagKey<Item> logItemTag,
        EnumSet<WoodFamilyFeature> features
) {
    public WoodFamilyDefinition {
        features = features.isEmpty() ? EnumSet.noneOf(WoodFamilyFeature.class) : EnumSet.copyOf(features);
    }

    public boolean has(WoodFamilyFeature feature) {
        return features.contains(feature);
    }
}
