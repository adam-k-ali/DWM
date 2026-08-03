package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.network.TravelAudioS2CPayload;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Broadcasts demat/mat loop start/stop cues to exterior tracking players and interior plot occupants.
 */
public final class TardisTravelAudio {
    private static final int EXTERIOR_RANGE = 64;

    private TardisTravelAudio() {
    }

    public static void startDemat(MinecraftServer server, UUID tardisId, ServerWorld exteriorWorld, BlockPos exteriorPos) {
        broadcast(server, tardisId, TravelAudioS2CPayload.START_DEMAT, exteriorWorld, exteriorPos);
    }

    public static void startMat(MinecraftServer server, UUID tardisId, ServerWorld exteriorWorld, BlockPos exteriorPos) {
        broadcast(server, tardisId, TravelAudioS2CPayload.START_MAT, exteriorWorld, exteriorPos);
    }

    public static void stop(MinecraftServer server, UUID tardisId, @Nullable ServerWorld exteriorWorld, @Nullable BlockPos exteriorPos) {
        if (server == null || tardisId == null) {
            return;
        }
        Identifier exteriorDim = exteriorWorld != null
                ? exteriorWorld.getRegistryKey().getValue()
                : Identifier.of("minecraft", "overworld");
        BlockPos pos = exteriorPos != null ? exteriorPos : BlockPos.ORIGIN;
        TravelAudioS2CPayload exteriorStop = new TravelAudioS2CPayload(
                tardisId, TravelAudioS2CPayload.STOP, exteriorDim, pos, false);
        TravelAudioS2CPayload interiorStop = new TravelAudioS2CPayload(
                tardisId, TravelAudioS2CPayload.STOP, TardisDimensions.DIMENSION_ID, consolePos(tardisId), true);

        if (exteriorWorld != null) {
            for (ServerPlayerEntity player : PlayerLookup.around(exteriorWorld, pos, EXTERIOR_RANGE)) {
                ServerPlayNetworking.send(player, exteriorStop);
            }
        }
        ServerWorld interior = server.getWorld(TardisDimensions.TARDIS_WORLD_KEY);
        if (interior != null) {
            BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
            for (ServerPlayerEntity player : interior.getPlayers()) {
                if (BotiInteriorSampler.isInsideFootprint(player.getBlockPos(), origin)
                        || player.getBlockPos().isWithinDistance(consolePos(tardisId), EXTERIOR_RANGE)) {
                    ServerPlayNetworking.send(player, interiorStop);
                }
            }
        }
    }

    private static void broadcast(
            MinecraftServer server,
            UUID tardisId,
            byte action,
            ServerWorld exteriorWorld,
            BlockPos exteriorPos
    ) {
        if (server == null || tardisId == null || exteriorWorld == null || exteriorPos == null) {
            return;
        }
        Identifier exteriorDim = exteriorWorld.getRegistryKey().getValue();
        TravelAudioS2CPayload exteriorCue = new TravelAudioS2CPayload(
                tardisId, action, exteriorDim, exteriorPos, false);
        for (ServerPlayerEntity player : PlayerLookup.around(exteriorWorld, exteriorPos, EXTERIOR_RANGE)) {
            ServerPlayNetworking.send(player, exteriorCue);
        }

        BlockPos console = consolePos(tardisId);
        TravelAudioS2CPayload interiorCue = new TravelAudioS2CPayload(
                tardisId, action, TardisDimensions.DIMENSION_ID, console, true);
        ServerWorld interior = server.getWorld(TardisDimensions.TARDIS_WORLD_KEY);
        if (interior == null) {
            return;
        }
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        for (ServerPlayerEntity player : interior.getPlayers()) {
            if (BotiInteriorSampler.isInsideFootprint(player.getBlockPos(), origin)
                    || player.getBlockPos().isWithinDistance(console, EXTERIOR_RANGE)) {
                ServerPlayNetworking.send(player, interiorCue);
            }
        }
    }

    static BlockPos consolePos(UUID tardisId) {
        return TardisPlotAllocator.plotOrigin(tardisId).add(5, 1, 5);
    }
}
