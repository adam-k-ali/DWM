package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.block.DWMBlocks;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.biome.Biome;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exterior sampling helpers for SOTO: atmosphere, ghost stream geometry, and chunk samples.
 * Relative footprint coords: min corner = {@code exteriorPos + (-5, -1, -5)}; TARDIS at (5, 1, 5).
 *
 * <p>Phase 1 ghost streaming uses a separate {@link #STREAM_RADIUS_CHUNKS} ticketed box
 * for live entity keep-alive.
 */
public final class SotoExteriorSampler {
    public static final int SIZE_X = 11;
    public static final int SIZE_Y = 7;
    public static final int SIZE_Z = 11;

    /** Half-side of ghost stream radius in chunks (Chebyshev). Cap for Phase 1. */
    public static final int STREAM_RADIUS_CHUNKS = 2;

    /** Vertical half-extent (blocks) around the exterior for stream sampling / entity AABB. */
    public static final int STREAM_Y_RADIUS = 48;

    /** Relative position of the exterior TARDIS block within the footprint. */
    public static final BlockPos RELATIVE_TARDIS_POS = new BlockPos(5, 1, 5);

    /** Offset from exterior block pos to footprint min corner. */
    public static final BlockPos FOOTPRINT_MIN_OFFSET = new BlockPos(-5, -1, -5);

    private static final ChunkTicketType<ChunkPos> SOTO_TICKET =
            ChunkTicketType.create("dwm_soto", Comparator.comparingLong(ChunkPos::toLong), 80);

    private SotoExteriorSampler() {
    }

    public static BlockPos footprintOrigin(BlockPos exteriorPos) {
        return exteriorPos.add(FOOTPRINT_MIN_OFFSET);
    }

    /**
     * Whether a block should appear in the interior SOTO preview.
     * The exterior TARDIS block is excluded (drawn as a synthetic chameleon shell historically;
     * portal terrain comes from the ghost stream, which also skips the TARDIS block).
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

    /**
     * Samples exterior sky/fog atmosphere at the TARDIS block (single biome point).
     */
    public static SotoAtmosphere sampleAtmosphere(ServerWorld exteriorWorld, BlockPos exteriorPos) {
        Identifier effectsId = exteriorWorld.getDimension().effects();
        long timeOfDay = exteriorWorld.getTimeOfDay();
        float rain = exteriorWorld.getRainGradient(0.0f);
        float thunder = exteriorWorld.getThunderGradient(0.0f);
        Biome biome = exteriorWorld.getBiome(exteriorPos).value();
        return new SotoAtmosphere(
                effectsId,
                timeOfDay,
                rain,
                thunder,
                biome.getSkyColor(),
                biome.getFogColor()
        );
    }

    /**
     * Chunk bounds for ghost streaming: Chebyshev radius {@link #STREAM_RADIUS_CHUNKS}
     * around the exterior block's chunk. Returns {@code [minCX, maxCX, minCZ, maxCZ]}.
     */
    public static int[] streamChunkBounds(BlockPos exteriorPos) {
        int cx = ChunkSectionPos.getSectionCoord(exteriorPos.getX());
        int cz = ChunkSectionPos.getSectionCoord(exteriorPos.getZ());
        return new int[]{
                cx - STREAM_RADIUS_CHUNKS,
                cx + STREAM_RADIUS_CHUNKS,
                cz - STREAM_RADIUS_CHUNKS,
                cz + STREAM_RADIUS_CHUNKS
        };
    }

    /** Axis-aligned box covering the ghost stream radius (horizontal chunks + vertical radius). */
    public static Box streamBox(BlockPos exteriorPos) {
        int half = STREAM_RADIUS_CHUNKS * 16;
        return new Box(
                exteriorPos.getX() - half,
                exteriorPos.getY() - STREAM_Y_RADIUS,
                exteriorPos.getZ() - half,
                exteriorPos.getX() + half + 1,
                exteriorPos.getY() + STREAM_Y_RADIUS + 1,
                exteriorPos.getZ() + half + 1
        );
    }

    public static boolean isInsideStreamRadius(BlockPos worldPos, BlockPos exteriorPos) {
        if (worldPos == null || exteriorPos == null) {
            return false;
        }
        int cx = ChunkSectionPos.getSectionCoord(worldPos.getX());
        int cz = ChunkSectionPos.getSectionCoord(worldPos.getZ());
        int ecx = ChunkSectionPos.getSectionCoord(exteriorPos.getX());
        int ecz = ChunkSectionPos.getSectionCoord(exteriorPos.getZ());
        int dx = Math.abs(cx - ecx);
        int dz = Math.abs(cz - ecz);
        if (dx > STREAM_RADIUS_CHUNKS || dz > STREAM_RADIUS_CHUNKS) {
            return false;
        }
        int dy = Math.abs(worldPos.getY() - exteriorPos.getY());
        return dy <= STREAM_Y_RADIUS;
    }

    /**
     * Ticket-only keep-alive for the ghost stream radius. Does not call {@code getChunk}
     * (avoids synchronous force-loads every tick).
     */
    public static void addStreamTickets(ServerWorld world, BlockPos exteriorPos) {
        if (world == null || exteriorPos == null) {
            return;
        }
        int[] bounds = streamChunkBounds(exteriorPos);
        var chunkManager = world.getChunkManager();
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                ChunkPos chunkPos = new ChunkPos(cx, cz);
                chunkManager.addTicket(SOTO_TICKET, chunkPos, 2, chunkPos);
            }
        }
    }

    /**
     * Cheap probe: queries already-loaded entities in the stream box without force-loading chunks.
     */
    public static boolean hasEntities(ServerWorld exteriorWorld, BlockPos exteriorPos) {
        if (exteriorWorld == null || exteriorPos == null) {
            return false;
        }
        return !exteriorWorld.getOtherEntities(null, streamBox(exteriorPos), entity -> !entity.isRemoved()).isEmpty();
    }

    /**
     * Resets mob despawn counters in the stream box. Tickets only — no per-tick {@code getChunk}.
     */
    public static void keepMobAiActive(ServerWorld exteriorWorld, BlockPos exteriorPos) {
        if (exteriorWorld == null || exteriorPos == null) {
            return;
        }
        if (!exteriorWorld.getPlayers().isEmpty()) {
            return;
        }
        addStreamTickets(exteriorWorld, exteriorPos);
        for (Entity entity : exteriorWorld.getOtherEntities(null, streamBox(exteriorPos), e -> !e.isRemoved())) {
            if (entity instanceof MobEntity mob && mob.getDespawnCounter() != 0) {
                mob.setDespawnCounter(0);
            }
        }
    }

    /**
     * Collects non-air visible block states (+ BE NBT) for one chunk column within stream Y range.
     * Positions in the returned maps are world-absolute.
     */
    public static StreamChunkSample sampleStreamChunk(
            ServerWorld exteriorWorld,
            BlockPos exteriorPos,
            int chunkX,
            int chunkZ
    ) {
        if (exteriorWorld == null || exteriorPos == null) {
            return StreamChunkSample.empty(chunkX, chunkZ);
        }
        exteriorWorld.getChunk(chunkX, chunkZ);
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        Map<BlockPos, NbtCompound> blockEntities = new HashMap<>();
        RegistryWrapper.WrapperLookup registries = exteriorWorld.getRegistryManager();
        int minY = Math.max(exteriorWorld.getBottomY(), exteriorPos.getY() - STREAM_Y_RADIUS);
        int maxY = Math.min(exteriorWorld.getBottomY() + exteriorWorld.getHeight() - 1, exteriorPos.getY() + STREAM_Y_RADIUS);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                for (int y = minY; y <= maxY; y++) {
                    mutable.set(baseX + lx, y, baseZ + lz);
                    BlockState state = exteriorWorld.getBlockState(mutable);
                    if (!isSotoVisible(state)) {
                        continue;
                    }
                    BlockPos immutable = mutable.toImmutable();
                    blocks.put(immutable, state);
                    BlockEntity blockEntity = exteriorWorld.getBlockEntity(mutable);
                    if (blockEntity != null) {
                        blockEntities.put(immutable, BotiInteriorSampler.captureSyncNbt(blockEntity, registries));
                    }
                }
            }
        }
        return new StreamChunkSample(chunkX, chunkZ, blocks, blockEntities);
    }

    public static List<Entity> collectStreamEntities(ServerWorld exteriorWorld, BlockPos exteriorPos) {
        if (exteriorWorld == null || exteriorPos == null) {
            return List.of();
        }
        addStreamTickets(exteriorWorld, exteriorPos);
        return List.copyOf(exteriorWorld.getOtherEntities(null, streamBox(exteriorPos), entity -> !entity.isRemoved()));
    }

    /** One streamed chunk column for ghost sync. */
    public record StreamChunkSample(
            int chunkX,
            int chunkZ,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntities
    ) {
        public StreamChunkSample {
            blocks = blocks == null ? Map.of() : Map.copyOf(blocks);
            blockEntities = blockEntities == null ? Map.of() : Map.copyOf(blockEntities);
        }

        public static StreamChunkSample empty(int chunkX, int chunkZ) {
            return new StreamChunkSample(chunkX, chunkZ, Map.of(), Map.of());
        }
    }

    public static boolean isInsideFootprint(BlockPos worldPos, BlockPos footprintOrigin) {
        int localX = worldPos.getX() - footprintOrigin.getX();
        int localY = worldPos.getY() - footprintOrigin.getY();
        int localZ = worldPos.getZ() - footprintOrigin.getZ();
        return localX >= 0 && localX < SIZE_X
                && localY >= 0 && localY < SIZE_Y
                && localZ >= 0 && localZ < SIZE_Z;
    }
}
