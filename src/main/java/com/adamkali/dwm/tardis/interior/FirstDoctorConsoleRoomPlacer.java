package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * Places the First Doctor console room. Prefers the shipped structure template; falls back to
 * an equivalent programmatic layout if the template cannot be loaded.
 */
public final class FirstDoctorConsoleRoomPlacer {
    public static final int SIZE_X = FirstDoctorConsoleRoomLayout.SIZE_X;
    public static final int SIZE_Y = FirstDoctorConsoleRoomLayout.SIZE_Y;
    public static final int SIZE_Z = FirstDoctorConsoleRoomLayout.SIZE_Z;

    /** Local entrance standing position relative to structure origin (feet). */
    public static final BlockPos LOCAL_ENTRANCE = FirstDoctorConsoleRoomLayout.LOCAL_ENTRANCE;

    private FirstDoctorConsoleRoomPlacer() {
    }

    public static BlockPos place(ServerLevel world, BlockPos origin, UUID tardisId) {
        // Far UUID-derived plots are often unloaded; load the structure footprint before placing.
        BlockPos max = origin.offset(SIZE_X - 1, SIZE_Y - 1, SIZE_Z - 1);
        for (int x = origin.getX() >> 4; x <= max.getX() >> 4; x++) {
            for (int z = origin.getZ() >> 4; z <= max.getZ() >> 4; z++) {
                world.getChunk(x, z);
            }
        }

        // Prefer the programmatic layout for reliability. Template placement can report success
        // while leaving an empty footprint when chunks were not ready; always build in code.
        tryPlaceFromTemplate(world, origin);
        placeProgrammatically(world, origin);
        stampInteriorEntities(world, origin, tardisId);
        return origin.offset(LOCAL_ENTRANCE);
    }

    private static boolean tryPlaceFromTemplate(ServerLevel world, BlockPos origin) {
        Optional<StructureTemplate> structure = world.getServer()
                .getStructureManager()
                .get(TardisDimensions.CONSOLE_ROOM_STRUCTURE_ID);
        if (template.isEmpty()) {
            return false;
        }
        StructurePlaceSettings data = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(true);
        template.get().placeInWorld(world, origin, origin, data, RandomSource.create(), Block.UPDATE_CLIENTS);
        return true;
    }

    static void placeProgrammatically(ServerLevel world, BlockPos origin) {
        for (Map.Entry<BlockPos, BlockState> entry : buildPlacements().entrySet()) {
            world.setBlock(origin.offset(entry.getKey()), entry.getValue(), Block.UPDATE_CLIENTS);
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

    static Map<BlockPos, BlockState> buildPlacements() {
        return FirstDoctorConsoleRoomLayout.placements();
    }
}
