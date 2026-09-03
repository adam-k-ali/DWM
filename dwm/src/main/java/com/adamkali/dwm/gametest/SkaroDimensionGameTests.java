package com.adamkali.dwm.gametest;

import com.adamkali.dwm.tardis.logic.LandingSiteLogic;
import com.adamkali.dwm.world.DWMBiomeKeys;
import com.adamkali.dwm.world.DWMBiomeTags;
import com.adamkali.dwm.world.DWMChunkGeneratorSettings;
import com.adamkali.dwm.world.SkaroDimensions;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SkaroDimensionGameTests {
    private static final List<ResourceKey<Biome>> SKARO_BIOMES = List.of(
            DWMBiomeKeys.SKARO_IRRADIATED_WASTES,
            DWMBiomeKeys.SKARO_PETRIFIED_JUNGLE,
            DWMBiomeKeys.SKARO_DRAMMANKIN_MIRE,
            DWMBiomeKeys.SKARO_DRAMMANKIN_MOUNTAINS,
            DWMBiomeKeys.SKARO_THAL_PLATEAU
    );

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void skaroLevelAndBiomesAreRegistered(GameTestHelper context) {
        var registries = context.getLevel().registryAccess();
        Registry<Biome> biomes = registries.lookupOrThrow(Registries.BIOME);
        for (ResourceKey<Biome> key : SKARO_BIOMES) {
            if (biomes.get(key).isEmpty()) {
                throw new AssertionError("Expected biome to be registered: " + key.identifier());
            }
        }
        int tagged = 0;
        Set<ResourceKey<Biome>> taggedKeys = new HashSet<>();
        for (Holder<Biome> holder : biomes.getTagOrEmpty(DWMBiomeTags.IS_SKARO)) {
            tagged++;
            holder.unwrapKey().ifPresent(taggedKeys::add);
        }
        if (tagged != 5) {
            throw new AssertionError("Expected exactly 5 #dwm:is_skaro biomes, got " + tagged);
        }
        if (!taggedKeys.containsAll(SKARO_BIOMES)) {
            throw new AssertionError("Tag #dwm:is_skaro missing expected biomes: " + taggedKeys);
        }

        ResourceKey<LevelStem> stemKey = ResourceKey.create(Registries.LEVEL_STEM, SkaroDimensions.DIMENSION_ID);
        Registry<LevelStem> stems = registries.lookupOrThrow(Registries.LEVEL_STEM);
        if (stems.get(stemKey).isEmpty()) {
            throw new AssertionError("Expected level stem dwm:skaro to be registered");
        }

        ResourceKey<DimensionType> typeKey = ResourceKey.create(Registries.DIMENSION_TYPE, SkaroDimensions.DIMENSION_ID);
        Registry<DimensionType> types = registries.lookupOrThrow(Registries.DIMENSION_TYPE);
        if (types.get(typeKey).isEmpty()) {
            throw new AssertionError("Expected dimension type dwm:skaro to be registered");
        }

        Registry<NoiseGeneratorSettings> noise = registries.lookupOrThrow(Registries.NOISE_SETTINGS);
        if (noise.get(DWMChunkGeneratorSettings.SKARO).isEmpty()) {
            throw new AssertionError("Expected noise settings dwm:skaro to be registered");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 200)
    public void skaroSurfaceHeightResolvesForLanding(GameTestHelper context) {
        Registry<NoiseGeneratorSettings> noise =
                context.getLevel().registryAccess().lookupOrThrow(Registries.NOISE_SETTINGS);
        Holder.Reference<NoiseGeneratorSettings> settings = noise.get(DWMChunkGeneratorSettings.SKARO)
                .orElseThrow(() -> new AssertionError("Expected noise settings dwm:skaro"));
        NoiseGeneratorSettings value = settings.value();
        if (value.seaLevel() != 63) {
            throw new AssertionError("Expected Skaro sea level 63, got " + value.seaLevel());
        }
        if (!value.defaultBlock().is(Blocks.STONE)) {
            throw new AssertionError("Expected Skaro default block stone, got " + value.defaultBlock());
        }

        ServerLevel skaro = context.getLevel().getServer().getLevel(SkaroDimensions.SKARO_WORLD_KEY);
        if (skaro != null) {
            int x = 0;
            int z = 0;
            skaro.getChunk(x >> 4, z >> 4);
            int surfaceY = skaro.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (surfaceY <= skaro.getMinY() || surfaceY >= skaro.getMaxY()) {
                throw new AssertionError("Skaro surface height out of world bounds: " + surfaceY);
            }
            Optional<BlockPos> landing = LandingSiteLogic.findSurfaceLanding(
                    skaro,
                    new BlockPos(x, surfaceY, z),
                    Direction.NORTH
            );
            if (landing.isEmpty()) {
                throw new AssertionError("Expected a resolvable Skaro surface landing near 0,0");
            }
            context.succeed();
            return;
        }

        BlockPos shellRel = new BlockPos(2, 2, 2);
        context.setBlock(shellRel.below(), Blocks.STONE);
        context.setBlock(shellRel, Blocks.AIR);
        context.setBlock(shellRel.above(), Blocks.AIR);
        context.setBlock(shellRel.north(), Blocks.AIR);
        context.setBlock(shellRel.north().above(), Blocks.AIR);
        Optional<BlockPos> landing = LandingSiteLogic.findSurfaceInColumn(
                context.getLevel(),
                context.absolutePos(shellRel).getX(),
                context.absolutePos(shellRel).getZ(),
                Direction.NORTH
        );
        if (landing.isEmpty()) {
            throw new AssertionError("Expected LandingSiteLogic to resolve a surface column for Skaro travel");
        }
        context.succeed();
    }
}
