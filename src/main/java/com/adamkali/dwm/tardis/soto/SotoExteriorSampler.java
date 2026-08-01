package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationPropertyHelper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

/**
 * Samples a view-distance-capped exterior cube for SOTO sync, with portal-style visibility
 * culling from the exterior door. Relative coords are TARDIS-centered (exterior block = 0,0,0).
 */
public final class SotoExteriorSampler {
    /** Hard cap on client view distance used for SOTO (chunks). */
    public static final int MAX_RADIUS_CHUNKS = 8;
    public static final int MIN_RADIUS_CHUNKS = 1;
    public static final int DEFAULT_RADIUS_CHUNKS = MAX_RADIUS_CHUNKS;

    public static final int MAX_RADIUS_BLOCKS = MAX_RADIUS_CHUNKS * 16;

    /**
     * Hard cap on flood-fill cell visits so open sky cannot expand to the full RD cube.
     * Surfaces already collected remain in the sample.
     */
    public static final int MAX_FLOOD_VISITS = 48_000;

    /** Relative position of the exterior TARDIS block within the sample space. */
    public static final BlockPos RELATIVE_TARDIS_POS = BlockPos.ORIGIN;

    private static final ChunkTicketType<ChunkPos> SOTO_TICKET =
            ChunkTicketType.create("dwm_soto", Comparator.comparingLong(ChunkPos::toLong), 80);

    private static final Direction[] NEIGHBORS = Direction.values();

    private SotoExteriorSampler() {
    }

    public static int clampRadiusChunks(int viewDistanceChunks) {
        return Math.max(MIN_RADIUS_CHUNKS, Math.min(MAX_RADIUS_CHUNKS, viewDistanceChunks));
    }

    public static int radiusBlocks(int radiusChunks) {
        return clampRadiusChunks(radiusChunks) * 16;
    }

    /** Min corner of the max-capped cube centered on {@code exteriorPos}. */
    public static BlockPos maxFootprintOrigin(BlockPos exteriorPos) {
        return exteriorPos.add(-MAX_RADIUS_BLOCKS, -MAX_RADIUS_BLOCKS, -MAX_RADIUS_BLOCKS);
    }

    /** Min corner of the cube for a specific radius. */
    public static BlockPos footprintOrigin(BlockPos exteriorPos, int radiusChunks) {
        int r = radiusBlocks(radiusChunks);
        return exteriorPos.add(-r, -r, -r);
    }

    /**
     * @deprecated Prefer {@link #footprintOrigin(BlockPos, int)} or {@link #maxFootprintOrigin(BlockPos)}.
     * Kept for callers that want the max dirty AABB origin.
     */
    @Deprecated
    public static BlockPos footprintOrigin(BlockPos exteriorPos) {
        return maxFootprintOrigin(exteriorPos);
    }

    public static BlockPos toRelative(BlockPos worldPos, BlockPos exteriorPos) {
        return worldPos.subtract(exteriorPos);
    }

    public static BlockPos toWorld(BlockPos relative, BlockPos exteriorPos) {
        return exteriorPos.add(relative);
    }

    public static boolean isWithinChebyshev(BlockPos relative, int radiusBlocks) {
        return Math.max(Math.abs(relative.getX()), Math.max(Math.abs(relative.getY()), Math.abs(relative.getZ())))
                <= radiusBlocks;
    }

    /**
     * Whether a world position lies inside the max-capped SOTO dirty cube around the exterior.
     */
    public static boolean isInsideMaxFootprint(BlockPos worldPos, BlockPos exteriorPos) {
        return isWithinChebyshev(toRelative(worldPos, exteriorPos), MAX_RADIUS_BLOCKS);
    }

    /**
     * @deprecated Use {@link #isInsideMaxFootprint(BlockPos, BlockPos)} with exterior pos.
     * Interprets the second arg as footprint origin of the max cube.
     */
    @Deprecated
    public static boolean isInsideFootprint(BlockPos worldPos, BlockPos footprintOrigin) {
        BlockPos exterior = footprintOrigin.add(MAX_RADIUS_BLOCKS, MAX_RADIUS_BLOCKS, MAX_RADIUS_BLOCKS);
        return isInsideMaxFootprint(worldPos, exterior);
    }

    /**
     * Whether a block should appear in the interior SOTO preview.
     * The exterior TARDIS block is excluded (drawn as a synthetic chameleon shell).
     */
    public static boolean isSotoVisible(BlockState state) {
        return state != null
                && !state.isAir()
                && !state.isOf(Blocks.LIGHT)
                && !state.isOf(DWMBlocks.TARDIS_BLOCK);
    }

