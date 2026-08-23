package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.network.TravelAudioS2CPayload;
import com.adamkali.dwm.platform.DwmServices;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Broadcasts demat/mat/flight loop start/stop cues to exterior tracking players and interior plot occupants.
 */
public final class TardisTravelAudio {
    private static final int EXTERIOR_RANGE = 64;

    private TardisTravelAudio() {
    }

    public static void startDemat(MinecraftServer server, UUID tardisId, ServerLevel exteriorWorld, BlockPos exteriorPos) {
        broadcast(server, tardisId, TravelAudioS2CPayload.START_DEMAT, exteriorWorld, exteriorPos);
    }

    public static void startMat(MinecraftServer server, UUID tardisId, ServerLevel exteriorWorld, BlockPos exteriorPos) {
        broadcast(server, tardisId, TravelAudioS2CPayload.START_MAT, exteriorWorld, exteriorPos);
    }

    /**
     * Switches to the higher-pitched in-flight loop for interior listeners and stops exterior demat audio
     * (shell is gone; no positional exterior source).
     */
    public static void startFlight(MinecraftServer server, UUID tardisId, @Nullable ServerLevel exteriorWorld, @Nullable BlockPos exteriorPos) {
        if (server == null || tardisId == null) {
            return;
        }
        // Clear exterior demat loop at the departure site.
        if (exteriorWorld != null && exteriorPos != null) {
            Identifier exteriorDim = exteriorWorld.dimension().identifier();
            TravelAudioS2CPayload exteriorStop = new TravelAudioS2CPayload(
                    tardisId, TravelAudioS2CPayload.STOP, exteriorDim, exteriorPos, false);
            for (ServerPlayer player : DwmServices.get().playersAround(exteriorWorld, Vec3.atCenterOf(exteriorPos), EXTERIOR_RANGE)) {
                DwmServices.get().sendToPlayer(player, exteriorStop);
            }
        }

        BlockPos console = consolePos(tardisId);
        TravelAudioS2CPayload interiorCue = new TravelAudioS2CPayload(
                tardisId, TravelAudioS2CPayload.START_FLIGHT, TardisDimensions.DIMENSION_ID, console, true);
        ServerLevel interior = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interior == null) {
            return;
        }
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        for (ServerPlayer player : interior.players()) {
            if (BotiInteriorSampler.isInsideFootprint(player.blockPosition(), origin)
                    || player.blockPosition().closerThan(console, EXTERIOR_RANGE)) {
                DwmServices.get().sendToPlayer(player, interiorCue);
            }
        }
    }

    public static void stop(MinecraftServer server, UUID tardisId, @Nullable ServerLevel exteriorWorld, @Nullable BlockPos exteriorPos) {
        if (server == null || tardisId == null) {
            return;
        }
        Identifier exteriorDim = exteriorWorld != null
                ? exteriorWorld.dimension().identifier()
                : Identifier.fromNamespaceAndPath("minecraft", "overworld");
        BlockPos pos = exteriorPos != null ? exteriorPos : BlockPos.ZERO;
        TravelAudioS2CPayload exteriorStop = new TravelAudioS2CPayload(
                tardisId, TravelAudioS2CPayload.STOP, exteriorDim, pos, false);
        TravelAudioS2CPayload interiorStop = new TravelAudioS2CPayload(
                tardisId, TravelAudioS2CPayload.STOP, TardisDimensions.DIMENSION_ID, consolePos(tardisId), true);

        if (exteriorWorld != null) {
            for (ServerPlayer player : DwmServices.get().playersAround(exteriorWorld, Vec3.atCenterOf(pos), EXTERIOR_RANGE)) {
                DwmServices.get().sendToPlayer(player, exteriorStop);
            }
        }
        ServerLevel interior = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interior != null) {
            BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
            for (ServerPlayer player : interior.players()) {
                if (BotiInteriorSampler.isInsideFootprint(player.blockPosition(), origin)
                        || player.blockPosition().closerThan(consolePos(tardisId), EXTERIOR_RANGE)) {
                    DwmServices.get().sendToPlayer(player, interiorStop);
                }
            }
        }
    }

    private static void broadcast(
            MinecraftServer server,
            UUID tardisId,
            byte action,
            ServerLevel exteriorWorld,
            BlockPos exteriorPos
    ) {
        if (server == null || tardisId == null || exteriorWorld == null || exteriorPos == null) {
            return;
        }
        Identifier exteriorDim = exteriorWorld.dimension().identifier();
        TravelAudioS2CPayload exteriorCue = new TravelAudioS2CPayload(
                tardisId, action, exteriorDim, exteriorPos, false);
        for (ServerPlayer player : DwmServices.get().playersAround(exteriorWorld, Vec3.atCenterOf(exteriorPos), EXTERIOR_RANGE)) {
            DwmServices.get().sendToPlayer(player, exteriorCue);
        }

        BlockPos console = consolePos(tardisId);
        TravelAudioS2CPayload interiorCue = new TravelAudioS2CPayload(
                tardisId, action, TardisDimensions.DIMENSION_ID, console, true);
        ServerLevel interior = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interior == null) {
            return;
        }
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        for (ServerPlayer player : interior.players()) {
            if (BotiInteriorSampler.isInsideFootprint(player.blockPosition(), origin)
                    || player.blockPosition().closerThan(console, EXTERIOR_RANGE)) {
                DwmServices.get().sendToPlayer(player, interiorCue);
            }
        }
    }

    static BlockPos consolePos(UUID tardisId) {
        return TardisPlotAllocator.plotOrigin(tardisId).offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE);
    }
}
