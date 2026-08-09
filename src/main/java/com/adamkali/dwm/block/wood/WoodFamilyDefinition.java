package com.adamkali.dwm.block.wood;

import net.minecraft.block.BlockSetType;
import net.minecraft.block.MapColor;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.block.WoodType;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;

import java.util.EnumSet;

public record WoodFamilyDefinition(
        String id,
        String displayName,
        MapColor planksColor,
        MapColor barkColor,
        WoodType woodType,
        BlockSetType blockSetType,
        SaplingGenerator saplingGenerator,
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
