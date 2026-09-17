package com.adamkali.dwm.tardis.logic;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Landing-site helpers for exterior relocation into a selected biome.
 * Column checks never force-generate chunks; callers ticket and wait until FULL.
 */
public final class LandingSiteLogic {
    /** Default horizontal search radius for {@link ServerLevel#findClosestBiome3d}. */
    public static final int DEFAULT_SEARCH_RADIUS = 6400;

    /** Sample interval passed to {@link ServerLevel#findClosestBiome3d}. */
    public static final int LOCATE_INTERVAL = 64;

    /** Chebyshev radius of {@link #findNearbyValidLanding} (blocks). */
    public static final int NEARBY_SPIRAL_MAX_RADIUS = 8;

    /**
     * Chunk-ticket radius covering {@link #NEARBY_SPIRAL_MAX_RADIUS} plus a door-column neighbour.
     */
    public static final int NEARBY_TICKET_CHUNK_RADIUS = 1;

    /**
     * Chunk-ticket radius covering unstabilised scatter ({@link StabiliserLogic#SCATTER_RADIUS}).
     */
    public static final int SCATTER_TICKET_CHUNK_RADIUS = 2;

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
     * Noise-only closest-biome search. Does not load or generate chunks.
     * Empty when the biome cannot be found.
     */
    public static Optional<BlockPos> locateClosestBiome(
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
        return Optional.of(located.getFirst());
    }

    public static Optional<BlockPos> locateClosestBiome(
            ServerLevel world,
            ResourceKey<Biome> biome,
            BlockPos searchOrigin
    ) {
        return locateClosestBiome(world, biome, searchOrigin, DEFAULT_SEARCH_RADIUS);
    }

    /**
     * Locates a surface landing position in {@code biome} near {@code searchOrigin}.
     * Empty when the biome cannot be found or no valid surface cell exists in currently
     * readable columns (does not force-load).
     */
    public static Optional<BlockPos> findLanding(
            ServerLevel world,
            ResourceKey<Biome> biome,
            BlockPos searchOrigin,
            int radius,
            Direction doorFacing
    ) {
        Optional<BlockPos> biomePos = locateClosestBiome(world, biome, searchOrigin, radius);
        if (biomePos.isEmpty()) {
            return Optional.empty();
        }
        return findSurfaceLanding(world, biomePos.get(), doorFacing);
    }

    /**
     * Tries {@code target} if valid; otherwise spirals nearby for a valid shell cell.
     * Used for waypoint exact-coordinate landings. Does not force-load chunks.
     */
    public static Optional<BlockPos> findLandingAtOrNearby(
            LevelReader world,
            BlockPos target,
            Direction doorFacing
    ) {
        if (world == null || target == null || doorFacing == null) {
            return Optional.empty();
        }
        if (!isColumnReadable(world, target.getX(), target.getZ())) {
            return Optional.empty();
        }
        if (isValidLanding(world, target, doorFacing)) {
            return Optional.of(target);
        }
        return findNearbyValidLanding(world, target.getX(), target.getZ(), doorFacing);
    }

