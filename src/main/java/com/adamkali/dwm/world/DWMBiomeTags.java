package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

public final class DWMBiomeTags {
    public static final TagKey<Biome> IS_GALLIFREY = TagKey.of(
            RegistryKeys.BIOME,
            Identifier.of(DWMReference.MOD_ID, "is_gallifrey")
    );

    private DWMBiomeTags() {
    }
}
