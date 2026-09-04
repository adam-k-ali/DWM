package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;

public final class DWMChunkGeneratorSettingsBootstrap {
    private DWMChunkGeneratorSettingsBootstrap() {
    }

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> registerable) {
        registerable.register(
                DWMChunkGeneratorSettings.GALLIFREY,
                new NoiseGeneratorSettings(
                        NoiseSettings.create(-64, 384, 1, 2),
                        DWMBlocks.GALLIFREY_STONE.defaultBlockState(),
                        Blocks.WATER.defaultBlockState(),
                        NoiseRouterData.overworld(
                                registerable.lookup(Registries.DENSITY_FUNCTION),
                                registerable.lookup(Registries.NOISE),
                                false,
                                false
                        ),
                        GallifreySurfaceRules.create(registerable.lookup(Registries.BIOME)),
                        new OverworldBiomeBuilder().spawnTarget(),
                        63,
                        false,
                        true,
                        true,
                        false
                )
        );
        registerable.register(
                DWMChunkGeneratorSettings.SKARO,
                new NoiseGeneratorSettings(
                        NoiseSettings.create(-64, 384, 1, 2),
                        Blocks.STONE.defaultBlockState(),
                        Blocks.WATER.defaultBlockState(),
                        NoiseRouterData.overworld(
                                registerable.lookup(Registries.DENSITY_FUNCTION),
                                registerable.lookup(Registries.NOISE),
                                false,
                                false
                        ),
                        SkaroSurfaceRules.create(registerable.lookup(Registries.BIOME)),
                        new OverworldBiomeBuilder().spawnTarget(),
                        63,
                        false,
                        true,
                        true,
                        false
                )
        );
    }
}
