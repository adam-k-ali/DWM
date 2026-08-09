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

    public static final SaplingGenerator DARK_ASH = new SaplingGenerator(
            "dark_ash",
            Optional.empty(),
            Optional.of(DWMConfiguredFeatures.DARK_ASH),
            Optional.empty()
    );

    public static final SaplingGenerator CARDINAL = new SaplingGenerator(
            "cardinal",
            Optional.empty(),
            Optional.of(DWMConfiguredFeatures.CARDINAL),
            Optional.empty()
    );

    private DWMSaplingGenerators() {
    }
}
