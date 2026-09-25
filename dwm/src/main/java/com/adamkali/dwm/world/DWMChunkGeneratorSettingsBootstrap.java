package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;

import java.util.Optional;

public final class DWMChunkGeneratorSettingsBootstrap {
    private DWMChunkGeneratorSettingsBootstrap() {
    }

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> registerable) {
        registerable.register(
                DWMChunkGeneratorSettings.GALLIFREY,
                overworldLike(
                        registerable,
                        DWMBlocks.GALLIFREY_STONE.defaultBlockState(),
                        GallifreySurfaceRules.create(registerable.lookup(Registries.BIOME))
                )
        );
        registerable.register(
                DWMChunkGeneratorSettings.SKARO,
                overworldLike(
                        registerable,
                        Blocks.STONE.defaultBlockState(),
                        SkaroSurfaceRules.create(registerable.lookup(Registries.BIOME))
                )
        );
    }

    private static NoiseGeneratorSettings overworldLike(
            BootstrapContext<NoiseGeneratorSettings> registerable,
            net.minecraft.world.level.block.state.BlockState defaultBlock,
            net.minecraft.world.level.levelgen.material.rule.MaterialRule materialRule
    ) {
        var density = registerable.lookup(Registries.DENSITY_FUNCTION);
        var noise = registerable.lookup(Registries.NOISE);
        Aquifer.Config aquifers = NoiseRouterData.overworldAquifers(
                density,
                noise,
                NoiseRouterData.OVERWORLD_FUNCTIONS
        );
        return new NoiseGeneratorSettings(
                NoiseSettings.create(-64, 384),
                defaultBlock,
                Blocks.WATER.defaultBlockState(),
                NoiseRouterData.overworld(density, NoiseRouterData.OVERWORLD_FUNCTIONS),
                Holder.direct(materialRule),
                java.util.List.of(),
                63,
                false,
                Optional.of(aquifers),
                false,
                NoiseGeneratorSettings.DebugFunctions.EMPTY
        );
    }
}
