package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.block.TardisBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.tardis.TardisExteriorFacing;
import com.adamkali.dwm.tardis.boti.BotiInteriorSyncService;
import com.adamkali.dwm.tardis.boti.BotiPlotIndex;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.adamkali.dwm.tardis.soto.SotoExteriorIndex;
import com.adamkali.dwm.tardis.soto.SotoExteriorSyncService;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class TardisInteriorService {
    private TardisInteriorService() {
    }

    /**
     * Ensures the interior exists for this exterior TARDIS, recording the entrance on the block entity.
     *
     * @return entrance feet position in the TARDIS dimension, or null on failure
     */
    public static @Nullable BlockPos ensureInterior(ServerWorld exteriorWorld, TardisBlockEntity exteriorEntity) {
        UUID tardisId = exteriorEntity.getTardisId();
        if (tardisId == null) {
            return null;
        }

        MinecraftServer server = exteriorWorld.getServer();
        ServerWorld interiorWorld = server.getWorld(TardisDimensions.TARDIS_WORLD_KEY);
        if (interiorWorld == null) {
            return null;
        }

        updateExteriorLocation(exteriorWorld, exteriorEntity);

        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        if (exteriorEntity.isInteriorGenerated() && exteriorEntity.getInteriorEntrance() != null) {
            // Recover from a prior empty place (e.g. unloaded chunks) by regenerating if the floor is missing.
            BlockPos entrance = exteriorEntity.getInteriorEntrance();
            if (!interiorWorld.getBlockState(entrance.down()).isAir()) {
                BotiPlotIndex.register(tardisId);
                return entrance;
            }
        }

        BlockPos entrance = FirstDoctorConsoleRoomPlacer.place(interiorWorld, origin, tardisId);
        exteriorEntity.setInteriorEntrance(entrance);
        exteriorEntity.setInteriorGenerated(true);
        BotiPlotIndex.register(tardisId);
        BotiInteriorSyncService.markDirty(tardisId);
        return entrance;
    }

    public static boolean tryEnterFromExterior(ServerPlayerEntity player, ServerWorld exteriorWorld, TardisBlockEntity exteriorEntity) {
        if (!TardisEntryGate.canEnter(TardisLogic.getDoorState(exteriorEntity.getTardisId()))) {
            return false;
        }
        BlockPos entrance = ensureInterior(exteriorWorld, exteriorEntity);
        if (entrance == null) {
            return false;
        }
        ServerWorld interiorWorld = exteriorWorld.getServer().getWorld(TardisDimensions.TARDIS_WORLD_KEY);
        if (interiorWorld == null) {
            return false;
        }
        TardisTeleport.teleport(player, interiorWorld, entrance, 0.0f);
        return true;
    }

    public static boolean tryExitToExterior(ServerPlayerEntity player, TardisInteriorDoorBlockEntity doorEntity) {
        UUID tardisId = doorEntity.getTardisId();
        if (tardisId == null) {
            return false;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null || !model.hasExteriorLocation || model.exteriorDimension == null) {
            return false;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(model.exteriorDimension));
        ServerWorld exteriorWorld = server.getWorld(worldKey);
        if (exteriorWorld == null) {
            return false;
        }

        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        Direction doorFacing = TardisExteriorFacing.doorDirection(model.exteriorRotation);
        BlockPos exitPos = exteriorPos.offset(doorFacing);
        float yaw = Direction.getHorizontalDegreesOrThrow(doorFacing);
        TardisTeleport.teleport(player, exteriorWorld, exitPos, yaw);
        return true;
    }

    private static void updateExteriorLocation(ServerWorld exteriorWorld, TardisBlockEntity exteriorEntity) {
        UUID tardisId = exteriorEntity.getTardisId();
        TardisDataModel model = TardisDataLoader.getOrCreate(tardisId);
        int rotation = 0;
        if (exteriorWorld.getBlockState(exteriorEntity.getPos()).contains(TardisBlock.FACING_ROTATION)) {
            rotation = exteriorWorld.getBlockState(exteriorEntity.getPos()).get(TardisBlock.FACING_ROTATION);
        }
        BlockPos pos = exteriorEntity.getPos();
        model.setExteriorLocation(
                exteriorWorld.getRegistryKey().getValue().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                rotation
        );
        SotoExteriorIndex.register(tardisId, model);
        SotoExteriorSyncService.markDirty(tardisId);
    }
}
