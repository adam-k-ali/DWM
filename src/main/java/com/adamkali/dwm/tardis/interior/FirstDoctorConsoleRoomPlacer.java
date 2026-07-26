package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Places the First Doctor console room. Prefers the shipped structure template; falls back to
 * an equivalent programmatic layout if the template cannot be loaded.
 */
public final class FirstDoctorConsoleRoomPlacer {
    public static final int SIZE_X = 11;
    public static final int SIZE_Y = 7;
    public static final int SIZE_Z = 11;

    /** Local entrance standing position relative to structure origin (feet). */
    public static final BlockPos LOCAL_ENTRANCE = new BlockPos(5, 1, 1);

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
        Map<BlockPos, BlockState> placements = new HashMap<>();
        BlockState floor = DWMBlocks.WHITE_TARDIS_WALL.getDefaultState();
        BlockState wall = DWMBlocks.WHITE_ROUNDEL_A.getDefaultState();
        BlockState roundel = DWMBlocks.WHITE_BIG_ROUNDEL_A.getDefaultState();
        BlockState ceiling = DWMBlocks.LIGHT_GRAY_TARDIS_WALL.getDefaultState();
        BlockState console = DWMBlocks.TEAL_BIG_ROUNDEL_A.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState light = Blocks.LIGHT.getDefaultState();
        BlockState doorState = DWMBlocks.TARDIS_INTERIOR_DOOR.getDefaultState()
                .with(TardisInteriorDoorBlock.FACING, Direction.SOUTH);

        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                placements.put(new BlockPos(x, 0, z), floor);
                placements.put(new BlockPos(x, SIZE_Y - 1, z), ceiling);
                for (int y = 1; y < SIZE_Y - 1; y++) {
                    boolean edge = x == 0 || x == SIZE_X - 1 || z == 0 || z == SIZE_Z - 1;
                    placements.put(new BlockPos(x, y, z), edge ? wall : air);
                }
            }
        }
        for (int y = 1; y <= 2; y++) {
            for (int x = 4; x <= 6; x++) {
                placements.put(new BlockPos(x, y, 0), doorState);
            }
        }
        placements.put(new BlockPos(5, 1, 5), console);
        placements.put(new BlockPos(5, 2, 5), roundel);
        placements.put(new BlockPos(6, 1, 5), floor);
        placements.put(new BlockPos(4, 1, 5), floor);
        placements.put(new BlockPos(5, 1, 6), floor);
        placements.put(new BlockPos(5, 1, 4), floor);
        placements.put(new BlockPos(5, 4, 5), light);
        placements.put(new BlockPos(0, 2, 5), roundel);
        placements.put(new BlockPos(SIZE_X - 1, 2, 5), roundel);
        placements.put(new BlockPos(5, 2, SIZE_Z - 1), roundel);
        return placements;
    }
}
