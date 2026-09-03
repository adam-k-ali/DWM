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
    public static final ResourceKey<Biome> GALLIFREY_BADLANDS = key("gallifrey_badlands");

    public static final ResourceKey<Biome> SKARO_IRRADIATED_WASTES = key("skaro_irradiated_wastes");
    public static final ResourceKey<Biome> SKARO_PETRIFIED_JUNGLE = key("skaro_petrified_jungle");
    public static final ResourceKey<Biome> SKARO_DRAMMANKIN_MIRE = key("skaro_drammankin_mire");
    public static final ResourceKey<Biome> SKARO_DRAMMANKIN_MOUNTAINS = key("skaro_drammankin_mountains");
    public static final ResourceKey<Biome> SKARO_THAL_PLATEAU = key("skaro_thal_plateau");

    private static ResourceKey<Biome> key(String path) {
        return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path));
    }

    private DWMBiomeKeys() {
    }
}
