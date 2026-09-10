package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.TardisExteriorFacing;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

/**
 * Deferred landing search: biome locate off the interaction thread, ticket destination
 * chunks, then validate and place on a later server tick so the lever does not hitch MSPT.
 */
public final class LandingResolveService {
    enum Phase {
        LOCATE,
        LOAD,
        VALIDATE,
        READY
    }

    enum Kind {
        TRAVEL,
        SUMMON
    }

    @FunctionalInterface
    interface BiomeLocator {
        Optional<BlockPos> locate(
                ServerLevel world,
                ResourceKey<Biome> biome,
                BlockPos searchOrigin,
                int radius
        );
    }

    private record Job(
            UUID tardisId,
            Kind kind,
            ResourceKey<Level> destinationDimension,
            Direction doorFacing,
            int facingRotation,
            boolean committed,
            @Nullable UUID requesterUuid,
            boolean needsBiomeLocate,
            @Nullable ResourceKey<Biome> biome,
            BlockPos searchOrigin,
            Phase phase,
            @Nullable BlockPos locateCenter,
            @Nullable BlockPos resolvedLanding,
            boolean locateInFlight,
            int ticketChunkRadius,
            long seq
    ) {
        Job withCommitted(@Nullable UUID requester) {
            return new Job(
                    tardisId, kind, destinationDimension, doorFacing, facingRotation, true,
                    requester != null ? requester : requesterUuid, needsBiomeLocate, biome, searchOrigin,
                    phase, locateCenter, resolvedLanding, locateInFlight, ticketChunkRadius, seq);
        }

        Job withPhase(Phase next) {
            return new Job(
                    tardisId, kind, destinationDimension, doorFacing, facingRotation, committed,
                    requesterUuid, needsBiomeLocate, biome, searchOrigin,
                    next, locateCenter, resolvedLanding, locateInFlight, ticketChunkRadius, seq);
        }

        Job withLocateCenter(@Nullable BlockPos center) {
            return new Job(
                    tardisId, kind, destinationDimension, doorFacing, facingRotation, committed,
                    requesterUuid, needsBiomeLocate, biome, searchOrigin,
                    Phase.LOAD, center, resolvedLanding, false, ticketChunkRadius, seq);
        }

        Job withResolved(@Nullable BlockPos landing, Phase next) {
            return new Job(
                    tardisId, kind, destinationDimension, doorFacing, facingRotation, committed,
                    requesterUuid, needsBiomeLocate, biome, searchOrigin,
                    next, locateCenter, landing, locateInFlight, ticketChunkRadius, seq);
        }

        Job withTicketRadius(int radius) {
            return new Job(
                    tardisId, kind, destinationDimension, doorFacing, facingRotation, committed,
                    requesterUuid, needsBiomeLocate, biome, searchOrigin,
                    Phase.LOAD, locateCenter, resolvedLanding, locateInFlight, radius, seq);
        }

        Job withLocateInFlight() {
            return new Job(
                    tardisId, kind, destinationDimension, doorFacing, facingRotation, committed,
                    requesterUuid, needsBiomeLocate, biome, searchOrigin,
                    phase, locateCenter, resolvedLanding, true, ticketChunkRadius, seq);
        }
    }

