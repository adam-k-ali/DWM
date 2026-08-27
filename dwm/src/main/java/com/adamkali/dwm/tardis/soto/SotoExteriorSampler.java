package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalLightData;
import com.adamkali.dwm.tardis.portal.PortalSampler;
import com.adamkali.dwm.tardis.portal.PortalStreamSample;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Exterior sampling helpers for SOTO: atmosphere, ghost stream geometry, and chunk samples.
 * Relative footprint coords: min corner = {@code exteriorPos + (-5, -1, -5)}; TARDIS at (5, 1, 5).
 *
 * <p>Phase 1 ghost streaming uses a separate {@link #STREAM_RADIUS_CHUNKS} ticketed box
 * for live entity keep-alive.
 */
public final class SotoExteriorSampler extends PortalSampler {
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

    static final SotoExteriorSampler INSTANCE = new SotoExteriorSampler();

    private SotoExteriorSampler() {
        super(SIZE_X, SIZE_Y, SIZE_Z, SOTO_TICKET);
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
        return INSTANCE.isVisible(state);
    }

    @Override
    public boolean isVisible(BlockState state) {
        return state != null
                && !state.isAir()
                && !state.is(Blocks.LIGHT)
                && !state.is(DWMBlocks.TARDIS_BLOCK);
    }

    public static Map<BlockPos, BlockState> filterVisible(Map<BlockPos, BlockState> placements) {
        return INSTANCE.filterVisibleBlocks(placements);
    }

    /**
     * Samples exterior sky/fog atmosphere at the TARDIS block (single biome point).
     */
    public static PortalAtmosphere sampleAtmosphere(ServerLevel exteriorWorld, BlockPos exteriorPos) {
        return PortalSampler.sampleAtmosphere(exteriorWorld, exteriorPos);
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
        INSTANCE.addTickets(world, streamChunkBounds(exteriorPos));
    }

    /**
     * Cheap probe: queries already-loaded entities in the stream box without force-loading chunks.
     */
    public static boolean hasEntities(ServerLevel exteriorWorld, BlockPos exteriorPos) {
        return INSTANCE.hasLoadedEntities(exteriorWorld, exteriorPos);
    }

    /**
     * Resets mob despawn counters in the stream box. Tickets only — no per-tick {@code getChunk}.
     */
    public static void keepMobAiActive(ServerLevel exteriorWorld, BlockPos exteriorPos) {
        INSTANCE.resetMobAi(exteriorWorld, exteriorPos);
    }

    public static PortalStreamSample samplePortalStreamChunk(
            ServerLevel exteriorWorld,
            BlockPos exteriorPos,
            int chunkX,
            int chunkZ
    ) {
        return sampleStreamChunk(exteriorWorld, exteriorPos, chunkX, chunkZ);
    }

    /**
     * Collects non-air visible block states (+ BE NBT) for one chunk column within stream Y range.
     * Positions in the returned maps are world-absolute.
     */
    public static PortalStreamSample sampleStreamChunk(
            ServerLevel exteriorWorld,
            BlockPos exteriorPos,
            int chunkX,
            int chunkZ
    ) {
        return INSTANCE.sampleChunkColumn(exteriorWorld, exteriorPos, chunkX, chunkZ);
    }

    public static List<Entity> collectStreamEntities(ServerLevel exteriorWorld, BlockPos exteriorPos) {
        return INSTANCE.collectEntities(exteriorWorld, exteriorPos);
    }

    public static boolean isInsideFootprint(BlockPos worldPos, BlockPos footprintOrigin) {
        return INSTANCE.inFootprint(worldPos, footprintOrigin);
    }

    @Override
    protected void ensureLoaded(ServerLevel world, BlockPos anchor) {
        addStreamTickets(world, anchor);
    }

    @Override
    protected AABB entityBox(BlockPos anchor) {
        return streamBox(anchor);
    }

    @Override
    protected YRange sampleYRange(ServerLevel world, BlockPos anchor) {
        int minY = Math.max(world.getMinY(), anchor.getY() - STREAM_Y_RADIUS);
        int maxY = Math.min(world.getMinY() + world.getHeight() - 1, anchor.getY() + STREAM_Y_RADIUS);
        return yRange(minY, maxY);
    }

    @Override
    protected PortalLightData sampleLight(
            ServerLevel world,
            BlockPos anchor,
            int chunkX,
            int chunkZ,
            YRange yRange,
            Map<BlockPos, BlockState> blocks,
            int lowestVisibleY,
            int highestVisibleY
    ) {
        if (blocks.isEmpty()) {
            return PortalLightData.EMPTY;
        }
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int lightMinY = Math.max(yRange.min(), lowestVisibleY - 1);
        int lightMaxY = Math.min(yRange.max(), highestVisibleY + 1);
        return PortalLightData.sample(
                world,
                new BlockPos(baseX, lightMinY, baseZ),
                new BlockPos(baseX + 15, lightMaxY, baseZ + 15)
        );
    }
}
