package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

public final class DWMBiomeKeys {
    public static final RegistryKey<Biome> GALLIFREY_PLAINS = key("gallifrey_plains");
    public static final RegistryKey<Biome> GALLIFREY_FOREST = key("gallifrey_forest");
    public static final RegistryKey<Biome> GALLIFREY_WASTES = key("gallifrey_wastes");

    private static RegistryKey<Biome> key(String path) {
        return RegistryKey.of(RegistryKeys.BIOME, Identifier.of(DWMReference.MOD_ID, path));
    }

    private DWMBiomeKeys() {
    }
}