    public static Map<BlockPos, BlockState> filterVisible(Map<BlockPos, BlockState> placements) {
        Map<BlockPos, BlockState> visible = new HashMap<>();
        for (Map.Entry<BlockPos, BlockState> entry : placements.entrySet()) {
            if (isSotoVisible(entry.getValue())) {
                visible.put(entry.getKey(), entry.getValue());
            }
        }
        return visible;
    }

    public static Direction facingFromRotation(int exteriorRotation) {
        float yaw = RotationPropertyHelper.toDegrees(exteriorRotation);
        return Direction.fromHorizontalDegrees(yaw);
    }

    /**
     * Flood-fill visibility sample. Keys are TARDIS-relative positions.
     * {@code blockAccess} returns world block states for absolute positions.
     */
    public static VisibilitySample collectVisible(
            BlockPos exteriorPos,
            int radiusChunks,
            int exteriorRotation,
            Function<BlockPos, BlockState> blockAccess
    ) {
        int r = radiusBlocks(radiusChunks);
        Direction facing = facingFromRotation(exteriorRotation);
        Map<BlockPos, BlockState> visible = new HashMap<>();
        Set<BlockPos> floodedRel = new HashSet<>();
        Set<BlockPos> visitedRel = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        List<BlockPos> seeds = doorSeeds(exteriorPos, facing);
        for (BlockPos seedWorld : seeds) {
            BlockPos seedRel = toRelative(seedWorld, exteriorPos);
            if (!isWithinChebyshev(seedRel, r) || !visitedRel.add(seedRel)) {
                continue;
            }
            BlockState seedState = blockAccess.apply(seedWorld);
            if (!canFloodThrough(seedState)) {
                if (isSotoVisible(seedState)) {
                    visible.put(seedRel, seedState);
                }
                continue;
            }
            queue.add(seedRel);
            floodedRel.add(seedRel);
            if (isSotoVisible(seedState)) {
                visible.put(seedRel, seedState);
            }
        }

        BlockPos.Mutable worldMutable = new BlockPos.Mutable();
        while (!queue.isEmpty() && visitedRel.size() < MAX_FLOOD_VISITS) {
            BlockPos rel = queue.poll();
            for (Direction dir : NEIGHBORS) {
                BlockPos nextRel = rel.offset(dir);
                if (!isWithinChebyshev(nextRel, r) || !visitedRel.add(nextRel)) {
                    continue;
                }
                if (visitedRel.size() > MAX_FLOOD_VISITS) {
                    break;
                }
                worldMutable.set(
                        exteriorPos.getX() + nextRel.getX(),
                        exteriorPos.getY() + nextRel.getY(),
                        exteriorPos.getZ() + nextRel.getZ()
                );
                BlockState nextState = blockAccess.apply(worldMutable);
                if (canFloodThrough(nextState)) {
                    floodedRel.add(nextRel);
                    queue.add(nextRel);
                    if (isSotoVisible(nextState)) {
                        visible.put(nextRel.toImmutable(), nextState);
                    }
                } else if (isSotoVisible(nextState)) {
                    visible.put(nextRel.toImmutable(), nextState);
                }
            }
        }

        return new VisibilitySample(Map.copyOf(visible), Set.copyOf(floodedRel));
    }

    public static Map<BlockPos, BlockState> sample(
            ServerWorld exteriorWorld,
            BlockPos exteriorPos,
            int radiusChunks,
            int exteriorRotation
    ) {
        return sampleAll(exteriorWorld, exteriorPos, radiusChunks, exteriorRotation).blocks();
    }

    public static Map<BlockPos, NbtCompound> sampleBlockEntities(
            ServerWorld exteriorWorld,
            BlockPos exteriorPos,
            int radiusChunks,
            int exteriorRotation
    ) {
        return sampleAll(exteriorWorld, exteriorPos, radiusChunks, exteriorRotation).blockEntities();
    }

