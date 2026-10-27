package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.ThalEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * Shared base of the settlement buildings. Pieces keep their orientation {@code null} so the inherited
 * {@code getWorldPos} / {@code placeBlock} work on absolute world coordinates; rotation is handled by
 * {@link ThalFrame} instead. Local coordinates are {@code (u, y, v)}: {@code y = 0} is the floor layer
 * (the terrain surface block, replaced), {@code v = 0} the door wall.
 */
abstract class ThalSettlementPiece extends StructurePiece {
    /** Residents' home radius: the layout bound plus a margin, so the whole settlement is home ground. */
    static final int RESIDENT_HOME_RADIUS = ThalSettlementLayout.BOUND + 6;

    protected final ThalFrame frame;
    /** World Y of the floor layer (the block row that replaces the surface). */
    protected final int floorY;
    /** Settlement centre that residents are homed to (defaults to this piece's own centre). */
    private int homeX;
    private int homeZ;

    /**
     * The terrain beardifier treats a piece box's {@code minY} as the first air block, so the box must start one
     * above the floor layer; otherwise the surface is reshaped to sit one block under the floor.
     */
    static int terrainPlaneY(int floorY) {
        return floorY + 1;
    }

    protected ThalSettlementPiece(StructurePieceType type, ThalFrame frame, int floorY, int height) {
        super(type, 0, new BoundingBox(
                frame.minX(), terrainPlaneY(floorY), frame.minZ(), frame.maxX(), floorY + height - 1, frame.maxZ()));
        this.frame = frame;
        this.floorY = floorY;
        this.homeX = (frame.minX() + frame.maxX()) / 2;
        this.homeZ = (frame.minZ() + frame.maxZ()) / 2;
        // Absolute coordinates: no piece orientation (this also initialises rotation/mirror to NONE).
        setOrientation(null);
    }

    protected ThalSettlementPiece(StructurePieceType type, CompoundTag tag) {
        super(type, tag);
        this.frame = new ThalFrame(
                tag.getIntOr("MinX", 0),
                tag.getIntOr("MinZ", 0),
                tag.getIntOr("Width", 1),
                tag.getIntOr("Depth", 1),
                Direction.from2DDataValue(tag.getIntOr("Facing", 0)));
        this.floorY = tag.getIntOr("FloorY", 0);
        this.homeX = tag.getIntOr("HomeX", (frame.minX() + frame.maxX()) / 2);
        this.homeZ = tag.getIntOr("HomeZ", (frame.minZ() + frame.maxZ()) / 2);
    }

    /** Homes this piece's residents to the settlement centre so they stay in the settlement. */
    void setSettlementCentre(int x, int z) {
        this.homeX = x;
        this.homeZ = z;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("MinX", frame.minX());
        tag.putInt("MinZ", frame.minZ());
        tag.putInt("Width", frame.width());
        tag.putInt("Depth", frame.depth());
        tag.putInt("Facing", frame.facing().get2DDataValue());
        tag.putInt("FloorY", floorY);
        tag.putInt("HomeX", homeX);
        tag.putInt("HomeZ", homeZ);
    }

    // --- local-coordinate helpers -------------------------------------------------------------------------

    protected final void put(WorldGenLevel level, BoundingBox chunkBB, BlockState state, int u, int y, int v) {
        placeBlock(level, state, frame.worldX(u, v), floorY + y, frame.worldZ(u, v), chunkBB);
    }

    protected final void fill(
            WorldGenLevel level, BoundingBox chunkBB, BlockState state,
            int u0, int y0, int v0, int u1, int y1, int v1
    ) {
        for (int u = u0; u <= u1; u++) {
            for (int v = v0; v <= v1; v++) {
                for (int y = y0; y <= y1; y++) {
                    put(level, chunkBB, state, u, y, v);
                }
            }
        }
    }

