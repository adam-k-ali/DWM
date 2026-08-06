package com.adamkali.dwm.block;

import com.adamkali.dwm.world.DWMConfiguredFeatures;
import net.minecraft.block.SaplingGenerator;

import java.util.Optional;

public final class DWMSaplingGenerators {
    public static final SaplingGenerator ASH = new SaplingGenerator(
            "ash",
            Optional.empty(),
            Optional.of(DWMConfiguredFeatures.ASH),
            Optional.empty()
    );

    private DWMSaplingGenerators() {
    }
}
