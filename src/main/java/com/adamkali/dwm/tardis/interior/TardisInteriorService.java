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
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.adamkali.dwm.tardis.soto.SotoExteriorIndex;
import com.adamkali.dwm.tardis.soto.SotoExteriorSyncService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
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
    public static @Nullable BlockPos ensureInterior(ServerLevel exteriorWorld, TardisBlockEntity exteriorEntity) {
        UUID tardisId = exteriorEntity.getTardisId();
        if (tardisId == null) {
            return null;
        }

        MinecraftServer server = exteriorWorld.getServer();
        ServerLevel interiorWorld = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interiorWorld == null) {
            return null;
        }

        updateExteriorLocation(exteriorWorld, exteriorEntity);

        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        if (exteriorEntity.isInteriorGenerated() && exteriorEntity.getInteriorEntrance() != null) {
            // Recover from a prior empty place (e.g. unloaded chunks) by regenerating if the floor is missing.
            BlockPos entrance = exteriorEntity.getInteriorEntrance();
            if (!interiorWorld.getBlockState(entrance.below()).isAir()) {
                BotiPlotIndex.register(tardisId);
                return entrance;
            }
        }

        BlockPos entrance = FirstDoctorConsoleRoomPlacer.place(interiorWorld, origin, tardisId);
        exteriorEntity.setInteriorEntrance(entrance);
        exteriorEntity.setInteriorGenerated(true);
        BotiPlotIndex.register(tardisId);
        BotiInteriorSyncService.setChanged(tardisId);
        return entrance;
    }

    public static boolean tryEnterFromExterior(ServerPlayer player, ServerLevel exteriorWorld, TardisBlockEntity exteriorEntity) {
        if (TardisTravelService.isTraveling(exteriorEntity.getTardisIdOrNull())) {
            return false;
        }
        if (!TardisEntryGate.canEnter(TardisLogic.getDoorState(exteriorEntity.getTardisId()))) {
            return false;
        }
        BlockPos entrance = ensureInterior(exteriorWorld, exteriorEntity);
        if (entrance == null) {
            return false;
        }
        ServerLevel interiorWorld = exteriorWorld.getServer().getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interiorWorld == null) {
            return false;
        }
        TardisTeleport.teleport(player, interiorWorld, entrance, 0.0f);
        return true;
    }

    public static boolean tryExitToExterior(ServerPlayer player, TardisInteriorDoorBlockEntity doorEntity) {
        UUID tardisId = doorEntity.getTardisId();
        if (tardisId == null) {
            return false;
        }
        if (TardisTravelService.isTraveling(tardisId)) {
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
        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, Identifier.parse(model.exteriorDimension));
        ServerLevel exteriorWorld = server.getLevel(worldKey);
        if (exteriorWorld == null) {
            return false;
        }

        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        Direction doorFacing = TardisExteriorFacing.doorDirection(model.exteriorRotation);
        BlockPos exitPos = exteriorPos.relative(doorFacing);
        float yaw = Direction.getYRot(doorFacing);
        TardisTeleport.teleport(player, exteriorWorld, exitPos, yaw);
        return true;
    }

    private static void updateExteriorLocation(ServerLevel exteriorWorld, TardisBlockEntity exteriorEntity) {
        UUID tardisId = exteriorEntity.getTardisId();
        TardisDataModel model = TardisDataLoader.getOrCreate(tardisId);
        int rotation = 0;
        if (exteriorWorld.getBlockState(exteriorEntity.getBlockPos()).hasProperty(TardisBlock.FACING_ROTATION)) {
            rotation = exteriorWorld.getBlockState(exteriorEntity.getBlockPos()).getValue(TardisBlock.FACING_ROTATION);
        }
        BlockPos pos = exteriorEntity.getBlockPos();
        model.setExteriorLocation(
                exteriorWorld.dimension().identifier().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                rotation
        );
        SotoExteriorIndex.register(tardisId, model);
        SotoExteriorSyncService.setChanged(tardisId);
    }
}
