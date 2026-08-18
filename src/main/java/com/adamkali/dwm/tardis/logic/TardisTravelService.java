package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.sound.DWMSounds;
import com.adamkali.dwm.tardis.TardisExteriorFacing;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisExteriorLocation;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.soto.SotoExteriorIndex;
import com.adamkali.dwm.tardis.portal.PortalStreamSyncService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-authoritative dematerialise → in-flight wait → lever-gated materialise loop for exterior shells.
 * Demat/mat phase lengths are code constants so loopable travel SFX can match gameplay duration.
 */
public final class TardisTravelService {
    /** Demat phase length after the door is closed (ticks). Loop SFX runs for this long. */
    public static final int DEMATERIALISING_DURATION_TICKS = 200;
    /** Elapsed ticks after door-closed before exterior shell is removed. Must be &lt; duration. */
    public static final int DEMATERIALISING_SHELL_REMOVE_AT_TICK = 80;
    /** Mat phase length after shell placed (ticks). Loop SFX runs for this long. */
    public static final int MATERIALISING_DURATION_TICKS = 160;

    private static final Set<UUID> ACTIVE = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<UUID, ShellSnapshot> FLIGHT_SHELLS = new ConcurrentHashMap<>();
    private static final Set<UUID> SHELL_REMOVED = ConcurrentHashMap.newKeySet();

    /**
     * Reason code for the most recent {@link #requestMaterialise} failure that left the TARDIS in flight.
     * Console/UI agents may map this to a player-facing overlay. Cleared on success or abort.
     */
    public static final String FAIL_PLAYER_OFFLINE = "player_offline";
    public static final String FAIL_INVALID_LANDING = "invalid_landing";

    private static @Nullable String lastMaterialiseFailureReason;

    private TardisTravelService() {
    }

    public static @Nullable String peekLastMaterialiseFailureReason() {
        return lastMaterialiseFailureReason;
    }

    /** Test/helper: clear the last materialise failure reason. */
    public static void clearLastMaterialiseFailureReason() {
        lastMaterialiseFailureReason = null;
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(TardisTravelService::onEndTick);
    }