    /**
     * Tries a small spiral of columns around {@code originX/Z} in already-readable chunks.
     */
    public static Optional<BlockPos> findNearbyValidLanding(
            LevelReader world,
            int originX,
            int originZ,
            Direction doorFacing
    ) {
        if (world == null || doorFacing == null) {
            return Optional.empty();
        }
        for (int radius = 1; radius <= NEARBY_SPIRAL_MAX_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }
                    int x = originX + dx;
                    int z = originZ + dz;
                    if (!isColumnReadable(world, x, z)) {
                        continue;
                    }
                    Optional<BlockPos> candidate = findSurfaceInColumn(world, x, z, doorFacing);
                    if (candidate.isPresent()) {
                        return candidate;
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
            LevelReader world,
            BlockPos searchOrigin,
            Direction doorFacing
    ) {
        if (world == null || searchOrigin == null || doorFacing == null) {
            return Optional.empty();
        }
        if (!isColumnReadable(world, searchOrigin.getX(), searchOrigin.getZ())) {
            return Optional.empty();
        }
        Optional<BlockPos> landing = findSurfaceInColumn(
                world, searchOrigin.getX(), searchOrigin.getZ(), doorFacing);
        if (landing.isPresent()) {
            return landing;
        }
        return findNearbyValidLanding(world, searchOrigin.getX(), searchOrigin.getZ(), doorFacing);
    }

    /**
     * Exclusive top Y of the playable interior when {@code hasCeiling} is set
     * (vanilla Nether: {@code minY + logicalHeight} = 128). Empty when there is no ceiling.
     */
    public static OptionalInt ceilingExclusiveY(boolean hasCeiling, int minY, int logicalHeight) {
        if (!hasCeiling) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(minY + logicalHeight);
    }

    /**
     * {@link #ceilingExclusiveY(boolean, int, int)} from {@code world}'s dimension type.
     */
    public static OptionalInt ceilingExclusiveY(@Nullable LevelReader world) {
        if (world == null) {
            return OptionalInt.empty();
        }
        DimensionType type = dimensionTypeOf(world);
        if (type == null) {
            return OptionalInt.empty();
        }
        return ceilingExclusiveY(type.hasCeiling(), world.getMinY(), type.logicalHeight());
    }

    /**
     * Heightmap surface, or the highest safe floor below a dimensional ceiling.
     * Empty when the column has no valid shell cell.
     */
    public static Optional<BlockPos> findSurfaceInColumn(
            LevelReader world,
            int x,
            int z,
            Direction doorFacing
    ) {
        return findSurfaceInColumn(world, x, z, doorFacing, ceilingExclusiveY(world));
    }

    /**
     * Like {@link #findSurfaceInColumn(LevelReader, int, int, Direction)} with an explicit ceiling cap.
     */
    public static Optional<BlockPos> findSurfaceInColumn(
            LevelReader world,
            int x,
            int z,
            Direction doorFacing,
            OptionalInt ceilingExclusiveY
    ) {
        if (world == null || doorFacing == null) {
            return Optional.empty();
        }
        int heightmapY = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (ceilingExclusiveY.isEmpty() || heightmapY < ceilingExclusiveY.getAsInt()) {
            BlockPos landing = new BlockPos(x, heightmapY, z);
            if (isValidLanding(world, landing, doorFacing)) {
                return Optional.of(landing);
            }
            return Optional.empty();
        }
        int minY = world.getMinY();
        int startY = ceilingExclusiveY.getAsInt() - 1;
        for (int y = startY; y > minY; y--) {
            BlockPos candidate = new BlockPos(x, y, z);
            if (!isReplaceable(world.getBlockState(candidate))) {
                continue;
            }
            if (isDryValidLanding(world, candidate, doorFacing)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
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
        if (!isColumnReadable(world, door.getX(), door.getZ())) {
            return false;
        }
        BlockState doorFeet = world.getBlockState(door);
        BlockState doorHead = world.getBlockState(door.above());
        return isReplaceable(doorFeet) && isReplaceable(doorHead);
    }

    /**
     * True when {@code world} can read the column without generating (FULL chunk, or non-server reader).
     */
    public static boolean isColumnReadable(@Nullable LevelReader world, int blockX, int blockZ) {
        if (!(world instanceof ServerLevel serverLevel)) {
            return world != null;
        }
        return isChunkLoaded(serverLevel, blockX, blockZ);
    }

    public static boolean isChunkLoaded(@Nullable ServerLevel world, int blockX, int blockZ) {
        if (world == null) {
            return false;
        }
        return world.getChunkSource().getChunkNow(blockX >> 4, blockZ >> 4) != null;
    }

    /**
     * True when every chunk in the Chebyshev {@code chunkRadius} around {@code center} is FULL.
     */
    public static boolean isRegionLoaded(@Nullable ServerLevel world, @Nullable BlockPos center, int chunkRadius) {
        if (world == null || center == null) {
            return false;
        }
        int originX = center.getX() >> 4;
        int originZ = center.getZ() >> 4;
        int radius = Math.max(0, chunkRadius);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (world.getChunkSource().getChunkNow(originX + dx, originZ + dz) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int ticketChunkRadius(boolean scatter) {
        return scatter ? SCATTER_TICKET_CHUNK_RADIUS : NEARBY_TICKET_CHUNK_RADIUS;
    }

    private static boolean isDryValidLanding(LevelReader world, BlockPos pos, Direction doorFacing) {
        if (!isValidLanding(world, pos, doorFacing)) {
            return false;
        }
        BlockPos door = pos.relative(doorFacing);
        return isDry(world.getBlockState(pos))
                && isDry(world.getBlockState(pos.above()))
                && isDry(world.getBlockState(door))
                && isDry(world.getBlockState(door.above()));
    }

    private static boolean isDry(BlockState state) {
        return state.getFluidState().isEmpty();
    }

    private static boolean isReplaceable(BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }

    @Nullable
    private static DimensionType dimensionTypeOf(LevelReader world) {
        if (world instanceof Level level) {
            return level.dimensionType();
        }
        return null;
    }
}
