package com.adamkali.dwm.tardis.logic;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Landing-site helpers for exterior relocation into a selected biome.
 */
public final class LandingSiteLogic {
    /** Default horizontal search radius for {@link ServerLevel#findClosestBiome3d}. */
    public static final int DEFAULT_SEARCH_RADIUS = 6400;

    /** Sample interval passed to {@link ServerLevel#findClosestBiome3d}. */
    public static final int LOCATE_INTERVAL = 64;

    private LandingSiteLogic() {
    }

    /**
     * Parses a biome registry id string into a {@link ResourceKey}.
     */
    public static Optional<ResourceKey<Biome>> parseBiome(@Nullable String biomeId) {
        if (biomeId == null || biomeId.isBlank()) {
            return Optional.empty();
        }
        Identifier id = Identifier.tryParse(biomeId);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.of(ResourceKey.create(Registries.BIOME, id));
    }

    /**
     * Locates a surface landing position in {@code biome} near {@code searchOrigin}.
     * Empty when the biome cannot be found or no valid surface cell exists.
     */
    public static Optional<BlockPos> findLanding(
            ServerLevel world,
            ResourceKey<Biome> biome,
            BlockPos searchOrigin,
            int radius
    ) {
        if (world == null || biome == null || searchOrigin == null) {
            return Optional.empty();
        }
        Pair<BlockPos, Holder<Biome>> located = world.findClosestBiome3d(
                entry -> entry.is(biome),
                searchOrigin,
                Math.max(1, radius),
                LOCATE_INTERVAL,
                LOCATE_INTERVAL
        );
        if (located == null) {
            return Optional.empty();
        }
        BlockPos biomePos = located.getFirst();
        // locateBiome can return coordinates in unloaded chunks; heightmap then reports world bottom.
        world.getChunk(biomePos);
        int topY = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, biomePos.getX(), biomePos.getZ());
        BlockPos landing = new BlockPos(biomePos.getX(), topY, biomePos.getZ());
        if (!isValidLanding(world, landing)) {
            return findNearbyValidLanding(world, biomePos.getX(), biomePos.getZ());
        }
        return Optional.of(landing);
    }

    /**
     * Tries a small spiral of columns around {@code originX/Z} after the chunk is loaded.
     */
    private static Optional<BlockPos> findNearbyValidLanding(ServerLevel world, int originX, int originZ) {
        for (int radius = 1; radius <= 8; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }
                    int x = originX + dx;
                    int z = originZ + dz;
                    world.getChunk(x >> 4, z >> 4);
                    int topY = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos candidate = new BlockPos(x, topY, z);
                    if (isValidLanding(world, candidate)) {
                        return Optional.of(candidate);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<BlockPos> findLanding(
            ServerLevel world,
            ResourceKey<Biome> biome,
            BlockPos searchOrigin
    ) {
        return findLanding(world, biome, searchOrigin, DEFAULT_SEARCH_RADIUS);
    }

    /**
     * Surface landing near {@code searchOrigin} without biome filtering (untagged / modded dims).
     */
    public static Optional<BlockPos> findSurfaceLanding(ServerLevel world, BlockPos searchOrigin) {
        if (world == null || searchOrigin == null) {
            return Optional.empty();
        }
        world.getChunk(searchOrigin);
        int topY = world.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                searchOrigin.getX(),
                searchOrigin.getZ()
        );
        BlockPos landing = new BlockPos(searchOrigin.getX(), topY, searchOrigin.getZ());
        if (isValidLanding(world, landing)) {
            return Optional.of(landing);
        }
        return findNearbyValidLanding(world, searchOrigin.getX(), searchOrigin.getZ());
    }

    /**
     * Shell needs a solid floor under {@code pos} and replaceable space at {@code pos} and above.
     */
    public static boolean isValidLanding(LevelReader world, BlockPos pos) {
        if (world == null || pos == null || world.isOutsideBuildHeight(pos)) {
            return false;
        }
        BlockState below = world.getBlockState(pos.below());
        if (!below.isFaceSturdy(world, pos.below(), Direction.UP)) {
            return false;
        }
        BlockState feet = world.getBlockState(pos);
        BlockState head = world.getBlockState(pos.above());
        return (feet.isAir() || feet.canBeReplaced()) && (head.isAir() || head.canBeReplaced());
    }
}
