package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.sound.DWMSounds;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.soto.SotoExteriorIndex;
import com.adamkali.dwm.tardis.soto.SotoExteriorSyncService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
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

    private TardisTravelService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(TardisTravelService::onEndTick);
    }

    /**
     * Begins travel for {@code tardisId} toward its current {@code selectedBiome}.
     *
     * @return {@link ActionResult#SUCCESS} when travel started,
     * {@link ActionResult#FAIL} when preconditions fail,
     * {@link ActionResult#PASS} when already traveling
     */
    public static ActionResult startTravel(UUID tardisId, MinecraftServer server) {
        if (tardisId == null) {
            return ActionResult.FAIL;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            return ActionResult.FAIL;
        }
        if (model.getTravelPhase().isTraveling()) {
            return ActionResult.PASS;
        }
        if (server == null) {
            return ActionResult.FAIL;
        }
        if (!model.hasExteriorLocation || model.exteriorDimension == null) {
            return ActionResult.FAIL;
        }
        if (model.selectedBiome == null || model.selectedBiome.isBlank()) {
            return ActionResult.FAIL;
        }
        if (LandingSiteLogic.parseBiome(model.selectedBiome).isEmpty()) {
            return ActionResult.FAIL;
        }

        ServerWorld exteriorWorld = getExteriorWorld(server, model);
        if (exteriorWorld == null) {
            return ActionResult.FAIL;
        }
        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        if (!(exteriorWorld.getBlockEntity(exteriorPos) instanceof TardisBlockEntity)) {
            return ActionResult.FAIL;
        }

        model.travelDestinationBiome = model.selectedBiome;
        model.travelPhaseTicks = 0;
        model.setTravelPhase(TardisTravelPhase.DEMATERIALISING);
        if (model.doorState.isOpen || model.doorState.doorSwing > 0.0f) {
            model.doorState.isOpen = false;
            model.markDirty();
            SotoExteriorSyncService.markDirty(tardisId);
        }
        ACTIVE.add(tardisId);
        return ActionResult.SUCCESS;
    }

    /**
     * Begins materialisation while {@code tardisId} is {@link TardisTravelPhase#IN_FLIGHT}.
     *
     * @return {@link ActionResult#SUCCESS} when materialisation started,
     * {@link ActionResult#FAIL} when preconditions fail,
     * {@link ActionResult#PASS} when not in a phase that accepts materialise
     */
    public static ActionResult requestMaterialise(UUID tardisId, MinecraftServer server) {
        if (tardisId == null) {
            return ActionResult.FAIL;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            return ActionResult.FAIL;
        }
        if (model.getTravelPhase() != TardisTravelPhase.IN_FLIGHT) {
            return ActionResult.PASS;
        }
        if (server == null) {
            return ActionResult.FAIL;
        }

        ServerWorld exteriorWorld = getExteriorWorld(server, model);
        ShellSnapshot snapshot = FLIGHT_SHELLS.get(tardisId);
        if (exteriorWorld == null || snapshot == null) {
            abortToIdle(server, tardisId, model);
            return ActionResult.FAIL;
        }

        BlockPos oldPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        BlockPos landing = resolveLanding(exteriorWorld, model, oldPos).orElse(oldPos);

        placeShell(exteriorWorld, landing, snapshot);
        model.setExteriorLocation(
                exteriorWorld.getRegistryKey().getValue().toString(),
                landing.getX(),
                landing.getY(),
                landing.getZ(),
                snapshot.facingRotation()
        );
        SotoExteriorIndex.register(tardisId, model);
        SotoExteriorSyncService.markDirty(tardisId);

        model.doorState.isOpen = true;
        model.doorState.doorSwing = 0.0f;
        model.travelPhaseTicks = MATERIALISING_DURATION_TICKS;
        model.markDirty();
        model.setTravelPhase(TardisTravelPhase.MATERIALISING);
        FLIGHT_SHELLS.remove(tardisId);
        SHELL_REMOVED.remove(tardisId);
        ACTIVE.add(tardisId);
        TardisTravelAudio.startMat(server, tardisId, exteriorWorld, landing);
        return ActionResult.SUCCESS;
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
            model.markDirty();
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
            model.markDirty();
            if (model.travelPhaseTicks > 0) {
                return false;
            }
        }
        model.travelDestinationBiome = null;
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
                ServerWorld exteriorWorld = getExteriorWorld(server, model);
                TardisTravelAudio.stop(server, tardisId, exteriorWorld, exteriorPos);
                SHELL_REMOVED.remove(tardisId);
                // FLIGHT_SHELLS kept for materialise.
            }
            return;
        }

        // Door-close prelude before the configurable demat/vworp window.
        TardisLogic.updateDoorState(tardisId);
        if (model.doorState.doorSwing > 0.0f) {
            SotoExteriorSyncService.markDirty(tardisId);
            return;
        }

        ServerWorld exteriorWorld = getExteriorWorld(server, model);
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
        model.markDirty();
        TardisTravelAudio.startDemat(server, tardisId, exteriorWorld, exteriorPos);
    }

    private static void removeExteriorShell(MinecraftServer server, UUID tardisId, TardisDataModel model) {
        ServerWorld exteriorWorld = getExteriorWorld(server, model);
        if (exteriorWorld == null) {
            return;
        }
        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        if (exteriorWorld.getBlockEntity(exteriorPos) instanceof TardisBlockEntity) {
            exteriorWorld.setBlockState(exteriorPos, Blocks.AIR.getDefaultState(), 3);
        }
        SotoExteriorIndex.unregister(tardisId);
        SotoExteriorSyncService.markDirty(tardisId);
        SHELL_REMOVED.add(tardisId);
        model.markDirty();
    }

    private static void tickMaterialising(MinecraftServer server, UUID tardisId, TardisDataModel model) {
        TardisLogic.updateDoorState(tardisId);
        SotoExteriorSyncService.markDirty(tardisId);
        boolean finished = advanceMaterialisingHold(model);
        if (!finished) {
            return;
        }
        ACTIVE.remove(tardisId);
        ServerWorld exteriorWorld = getExteriorWorld(server, model);
        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        TardisTravelAudio.stop(server, tardisId, exteriorWorld, exteriorPos);
        playMaterialiseThud(server, tardisId, exteriorWorld, exteriorPos);
    }

    private static void playMaterialiseThud(
            MinecraftServer server,
            UUID tardisId,
            @Nullable ServerWorld exteriorWorld,
            BlockPos exteriorPos
    ) {
        if (exteriorWorld != null) {
            exteriorWorld.playSound(
                    null,
                    exteriorPos,
                    DWMSounds.TARDIS_MATERIALISE_THUD,
                    SoundCategory.BLOCKS,
                    1.0F,
                    1.0F
            );
        }
        if (server == null) {
            return;
        }
        ServerWorld interior = server.getWorld(TardisDimensions.TARDIS_WORLD_KEY);
        if (interior == null) {
            return;
        }
        BlockPos console = TardisTravelAudio.consolePos(tardisId);
        interior.playSound(
                null,
                console,
                DWMSounds.TARDIS_MATERIALISE_THUD,
                SoundCategory.BLOCKS,
                1.0F,
                1.0F
        );
    }

    private static Optional<BlockPos> resolveLanding(
            ServerWorld world,
            TardisDataModel model,
            BlockPos searchOrigin
    ) {
        Optional<RegistryKey<Biome>> biome = LandingSiteLogic.parseBiome(model.travelDestinationBiome);
        if (biome.isEmpty()) {
            return Optional.empty();
        }
        return LandingSiteLogic.findLanding(world, biome.get(), searchOrigin);
    }

    private static void abortToIdle(MinecraftServer server, UUID tardisId, TardisDataModel model) {
        ShellSnapshot snapshot = FLIGHT_SHELLS.remove(tardisId);
        SHELL_REMOVED.remove(tardisId);
        ServerWorld exteriorWorld = getExteriorWorld(server, model);
        BlockPos pos = model.hasExteriorLocation
                ? new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ)
                : BlockPos.ORIGIN;
        TardisTravelAudio.stop(server, tardisId, exteriorWorld, pos);
        if (snapshot != null && exteriorWorld != null && model.hasExteriorLocation) {
            if (!(exteriorWorld.getBlockEntity(pos) instanceof TardisBlockEntity)) {
                placeShell(exteriorWorld, pos, snapshot);
                SotoExteriorIndex.register(tardisId, model);
                SotoExteriorSyncService.markDirty(tardisId);
            }
        }
        model.travelDestinationBiome = null;
        model.travelPhaseTicks = 0;
        model.setTravelPhase(TardisTravelPhase.IDLE);
        ACTIVE.remove(tardisId);
    }

    private static void placeShell(ServerWorld world, BlockPos pos, ShellSnapshot snapshot) {
        world.getChunk(pos);
        BlockState state = DWMBlocks.TARDIS_BLOCK.getDefaultState()
                .with(TardisBlock.FACING_ROTATION, snapshot.facingRotation());
        world.setBlockState(pos, state, 3);
        if (world.getBlockEntity(pos) instanceof TardisBlockEntity be) {
            be.restoreTravelIdentity(snapshot.tardisId(), snapshot.interiorEntrance(), snapshot.interiorGenerated());
        }
    }

    private static @Nullable ServerWorld getExteriorWorld(MinecraftServer server, TardisDataModel model) {
        if (model.exteriorDimension == null) {
            return null;
        }
        Identifier id = Identifier.tryParse(model.exteriorDimension);
        if (id == null) {
            return null;
        }
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
    }

    private record ShellSnapshot(
            UUID tardisId,
            @Nullable BlockPos interiorEntrance,
            boolean interiorGenerated,
            int facingRotation
    ) {
        static ShellSnapshot capture(TardisBlockEntity be, BlockState state) {
            int rotation = state.contains(TardisBlock.FACING_ROTATION)
                    ? state.get(TardisBlock.FACING_ROTATION)
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
