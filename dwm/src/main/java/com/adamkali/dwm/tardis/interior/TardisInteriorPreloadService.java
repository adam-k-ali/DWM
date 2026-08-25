package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.boti.BotiPlotIndex;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Deferred interior placement for BOTI warmup: tickets footprint chunks, waits for vanilla async
 * load, then places on a later server tick so approach does not hitch MSPT.
 */
public final class TardisInteriorPreloadService {
    /** At most one structure place per server tick across all TARDISes. */
    private static final int MAX_PLACES_PER_TICK = 1;

    enum Phase {
        LOADING,
        READY_TO_PLACE
    }

    private record Job(
            UUID tardisId,
            ResourceKey<Level> exteriorDimension,
            BlockPos exteriorPos,
            Phase phase
    ) {
        Job withPhase(Phase next) {
            return new Job(tardisId, exteriorDimension, exteriorPos, next);
        }
    }

    private static final Map<UUID, Job> JOBS = new ConcurrentHashMap<>();

    private TardisInteriorPreloadService() {
    }

    public static void clear() {
        JOBS.clear();
    }

    /** Test helper: pending / in-flight job count. */
    public static int pendingJobCount() {
        return JOBS.size();
    }

    /** Test helper: whether a job exists for {@code tardisId}. */
    public static boolean hasJob(UUID tardisId) {
        return tardisId != null && JOBS.containsKey(tardisId);
    }

    /** Test helper: current phase, or null. */
    public static @Nullable Phase getPhase(UUID tardisId) {
        Job job = tardisId == null ? null : JOBS.get(tardisId);
        return job == null ? null : job.phase();
    }

    /**
     * Cancels an in-flight job (e.g. sync {@code ensureInterior} won the race).
     */
    public static void cancel(UUID tardisId) {
        if (tardisId != null) {
            JOBS.remove(tardisId);
        }
    }

    /**
     * O(1) enqueue when a player is near the exterior. No world writes on this call.
     */
    public static void requestPreload(ServerLevel exteriorWorld, TardisBlockEntity exteriorEntity) {
        if (exteriorWorld == null || exteriorEntity == null || exteriorWorld.isClientSide()) {
            return;
        }
        if (!DWMConfig.getBoolean(DWMConfig.ENABLE_DOOR_PORTALS)) {
            return;
        }
        UUID tardisId = exteriorEntity.getTardisIdOrNull();
        if (tardisId == null) {
            return;
        }
        if (JOBS.containsKey(tardisId)) {
            return;
        }
        if (exteriorEntity.isInteriorGenerated() && exteriorEntity.getInteriorEntrance() != null) {
            BotiPlotIndex.register(tardisId);
            return;
        }
        JOBS.put(
                tardisId,
                new Job(
                        tardisId,
                        exteriorWorld.dimension(),
                        exteriorEntity.getBlockPos().immutable(),
                        Phase.LOADING
                )
        );
    }

    /**
     * Advances deferred jobs: ticket → wait for chunks → place (amortized).
     */
    public static void tick(MinecraftServer server) {
        if (server == null || JOBS.isEmpty()) {
            return;
        }
        ServerLevel interiorWorld = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interiorWorld == null) {
            return;
        }

        int placesThisTick = 0;
        List<UUID> toRemove = new ArrayList<>();
        for (UUID tardisId : List.copyOf(JOBS.keySet())) {
            Job job = JOBS.get(tardisId);
            if (job == null) {
                continue;
            }
            BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
            if (job.phase() == Phase.LOADING) {
                BotiInteriorSampler.addFootprintTickets(interiorWorld, origin);
                if (!BotiInteriorSampler.areFootprintChunksLoaded(interiorWorld, origin)) {
                    continue;
                }
                job = job.withPhase(Phase.READY_TO_PLACE);
                JOBS.put(tardisId, job);
            }
            if (job.phase() != Phase.READY_TO_PLACE) {
                continue;
            }
            if (placesThisTick >= MAX_PLACES_PER_TICK) {
                continue;
            }
            if (completePlace(server, interiorWorld, job)) {
                placesThisTick++;
            }
            // Drop whether place succeeded or exterior vanished — avoid spinning.
            toRemove.add(tardisId);
        }
        for (UUID id : toRemove) {
            JOBS.remove(id);
        }
    }

    private static boolean completePlace(MinecraftServer server, ServerLevel interiorWorld, Job job) {
        ServerLevel exteriorWorld = server.getLevel(job.exteriorDimension());
        if (exteriorWorld == null) {
            return false;
        }
        if (!(exteriorWorld.getBlockEntity(job.exteriorPos()) instanceof TardisBlockEntity exterior)) {
            return false;
        }
        UUID beId = exterior.getTardisIdOrNull();
        if (beId == null || !beId.equals(job.tardisId())) {
            return false;
        }
        if (exterior.isInteriorGenerated() && exterior.getInteriorEntrance() != null) {
            BotiPlotIndex.register(job.tardisId());
            return true;
        }
        return TardisInteriorService.placeInteriorDeferred(exteriorWorld, exterior, interiorWorld);
    }

    /**
     * Test helper: inject a LOADING job without going through {@link #requestPreload}.
     */
    static void enqueueForTest(UUID tardisId, ResourceKey<Level> exteriorDimension, BlockPos exteriorPos) {
        JOBS.put(tardisId, new Job(tardisId, exteriorDimension, exteriorPos, Phase.LOADING));
    }

    /**
     * Test helper: force a job into READY_TO_PLACE.
     */
    static void markReadyToPlaceForTest(UUID tardisId) {
        Job job = JOBS.get(tardisId);
        if (job != null) {
            JOBS.put(tardisId, job.withPhase(Phase.READY_TO_PLACE));
        }
    }
}
