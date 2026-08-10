package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

public final class DWMChunkGeneratorSettings {
    public static final RegistryKey<ChunkGeneratorSettings> GALLIFREY = RegistryKey.of(
            RegistryKeys.CHUNK_GENERATOR_SETTINGS,
            Identifier.of(DWMReference.MOD_ID, "gallifrey")
    );

    private DWMChunkGeneratorSettings() {
    }
}
