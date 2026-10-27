package com.adamkali.dwm.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** The communal shelter: a larger hall with a table, seats and beds, housing two or three Thals. */
public class ThalShelterPiece extends ThalSettlementPiece {
    public static final int HEIGHT = 9;
    private static final int WALL_HEIGHT = 4;

    private final int residents;
    private boolean spawnedResidents;

    public ThalShelterPiece(ThalFrame frame, int floorY, int residents) {
        super(DWMStructurePieceTypes.THAL_SHELTER, frame, floorY, HEIGHT);
        this.residents = residents;
    }

    public ThalShelterPiece(CompoundTag tag) {
        super(DWMStructurePieceTypes.THAL_SHELTER, tag);
        this.residents = tag.getIntOr("Residents", 2);
        this.spawnedResidents = tag.getBooleanOr("SpawnedResidents", false);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putInt("Residents", residents);
        tag.putBoolean("SpawnedResidents", spawnedResidents);
    }

    @Override
    public void postProcess(
            WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
            RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos
    ) {
        int w = frame.width();
        int d = frame.depth();
        int doorU = frame.doorU();

        foundationEverywhere(level, chunkBB);
        clear(level, chunkBB, 0, 1, 0, w - 1, HEIGHT - 1, d - 1);
        fill(level, chunkBB, ThalSettlementPalette.planks(), 0, 0, 0, w - 1, 0, d - 1);

        for (int u = 0; u < w; u++) {
            for (int v = 0; v < d; v++) {
                if (!isPerimeter(u, v)) {
                    continue;
                }
                boolean pillar = isCorner(u, v)
                        || (v == 0 && (u == 2 || u == w - 3))
                        || ((u == 0 || u == w - 1) && v == d / 2)
                        || (v == d - 1 && u == doorU);
                var wall = pillar ? ThalSettlementPalette.post() : ThalSettlementPalette.planks();
                fill(level, chunkBB, wall, u, 1, v, u, WALL_HEIGHT, v);
                if (pillar) {
                    foundation(level, chunkBB, ThalSettlementPalette.post(), u, v);
                }
            }
        }

        // Windows between the pillars on the side and back walls.
        for (int y = 2; y <= 3; y++) {
            for (int v : new int[]{1, 2, d - 3, d - 2}) {
                put(level, chunkBB, ThalSettlementPalette.window(), 0, y, v);
                put(level, chunkBB, ThalSettlementPalette.window(), w - 1, y, v);
            }
            put(level, chunkBB, ThalSettlementPalette.window(), 2, y, d - 1);
            put(level, chunkBB, ThalSettlementPalette.window(), w - 3, y, d - 1);
        }
        put(level, chunkBB, ThalSettlementPalette.door(frame.inward(), DoubleBlockHalf.LOWER), doorU, 1, 0);
        put(level, chunkBB, ThalSettlementPalette.door(frame.inward(), DoubleBlockHalf.UPPER), doorU, 2, 0);

        // Long table with seats either side.
        int tableV = d / 2;
        for (int u = doorU - 1; u <= doorU + 1; u++) {
            put(level, chunkBB, ThalSettlementPalette.slab(), u, 1, tableV);
            put(level, chunkBB, ThalSettlementPalette.stairs(frame.facing()), u, 1, tableV - 1);
            put(level, chunkBB, ThalSettlementPalette.stairs(frame.inward()), u, 1, tableV + 1);
        }
        // Beds along the back wall, crafting and storage beside them.
        for (int u : new int[]{1, w - 2}) {
            put(level, chunkBB, ThalSettlementPalette.bed(frame.inward(), BedPart.FOOT), u, 1, d - 3);
            put(level, chunkBB, ThalSettlementPalette.bed(frame.inward(), BedPart.HEAD), u, 1, d - 2);
        }
        put(level, chunkBB, ThalSettlementPalette.craftingTable(), 2, 1, d - 2);
        put(level, chunkBB, ThalSettlementPalette.barrel(), w - 3, 1, d - 2);
        for (int u : new int[]{doorU - 1, doorU + 1}) {
            put(level, chunkBB, ThalSettlementPalette.hangingLantern(), u, WALL_HEIGHT, 1);
            put(level, chunkBB, ThalSettlementPalette.hangingLantern(), u, WALL_HEIGHT, d - 2);
        }

        roof(level, chunkBB, WALL_HEIGHT + 1);

        if (!spawnedResidents && spawnResidents(level, chunkBB, 2, 2, residents)) {
            spawnedResidents = true;
        }
    }
}
