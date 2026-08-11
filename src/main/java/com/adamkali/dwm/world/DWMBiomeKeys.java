package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

public final class DWMBiomeKeys {
    public static final ResourceKey<Biome> GALLIFREY_PLAINS = key("gallifrey_plains");
    public static final ResourceKey<Biome> GALLIFREY_FOREST = key("gallifrey_forest");
    public static final ResourceKey<Biome> GALLIFREY_WASTES = key("gallifrey_wastes");

    private static ResourceKey<Biome> key(String path) {
        return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path));
    }

    private DWMBiomeKeys() {
    }
}
