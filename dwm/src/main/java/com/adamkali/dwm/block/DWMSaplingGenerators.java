package com.adamkali.dwm.block;

import com.adamkali.dwm.world.DWMConfiguredFeatures;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.resources.ResourceKey;

public final class DWMSaplingGenerators {
    public static final TreeGrower ASH = grower("ash", DWMConfiguredFeatures.ASH);
    public static final TreeGrower DARK_ASH = grower("dark_ash", DWMConfiguredFeatures.DARK_ASH);
    public static final TreeGrower CARDINAL = grower("cardinal", DWMConfiguredFeatures.CARDINAL);

    private static TreeGrower grower(String name, ResourceKey<Feature> tree) {
        return new TreeGrower(
                name,
                WeightedList.of(tree),
                WeightedList.of(),
                WeightedList.of(),
                tree
        );
    }

    private DWMSaplingGenerators() {
    }
}
