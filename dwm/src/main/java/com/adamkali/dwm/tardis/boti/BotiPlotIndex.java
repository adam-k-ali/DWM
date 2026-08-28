package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.portal.PortalSampler;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;

/**
 * Maps allocated interior plot grid cells to TARDIS ids so block edits can dirty the right snapshot.
 */
public final class BotiPlotIndex {
    private static final Map<Long, UUID> PLOT_OWNERS = new ConcurrentHashMap<>();
    private static final Map<UUID, BlockPos> ORIGINS = new ConcurrentHashMap<>();

    private BotiPlotIndex() {
    }

    public static void clear() {
        PLOT_OWNERS.clear();
        ORIGINS.clear();
    }

    public static void register(UUID tardisId) {
        if (tardisId == null) {
            return;
        }
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        ORIGINS.put(tardisId, origin);
        PLOT_OWNERS.put(plotKey(origin), tardisId);
    }

    public static boolean isRegistered(UUID tardisId) {
        return tardisId != null && ORIGINS.containsKey(tardisId);
    }

    /** Snapshot of currently registered TARDIS ids (for entity occupancy dirtying). */
    public static Set<UUID> registeredIds() {
        return Collections.unmodifiableSet(ORIGINS.keySet());
    }

    public static @Nullable BlockPos getOrigin(UUID tardisId) {
        return ORIGINS.get(tardisId);
    }

    /**
     * Resolves the owning TARDIS for a world position in {@code dwm:tardis}, or null if outside a
     * registered plot stream.
     */
    public static @Nullable UUID resolve(BlockPos worldPos) {
        return resolve(worldPos, PortalSampler.DEFAULT_STREAM_RADIUS_CHUNKS);
    }

    public static @Nullable UUID resolve(BlockPos worldPos, int radiusChunks) {
        int gridX = Math.floorDiv(worldPos.getX(), TardisPlotAllocator.PLOT_SPACING);
        int gridZ = Math.floorDiv(worldPos.getZ(), TardisPlotAllocator.PLOT_SPACING);
        BlockPos origin = new BlockPos(
                gridX * TardisPlotAllocator.PLOT_SPACING,
                TardisPlotAllocator.PLOT_BASE_Y,
                gridZ * TardisPlotAllocator.PLOT_SPACING
        );
        UUID owner = PLOT_OWNERS.get(plotKey(origin));
        if (owner == null) {
            return null;
        }
        if (!BotiInteriorSampler.isInsidePlotStream(worldPos, origin, radiusChunks)) {
            return null;
        }
        return owner;
    }

    static long plotKey(BlockPos origin) {
        int gridX = Math.floorDiv(origin.getX(), TardisPlotAllocator.PLOT_SPACING);
        int gridZ = Math.floorDiv(origin.getZ(), TardisPlotAllocator.PLOT_SPACING);
        return ((long) gridX << 32) | (gridZ & 0xffffffffL);
    }
}
