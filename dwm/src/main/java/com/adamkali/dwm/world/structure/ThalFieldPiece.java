package com.adamkali.dwm.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * A tilled field: two blocks of farmland either side of a central water channel, planted with
 * {@link ThalSettlementPalette#FIELD_CROP}. The cell at the door is a path tile so the field can be entered.
 */
public class ThalFieldPiece extends ThalSettlementPiece {
    public static final int HEIGHT = 3;

    public ThalFieldPiece(ThalFrame frame, int floorY) {
        super(DWMStructurePieceTypes.THAL_FIELD, frame, floorY, HEIGHT);
    }

    public ThalFieldPiece(CompoundTag tag) {
        super(DWMStructurePieceTypes.THAL_FIELD, tag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
    }

    @Override
    public void postProcess(
            WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
            RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos
    ) {
        int w = frame.width();
        int d = frame.depth();
        int doorU = frame.doorU();
        int channelV = d / 2;

        foundationEverywhere(level, chunkBB);
        clear(level, chunkBB, 0, 1, 0, w - 1, HEIGHT - 1, d - 1);
        fill(level, chunkBB, ThalSettlementPalette.farmland(), 0, 0, 0, w - 1, 0, d - 1);

        // Water channel capped at both ends so it cannot flow out.
        fill(level, chunkBB, ThalSettlementPalette.water(), 1, 0, channelV, w - 2, 0, channelV);
        put(level, chunkBB, ThalSettlementPalette.post(), 0, 0, channelV);
        put(level, chunkBB, ThalSettlementPalette.post(), w - 1, 0, channelV);

        for (int u = 0; u < w; u++) {
            for (int v = 0; v < d; v++) {
                if (v == channelV || isCorner(u, v) || (u == doorU && v == 0)) {
                    continue;
                }
                int worldX = frame.worldX(u, v);
                int worldZ = frame.worldZ(u, v);
                int span = ThalSettlementPalette.FIELD_CROP.getMaxAge() - ThalSettlementPalette.FIELD_CROP_MIN_AGE + 1;
                int age = ThalSettlementPalette.FIELD_CROP_MIN_AGE
                        + RandomSource.create(BlockPos.asLong(worldX, floorY, worldZ)).nextInt(span);
                put(level, chunkBB, ThalSettlementPalette.crop(age), u, 1, v);
            }
        }

        // Corner posts and the walkable entrance tile.
        for (int u : new int[]{0, w - 1}) {
            for (int v : new int[]{0, d - 1}) {
                put(level, chunkBB, ThalSettlementPalette.post(), u, 0, v);
                put(level, chunkBB, ThalSettlementPalette.wall(), u, 1, v);
            }
        }
        put(level, chunkBB, ThalSettlementPalette.path(), doorU, 0, 0);
    }
}
