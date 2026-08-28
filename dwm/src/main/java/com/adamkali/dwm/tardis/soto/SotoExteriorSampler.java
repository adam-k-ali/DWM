package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalLightData;
import com.adamkali.dwm.tardis.portal.PortalSampler;
import com.adamkali.dwm.tardis.portal.PortalStreamSample;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * Exterior sampling helpers for SOTO: atmosphere, hitch geometry, and chunk samples.
 * Relative hitch coords: min corner = {@code exteriorPos + (-5, -1, -5)}; TARDIS at (5, 1, 5).
 *
 * <p>Ghost streaming uses a Chebyshev box sized from Minecraft view distance. LOADING tickets
 * cover that radius; SIMULATION tickets use server simulation distance.
 */
public final class SotoExteriorSampler extends PortalSampler {
    public static final int SIZE_X = 11;
    public static final int SIZE_Y = 7;
    public static final int SIZE_Z = 11;

    /** Relative position of the exterior TARDIS block within the hitch footprint. */
    public static final BlockPos RELATIVE_TARDIS_POS = new BlockPos(5, 1, 5);

    /** Offset from exterior block pos to hitch footprint min corner. */
    public static final BlockPos FOOTPRINT_MIN_OFFSET = new BlockPos(-5, -1, -5);

    static final SotoExteriorSampler INSTANCE = new SotoExteriorSampler();

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
     * Chunk bounds for ghost streaming: Chebyshev radius around the exterior block's chunk.
     * Returns {@code [minCX, maxCX, minCZ, maxCZ]}.
     */
    public static int[] streamChunkBounds(BlockPos exteriorPos, int radiusChunks) {
        return PortalSampler.streamChunkBounds(exteriorPos, radiusChunks);
    }

    public static int[] streamChunkBounds(BlockPos exteriorPos) {
        return streamChunkBounds(exteriorPos, PortalSampler.DEFAULT_STREAM_RADIUS_CHUNKS);
    }

    public static int[] streamChunkBounds(ServerLevel world, BlockPos exteriorPos) {
        return streamChunkBounds(exteriorPos, PortalSampler.streamRadiusChunks(world));
    }

    /** Axis-aligned box covering the ghost stream radius (horizontal chunks + vertical radius). */
    public static AABB streamBox(BlockPos exteriorPos, int radiusChunks) {
        return PortalSampler.streamBox(
                exteriorPos, radiusChunks, PortalSampler.streamYRadiusBlocks(radiusChunks));
    }

    public static AABB streamBox(BlockPos exteriorPos) {
        return streamBox(exteriorPos, PortalSampler.DEFAULT_STREAM_RADIUS_CHUNKS);
    }

    public static boolean isInsideStreamRadius(BlockPos worldPos, BlockPos exteriorPos, int radiusChunks) {
        return PortalSampler.isInsideStreamRadius(
                worldPos, exteriorPos, radiusChunks, PortalSampler.streamYRadiusBlocks(radiusChunks));
    }

    public static boolean isInsideStreamRadius(BlockPos worldPos, BlockPos exteriorPos) {
        return isInsideStreamRadius(worldPos, exteriorPos, PortalSampler.DEFAULT_STREAM_RADIUS_CHUNKS);
    }

    /**
     * Ticket-only keep-alive for the ghost stream radius. Does not call {@code getChunk}
     * (avoids synchronous force-loads every tick).
     */
    public static void addStreamTickets(ServerLevel world, BlockPos exteriorPos) {
        if (world == null || exteriorPos == null) {
            return;
        }
        INSTANCE.addStreamTickets(world, exteriorPos, PortalSampler.streamRadiusChunks(world));
    }

    /**
     * Cheap probe: queries already-loaded entities in the simulation box without force-loading chunks.
     */
    public static boolean hasEntities(ServerLevel exteriorWorld, BlockPos exteriorPos) {
        return INSTANCE.hasLoadedEntities(exteriorWorld, exteriorPos);
    }

    /**
     * Resets mob despawn counters in the simulation box. Tickets only — no per-tick {@code getChunk}.
     */
    public static void keepMobAiActive(ServerLevel exteriorWorld, BlockPos exteriorPos) {
        INSTANCE.resetMobAi(exteriorWorld, exteriorPos);
    }

    public static @Nullable PortalStreamSample samplePortalStreamChunk(
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
     *
     * @return the sample, or {@code null} when the column is not yet {@code FULL}
     */
    public static @Nullable PortalStreamSample sampleStreamChunk(
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
        if (worldPos == null || footprintOrigin == null) {
            return false;
        }
        int localX = worldPos.getX() - footprintOrigin.getX();
        int localY = worldPos.getY() - footprintOrigin.getY();
        int localZ = worldPos.getZ() - footprintOrigin.getZ();
        return localX >= 0 && localX < SIZE_X
                && localY >= 0 && localY < SIZE_Y
                && localZ >= 0 && localZ < SIZE_Z;
    }

    @Override
    protected void ensureLoaded(ServerLevel world, BlockPos anchor) {
        addStreamTickets(world, anchor);
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
                world.getLightEngine(),
                new BlockPos(baseX, lightMinY, baseZ),
                new BlockPos(baseX + 15, lightMaxY, baseZ + 15)
        );
    }
}