    /**
     * Begins travel for {@code tardisId} toward its selected destination (biome, waypoint, or player).
     *
     * @return {@link InteractionResult#SUCCESS} when travel started,
     * {@link InteractionResult#FAIL} when preconditions fail,
     * {@link InteractionResult#PASS} when already traveling
     */
    public static InteractionResult startTravel(UUID tardisId, MinecraftServer server) {
        if (tardisId == null) {
            return InteractionResult.FAIL;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            return InteractionResult.FAIL;
        }
        if (model.getTravelPhase().isTraveling()) {
            return InteractionResult.PASS;
        }
        if (server == null) {
            return InteractionResult.FAIL;
        }
        if (!model.hasExteriorLocation || model.exteriorDimension == null) {
            return InteractionResult.FAIL;
        }
        if (!hasValidDestinationSelection(model)) {
            return InteractionResult.FAIL;
        }

        DestinationMode mode = model.getDestinationMode();
        String destinationDimension = null;
        String destinationBiome = null;
        UUID travelPlayerUuid = null;
        int destX = 0;
        int destY = 0;
        int destZ = 0;
        int destRotation = 0;

        switch (mode) {
            case WAYPOINT -> {
                TardisWaypoint waypoint = WaypointLogic.find(model, model.selectedWaypointId).orElse(null);
                if (waypoint == null || waypoint.dimension == null || waypoint.dimension.isBlank()) {
                    return InteractionResult.FAIL;
                }
                destinationDimension = waypoint.dimension;
                destX = waypoint.x;
                destY = waypoint.y;
                destZ = waypoint.z;
                destRotation = waypoint.rotation;
            }
            case FAST_RETURN -> {
                TardisExteriorLocation location = FastReturnLogic.selected(model).orElse(null);
                if (location == null || location.dimension == null || location.dimension.isBlank()) {
                    return InteractionResult.FAIL;
                }
                destinationDimension = location.dimension;
                destX = location.x;
                destY = location.y;
                destZ = location.z;
                destRotation = location.rotation;
            }
            case PLAYER -> {
                if (!PlayerLocatorLogic.isOnline(server, model.selectedPlayerUuid)) {
                    return InteractionResult.FAIL;
                }
                ServerPlayer target = PlayerLocatorLogic.resolve(server, model.selectedPlayerUuid).orElse(null);
                if (target == null) {
                    return InteractionResult.FAIL;
                }
                destinationDimension = target.level().dimension().identifier().toString();
                travelPlayerUuid = model.selectedPlayerUuid;
            }
            case BIOME -> {
                destinationDimension = TardisLogic.effectiveDestinationDimension(model);
                if (destinationDimension == null || destinationDimension.isBlank()) {
                    return InteractionResult.FAIL;
                }
                boolean requiresBiome = BiomeSelectorLogic.tagForDimension(destinationDimension).isPresent();
                if (requiresBiome) {
                    if (model.selectedBiome == null || model.selectedBiome.isBlank()) {
                        return InteractionResult.FAIL;
                    }
                    if (LandingSiteLogic.parseBiome(model.selectedBiome).isEmpty()) {
                        return InteractionResult.FAIL;
                    }
                }
                destinationBiome = model.selectedBiome;
            }
            case TELEPATHIC -> {
                if (!PlayerLocatorLogic.isOnline(server, model.selectedPlayerUuid)) {
                    return InteractionResult.FAIL;
                }
                ServerPlayer target = PlayerLocatorLogic.resolve(server, model.selectedPlayerUuid).orElse(null);
                if (target == null) {
                    return InteractionResult.FAIL;
                }
                TelepathicCircuitLogic.Destination home = TelepathicCircuitLogic.resolveFor(target);
                destinationDimension = home.dimensionId();
                destX = home.x();
                destY = home.y();
                destZ = home.z();
                destRotation = model.exteriorRotation;
                travelPlayerUuid = model.selectedPlayerUuid;
            }
        }

        if (destinationDimension == null || destinationDimension.isBlank() || level(server, destinationDimension) == null) {
            return InteractionResult.FAIL;
        }

        ServerLevel exteriorWorld = getExteriorWorld(server, model);
        if (exteriorWorld == null) {
            return InteractionResult.FAIL;
        }
        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        if (!(exteriorWorld.getBlockEntity(exteriorPos) instanceof TardisBlockEntity)) {
            return InteractionResult.FAIL;
        }

        model.travelDestinationMode = mode;
        model.travelDestinationDimension = destinationDimension;
        model.travelDestinationBiome = destinationBiome;
        model.travelDestinationX = destX;
        model.travelDestinationY = destY;
        model.travelDestinationZ = destZ;
        model.travelDestinationRotation = destRotation;
        model.travelTargetPlayerUuid = travelPlayerUuid;
        model.travelPhaseTicks = 0;
        model.setTravelPhase(TardisTravelPhase.DEMATERIALISING);
        if (model.doorState.isOpen || model.doorState.doorSwing > 0.0f) {
            model.doorState.isOpen = false;
            model.setChanged();
            PortalStreamSyncService.setMetaChanged(tardisId);
        }
        lastMaterialiseFailureReason = null;
        ACTIVE.add(tardisId);
        return InteractionResult.SUCCESS;
    }