    private static final TicketType LANDING_LOAD_TICKET = new TicketType(40L, TicketType.FLAG_LOADING);
    private static final AtomicLong SEQ = new AtomicLong();
    private static final Map<UUID, Job> JOBS = new ConcurrentHashMap<>();
    private static final Executor LOCATE_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "dwm-landing-locate");
        thread.setDaemon(true);
        return thread;
    });

    private static BiomeLocator biomeLocator = LandingSiteLogic::locateClosestBiome;

    private LandingResolveService() {
    }

    public static void clear() {
        JOBS.clear();
        biomeLocator = LandingSiteLogic::locateClosestBiome;
    }

    public static int pendingJobCount() {
        return JOBS.size();
    }

    public static boolean hasJob(@Nullable UUID tardisId) {
        return tardisId != null && JOBS.containsKey(tardisId);
    }

    public static boolean isCommitted(@Nullable UUID tardisId) {
        Job job = tardisId == null ? null : JOBS.get(tardisId);
        return job != null && job.committed();
    }

    /**
     * True when a job is still searching or loading (lever should show locating overlay).
     */
    public static boolean isWaitingForLanding(@Nullable UUID tardisId) {
        Job job = tardisId == null ? null : JOBS.get(tardisId);
        return job != null && job.phase() != Phase.READY;
    }

    public static @Nullable Phase getPhase(@Nullable UUID tardisId) {
        Job job = tardisId == null ? null : JOBS.get(tardisId);
        return job == null ? null : job.phase();
    }

    public static void cancel(@Nullable UUID tardisId) {
        if (tardisId != null) {
            JOBS.remove(tardisId);
        }
    }

    static void setBiomeLocatorForTests(@Nullable BiomeLocator locator) {
        biomeLocator = locator == null ? LandingSiteLogic::locateClosestBiome : locator;
    }

    static void enqueueForTest(UUID tardisId, boolean committed, Phase phase) {
        BlockPos origin = BlockPos.ZERO;
        JOBS.put(tardisId, new Job(
                tardisId,
                Kind.TRAVEL,
                ResourceKey.create(Registries.DIMENSION, Identifier.parse("minecraft:overworld")),
                Direction.NORTH,
                0,
                committed,
                null,
                false,
                null,
                origin,
                phase,
                origin,
                phase == Phase.READY ? origin : null,
                false,
                LandingSiteLogic.NEARBY_TICKET_CHUNK_RADIUS,
                SEQ.incrementAndGet()
        ));
    }

    /**
     * Start an uncommitted travel resolve (biome/exact) so search can finish during {@code IN_FLIGHT}.
     */
    public static void prefetch(MinecraftServer server, UUID tardisId) {
        if (server == null || tardisId == null || hasJob(tardisId)) {
            return;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null || model.getTravelPhase() != TardisTravelPhase.IN_FLIGHT) {
            return;
        }
        if (TardisTravelService.effectiveTravelMode(model) == DestinationMode.PLAYER) {
            return;
        }
        startTravelJob(server, tardisId, model, false, null);
    }

    /**
     * Mark an existing job committed, or start a committed travel job.
     *
     * @return {@code false} when the job cannot start (caller should treat as FAIL)
     */
    public static boolean commit(
            MinecraftServer server,
            UUID tardisId,
            @Nullable UUID requesterUuid
    ) {
        if (server == null || tardisId == null) {
            return false;
        }
        Job existing = JOBS.get(tardisId);
        if (existing != null) {
            Job next = existing.withCommitted(requesterUuid);
            JOBS.put(tardisId, next);
            if (next.phase() == Phase.READY && next.resolvedLanding() != null) {
                tickOne(server, tardisId);
            }
            return hasJob(tardisId) || TardisDataLoader.get(tardisId) == null
                    || TardisDataLoader.get(tardisId).getTravelPhase() == TardisTravelPhase.MATERIALISING;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            return false;
        }
        return startTravelJob(server, tardisId, model, true, requesterUuid);
    }

    /**
     * Exact landing with no scatter/locks (Stattenheim / summon auto-mat).
     */
    public static boolean commitExact(
            MinecraftServer server,
            UUID tardisId,
            ServerLevel destinationWorld,
            BlockPos landing,
            int facingRotation,
            @Nullable UUID requesterUuid
    ) {
        if (server == null || tardisId == null || destinationWorld == null || landing == null) {
            return false;
        }
        Direction doorFacing = TardisExteriorFacing.doorDirection(facingRotation);
        JOBS.put(tardisId, new Job(
                tardisId,
                Kind.SUMMON,
                destinationWorld.dimension(),
                doorFacing,
                facingRotation,
                true,
                requesterUuid,
                false,
                null,
                landing.immutable(),
                Phase.LOAD,
                landing.immutable(),
                null,
                false,
                LandingSiteLogic.NEARBY_TICKET_CHUNK_RADIUS,
                SEQ.incrementAndGet()
        ));
        return true;
    }

    public static void tick(MinecraftServer server, UUID tardisId) {
        tickOne(server, tardisId);
    }

    private static boolean startTravelJob(
            MinecraftServer server,
            UUID tardisId,
            TardisDataModel model,
            boolean committed,
            @Nullable UUID requesterUuid
    ) {
        DestinationMode mode = TardisTravelService.effectiveTravelMode(model);
        int facingRotation = TardisTravelService.flightFacingRotation(tardisId, model);
        Direction doorFacing = TardisExteriorFacing.doorDirection(facingRotation);
        ServerLevel destinationWorld;
        BlockPos searchOrigin;
        boolean needsBiomeLocate = false;
        ResourceKey<Biome> biome = null;

        if (mode == DestinationMode.PLAYER) {
            Optional<ServerPlayer> target = PlayerLocatorLogic.resolve(server, model.travelTargetPlayerUuid);
            if (target.isEmpty()) {
                if (committed) {
                    TardisTravelService.setLastMaterialiseFailureReason(TardisTravelService.FAIL_PLAYER_OFFLINE);
                    overlayRequester(server, requesterUuid, "dwm.console.travel_player_offline");
                }
                return false;
            }
            ServerPlayer player = target.get();
            destinationWorld = (ServerLevel) player.level();
            searchOrigin = player.blockPosition();
        } else {
            destinationWorld = TardisTravelService.getDestinationWorld(server, model);
            if (destinationWorld == null) {
                if (committed) {
                    TardisTravelService.abortToIdle(server, tardisId, model);
                }
                return false;
            }
            searchOrigin = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
            if (TardisTravelService.isExactCoordMode(mode)) {
                searchOrigin = TardisTravelService.exactCoordTargetFromSnapshot(model).orElse(searchOrigin);
            } else {
                Optional<ResourceKey<Biome>> parsed = LandingSiteLogic.parseBiome(model.travelDestinationBiome);
                if (parsed.isPresent()) {
                    biome = parsed.get();
                    needsBiomeLocate = true;
                }
            }
        }

        int ticketRadius = LandingSiteLogic.ticketChunkRadius(
                !StabiliserLogic.isEnabled(model));
        Phase phase = needsBiomeLocate ? Phase.LOCATE : Phase.LOAD;
        BlockPos locateCenter = needsBiomeLocate ? null : searchOrigin;
        JOBS.put(tardisId, new Job(
                tardisId,
                Kind.TRAVEL,
                destinationWorld.dimension(),
                doorFacing,
                facingRotation,
                committed,
                requesterUuid,
                needsBiomeLocate,
                biome,
                searchOrigin.immutable(),
                phase,
                locateCenter,
                null,
                false,
                ticketRadius,
                SEQ.incrementAndGet()
        ));
        return true;
    }

    private static void tickOne(MinecraftServer server, UUID tardisId) {
        Job job = JOBS.get(tardisId);
        if (job == null || server == null) {
            return;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null || model.getTravelPhase() != TardisTravelPhase.IN_FLIGHT) {
            JOBS.remove(tardisId);
            return;
        }
        ServerLevel world = server.getLevel(job.destinationDimension());
        if (world == null) {
            fail(server, tardisId, job, TardisTravelService.FAIL_INVALID_LANDING);
            return;
        }

        switch (job.phase()) {
            case LOCATE -> tickLocate(server, world, job);
            case LOAD -> tickLoad(world, job);
            case VALIDATE -> tickValidate(server, world, model, job);
            case READY -> {
                if (job.committed()) {
                    tickValidate(server, world, model, job.withPhase(Phase.VALIDATE));
                }
            }
        }
    }

    private static void tickLocate(MinecraftServer server, ServerLevel world, Job job) {
        if (job.locateInFlight()) {
            return;
        }
        if (!job.needsBiomeLocate() || job.biome() == null) {
            JOBS.put(job.tardisId(), job.withLocateCenter(job.searchOrigin()));
            return;
        }
        JOBS.put(job.tardisId(), job.withLocateInFlight());
        long seq = job.seq();
        UUID tardisId = job.tardisId();
        ResourceKey<Biome> biome = job.biome();
        BlockPos origin = job.searchOrigin();
        CompletableFuture.supplyAsync(
                () -> biomeLocator.locate(world, biome, origin, LandingSiteLogic.DEFAULT_SEARCH_RADIUS),
                LOCATE_EXECUTOR
        ).whenComplete((result, error) -> server.execute(() -> onLocateDone(tardisId, seq, origin, result, error)));
    }

    private static void onLocateDone(
            UUID tardisId,
            long seq,
            BlockPos fallback,
            @Nullable Optional<BlockPos> result,
            @Nullable Throwable error
    ) {
        Job job = JOBS.get(tardisId);
        if (job == null || job.seq() != seq) {
            return;
        }
        BlockPos center = fallback;
        if (error == null && result != null && result.isPresent()) {
            center = result.get();
        }
        JOBS.put(tardisId, job.withLocateCenter(center));
    }

    private static void tickLoad(ServerLevel world, Job job) {
        BlockPos center = job.locateCenter() != null ? job.locateCenter() : job.searchOrigin();
        addLandingTickets(world, center, job.ticketChunkRadius());
        if (job.resolvedLanding() != null) {
            addLandingTickets(world, job.resolvedLanding(), job.ticketChunkRadius());
        }
        BlockPos waitAt = job.resolvedLanding() != null ? job.resolvedLanding() : center;
        if (!LandingSiteLogic.isRegionLoaded(world, waitAt, job.ticketChunkRadius())) {
            return;
        }
        JOBS.put(job.tardisId(), job.withPhase(Phase.VALIDATE));
    }

    private static void tickValidate(
            MinecraftServer server,
            ServerLevel world,
            TardisDataModel model,
            Job job
    ) {
        if (job.kind() == Kind.SUMMON) {
            Optional<BlockPos> landing = LandingSiteLogic.findLandingAtOrNearby(
                    world, job.searchOrigin(), job.doorFacing());
            if (landing.isEmpty()) {
                fail(server, job.tardisId(), job, TardisTravelService.FAIL_INVALID_LANDING);
                return;
            }
            if (!job.committed()) {
                JOBS.put(job.tardisId(), job.withResolved(landing.get(), Phase.READY));
                return;
            }
            place(server, job.tardisId(), world, landing.get(), job.facingRotation());
            return;
        }

        BlockPos resolved = job.resolvedLanding();
        if (resolved == null) {
            BlockPos center = job.locateCenter() != null ? job.locateCenter() : job.searchOrigin();
            DestinationMode mode = TardisTravelService.effectiveTravelMode(model);
            Optional<BlockPos> found;
            if (TardisTravelService.isExactCoordMode(mode) || mode == DestinationMode.PLAYER) {
                found = LandingSiteLogic.findLandingAtOrNearby(world, job.searchOrigin(), job.doorFacing());
            } else {
                found = LandingSiteLogic.findSurfaceLanding(world, center, job.doorFacing());
                if (found.isEmpty() && !center.equals(job.searchOrigin())) {
                    JOBS.put(job.tardisId(), job.withLocateCenter(job.searchOrigin()));
                    return;
                }
            }
            if (found.isEmpty()) {
                if (TardisTravelService.isExactCoordMode(mode) || mode == DestinationMode.PLAYER) {
                    fail(server, job.tardisId(), job, TardisTravelService.FAIL_INVALID_LANDING);
                    return;
                }
                found = Optional.of(job.searchOrigin());
            }
            resolved = found.get();
            int neededRadius = LandingSiteLogic.ticketChunkRadius(!StabiliserLogic.isEnabled(model));
            if (neededRadius > job.ticketChunkRadius()) {
                JOBS.put(job.tardisId(), job.withResolved(resolved, Phase.LOAD).withTicketRadius(neededRadius));
                return;
            }
            job = job.withResolved(resolved, Phase.VALIDATE);
            JOBS.put(job.tardisId(), job);
        }

        if (!job.committed()) {
            JOBS.put(job.tardisId(), job.withResolved(resolved, Phase.READY));
            return;
        }

        int neededRadius = LandingSiteLogic.ticketChunkRadius(!StabiliserLogic.isEnabled(model));
        if (neededRadius > job.ticketChunkRadius()
                || !LandingSiteLogic.isRegionLoaded(world, resolved, neededRadius)) {
            JOBS.put(job.tardisId(), job.withResolved(resolved, Phase.LOAD).withTicketRadius(neededRadius));
            return;
        }

        Optional<BlockPos> finished = TardisTravelService.finishTravelLanding(
                world, model, resolved, job.doorFacing());
        if (finished.isEmpty()) {
            fail(server, job.tardisId(), job, TardisTravelService.FAIL_INVALID_LANDING);
            return;
        }
        place(server, job.tardisId(), world, finished.get(), job.facingRotation());
    }

    private static void place(
            MinecraftServer server,
            UUID tardisId,
            ServerLevel world,
            BlockPos landing,
            int facingRotation
    ) {
        JOBS.remove(tardisId);
        TardisTravelService.completeResolvedMaterialise(tardisId, server, world, landing, facingRotation);
    }

    private static void fail(MinecraftServer server, UUID tardisId, Job job, String reason) {
        JOBS.remove(tardisId);
        if (!job.committed()) {
            return;
        }
        TardisTravelService.setLastMaterialiseFailureReason(reason);
        String key = TardisTravelService.FAIL_PLAYER_OFFLINE.equals(reason)
                ? "dwm.console.travel_player_offline"
                : "dwm.console.travel_invalid_landing";
        overlayRequester(server, job.requesterUuid(), key);
    }

    private static void overlayRequester(MinecraftServer server, @Nullable UUID requester, String key) {
        if (server == null || requester == null || key == null) {
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(requester);
        if (player != null) {
            player.sendOverlayMessage(Component.translatable(key));
        }
    }

    static void addLandingTickets(ServerLevel world, BlockPos center, int chunkRadius) {
        if (world == null || center == null) {
            return;
        }
        world.getChunkSource().addTicketWithRadius(
                LANDING_LOAD_TICKET,
                new ChunkPos(center.getX() >> 4, center.getZ() >> 4),
                Math.max(0, chunkRadius));
    }
}
