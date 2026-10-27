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

/** A small Thal dwelling: petrified walls, stair roof, door, bed and one or two resident Thals. */
public class ThalHutPiece extends ThalSettlementPiece {
    public static final int HEIGHT = 7;
    private static final int WALL_HEIGHT = 3;

    private final int residents;
    private boolean spawnedResidents;

    public ThalHutPiece(ThalFrame frame, int floorY, int residents) {
        super(DWMStructurePieceTypes.THAL_HUT, frame, floorY, HEIGHT);
        this.residents = residents;
    }

    public ThalHutPiece(CompoundTag tag) {
        super(DWMStructurePieceTypes.THAL_HUT, tag);
        this.residents = tag.getIntOr("Residents", 1);
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
                var wall = isCorner(u, v) ? ThalSettlementPalette.post() : ThalSettlementPalette.planks();
                fill(level, chunkBB, wall, u, 1, v, u, WALL_HEIGHT, v);
                if (isCorner(u, v)) {
                    foundation(level, chunkBB, ThalSettlementPalette.post(), u, v);
                }
            }
        }

        // Windows and door.
        put(level, chunkBB, ThalSettlementPalette.window(), doorU, 2, d - 1);
        put(level, chunkBB, ThalSettlementPalette.window(), 0, 2, d / 2);
        put(level, chunkBB, ThalSettlementPalette.window(), w - 1, 2, d / 2);
        put(level, chunkBB, ThalSettlementPalette.door(frame.inward(), DoubleBlockHalf.LOWER), doorU, 1, 0);
        put(level, chunkBB, ThalSettlementPalette.door(frame.inward(), DoubleBlockHalf.UPPER), doorU, 2, 0);

        // Furnishings.
        put(level, chunkBB, ThalSettlementPalette.bed(frame.inward(), BedPart.FOOT), 1, 1, 2);
        put(level, chunkBB, ThalSettlementPalette.bed(frame.inward(), BedPart.HEAD), 1, 1, 3);
        put(level, chunkBB, ThalSettlementPalette.craftingTable(), w - 2, 1, d - 2);
        put(level, chunkBB, ThalSettlementPalette.hangingLantern(), 1, WALL_HEIGHT, 1);

        roof(level, chunkBB, WALL_HEIGHT + 1);

        if (!spawnedResidents && spawnResidents(level, chunkBB, doorU, 2, residents)) {
            spawnedResidents = true;
        }
    }
}