    /**
     * Single flood-fill pass producing blocks, block-entity NBT, and entity samples.
     */
    public static FullSample sampleAll(
            ServerWorld exteriorWorld,
            BlockPos exteriorPos,
            int radiusChunks,
            int exteriorRotation
    ) {
        int clamped = clampRadiusChunks(radiusChunks);
        ensureFootprintChunksLoaded(exteriorWorld, exteriorPos, clamped);
        VisibilitySample visibility = collectVisible(
                exteriorPos, clamped, exteriorRotation, exteriorWorld::getBlockState
        );
        RegistryWrapper.WrapperLookup registries = exteriorWorld.getRegistryManager();
        Map<BlockPos, NbtCompound> blockEntities = new HashMap<>();
        for (BlockPos rel : visibility.blocks().keySet()) {
            BlockPos worldPos = toWorld(rel, exteriorPos);
            BlockEntity blockEntity = exteriorWorld.getBlockEntity(worldPos);
            if (blockEntity == null) {
                continue;
            }
            blockEntities.put(rel, BotiInteriorSampler.captureSyncNbt(blockEntity, registries));
        }
        List<BotiEntitySample> entities = sampleEntitiesFromVisibility(
                exteriorWorld, exteriorPos, clamped, visibility
        );
        return new FullSample(
                visibility.blocks(),
                Map.copyOf(blockEntities),
                entities,
                visibility.floodedRel()
        );
    }

    public static Box footprintBox(BlockPos exteriorPos, int radiusChunks) {
        int r = radiusBlocks(radiusChunks);
        return new Box(
                exteriorPos.getX() - r,
                exteriorPos.getY() - r,
                exteriorPos.getZ() - r,
                exteriorPos.getX() + r + 1,
                exteriorPos.getY() + r + 1,
                exteriorPos.getZ() + r + 1
        );
    }

    public static Box maxFootprintBox(BlockPos exteriorPos) {
        return footprintBox(exteriorPos, MAX_RADIUS_CHUNKS);
    }

    public static int[] footprintChunkBounds(BlockPos exteriorPos, int radiusChunks) {
        int r = radiusBlocks(radiusChunks);
        return new int[]{
                ChunkSectionPos.getSectionCoord(exteriorPos.getX() - r),
                ChunkSectionPos.getSectionCoord(exteriorPos.getX() + r),
                ChunkSectionPos.getSectionCoord(exteriorPos.getZ() - r),
                ChunkSectionPos.getSectionCoord(exteriorPos.getZ() + r)
        };
    }

