package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalLightData;
import com.adamkali.dwm.tardis.portal.PortalStreamSample;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.phys.AABB;

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

    private static final TicketType SOTO_TICKET = new TicketType(80, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION);

    private SotoExteriorSampler() {
    }

    public static BlockPos footprintOrigin(BlockPos exteriorPos) {
        return exteriorPos.offset(FOOTPRINT_MIN_OFFSET);
    }

    /**
     * Whether a block should appear in the interior SOTO preview.
     * The exterior TARDIS block is excluded (drawn as a synthetic chameleon shell historically;
     * portal terrain comes from the ghost stream, which also skips the TARDIS block).
     */
    public static boolean isSotoVisible(BlockState state) {
        return state != null
                && !state.isAir()
                && !state.is(Blocks.LIGHT)
                && !state.is(DWMBlocks.TARDIS_BLOCK);
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
    public static PortalAtmosphere sampleAtmosphere(ServerLevel exteriorWorld, BlockPos exteriorPos) {
        Identifier effectsId = exteriorWorld.dimensionTypeRegistration()
                .unwrapKey()
                .map(ResourceKey::identifier)
                .orElseGet(BuiltinDimensionTypes.OVERWORLD::identifier);
        long timeOfDay = exteriorWorld.getOverworldClockTime();
        float rain = exteriorWorld.getRainLevel(0.0f);
        float thunder = exteriorWorld.getThunderLevel(0.0f);
        var attrs = exteriorWorld.environmentAttributes();
        return new PortalAtmosphere(
                effectsId,
                timeOfDay,
                rain,
                thunder,
                attrs.getValue(EnvironmentAttributes.SKY_COLOR, exteriorPos),
                attrs.getValue(EnvironmentAttributes.FOG_COLOR, exteriorPos)
        );
    }

    /**
     * Chunk bounds for ghost streaming: Chebyshev radius {@link #STREAM_RADIUS_CHUNKS}
     * around the exterior block's chunk. Returns {@code [minCX, maxCX, minCZ, maxCZ]}.
     */
    public static int[] streamChunkBounds(BlockPos exteriorPos) {
        int cx = SectionPos.blockToSectionCoord(exteriorPos.getX());
        int cz = SectionPos.blockToSectionCoord(exteriorPos.getZ());
        return new int[]{
                cx - STREAM_RADIUS_CHUNKS,
                cx + STREAM_RADIUS_CHUNKS,
                cz - STREAM_RADIUS_CHUNKS,
                cz + STREAM_RADIUS_CHUNKS
        };
    }

    /** Axis-aligned box covering the ghost stream radius (horizontal chunks + vertical radius). */
    public static AABB streamBox(BlockPos exteriorPos) {
        int half = STREAM_RADIUS_CHUNKS * 16;
        return new AABB(
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
        int cx = SectionPos.blockToSectionCoord(worldPos.getX());
        int cz = SectionPos.blockToSectionCoord(worldPos.getZ());
        int ecx = SectionPos.blockToSectionCoord(exteriorPos.getX());
        int ecz = SectionPos.blockToSectionCoord(exteriorPos.getZ());
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
    public static void addStreamTickets(ServerLevel world, BlockPos exteriorPos) {
        if (world == null || exteriorPos == null) {
            return;
        }
        int[] bounds = streamChunkBounds(exteriorPos);
        var chunkManager = world.getChunkSource();
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                ChunkPos chunkPos = new ChunkPos(cx, cz);
                chunkManager.addTicketWithRadius(SOTO_TICKET, chunkPos, 2);
            }
        }
    }

    /**
     * Cheap probe: queries already-loaded entities in the stream box without force-loading chunks.
     */
    public static boolean hasEntities(ServerLevel exteriorWorld, BlockPos exteriorPos) {
        if (exteriorWorld == null || exteriorPos == null) {
            return false;
        }
        return !exteriorWorld.getEntities((Entity) null, streamBox(exteriorPos), entity -> !entity.isRemoved()).isEmpty();
    }

    /**
     * Resets mob despawn counters in the stream box. Tickets only — no per-tick {@code getChunk}.
     */
    public static void keepMobAiActive(ServerLevel exteriorWorld, BlockPos exteriorPos) {
        if (exteriorWorld == null || exteriorPos == null) {
            return;
        }
        if (!exteriorWorld.players().isEmpty()) {
            return;
        }
        addStreamTickets(exteriorWorld, exteriorPos);
        for (Entity entity : exteriorWorld.getEntities((Entity) null, streamBox(exteriorPos), e -> !e.isRemoved())) {
            if (entity instanceof Mob mob && mob.getNoActionTime() != 0) {
                mob.setNoActionTime(0);
            }
        }
    }

    public static PortalStreamSample samplePortalStreamChunk(
            ServerLevel exteriorWorld,
            BlockPos exteriorPos,
            int chunkX,
            int chunkZ
    ) {
        StreamChunkSample sample = sampleStreamChunk(exteriorWorld, exteriorPos, chunkX, chunkZ);
        return new PortalStreamSample(
                sample.chunkX(), sample.chunkZ(), sample.blocks(), sample.blockEntities(), sample.lightData()
        );
    }

    /**
     * Collects non-air visible block states (+ BE NBT) for one chunk column within stream Y range.
     * Positions in the returned maps are world-absolute.
     */
    public static StreamChunkSample sampleStreamChunk(
            ServerLevel exteriorWorld,
            BlockPos exteriorPos,
            int chunkX,
            int chunkZ
    ) {
        if (exteriorWorld == null || exteriorPos == null) {
            return StreamChunkSample.empty(chunkX, chunkZ);
        }
        exteriorWorld.getChunk(chunkX, chunkZ);
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        Map<BlockPos, CompoundTag> blockEntities = new HashMap<>();
        int lowestVisibleY = Integer.MAX_VALUE;
        int highestVisibleY = Integer.MIN_VALUE;
        HolderLookup.Provider registries = exteriorWorld.registryAccess();
        int minY = Math.max(exteriorWorld.getMinY(), exteriorPos.getY() - STREAM_Y_RADIUS);
        int maxY = Math.min(exteriorWorld.getMinY() + exteriorWorld.getHeight() - 1, exteriorPos.getY() + STREAM_Y_RADIUS);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
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
                    BlockPos immutable = mutable.immutable();
                    blocks.put(immutable, state);
                    lowestVisibleY = Math.min(lowestVisibleY, y);
                    highestVisibleY = Math.max(highestVisibleY, y);
                    BlockEntity blockEntity = exteriorWorld.getBlockEntity(mutable);
                    if (blockEntity != null) {
                        blockEntities.put(immutable, BotiInteriorSampler.captureSyncNbt(blockEntity, registries));
                    }
                }
            }
        }
        PortalLightData lightData = PortalLightData.EMPTY;
        if (!blocks.isEmpty()) {
            int lightMinY = Math.max(minY, lowestVisibleY - 1);
            int lightMaxY = Math.min(maxY, highestVisibleY + 1);
            lightData = PortalLightData.sample(
                    exteriorWorld,
                    new BlockPos(baseX, lightMinY, baseZ),
                    new BlockPos(baseX + 15, lightMaxY, baseZ + 15)
            );
        }
        return new StreamChunkSample(chunkX, chunkZ, blocks, blockEntities, lightData);
    }

    public static List<Entity> collectStreamEntities(ServerLevel exteriorWorld, BlockPos exteriorPos) {
        if (exteriorWorld == null || exteriorPos == null) {
            return List.of();
        }
        addStreamTickets(exteriorWorld, exteriorPos);
        return List.copyOf(exteriorWorld.getEntities((Entity) null, streamBox(exteriorPos), entity -> !entity.isRemoved()));
    }

    /** One streamed chunk column for ghost sync. */
    public record StreamChunkSample(
            int chunkX,
            int chunkZ,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, CompoundTag> blockEntities,
            PortalLightData lightData
    ) {
        public StreamChunkSample(
                int chunkX,
                int chunkZ,
                Map<BlockPos, BlockState> blocks,
                Map<BlockPos, CompoundTag> blockEntities
        ) {
            this(chunkX, chunkZ, blocks, blockEntities, PortalLightData.EMPTY);
        }

        public StreamChunkSample {
            blocks = blocks == null ? Map.of() : Map.copyOf(blocks);
            blockEntities = blockEntities == null ? Map.of() : Map.copyOf(blockEntities);
            lightData = lightData == null ? PortalLightData.EMPTY : lightData;
        }

        public static StreamChunkSample empty(int chunkX, int chunkZ) {
            return new StreamChunkSample(chunkX, chunkZ, Map.of(), Map.of(), PortalLightData.EMPTY);
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
