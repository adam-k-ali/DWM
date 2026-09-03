package com.adamkali.dwm.gametest;

import com.adamkali.dwm.tardis.logic.LandingSiteLogic;
import com.adamkali.dwm.world.DWMBiomeKeys;
import com.adamkali.dwm.world.DWMBiomeTags;
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
import net.minecraft.world.level.levelgen.Heightmap;

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

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 200)
    public void skaroLevelAndBiomesAreRegistered(GameTestHelper context) {
        ServerLevel skaro = requireSkaro(context);
        Registry<Biome> biomes = skaro.registryAccess().lookupOrThrow(Registries.BIOME);
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
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 200)
    public void skaroSurfaceHeightResolvesForLanding(GameTestHelper context) {
        ServerLevel skaro = requireSkaro(context);
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
    }

    private static ServerLevel requireSkaro(GameTestHelper context) {
        ServerLevel skaro = context.getLevel().getServer().getLevel(SkaroDimensions.SKARO_WORLD_KEY);
        if (skaro == null) {
            throw new AssertionError("Expected dwm:skaro to load on the dedicated server");
        }
        return skaro;
    }
}