    /**
     * Adds SOTO chunk tickets for the footprint. Does <strong>not</strong> synchronously load
     * chunks — use {@link #ensureFootprintChunksLoaded} when sampling needs block data now.
     */
    public static void addFootprintTickets(ServerWorld world, BlockPos exteriorPos, int radiusChunks) {
        if (world == null || exteriorPos == null) {
            return;
        }
        int[] bounds = footprintChunkBounds(exteriorPos, radiusChunks);
        var chunkManager = world.getChunkManager();
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                ChunkPos chunkPos = new ChunkPos(cx, cz);
                chunkManager.addTicket(SOTO_TICKET, chunkPos, 2, chunkPos);
            }
        }
    }

    /**
     * Tickets the footprint and synchronously loads chunks for an immediate sample.
     * Avoid calling this every tick — prefer {@link #addFootprintTickets} for keep-alive.
     */
    public static void ensureFootprintChunksLoaded(ServerWorld world, BlockPos exteriorPos, int radiusChunks) {
        if (world == null || exteriorPos == null) {
            return;
        }
        addFootprintTickets(world, exteriorPos, radiusChunks);
        int[] bounds = footprintChunkBounds(exteriorPos, radiusChunks);
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                world.getChunk(cx, cz);
            }
        }
    }

    /**
     * Cheap entity occupancy probe over already-loaded chunks. Does not force-load.
     */
    public static boolean hasEntities(ServerWorld exteriorWorld, BlockPos exteriorPos, int radiusChunks) {
        if (exteriorWorld == null || exteriorPos == null) {
            return false;
        }
        int clamped = clampRadiusChunks(radiusChunks);
        return !exteriorWorld.getOtherEntities(
                null, footprintBox(exteriorPos, clamped), entity -> !entity.isRemoved()
        ).isEmpty();
    }

    public static boolean hasEntities(ServerWorld exteriorWorld, BlockPos exteriorPos) {
        return hasEntities(exteriorWorld, exteriorPos, DEFAULT_RADIUS_CHUNKS);
    }

    /**
     * Resets mob despawn counters in the SOTO radius when no players are in the exterior world.
     * Does not force-load chunks — callers must keep tickets alive separately.
     */
    public static void keepMobAiActive(ServerWorld exteriorWorld, BlockPos exteriorPos, int radiusChunks) {
        if (exteriorWorld == null || exteriorPos == null) {
            return;
        }
        if (!exteriorWorld.getPlayers().isEmpty()) {
            return;
        }
        int clamped = clampRadiusChunks(radiusChunks);
        for (Entity entity : exteriorWorld.getOtherEntities(
                null, footprintBox(exteriorPos, clamped), e -> !e.isRemoved()
        )) {
            if (entity instanceof MobEntity mob && mob.getDespawnCounter() != 0) {
                mob.setDespawnCounter(0);
            }
        }
    }

    public static void keepMobAiActive(ServerWorld exteriorWorld, BlockPos exteriorPos) {
        keepMobAiActive(exteriorWorld, exteriorPos, DEFAULT_RADIUS_CHUNKS);
    }

    /**
     * Re-samples entities only (no terrain flood). Entities in the radius box that sit in or
     * beside {@code floodedRel} are included; if {@code floodedRel} is empty, all entities in
     * the box are included.
     */
    public static List<BotiEntitySample> sampleEntitiesOnly(
            ServerWorld exteriorWorld,
            BlockPos exteriorPos,
            int radiusChunks,
            Set<BlockPos> floodedRel
    ) {
        int clamped = clampRadiusChunks(radiusChunks);
        List<Entity> found = exteriorWorld.getOtherEntities(
                null, footprintBox(exteriorPos, clamped), entity -> !entity.isRemoved()
        );
        if (found.isEmpty()) {
            return List.of();
        }
        boolean filterByFlood = floodedRel != null && !floodedRel.isEmpty();
        List<BotiEntitySample> samples = new ArrayList<>();
        for (Entity entity : found) {
            if (filterByFlood && !isEntityInVisibleVolume(entity, exteriorPos, floodedRel)) {
                continue;
            }
            BotiEntitySample sample = captureEntity(entity, exteriorPos);
            if (sample != null) {
                samples.add(sample);
            }
        }
        return List.copyOf(samples);
    }

    public static List<BotiEntitySample> sampleEntities(
            ServerWorld exteriorWorld,
            BlockPos exteriorPos,
            int radiusChunks,
            int exteriorRotation
    ) {
        return sampleAll(exteriorWorld, exteriorPos, radiusChunks, exteriorRotation).entities();
    }

    public static BotiEntitySample captureEntity(Entity entity, BlockPos exteriorPos) {
        return BotiInteriorSampler.captureEntity(entity, exteriorPos);
    }

    public record VisibilitySample(Map<BlockPos, BlockState> blocks, Set<BlockPos> floodedRel) {
    }

    public record FullSample(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntities,
            List<BotiEntitySample> entities,
            Set<BlockPos> floodedRel
    ) {
        public FullSample {
            floodedRel = floodedRel == null ? Set.of() : Set.copyOf(floodedRel);
        }
    }

    private static List<BotiEntitySample> sampleEntitiesFromVisibility(
            ServerWorld exteriorWorld,
            BlockPos exteriorPos,
            int radiusChunks,
            VisibilitySample visibility
    ) {
        List<Entity> found = exteriorWorld.getOtherEntities(
                null, footprintBox(exteriorPos, radiusChunks), entity -> !entity.isRemoved()
        );
        if (found.isEmpty()) {
            return List.of();
        }
        List<BotiEntitySample> samples = new ArrayList<>();
        for (Entity entity : found) {
            if (!isEntityInVisibleVolume(entity, exteriorPos, visibility.floodedRel())) {
                continue;
            }
            BotiEntitySample sample = captureEntity(entity, exteriorPos);
            if (sample != null) {
                samples.add(sample);
            }
        }
        return List.copyOf(samples);
    }

    private static List<BlockPos> doorSeeds(BlockPos exteriorPos, Direction facing) {
        BlockPos outside = exteriorPos.offset(facing);
        return List.of(outside, outside.up());
    }

    private static boolean canFloodThrough(BlockState state) {
        if (state == null || state.isOf(DWMBlocks.TARDIS_BLOCK)) {
            return false;
        }
        return !state.isOpaqueFullCube();
    }

    private static boolean isEntityInVisibleVolume(
            Entity entity,
            BlockPos exteriorPos,
            Set<BlockPos> floodedRel
    ) {
        BlockPos feet = BlockPos.ofFloored(entity.getX(), entity.getY(), entity.getZ());
        BlockPos rel = toRelative(feet, exteriorPos);
        if (floodedRel.contains(rel)) {
            return true;
        }
        for (Direction dir : NEIGHBORS) {
            if (floodedRel.contains(rel.offset(dir))) {
                return true;
            }
        }
        return false;
    }
}
