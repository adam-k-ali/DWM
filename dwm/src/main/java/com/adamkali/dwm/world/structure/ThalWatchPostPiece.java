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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * A 3x3 petrified-log watch tower: a door and a ladder inside lead up to a railed platform under a slab roof.
 * No guards stand on it (guards are out of scope for the settlement).
 */
public class ThalWatchPostPiece extends ThalSettlementPiece {
    public static final int HEIGHT = 9;
    /** Height of the platform deck; the ladder runs one block above it. */
    static final int DECK_Y = 5;

    public ThalWatchPostPiece(ThalFrame frame, int floorY) {
        super(DWMStructurePieceTypes.THAL_WATCH_POST, frame, floorY, HEIGHT);
    }

    public ThalWatchPostPiece(CompoundTag tag) {
        super(DWMStructurePieceTypes.THAL_WATCH_POST, tag);
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
        // The frame is 3x3: front row v=0 (door in the middle), back row v=2, ladder in the centre cell.
        foundationEverywhere(level, chunkBB);
        clear(level, chunkBB, 0, 1, 0, 2, HEIGHT - 1, 2);
        fill(level, chunkBB, ThalSettlementPalette.planks(), 0, 0, 0, 2, 0, 2);

        // Corner pillars reach the roof.
        for (int u : new int[]{0, 2}) {
            for (int v : new int[]{0, 2}) {
                fill(level, chunkBB, ThalSettlementPalette.post(), u, 0, v, u, DECK_Y + 2, v);
            }
        }
        // Walls up to the deck: sides and back solid, front holds the door and a lintel.
        fill(level, chunkBB, ThalSettlementPalette.planks(), 0, 1, 1, 0, DECK_Y - 1, 1);
        fill(level, chunkBB, ThalSettlementPalette.planks(), 2, 1, 1, 2, DECK_Y - 1, 1);
        fill(level, chunkBB, ThalSettlementPalette.planks(), 1, 1, 2, 1, DECK_Y + 1, 2);
        fill(level, chunkBB, ThalSettlementPalette.planks(), 1, 3, 0, 1, DECK_Y - 1, 0);
        put(level, chunkBB, ThalSettlementPalette.door(frame.inward(), DoubleBlockHalf.LOWER), 1, 1, 0);
        put(level, chunkBB, ThalSettlementPalette.door(frame.inward(), DoubleBlockHalf.UPPER), 1, 2, 0);

        // Deck ring (centre stays open for the ladder), railing on the sides, open above the door.
        for (int u = 0; u < 3; u++) {
            for (int v = 0; v < 3; v++) {
                if (u != 1 || v != 1) {
                    if (!isCorner(u, v)) {
                        put(level, chunkBB, ThalSettlementPalette.planks(), u, DECK_Y, v);
                    }
                }
            }
        }
        put(level, chunkBB, ThalSettlementPalette.wall(), 0, DECK_Y + 1, 1);
        put(level, chunkBB, ThalSettlementPalette.wall(), 2, DECK_Y + 1, 1);

        // Ladder rides the back wall's inner face; it faces the door.
        fill(level, chunkBB, ThalSettlementPalette.ladder(frame.facing()), 1, 1, 1, 1, DECK_Y + 1, 1);

        // Roof and a lantern beside the ladder.
        fill(level, chunkBB, ThalSettlementPalette.slab(), 0, DECK_Y + 3, 0, 2, DECK_Y + 3, 2);
        put(level, chunkBB, ThalSettlementPalette.hangingLantern(), 0, DECK_Y + 2, 1);
    }
}
