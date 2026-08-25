package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public final class DWMChunkGeneratorSettings {
    public static final ResourceKey<NoiseGeneratorSettings> GALLIFREY = ResourceKey.create(
            Registries.NOISE_SETTINGS,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "gallifrey")
    );

    private DWMChunkGeneratorSettings() {
    }
}
