package com.adamkali.dwm.world;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;

/**
 * Gallifrey destination biomes — orange-red atmosphere, Phase 1 terrain/wood features.
 */
public final class DWMBiomeBootstrap {
    /** Warm orange water. */
    private static final int WATER_COLOR = 0xC45A2A;
    private static final int WATER_FOG_COLOR = 0x8B3A1E;
    /** Dusty red fog / sky. */
    private static final int FOG_COLOR = 0xC9784A;
    private static final int SKY_COLOR = 0xE8A060;
    private static final int FOLIAGE_COLOR = 0xB85C28;
    private static final int GRASS_COLOR = 0xC96B2E;

    private DWMBiomeBootstrap() {
    }

    public static void bootstrap(Registerable<Biome> registerable) {
        RegistryEntryLookup<PlacedFeature> features = registerable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
        RegistryEntryLookup<ConfiguredCarver<?>> carvers = registerable.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER);

        registerable.register(DWMBiomeKeys.GALLIFREY_PLAINS, createPlains(features, carvers));
        registerable.register(DWMBiomeKeys.GALLIFREY_FOREST, createForest(features, carvers));
        registerable.register(DWMBiomeKeys.GALLIFREY_WASTES, createWastes(features, carvers));
    }

    private static Biome createPlains(
            RegistryEntryLookup<PlacedFeature> features,
            RegistryEntryLookup<ConfiguredCarver<?>> carvers
    ) {
        SpawnSettings.Builder spawns = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addBatsAndMonsters(spawns);

        GenerationSettings.LookupBackedBuilder generation = new GenerationSettings.LookupBackedBuilder(features, carvers);
        addBasicFeatures(generation);
        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, DWMPlacedFeatures.ASH_PLAINS);

        return buildBiome(true, 0.9F, 0.3F, spawns, generation);
    }

    private static Biome createForest(
            RegistryEntryLookup<PlacedFeature> features,
            RegistryEntryLookup<ConfiguredCarver<?>> carvers
    ) {
        SpawnSettings.Builder spawns = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addBatsAndMonsters(spawns);

        GenerationSettings.LookupBackedBuilder generation = new GenerationSettings.LookupBackedBuilder(features, carvers);
        addBasicFeatures(generation);
        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, DWMPlacedFeatures.ASH_FOREST);
        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, DWMPlacedFeatures.DARK_ASH_FOREST);
        generation.feature(GenerationStep.Feature.VEGETAL_DECORATION, DWMPlacedFeatures.CARDINAL_FOREST);

        return buildBiome(true, 0.85F, 0.6F, spawns, generation);
    }

    private static Biome createWastes(
            RegistryEntryLookup<PlacedFeature> features,
            RegistryEntryLookup<ConfiguredCarver<?>> carvers
    ) {
        SpawnSettings.Builder spawns = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addBatsAndMonsters(spawns);

        GenerationSettings.LookupBackedBuilder generation = new GenerationSettings.LookupBackedBuilder(features, carvers);
        addBasicFeatures(generation);

        return buildBiome(false, 1.2F, 0.0F, spawns, generation);
    }

    private static void addBasicFeatures(GenerationSettings.LookupBackedBuilder generation) {
        DefaultBiomeFeatures.addLandCarvers(generation);
        DefaultBiomeFeatures.addAmethystGeodes(generation);
        DefaultBiomeFeatures.addDungeons(generation);
        DefaultBiomeFeatures.addMineables(generation);
        DefaultBiomeFeatures.addDefaultOres(generation);
        DefaultBiomeFeatures.addDefaultDisks(generation);
        DefaultBiomeFeatures.addSprings(generation);
        DefaultBiomeFeatures.addFrozenTopLayer(generation);
    }

    private static Biome buildBiome(
            boolean precipitation,
            float temperature,
            float downfall,
            SpawnSettings.Builder spawns,
            GenerationSettings.LookupBackedBuilder generation
    ) {
        BiomeEffects effects = new BiomeEffects.Builder()
                .waterColor(WATER_COLOR)
                .waterFogColor(WATER_FOG_COLOR)
                .fogColor(FOG_COLOR)
                .skyColor(SKY_COLOR)
                .foliageColor(FOLIAGE_COLOR)
                .grassColor(GRASS_COLOR)
                .moodSound(BiomeMoodSound.CAVE)
                .build();

        return new Biome.Builder()
                .precipitation(precipitation)
                .temperature(temperature)
                .downfall(downfall)
                .effects(effects)
                .spawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }
}