    protected final void clear(
            WorldGenLevel level, BoundingBox chunkBB, int u0, int y0, int v0, int u1, int y1, int v1
    ) {
        fill(level, chunkBB, ThalSettlementPalette.air(), u0, y0, v0, u1, y1, v1);
    }

    /** Fills the column under the floor layer at {@code (u, v)} until it reaches solid ground. */
    protected final void foundation(WorldGenLevel level, BoundingBox chunkBB, BlockState state, int u, int v) {
        fillColumnDown(level, state, frame.worldX(u, v), floorY - 1, frame.worldZ(u, v), chunkBB);
    }

    protected final void foundationEverywhere(WorldGenLevel level, BoundingBox chunkBB) {
        for (int u = 0; u < frame.width(); u++) {
            for (int v = 0; v < frame.depth(); v++) {
                foundation(level, chunkBB, ThalSettlementPalette.foundation(), u, v);
            }
        }
    }

    protected final boolean isPerimeter(int u, int v) {
        return u == 0 || v == 0 || u == frame.width() - 1 || v == frame.depth() - 1;
    }

    protected final boolean isCorner(int u, int v) {
        return (u == 0 || u == frame.width() - 1) && (v == 0 || v == frame.depth() - 1);
    }

    /** Stepped roof: stair eaves around plank tiers, capped with slabs once a tier is too small. */
    protected final void roof(WorldGenLevel level, BoundingBox chunkBB, int startY) {
        int y = startY;
        for (int inset = 0; ; inset++, y++) {
            int u0 = inset;
            int u1 = frame.width() - 1 - inset;
            int v0 = inset;
            int v1 = frame.depth() - 1 - inset;
            if (u1 - u0 + 1 < 3 || v1 - v0 + 1 < 3) {
                fill(level, chunkBB, ThalSettlementPalette.slab(), u0, y, v0, u1, y, v1);
                return;
            }
            for (int u = u0; u <= u1; u++) {
                for (int v = v0; v <= v1; v++) {
                    BlockState state = ThalSettlementPalette.planks();
                    boolean edgeU = u == u0 || u == u1;
                    boolean edgeV = v == v0 || v == v1;
                    if (edgeU != edgeV) {
                        Direction ascending;
                        if (v == v0) {
                            ascending = frame.inward();
                        } else if (v == v1) {
                            ascending = frame.facing();
                        } else if (u == u0) {
                            ascending = frame.uDirection();
                        } else {
                            ascending = frame.uDirection().getOpposite();
                        }
                        state = ThalSettlementPalette.stairs(ascending);
                    }
                    put(level, chunkBB, state, u, y, v);
                }
            }
        }
    }

    // --- residents ---------------------------------------------------------------------------------------

    /**
     * Spawns {@code count} persistent Thals at local cell {@code (u, v)} (standing on the floor layer) if that
     * cell is inside the chunk currently being generated, homed within {@link #RESIDENT_HOME_RADIUS} of the
     * settlement centre so they wander the settlement rather than leave it. Returns whether they were spawned so the caller can
     * set its once-only flag, as the vanilla swamp hut does for its witch.
     */
    protected final boolean spawnResidents(WorldGenLevel level, BoundingBox chunkBB, int u, int v, int count) {
        BlockPos pos = new BlockPos(frame.worldX(u, v), floorY + 1, frame.worldZ(u, v));
        if (!chunkBB.isInside(pos)) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            ThalEntity thal = DWMEntityTypes.THAL.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (thal == null) {
                continue;
            }
            thal.setPersistenceRequired();
            thal.setHomeTo(new BlockPos(homeX, floorY + 1, homeZ), RESIDENT_HOME_RADIUS);
            double offset = (i - (count - 1) / 2.0) * 0.4;
            thal.snapTo(pos.getX() + 0.5 + offset, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
            thal.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
            level.addFreshEntityWithPassengers(thal);
        }
        return true;
    }

    @Override
    public abstract void postProcess(
            WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
            RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos);
}
