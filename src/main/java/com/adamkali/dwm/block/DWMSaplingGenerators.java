package com.adamkali.dwm.block;

import com.adamkali.dwm.world.DWMConfiguredFeatures;
import java.util.Optional;
import net.minecraft.world.level.block.grower.TreeGrower;

public final class DWMSaplingGenerators {
    public static final TreeGrower ASH = new TreeGrower(
            "ash",
            Optional.empty(),
            Optional.of(DWMConfiguredFeatures.ASH),
            Optional.empty()
    );

    public static final TreeGrower DARK_ASH = new TreeGrower(
            "dark_ash",
            Optional.empty(),
            Optional.of(DWMConfiguredFeatures.DARK_ASH),
            Optional.empty()
    );

    public static final TreeGrower CARDINAL = new TreeGrower(
            "cardinal",
            Optional.empty(),
            Optional.of(DWMConfiguredFeatures.CARDINAL),
            Optional.empty()
    );

    private DWMSaplingGenerators() {
    }
}
