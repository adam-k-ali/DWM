package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class DWMBiomeTags {
    public static final TagKey<Biome> IS_GALLIFREY = TagKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "is_gallifrey")
    );

    private DWMBiomeTags() {
    }
}
