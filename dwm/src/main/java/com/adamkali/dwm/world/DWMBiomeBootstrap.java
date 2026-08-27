package com.adamkali.dwm.world;

import com.adamkali.dwm.entity.DWMEntityTypes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

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

    public static void bootstrap(BootstrapContext<Biome> registerable) {
        HolderGetter<PlacedFeature> features = registerable.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = registerable.lookup(Registries.CONFIGURED_CARVER);

        registerable.register(DWMBiomeKeys.GALLIFREY_PLAINS, createPlains(features, carvers));
        registerable.register(DWMBiomeKeys.GALLIFREY_FOREST, createForest(features, carvers));
        registerable.register(DWMBiomeKeys.GALLIFREY_WASTES, createWastes(features, carvers));
        registerable.register(DWMBiomeKeys.GALLIFREY_BADLANDS, createBadlands(features, carvers));
    }

    private static Biome createPlains(
            HolderGetter<PlacedFeature> features,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(spawns);
        addBroakirSpawns(spawns);
        addFlutterwingSpawns(spawns);
        addTimeLordSpawns(spawns);

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        addBasicFeatures(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DWMPlacedFeatures.ASH_PLAINS);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DWMPlacedFeatures.GALLIFREY_FLOWERS_PLAINS);

        return buildBiome(true, 0.9F, 0.3F, spawns, generation);
    }

    private static Biome createForest(
            HolderGetter<PlacedFeature> features,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(spawns);
        addBroakirSpawns(spawns);
        addFlutterwingSpawns(spawns);
        addMewingDogSpawns(spawns);
        addTimeLordSpawns(spawns);

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        addBasicFeatures(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DWMPlacedFeatures.ASH_FOREST);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DWMPlacedFeatures.DARK_ASH_FOREST);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DWMPlacedFeatures.CARDINAL_FOREST);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DWMPlacedFeatures.GALLIFREY_FLOWERS_FOREST);

        return buildBiome(true, 0.85F, 0.6F, spawns, generation);
    }

    private static Biome createWastes(
            HolderGetter<PlacedFeature> features,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(spawns);

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        addBasicFeatures(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DWMPlacedFeatures.SACCHARINE_CANE_WASTES);

        return buildBiome(false, 1.2F, 0.0F, spawns, generation);
    }

    private static Biome createBadlands(
            HolderGetter<PlacedFeature> features,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(spawns);

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        addBasicFeatures(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DWMPlacedFeatures.SACCHARINE_CANE_BADLANDS);

        return buildBiome(false, 1.2F, 0.0F, spawns, generation);
    }

    private static void addBasicFeatures(BiomeGenerationSettings.Builder generation) {
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.GALLIFREY_COAL_ORE_UPPER);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.GALLIFREY_COAL_ORE_LOWER);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.GALLIFREY_IRON_ORE_UPPER);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.GALLIFREY_IRON_ORE_MIDDLE);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.GALLIFREY_IRON_ORE_SMALL);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.GALLIFREY_GOLD_ORE);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.GALLIFREY_GOLD_ORE_LOWER);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.GALLIFREY_DIAMOND_ORE);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.GALLIFREY_DIAMOND_ORE_MEDIUM);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.GALLIFREY_DIAMOND_ORE_LARGE);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.GALLIFREY_DIAMOND_ORE_BURIED);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.AZBANTIUM_ORE);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, DWMPlacedFeatures.ZEITON_ORE);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        BiomeDefaultFeatures.addDefaultSprings(generation);
        BiomeDefaultFeatures.addSurfaceFreezing(generation);
    }

    private static void addBroakirSpawns(MobSpawnSettings.Builder spawns) {
        spawns.addSpawn(
                MobCategory.CREATURE,
                10,
                new MobSpawnSettings.SpawnerData(DWMEntityTypes.BROAKIR, 2, 4)
        );
    }

    private static void addFlutterwingSpawns(MobSpawnSettings.Builder spawns) {
        spawns.addSpawn(
                MobCategory.CREATURE,
                10,
                new MobSpawnSettings.SpawnerData(DWMEntityTypes.FLUTTERWING, 2, 4)
        );
    }

    private static void addMewingDogSpawns(MobSpawnSettings.Builder spawns) {
        spawns.addSpawn(
                MobCategory.CREATURE,
                8,
                new MobSpawnSettings.SpawnerData(DWMEntityTypes.MEWING_DOG, 2, 4)
        );
    }

    private static void addTimeLordSpawns(MobSpawnSettings.Builder spawns) {
        spawns.addSpawn(
                MobCategory.CREATURE,
                8,
                new MobSpawnSettings.SpawnerData(DWMEntityTypes.TIME_LORD, 1, 3)
        );
    }

    private static Biome buildBiome(
            boolean precipitation,
            float temperature,
            float downfall,
            MobSpawnSettings.Builder spawns,
            BiomeGenerationSettings.Builder generation
    ) {
        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .waterColor(WATER_COLOR)
                .foliageColorOverride(FOLIAGE_COLOR)
                .grassColorOverride(GRASS_COLOR)
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(precipitation)
                .temperature(temperature)
                .downfall(downfall)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, WATER_FOG_COLOR)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, FOG_COLOR)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, SKY_COLOR)
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }
}
