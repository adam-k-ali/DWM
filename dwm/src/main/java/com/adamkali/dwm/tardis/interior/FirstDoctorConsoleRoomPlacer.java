package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.FirstDoctorConsoleSync;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;

/**
 * Places the First Doctor console room from the shipped {@code dwm:first_doctor_console_room}
 * structure template, then links interior door / console block entities to the TARDIS id.
 */
public final class FirstDoctorConsoleRoomPlacer {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int SIZE_X = FirstDoctorConsoleRoomLayout.SIZE_X;
    public static final int SIZE_Y = FirstDoctorConsoleRoomLayout.SIZE_Y;
    public static final int SIZE_Z = FirstDoctorConsoleRoomLayout.SIZE_Z;

    /** Local entrance standing position relative to structure origin (feet). */
    public static final BlockPos LOCAL_ENTRANCE = FirstDoctorConsoleRoomLayout.LOCAL_ENTRANCE;

    private FirstDoctorConsoleRoomPlacer() {
    }

    /**
     * Places the console room after force-loading footprint chunks (sync enter / rebuild path).
     * Prefer {@link #placeAssumingChunksLoaded} when chunks were ticketed asynchronously first.
     */
    public static BlockPos place(ServerLevel world, BlockPos origin, UUID tardisId) {
        BotiInteriorSampler.forceLoadFootprintChunks(world, origin);
        return placeAssumingChunksLoaded(world, origin, tardisId);
    }

    /**
     * Places the console room assuming footprint columns are already loaded (deferred preload).
     */
    public static BlockPos placeAssumingChunksLoaded(ServerLevel world, BlockPos origin, UUID tardisId) {
        if (!tryPlaceFromTemplate(world, origin)) {
            LOGGER.error(
                    "Failed to load structure template {}; interior not placed for {}",
                    TardisDimensions.CONSOLE_ROOM_STRUCTURE_ID,
                    tardisId
            );
            return origin.offset(LOCAL_ENTRANCE);
        }

        completeInteriorDoorBank(world, origin);
        placeInteriorLight(world, origin);
        stampInteriorEntities(world, origin, tardisId);
        applyDoorOpenFromModel(world, origin, tardisId);
        FirstDoctorConsoleSync.syncFromModel(world.getServer(), tardisId);

        return origin.offset(LOCAL_ENTRANCE);
    }

    private static boolean tryPlaceFromTemplate(ServerLevel world, BlockPos origin) {
        Optional<StructureTemplate> structure = world.getServer()
                .getStructureManager()
                .get(TardisDimensions.CONSOLE_ROOM_STRUCTURE_ID);
        if (structure.isEmpty()) {
            return false;
        }
        StructurePlaceSettings data = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(true);
        structure.get().placeInWorld(world, origin, origin, data, RandomSource.create(), Block.UPDATE_CLIENTS);
        return true;
    }

    static void placeInteriorLight(ServerLevel world, BlockPos origin) {
        world.setBlock(
                origin.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE.above(3)),
                Blocks.LIGHT.defaultBlockState(),
                Block.UPDATE_CLIENTS
        );
    }

    /**
     * The shipped template may only contain the door-bank origin cell; fill the remaining five
     * cells so BER, collision, SOTO, and exit teleport all see a full 3×2 bank.
     */
    static void completeInteriorDoorBank(ServerLevel world, BlockPos origin) {
        BlockPos doorOrigin = origin.offset(FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN);
        BlockState originState = world.getBlockState(doorOrigin);
        if (!originState.is(DWMBlocks.TARDIS_INTERIOR_DOOR)
                || !TardisInteriorDoorBlock.isOrigin(originState)) {
            Direction facing = Direction.SOUTH;
            boolean open = false;
            if (originState.is(DWMBlocks.TARDIS_INTERIOR_DOOR)) {
                facing = originState.getValue(TardisInteriorDoorBlock.FACING);
                open = originState.getValue(TardisInteriorDoorBlock.OPEN);
            }
            world.setBlock(
                    doorOrigin,
                    TardisInteriorDoorBlock.bankCellState(facing, DoubleBlockHalf.LOWER, 0, open),
                    Block.UPDATE_CLIENTS
            );
            originState = world.getBlockState(doorOrigin);
        }

        Direction facing = originState.getValue(TardisInteriorDoorBlock.FACING);
        boolean open = originState.getValue(TardisInteriorDoorBlock.OPEN);
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (int slot = 0; slot < TardisInteriorDoorBlock.BANK_WIDTH; slot++) {
                BlockPos cell = TardisInteriorDoorBlock.cellPos(doorOrigin, facing, half, slot);
                world.setBlock(
                        cell,
                        TardisInteriorDoorBlock.bankCellState(facing, half, slot, open),
                        Block.UPDATE_CLIENTS
                );
            }
        }
    }

    private static void applyDoorOpenFromModel(ServerLevel world, BlockPos origin, UUID tardisId) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        boolean open = model != null && model.doorState.isOpen;
        BlockPos doorOrigin = origin.offset(FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN);
        BlockState originState = world.getBlockState(doorOrigin);
        if (!originState.is(DWMBlocks.TARDIS_INTERIOR_DOOR)) {
            return;
        }
        TardisInteriorDoorBlock.setOpen(world, doorOrigin, originState, open, true);
        if (world.getBlockEntity(doorOrigin) instanceof TardisInteriorDoorBlockEntity door) {
            door.setOpen(open, true);
        }
    }

    private static void stampInteriorEntities(ServerLevel world, BlockPos origin, UUID tardisId) {
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (world.getBlockEntity(pos) instanceof TardisInteriorDoorBlockEntity doorEntity) {
                        doorEntity.setTardisId(tardisId);
                    } else if (world.getBlockEntity(pos) instanceof FirstDoctorConsoleBlockEntity consoleEntity) {
                        consoleEntity.setTardisId(tardisId);
                    }
                }
            }
        }
    }
}