    /**
     * Begins materialisation while {@code tardisId} is {@link TardisTravelPhase#IN_FLIGHT}.
     *
     * @return {@link InteractionResult#SUCCESS} when materialisation started,
     * {@link InteractionResult#FAIL} when preconditions fail,
     * {@link InteractionResult#PASS} when not in a phase that accepts materialise
     */
    public static InteractionResult requestMaterialise(UUID tardisId, MinecraftServer server) {
        if (tardisId == null) {
            return InteractionResult.FAIL;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            return InteractionResult.FAIL;
        }
        if (model.getTravelPhase() != TardisTravelPhase.IN_FLIGHT) {
            return InteractionResult.PASS;
        }
        if (server == null) {
            return InteractionResult.FAIL;
        }

        lastMaterialiseFailureReason = null;
        ShellSnapshot snapshot = FLIGHT_SHELLS.get(tardisId);
        if (snapshot == null) {
            abortToIdle(server, tardisId, model);
            return InteractionResult.FAIL;
        }

        DestinationMode mode = effectiveTravelMode(model);
        ServerLevel destinationWorld;
        BlockPos landing;
        int facingRotation = snapshot.facingRotation();
        if (isExactCoordMode(mode)) {
            facingRotation = model.travelDestinationRotation;
        }
        Direction doorFacing = TardisExteriorFacing.doorDirection(facingRotation);

        if (mode == DestinationMode.PLAYER) {
            Optional<ServerPlayer> target = PlayerLocatorLogic.resolve(server, model.travelTargetPlayerUuid);
            if (target.isEmpty()) {
                // Stay in flight — do not silently land elsewhere.
                lastMaterialiseFailureReason = FAIL_PLAYER_OFFLINE;
                return InteractionResult.FAIL;
            }
            ServerPlayer player = target.get();
            destinationWorld = (ServerLevel) player.level();
            Optional<BlockPos> resolved = resolvePlayerLanding(
                    destinationWorld, player.blockPosition(), doorFacing);
            if (resolved.isEmpty()) {
                lastMaterialiseFailureReason = FAIL_INVALID_LANDING;
                return InteractionResult.FAIL;
            }
            landing = resolved.get();
        } else {
            destinationWorld = getDestinationWorld(server, model);
            if (destinationWorld == null) {
                abortToIdle(server, tardisId, model);
                return InteractionResult.FAIL;
            }
            BlockPos oldPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
            Optional<BlockPos> resolved = resolveLanding(destinationWorld, model, oldPos, doorFacing);
            if (resolved.isEmpty() && isExactCoordMode(mode)) {
                lastMaterialiseFailureReason = FAIL_INVALID_LANDING;
                return InteractionResult.FAIL;
            }
            landing = resolved.orElse(oldPos);
        }

        Optional<BlockPos> scattered = StabiliserLogic.applyScatter(
                destinationWorld,
                landing,
                doorFacing,
                model,
                destinationWorld.getRandom()
        );
        if (scattered.isEmpty()) {
            lastMaterialiseFailureReason = FAIL_INVALID_LANDING;
            return InteractionResult.FAIL;
        }
        landing = CoordinateLockLogic.apply(scattered.get(), model);
        if (!LandingSiteLogic.isValidLanding(destinationWorld, landing, doorFacing)) {
            lastMaterialiseFailureReason = FAIL_INVALID_LANDING;
            return InteractionResult.FAIL;
        }

        FastReturnLogic.pushDeparted(model);
        placeShell(destinationWorld, landing, snapshot, facingRotation);
        if (destinationWorld.getBlockEntity(landing) instanceof TardisBlockEntity be) {
            be.setSyncedTravelPhase(TardisTravelPhase.MATERIALISING, destinationWorld.getGameTime());
        }
        model.setExteriorLocation(
                destinationWorld.dimension().identifier().toString(),
                landing.getX(),
                landing.getY(),
                landing.getZ(),
                facingRotation
        );
        FastReturnLogic.resetIndexAfterLanding(model);
        SotoExteriorIndex.register(tardisId, model);
        PortalStreamSyncService.setMetaChanged(tardisId);

        if (!model.doorsLocked) {
            model.doorState.isOpen = true;
            model.doorState.doorSwing = 0.0f;
        }
        model.travelPhaseTicks = MATERIALISING_DURATION_TICKS;
        model.setChanged();
        model.setTravelPhase(TardisTravelPhase.MATERIALISING);
        FLIGHT_SHELLS.remove(tardisId);
        SHELL_REMOVED.remove(tardisId);
        lastMaterialiseFailureReason = null;
        ACTIVE.add(tardisId);
        TardisTravelAudio.startMat(server, tardisId, destinationWorld, landing);
        return InteractionResult.SUCCESS;
    }

    public static boolean isTraveling(@Nullable UUID tardisId) {
        if (tardisId == null) {
            return false;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        return model != null && model.getTravelPhase().isTraveling();
    }

    /**
     * Advances dematerialising countdown toward {@link TardisTravelPhase#IN_FLIGHT}.
     * Package-visible for unit tests without a full server tick loop.
     *
     * @return {@code true} if the model transitioned to {@link TardisTravelPhase#IN_FLIGHT}
     */
    static boolean advanceDematerialisingHold(TardisDataModel model) {
        if (model == null || model.getTravelPhase() != TardisTravelPhase.DEMATERIALISING) {
            return false;
        }
        if (model.travelPhaseTicks > 0) {
            model.travelPhaseTicks--;
            model.setChanged();
            if (model.travelPhaseTicks > 0) {
                return false;
            }
        }
        model.travelPhaseTicks = 0;
        model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);
        return true;
    }

