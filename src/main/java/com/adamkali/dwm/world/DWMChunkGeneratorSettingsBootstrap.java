package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.densityfunction.DensityFunctions;

public final class DWMChunkGeneratorSettingsBootstrap {
    private DWMChunkGeneratorSettingsBootstrap() {
    }

    public static void bootstrap(Registerable<ChunkGeneratorSettings> registerable) {
        registerable.register(
                DWMChunkGeneratorSettings.GALLIFREY,
                new ChunkGeneratorSettings(
                        GenerationShapeConfig.create(-64, 384, 1, 2),
                        DWMBlocks.GALLIFREY_STONE.getDefaultState(),
                        Blocks.WATER.getDefaultState(),
                        DensityFunctions.createSurfaceNoiseRouter(
                                registerable.getRegistryLookup(RegistryKeys.DENSITY_FUNCTION),
                                registerable.getRegistryLookup(RegistryKeys.NOISE_PARAMETERS),
                                false,
                                false
                        ),
                        GallifreySurfaceRules.create(),
                        new VanillaBiomeParameters().getSpawnSuitabilityNoises(),
                        63,
                        false,
                        true,
                        true,
                        false
                )
        );
    }
}
