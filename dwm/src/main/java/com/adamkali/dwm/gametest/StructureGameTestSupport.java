package com.adamkali.dwm.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import com.adamkali.dwm.world.structure.ThalSettlementLayout;
import com.adamkali.dwm.world.structure.ThalSettlementStructure;

import java.util.ArrayList;
import java.util.List;

/** Places real jigsaw worldgen structures at a chosen spot, for server GameTests and client screenshots. */
public final class StructureGameTestSupport {
    private static final int MAX_SEEDS = 64;

    private StructureGameTestSupport() {
    }

    /**
     * Generates {@code structureId} as worldgen would (biome check bypassed, like {@code /place structure})
     * and places it the way {@code /place jigsaw} does, with the start piece's minimum corner at {@code anchor}.
     *
     * <p>Seeds are tried from 1 upward and the first start with at least {@code minPieces} pieces wins, so the
     * chosen pieces and rotation are reproducible.
     *
     * @return the placed pieces, start piece first
     */
    public static List<PoolElementStructurePiece> placeJigsaw(ServerLevel level, Identifier structureId, BlockPos anchor, int minPieces) {
        Holder.Reference<Structure> structure = level.registryAccess()
                .lookupOrThrow(Registries.STRUCTURE)
                .getOrThrow(ResourceKey.create(Registries.STRUCTURE, structureId));
        ChunkGenerator generator = level.getChunkSource().getGenerator();

        for (long seed = 1; seed <= MAX_SEEDS; seed++) {
            StructureStart start = structure.value().generate(
                    structure, level.dimension(), level.registryAccess(), generator, generator.getBiomeSource(),
                    level.getChunkSource().randomState(), level.getStructureManager(), seed,
                    ChunkPos.containing(anchor), 0, level, biome -> true);
            if (!start.isValid() || start.getPieces().size() < minPieces) {
                continue;
            }

            List<PoolElementStructurePiece> pieces = new ArrayList<>();
            for (StructurePiece piece : start.getPieces()) {
                pieces.add((PoolElementStructurePiece) piece);
            }
            BoundingBox startBox = pieces.getFirst().getBoundingBox();
            int dx = anchor.getX() - startBox.minX();
            int dy = anchor.getY() - startBox.minY();
            int dz = anchor.getZ() - startBox.minZ();
            for (PoolElementStructurePiece piece : pieces) {
                piece.move(dx, dy, dz);
            }

            RandomSource random = RandomSource.create(seed);
            for (PoolElementStructurePiece piece : pieces) {
                piece.place(level, level.structureManager(), generator, random, BoundingBox.infinite(), anchor, false);
            }
            return pieces;
        }
        throw new AssertionError(structureId + " never produced " + minPieces + " pieces in " + MAX_SEEDS + " seeds");
    }

    /** The box enclosing every placed piece. */
    public static BoundingBox bounds(List<PoolElementStructurePiece> pieces) {
        return BoundingBox.encapsulatingBoxes(pieces.stream().map(StructurePiece::getBoundingBox).toList()).orElseThrow();
    }

    /**
     * Builds the pieces of a Thal settlement for {@code layout} (nothing is placed yet).
     * {@code floorY} is the shared floor layer, the same value worldgen passes to
     * {@link ThalSettlementStructure#buildPieces}.
     */
    public static PiecesContainer buildThalPieces(ThalSettlementLayout.Layout layout, int centreX, int centreZ, int floorY) {
        StructurePiecesBuilder builder = new StructurePiecesBuilder();
        ThalSettlementStructure.buildPieces(builder, layout, centreX, centreZ, floorY);
        return builder.build();
    }

    /**
     * Builds a Thal settlement and places it into {@code level} the way world generation does: the start and
     * references are registered with the chunks, then {@link StructureStart#placeInChunk} runs chunk by chunk,
     * so doors and walls straddling chunk borders and resident spawning behave as in a real world. The caller
     * prepares the terrain and keeps the chunks loaded.
     *
     * @return the placed pieces
     */
    public static PiecesContainer placeThalSettlement(
            ServerLevel level, ThalSettlementLayout.Layout layout, int centreX, int centreZ, int floorY
    ) {
        PiecesContainer pieces = buildThalPieces(layout, centreX, centreZ, floorY);
        placeThalPieces(level, pieces, centreX, centreZ, floorY);
        return pieces;
    }

    /** Places already built (or save-and-reloaded) settlement pieces; see {@link #placeThalSettlement}. */
    public static void placeThalPieces(ServerLevel level, PiecesContainer pieces, int centreX, int centreZ, int floorY) {
        Structure structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                .getValueOrThrow(ThalSettlementStructure.KEY);
        ChunkPos startChunk = ChunkPos.containing(new BlockPos(centreX, floorY, centreZ));
        BoundingBox box = pieces.calculateBoundingBox();
        List<ChunkPos> chunks = new ArrayList<>();
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                chunks.add(new ChunkPos(cx, cz));
            }
        }

        StructureStart start = new StructureStart(structure, startChunk, 0, pieces);
        for (ChunkPos chunkPos : chunks) {
            ChunkAccess chunk = level.getChunk(chunkPos.x(), chunkPos.z());
            if (chunkPos.equals(startChunk)) {
                chunk.setStartForStructure(structure, start);
            }
            chunk.addReferenceForStructure(structure, startChunk.pack());
        }
        for (ChunkPos chunkPos : chunks) {
            start.placeInChunk(
                    level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(),
                    new BoundingBox(chunkPos.getMinBlockX(), level.getMinY(), chunkPos.getMinBlockZ(),
                            chunkPos.getMaxBlockX(), level.getMaxY(), chunkPos.getMaxBlockZ()),
                    chunkPos);
        }
    }
}
