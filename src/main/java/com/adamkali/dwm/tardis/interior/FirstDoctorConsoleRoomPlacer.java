package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    public static BlockPos place(ServerWorld world, BlockPos origin, UUID tardisId) {
        // Far UUID-derived plots are often unloaded; load the structure footprint before placing.
        BlockPos max = origin.add(SIZE_X - 1, SIZE_Y - 1, SIZE_Z - 1);
        for (int x = origin.getX() >> 4; x <= max.getX() >> 4; x++) {
            for (int z = origin.getZ() >> 4; z <= max.getZ() >> 4; z++) {
                world.getChunk(x, z);
            }
        }

        // Prefer the programmatic layout for reliability. Template placement can report success
        // while leaving an empty footprint when chunks were not ready; always build in code.
        tryPlaceFromTemplate(world, origin);
        placeProgrammatically(world, origin);
        stampInteriorDoors(world, origin, tardisId);
        return origin.add(LOCAL_ENTRANCE);
    }

    private static boolean tryPlaceFromTemplate(ServerWorld world, BlockPos origin) {
        Optional<StructureTemplate> template = world.getServer()
                .getStructureTemplateManager()
                .getTemplate(TardisDimensions.CONSOLE_ROOM_STRUCTURE_ID);
        if (template.isEmpty()) {
            return false;
        }
        StructurePlacementData data = new StructurePlacementData()
                .setMirror(BlockMirror.NONE)
                .setRotation(BlockRotation.NONE)
                .setIgnoreEntities(true);
        template.get().place(world, origin, origin, data, Random.create(), Block.NOTIFY_LISTENERS);
        return true;
    }

    static void placeProgrammatically(ServerWorld world, BlockPos origin) {
        for (Map.Entry<BlockPos, BlockState> entry : buildPlacements().entrySet()) {
            world.setBlockState(origin.add(entry.getKey()), entry.getValue(), Block.NOTIFY_LISTENERS);
        }
    }

    private static void stampInteriorDoors(ServerWorld world, BlockPos origin, UUID tardisId) {
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    if (world.getBlockEntity(pos) instanceof TardisInteriorDoorBlockEntity doorEntity) {
                        doorEntity.setTardisId(tardisId);
                    }
                }
            }
        }
    }

    static Map<BlockPos, BlockState> buildPlacements() {
        return FirstDoctorConsoleRoomLayout.placements();
    }
}
