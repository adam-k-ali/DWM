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
            int radius,
            Direction doorFacing
    ) {
        if (world == null || biome == null || searchOrigin == null || doorFacing == null) {
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
        if (!isValidLanding(world, landing, doorFacing)) {
            return findNearbyValidLanding(world, biomePos.getX(), biomePos.getZ(), doorFacing);
        }
        return Optional.of(landing);
    }

    /**
     * Tries {@code target} if valid; otherwise spirals nearby for a valid shell cell.
     * Used for waypoint exact-coordinate landings.
     */
    public static Optional<BlockPos> findLandingAtOrNearby(
            ServerLevel world,
            BlockPos target,
            Direction doorFacing
    ) {
        if (world == null || target == null || doorFacing == null) {
            return Optional.empty();
        }
        world.getChunk(target);
        if (isValidLanding(world, target, doorFacing)) {
            return Optional.of(target);
        }
        return findNearbyValidLanding(world, target.getX(), target.getZ(), doorFacing);
    }

    /**
     * Tries a small spiral of columns around {@code originX/Z} after the chunk is loaded.
     */
    public static Optional<BlockPos> findNearbyValidLanding(
            ServerLevel world,
            int originX,
            int originZ,
            Direction doorFacing
    ) {
        if (world == null || doorFacing == null) {
            return Optional.empty();
        }
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
                    if (isValidLanding(world, candidate, doorFacing)) {
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
            BlockPos searchOrigin,
            Direction doorFacing
    ) {
        return findLanding(world, biome, searchOrigin, DEFAULT_SEARCH_RADIUS, doorFacing);
    }

    /**
     * Surface landing near {@code searchOrigin} without biome filtering (untagged / modded dims).
     */
    public static Optional<BlockPos> findSurfaceLanding(
            ServerLevel world,
            BlockPos searchOrigin,
            Direction doorFacing
    ) {
        if (world == null || searchOrigin == null || doorFacing == null) {
            return Optional.empty();
        }
        world.getChunk(searchOrigin);
        int topY = world.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                searchOrigin.getX(),
                searchOrigin.getZ()
        );
        BlockPos landing = new BlockPos(searchOrigin.getX(), topY, searchOrigin.getZ());
        if (isValidLanding(world, landing, doorFacing)) {
            return Optional.of(landing);
        }
        return findNearbyValidLanding(world, searchOrigin.getX(), searchOrigin.getZ(), doorFacing);
    }

    /**
     * Shell needs a solid floor under {@code pos}, replaceable space at {@code pos} and above,
     * and replaceable space in the door-facing column (feet + head) used by exit teleport.
     */
    public static boolean isValidLanding(LevelReader world, BlockPos pos, Direction doorFacing) {
        if (world == null || pos == null || doorFacing == null || world.isOutsideBuildHeight(pos)) {
            return false;
        }
        BlockState below = world.getBlockState(pos.below());
        if (!below.isFaceSturdy(world, pos.below(), Direction.UP)) {
            return false;
        }
        BlockState feet = world.getBlockState(pos);
        BlockState head = world.getBlockState(pos.above());
        if (!isReplaceable(feet) || !isReplaceable(head)) {
            return false;
        }

        BlockPos door = pos.relative(doorFacing);
        if (world.isOutsideBuildHeight(door) || world.isOutsideBuildHeight(door.above())) {
            return false;
        }
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.getChunk(door);
        }
        BlockState doorFeet = world.getBlockState(door);
        BlockState doorHead = world.getBlockState(door.above());
        return isReplaceable(doorFeet) && isReplaceable(doorHead);
    }

    private static boolean isReplaceable(BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }
}