    /**
     * Advances materialising countdown toward {@link TardisTravelPhase#IDLE}.
     * Package-visible for unit tests.
     *
     * @return {@code true} if the model transitioned to {@link TardisTravelPhase#IDLE}
     */
    static boolean advanceMaterialisingHold(TardisDataModel model) {
        if (model == null || model.getTravelPhase() != TardisTravelPhase.MATERIALISING) {
            return false;
        }
        if (model.travelPhaseTicks > 0) {
            model.travelPhaseTicks--;
            model.setChanged();
            if (model.travelPhaseTicks > 0) {
                return false;
            }
        }
        model.clearTravelDestinationSnapshot();
        model.travelPhaseTicks = 0;
        model.setTravelPhase(TardisTravelPhase.IDLE);
        return true;
    }

    /** Whether demat countdown has reached the configured shell-removal elapsed tick. */
    static boolean shouldRemoveShell(TardisDataModel model) {
        if (model == null || model.getTravelPhase() != TardisTravelPhase.DEMATERIALISING) {
            return false;
        }
        int elapsed = DEMATERIALISING_DURATION_TICKS - model.travelPhaseTicks;
        return elapsed >= DEMATERIALISING_SHELL_REMOVE_AT_TICK;
    }

    private static void onEndTick(MinecraftServer server) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        for (UUID tardisId : Set.copyOf(ACTIVE)) {
            tickOne(server, tardisId);
        }
    }

    private static void tickOne(MinecraftServer server, UUID tardisId) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            ACTIVE.remove(tardisId);
            return;
        }

        switch (model.getTravelPhase()) {
            case DEMATERIALISING -> tickDematerialising(server, tardisId, model);
            case IN_FLIGHT -> {
                // Wait for lever-gated {@link #requestMaterialise}.
            }
            case MATERIALISING -> tickMaterialising(server, tardisId, model);
            case IDLE -> ACTIVE.remove(tardisId);
        }
    }

    private static void tickDematerialising(MinecraftServer server, UUID tardisId, TardisDataModel model) {
        // Countdown window: shell may still be present until remove-at tick.
        if (FLIGHT_SHELLS.containsKey(tardisId)) {
            if (!SHELL_REMOVED.contains(tardisId) && shouldRemoveShell(model)) {
                removeExteriorShell(server, tardisId, model);
            }
            boolean enteredFlight = advanceDematerialisingHold(model);
            if (enteredFlight) {
                BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
                ServerLevel exteriorWorld = getExteriorWorld(server, model);
                TardisTravelAudio.startFlight(server, tardisId, exteriorWorld, exteriorPos);
                SHELL_REMOVED.remove(tardisId);
                // FLIGHT_SHELLS kept for materialise.
            }
            return;
        }

        // Door-close prelude before the configurable demat/vworp window.
        ServerLevel exteriorWorld = getExteriorWorld(server, model);
        TardisLogic.updateDoorState(tardisId, exteriorWorld);
        if (model.doorState.doorSwing > 0.0f) {
            PortalStreamSyncService.setMetaChanged(tardisId);
            return;
        }

        if (exteriorWorld == null) {
            abortToIdle(server, tardisId, model);
            return;
        }

        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        if (!(exteriorWorld.getBlockEntity(exteriorPos) instanceof TardisBlockEntity be)) {
            abortToIdle(server, tardisId, model);
            return;
        }

        ShellSnapshot snapshot = ShellSnapshot.capture(be, exteriorWorld.getBlockState(exteriorPos));
        FLIGHT_SHELLS.put(tardisId, snapshot);
        SHELL_REMOVED.remove(tardisId);
        model.travelPhaseTicks = DEMATERIALISING_DURATION_TICKS;
        model.setChanged();
        be.setSyncedTravelPhase(TardisTravelPhase.DEMATERIALISING, exteriorWorld.getGameTime());
        TardisTravelAudio.startDemat(server, tardisId, exteriorWorld, exteriorPos);
    }

    private static void removeExteriorShell(MinecraftServer server, UUID tardisId, TardisDataModel model) {
        ServerLevel exteriorWorld = getExteriorWorld(server, model);
        if (exteriorWorld == null) {
            return;
        }
        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        if (exteriorWorld.getBlockEntity(exteriorPos) instanceof TardisBlockEntity) {
            exteriorWorld.setBlock(exteriorPos, Blocks.AIR.defaultBlockState(), 3);
        }
        SotoExteriorIndex.unregister(tardisId);
        PortalStreamSyncService.setMetaChanged(tardisId);
        SHELL_REMOVED.add(tardisId);
        model.setChanged();
    }

    private static void tickMaterialising(MinecraftServer server, UUID tardisId, TardisDataModel model) {
        TardisLogic.updateDoorState(tardisId, getExteriorWorld(server, model));
        PortalStreamSyncService.setMetaChanged(tardisId);
        boolean finished = advanceMaterialisingHold(model);
        if (!finished) {
            return;
        }
        ACTIVE.remove(tardisId);
        ServerLevel exteriorWorld = getExteriorWorld(server, model);
        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        if (exteriorWorld != null && exteriorWorld.getBlockEntity(exteriorPos) instanceof TardisBlockEntity be) {
            be.setSyncedTravelPhase(TardisTravelPhase.IDLE, exteriorWorld.getGameTime());
        }
        TardisTravelAudio.stop(server, tardisId, exteriorWorld, exteriorPos);
        playMaterialiseThud(server, tardisId, exteriorWorld, exteriorPos);
    }

    private static void playMaterialiseThud(
            MinecraftServer server,
            UUID tardisId,
            @Nullable ServerLevel exteriorWorld,
            BlockPos exteriorPos
    ) {
        if (exteriorWorld != null) {
            exteriorWorld.playSound(
                    null,
                    exteriorPos,
                    DWMSounds.TARDIS_MATERIALISE_THUD,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
        }
        if (server == null) {
            return;
        }
        ServerLevel interior = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interior == null) {
            return;
        }
        BlockPos console = TardisTravelAudio.consolePos(tardisId);
        interior.playSound(
                null,
                console,
                DWMSounds.TARDIS_MATERIALISE_THUD,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
    }

    private static Optional<BlockPos> resolveLanding(
            ServerLevel world,
            TardisDataModel model,
            BlockPos searchOrigin,
            Direction doorFacing
    ) {
        DestinationMode mode = effectiveTravelMode(model);
        if (isExactCoordMode(mode)) {
            return exactCoordTargetFromSnapshot(model)
                    .flatMap(target -> LandingSiteLogic.findLandingAtOrNearby(world, target, doorFacing));
        }
        Optional<ResourceKey<Biome>> biome = LandingSiteLogic.parseBiome(model.travelDestinationBiome);
        if (biome.isPresent()) {
            Optional<BlockPos> landing = LandingSiteLogic.findLanding(
                    world, biome.get(), searchOrigin, doorFacing);
            if (landing.isPresent()) {
                return landing;
            }
        }
        return LandingSiteLogic.findSurfaceLanding(world, searchOrigin, doorFacing);
    }

    private static Optional<BlockPos> resolvePlayerLanding(
            ServerLevel world,
            BlockPos playerPos,
            Direction doorFacing
    ) {
        return LandingSiteLogic.findLandingAtOrNearby(world, playerPos, doorFacing);
    }

    /**
     * Pure destination-mode selection check (no exterior-block or loaded-world checks).
     * Package-visible for unit tests.
     */
    static boolean hasValidDestinationSelection(@Nullable TardisDataModel model) {
        if (model == null) {
            return false;
        }
        return switch (model.getDestinationMode()) {
            case BIOME -> {
                String dim = TardisLogic.effectiveDestinationDimension(model);
                if (dim == null || dim.isBlank()) {
                    yield false;
                }
                boolean requiresBiome = BiomeSelectorLogic.tagForDimension(dim).isPresent();
                if (!requiresBiome) {
                    yield true;
                }
                yield model.selectedBiome != null
                        && !model.selectedBiome.isBlank()
                        && LandingSiteLogic.parseBiome(model.selectedBiome).isPresent();
            }
            case WAYPOINT -> WaypointLogic.find(model, model.selectedWaypointId).isPresent();
            case PLAYER -> model.selectedPlayerUuid != null;
            case FAST_RETURN -> FastReturnLogic.hasSelection(model);
            case TELEPATHIC -> TelepathicCircuitLogic.hasSelection(model);
        };
    }

    static boolean isExactCoordMode(@Nullable DestinationMode mode) {
        return mode == DestinationMode.WAYPOINT
                || mode == DestinationMode.FAST_RETURN
                || mode == DestinationMode.TELEPATHIC;
    }

    static DestinationMode effectiveTravelMode(@Nullable TardisDataModel model) {
        if (model == null) {
            return DestinationMode.BIOME;
        }
        if (model.travelDestinationMode != null) {
            return model.travelDestinationMode;
        }
        return model.getDestinationMode();
    }

    /**
     * Pure: BlockPos from flight exact-coord snapshot fields (waypoint or fast return).
     * Package-visible for unit tests.
     */
    static Optional<BlockPos> waypointTargetFromSnapshot(@Nullable TardisDataModel model) {
        return exactCoordTargetFromSnapshot(model);
    }

    static Optional<BlockPos> exactCoordTargetFromSnapshot(@Nullable TardisDataModel model) {
        if (model == null || !isExactCoordMode(effectiveTravelMode(model))) {
            return Optional.empty();
        }
        return Optional.of(new BlockPos(
                model.travelDestinationX,
                model.travelDestinationY,
                model.travelDestinationZ
        ));
    }

    private static void abortToIdle(MinecraftServer server, UUID tardisId, TardisDataModel model) {
        ShellSnapshot snapshot = FLIGHT_SHELLS.remove(tardisId);
        SHELL_REMOVED.remove(tardisId);
        ServerLevel exteriorWorld = getExteriorWorld(server, model);
        BlockPos pos = model.hasExteriorLocation
                ? new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ)
                : BlockPos.ZERO;
        TardisTravelAudio.stop(server, tardisId, exteriorWorld, pos);
        if (snapshot != null && exteriorWorld != null && model.hasExteriorLocation) {
            if (!(exteriorWorld.getBlockEntity(pos) instanceof TardisBlockEntity)) {
                placeShell(exteriorWorld, pos, snapshot);
                SotoExteriorIndex.register(tardisId, model);
                PortalStreamSyncService.setMetaChanged(tardisId);
            }
        }
        model.clearTravelDestinationSnapshot();
        model.travelPhaseTicks = 0;
        model.setTravelPhase(TardisTravelPhase.IDLE);
        lastMaterialiseFailureReason = null;
        ACTIVE.remove(tardisId);
    }

    private static void placeShell(ServerLevel world, BlockPos pos, ShellSnapshot snapshot) {
        placeShell(world, pos, snapshot, snapshot.facingRotation());
    }

    private static void placeShell(
            ServerLevel world,
            BlockPos pos,
            ShellSnapshot snapshot,
            int facingRotation
    ) {
        world.getChunk(pos);
        BlockState state = DWMBlocks.TARDIS_BLOCK.defaultBlockState()
                .setValue(TardisBlock.FACING_ROTATION, facingRotation);
        world.setBlock(pos, state, 3);
        if (world.getBlockEntity(pos) instanceof TardisBlockEntity be) {
            be.restoreTravelIdentity(snapshot.tardisId(), snapshot.interiorEntrance(), snapshot.interiorGenerated());
        }
    }

    private static @Nullable ServerLevel getExteriorWorld(MinecraftServer server, TardisDataModel model) {
        return level(server, model.exteriorDimension);
    }

    private static @Nullable ServerLevel getDestinationWorld(MinecraftServer server, TardisDataModel model) {
        String destination = model.travelDestinationDimension;
        if (destination == null || destination.isBlank()) {
            destination = TardisLogic.effectiveDestinationDimension(model);
        }
        return level(server, destination);
    }

    private static @Nullable ServerLevel level(MinecraftServer server, @Nullable String dimensionId) {
        if (dimensionId == null || dimensionId.isBlank()) {
            return null;
        }
        Identifier id = Identifier.tryParse(dimensionId);
        if (id == null) {
            return null;
        }
        return server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
    }

    private record ShellSnapshot(
            UUID tardisId,
            @Nullable BlockPos interiorEntrance,
            boolean interiorGenerated,
            int facingRotation
    ) {
        static ShellSnapshot capture(TardisBlockEntity be, BlockState state) {
            int rotation = state.hasProperty(TardisBlock.FACING_ROTATION)
                    ? state.getValue(TardisBlock.FACING_ROTATION)
                    : 0;
            return new ShellSnapshot(
                    be.getTardisId(),
                    be.getInteriorEntrance(),
                    be.isInteriorGenerated(),
                    rotation
            );
        }
    }

    /** Test helper: clear in-memory travel tracking. */
    public static void clearActiveForTests() {
        ACTIVE.clear();
        FLIGHT_SHELLS.clear();
        SHELL_REMOVED.clear();
        lastMaterialiseFailureReason = null;
    }

    /** Test helper: seed a flight shell snapshot without a world. */
    static void putFlightShellForTests(UUID tardisId, UUID shellTardisId) {
        FLIGHT_SHELLS.put(tardisId, new ShellSnapshot(shellTardisId, null, false, 0));
    }

    /** Test helper: mark shell removed for countdown tests. */
    static void markShellRemovedForTests(UUID tardisId) {
        SHELL_REMOVED.add(tardisId);
    }
}
