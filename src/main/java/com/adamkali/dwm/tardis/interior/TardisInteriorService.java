package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.block.TardisBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.tardis.TardisExteriorFacing;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.boti.BotiPlotIndex;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.adamkali.dwm.tardis.logic.TardisOwnershipLogic;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.adamkali.dwm.tardis.portal.PortalStreamSyncService;
import com.adamkali.dwm.tardis.soto.SotoExteriorIndex;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public final class TardisInteriorService {
    private TardisInteriorService() {
    }

    /**
     * Ensures the interior exists for this exterior TARDIS, recording the entrance on the block entity.
     * Sync path used by enter / rebuild — may force-load chunks. Approach preload uses
     * {@link TardisInteriorPreloadService} instead.
     *
     * @return entrance feet position in the TARDIS dimension, or null on failure
     */
    public static @Nullable BlockPos ensureInterior(ServerLevel exteriorWorld, TardisBlockEntity exteriorEntity) {
        UUID tardisId = exteriorEntity.getTardisId();
        if (tardisId == null) {
            return null;
        }

        // Sync enter wins over deferred preload.
        TardisInteriorPreloadService.cancel(tardisId);

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

        return placeInterior(exteriorWorld, exteriorEntity, interiorWorld, origin, true);
    }

    /**
     * Deferred-preload place: assumes footprint chunks are already loaded (no force-load).
     *
     * @return true when placement bookkeeping completed
     */
    static boolean placeInteriorDeferred(
            ServerLevel exteriorWorld,
            TardisBlockEntity exteriorEntity,
            ServerLevel interiorWorld
    ) {
        UUID tardisId = exteriorEntity.getTardisIdOrNull();
        if (tardisId == null) {
            return false;
        }
        updateExteriorLocation(exteriorWorld, exteriorEntity);
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        return placeInterior(exteriorWorld, exteriorEntity, interiorWorld, origin, false) != null;
    }

    private static @Nullable BlockPos placeInterior(
            ServerLevel exteriorWorld,
            TardisBlockEntity exteriorEntity,
            ServerLevel interiorWorld,
            BlockPos origin,
            boolean forceLoadChunks
    ) {
        UUID tardisId = exteriorEntity.getTardisId();
        BlockPos entrance = forceLoadChunks
                ? FirstDoctorConsoleRoomPlacer.place(interiorWorld, origin, tardisId)
                : FirstDoctorConsoleRoomPlacer.placeAssumingChunksLoaded(interiorWorld, origin, tardisId);
        exteriorEntity.setInteriorEntrance(entrance);
        exteriorEntity.setInteriorGenerated(true);
        BotiPlotIndex.register(tardisId);
        PortalStreamSyncService.markBotiFootprintDirty(tardisId);
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
        TardisOwnershipLogic.tryClaimOnEnter(exteriorEntity.getTardisIdOrNull(), player.getUUID());
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

        MinecraftServer server = player.level().getServer();
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

    /**
     * Rebuilds the console room at the deterministic plot in {@code dwm:tardis} for {@code tardisId}.
     * Preserves all linked TARDIS data (UUID, owner, waypoints, destinations, etc.).
     *
     * @return entrance feet position, or null on failure
     */
    public static @Nullable BlockPos regenerateInterior(MinecraftServer server, UUID tardisId) {
        if (server == null || tardisId == null) {
            return null;
        }
        ServerLevel interiorWorld = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interiorWorld == null) {
            return null;
        }
        return regenerateInterior(interiorWorld, TardisPlotAllocator.plotOrigin(tardisId), tardisId);
    }

    /**
     * Clears the room footprint, re-places the structure template, and re-stamps interior entities.
     * Does not create a new TARDIS id or clear {@link TardisDataModel} fields.
     *
     * @return entrance feet position, or null if refused (e.g. traveling)
     */
    public static @Nullable BlockPos regenerateInterior(ServerLevel world, BlockPos origin, UUID tardisId) {
        if (world == null || origin == null || tardisId == null) {
            return null;
        }
        if (TardisTravelService.isTraveling(tardisId)) {
            return null;
        }

        int sizeX = FirstDoctorConsoleRoomPlacer.SIZE_X;
        int sizeY = FirstDoctorConsoleRoomPlacer.SIZE_Y;
        int sizeZ = FirstDoctorConsoleRoomPlacer.SIZE_Z;
        BotiInteriorSampler.forceLoadFootprintChunks(world, origin);

        AABB roomBox = new AABB(
                origin.getX(),
                origin.getY(),
                origin.getZ(),
                origin.getX() + sizeX,
                origin.getY() + sizeY,
                origin.getZ() + sizeZ
        );
        List<ServerPlayer> occupants = new ArrayList<>(world.getEntitiesOfClass(ServerPlayer.class, roomBox));

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    world.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }

        BlockPos entrance = FirstDoctorConsoleRoomPlacer.place(world, origin, tardisId);
        refreshExteriorInteriorState(world.getServer(), tardisId, entrance);
        BotiPlotIndex.register(tardisId);
        PortalStreamSyncService.markBotiFootprintDirty(tardisId);

        for (ServerPlayer occupant : occupants) {
            if (occupant.isAlive()) {
                TardisTeleport.teleport(occupant, world, entrance, occupant.getYRot());
            }
        }
        return entrance;
    }

    private static void refreshExteriorInteriorState(
            @Nullable MinecraftServer server,
            UUID tardisId,
            BlockPos entrance
    ) {
        if (server == null) {
            return;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        ResourceKey<Level> worldKey = SotoExteriorIndex.getWorldKey(tardisId);
        BlockPos exteriorPos = SotoExteriorIndex.getExteriorPos(tardisId);
        if (worldKey == null || exteriorPos == null) {
            if (model == null || !model.hasExteriorLocation || model.exteriorDimension == null) {
                return;
            }
            worldKey = ResourceKey.create(Registries.DIMENSION, Identifier.parse(model.exteriorDimension));
            exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        }
        ServerLevel exteriorWorld = server.getLevel(worldKey);
        if (exteriorWorld == null) {
            return;
        }
        if (exteriorWorld.getBlockEntity(exteriorPos) instanceof TardisBlockEntity exteriorEntity) {
            exteriorEntity.setInteriorEntrance(entrance);
            exteriorEntity.setInteriorGenerated(true);
        }
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
        PortalStreamSyncService.setMetaChanged(tardisId);
    }
}
