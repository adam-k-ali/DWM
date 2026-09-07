package com.adamkali.dwm.world;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Skaro destination biomes — vanilla caves/ores, hostile atmosphere, no Gallifrey content.
 */
public final class SkaroBiomeBootstrap {
    private SkaroBiomeBootstrap() {
    }

    static void bootstrap(
            BootstrapContext<Biome> registerable,
            HolderGetter<PlacedFeature> features,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        registerable.register(DWMBiomeKeys.SKARO_IRRADIATED_WASTES, createIrradiatedWastes(features, carvers));
        registerable.register(DWMBiomeKeys.SKARO_PETRIFIED_JUNGLE, createPetrifiedJungle(features, carvers));
        registerable.register(DWMBiomeKeys.SKARO_DRAMMANKIN_MIRE, createDrammankinMire(features, carvers));
        registerable.register(DWMBiomeKeys.SKARO_DRAMMANKIN_MOUNTAINS, createDrammankinMountains(features, carvers));
        registerable.register(DWMBiomeKeys.SKARO_THAL_PLATEAU, createThalPlateau(features, carvers));
    }

    private static Biome createIrradiatedWastes(
            HolderGetter<PlacedFeature> features,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return buildBiome(
                false,
                1.2F,
                0.0F,
                emptySpawns(),
                basicGeneration(features, carvers),
                0x8B9A2A,
                0x6A7A18,
                0x9A8A30,
                0xC4B84A,
                0x8A8A40,
                0x9A9A48
        );
    }

    private static Biome createPetrifiedJungle(
            HolderGetter<PlacedFeature> features,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        BiomeGenerationSettings.Builder generation = basicGeneration(features, carvers);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DWMPlacedFeatures.PETRIFIED_JUNGLE_TREES);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DWMPlacedFeatures.PETRIFIED_JUNGLE_SNAGS);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DWMPlacedFeatures.FALLEN_PETRIFIED_JUNGLE_TREES);
        return buildBiome(
                true,
                0.9F,
                0.8F,
                emptySpawns(),
                generation,
                0x5A5040,
                0x4A4030,
                0x6E5E4E,
                0x8A7A68,
                0x6A5A48,
                0x7A6A58
        );
    }

    private static Biome createDrammankinMire(
            HolderGetter<PlacedFeature> features,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return buildBiome(
                true,
                0.8F,
                0.9F,
                emptySpawns(),
                basicGeneration(features, carvers),
                0x3A4A28,
                0x2A3A18,
                0x4A5A32,
                0x6A7048,
                0x4A5A30,
                0x5A6A38
        );
    }

    private static Biome createDrammankinMountains(
            HolderGetter<PlacedFeature> features,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return buildBiome(
                false,
                0.4F,
                0.2F,
                emptySpawns(),
                basicGeneration(features, carvers),
                0x4A5558,
                0x3A4548,
                0x5A5A58,
                0x7A7A78,
                0x5A5A58,
                0x6A6A68
        );
    }

    private static Biome createThalPlateau(
            HolderGetter<PlacedFeature> features,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return buildBiome(
                true,
                0.7F,
                0.4F,
                emptySpawns(),
                basicGeneration(features, carvers),
                0x4A6A7A,
                0x3A5A6A,
                0x8A9AAA,
                0x7A8A9A,
                0x6A7A68,
                0x7A8A78
        );
    }

    private static MobSpawnSettings.Builder emptySpawns() {
        return new MobSpawnSettings.Builder();
    }

    private static BiomeGenerationSettings.Builder basicGeneration(
            HolderGetter<PlacedFeature> features,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.DALEKANIUM_ORE_UPPER);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.DALEKANIUM_ORE_MIDDLE);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.DALEKANIUM_ORE_SMALL);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        BiomeDefaultFeatures.addDefaultSprings(generation);
        BiomeDefaultFeatures.addSurfaceFreezing(generation);
        return generation;
    }

    private static Biome buildBiome(
            boolean precipitation,
            float temperature,
            float downfall,
            MobSpawnSettings.Builder spawns,
            BiomeGenerationSettings.Builder generation,
            int waterColor,
            int waterFogColor,
            int fogColor,
            int skyColor,
            int foliageColor,
            int grassColor
    ) {
        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .waterColor(waterColor)
                .foliageColorOverride(foliageColor)
                .grassColorOverride(grassColor)
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(precipitation)
                .temperature(temperature)
                .downfall(downfall)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, waterFogColor)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, fogColor)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, skyColor)
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }
}
