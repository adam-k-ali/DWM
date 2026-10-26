package com.adamkali.dwm.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.List;

/**
 * The settlement's path network: dirt-path tiles at the common floor level. Every cell is laid on a filled
 * foundation with two blocks of headroom cleared so the whole network stays walkable (no steps).
 */
public class ThalPathPiece extends StructurePiece {
    private final int floorY;
    /** Packed {@code x0, z0, x1, z1, ...} world coordinates of the path cells. */
    private final int[] cells;

    public ThalPathPiece(List<ThalSettlementLayout.Cell> worldCells, int floorY) {
        super(DWMStructurePieceTypes.THAL_PATH, 0, boundsOf(worldCells, floorY));
        this.floorY = floorY;
        this.cells = new int[worldCells.size() * 2];
        for (int i = 0; i < worldCells.size(); i++) {
            cells[2 * i] = worldCells.get(i).x();
            cells[2 * i + 1] = worldCells.get(i).z();
        }
        setOrientation(null);
    }

    public ThalPathPiece(CompoundTag tag) {
        super(DWMStructurePieceTypes.THAL_PATH, tag);
        this.floorY = tag.getIntOr("FloorY", 0);
        this.cells = tag.getIntArray("Cells").orElse(new int[0]);
    }

    private static BoundingBox boundsOf(List<ThalSettlementLayout.Cell> cells, int floorY) {
        int minX = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (ThalSettlementLayout.Cell cell : cells) {
            minX = Math.min(minX, cell.x());
            maxX = Math.max(maxX, cell.x());
            minZ = Math.min(minZ, cell.z());
            maxZ = Math.max(maxZ, cell.z());
        }
        return new BoundingBox(minX, floorY, minZ, maxX, floorY + 2, maxZ);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("FloorY", floorY);
        tag.putIntArray("Cells", cells);
    }

    @Override
    public void postProcess(
            WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
            RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos
    ) {
        for (int i = 0; i + 1 < cells.length; i += 2) {
            int x = cells[i];
            int z = cells[i + 1];
            if (!chunkBB.isInside(x, floorY, z)) {
                continue;
            }
            fillColumnDown(level, ThalSettlementPalette.foundation(), x, floorY - 1, z, chunkBB);
            placeBlock(level, ThalSettlementPalette.path(), x, floorY, z, chunkBB);
            placeBlock(level, ThalSettlementPalette.air(), x, floorY + 1, z, chunkBB);
            placeBlock(level, ThalSettlementPalette.air(), x, floorY + 2, z, chunkBB);
        }
    }
}
