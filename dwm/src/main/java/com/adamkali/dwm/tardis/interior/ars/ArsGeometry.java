package com.adamkali.dwm.tardis.interior.ars;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * Pure block geometry for placing ARS pieces in an interior plot: piece boxes, doorway apertures, the
 * console annex and console doorway, and template placement positions. Boxes are plot-local (relative
 * to the plot origin, inclusive bounds) unless a method says otherwise; {@link BoundingBox} is mutable,
 * so every method returns a fresh box.
 */
public final class ArsGeometry {
    /** Fixed lobby joining the console room to the root link. Not a registry piece. */
    public static final Identifier ANNEX_STRUCTURE_ID =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis/ars/console_annex");
    /** Plot-local origin of the annex template (placed unrotated): just east of the console room's east wall. */
    public static final BlockPos ANNEX_ORIGIN = new BlockPos(11, ArsGrid.FLOOR_Y, 6);
    public static final int ANNEX_SIZE_X = 5;
    public static final int ANNEX_SIZE_Y = 6;
    public static final int ANNEX_SIZE_Z = 9;

    /** Console room east wall, carved open while any ARS piece exists: plot-local x. */
    public static final int CONSOLE_DOORWAY_X = 10;
    public static final int CONSOLE_DOORWAY_MIN_Z = 7;
    public static final int CONSOLE_DOORWAY_MAX_Z = 8;
    public static final int CONSOLE_DOORWAY_MIN_Y = 1;
    public static final int CONSOLE_DOORWAY_MAX_Y = 3;

    private ArsGeometry() {
    }

    /** Vanilla rotation for an ARS rotation {@code 0..3}. */
    public static Rotation rotation(int rotation) {
        if (!ArsGrid.isValidRotation(rotation)) {
            throw new IllegalArgumentException("Invalid ARS rotation: " + rotation);
        }
        return Rotation.values()[rotation];
    }

    /** Plot-local annex box. */
    public static BoundingBox annexBox() {
        return new BoundingBox(
                ANNEX_ORIGIN.getX(), ANNEX_ORIGIN.getY(), ANNEX_ORIGIN.getZ(),
                ANNEX_ORIGIN.getX() + ANNEX_SIZE_X - 1,
                ANNEX_ORIGIN.getY() + ANNEX_SIZE_Y - 1,
                ANNEX_ORIGIN.getZ() + ANNEX_SIZE_Z - 1);
    }

    /** Plot-local doorway through the console room's east wall (2 wide, 3 high). */
    public static BoundingBox consoleDoorwayBox() {
        return new BoundingBox(
                CONSOLE_DOORWAY_X, CONSOLE_DOORWAY_MIN_Y, CONSOLE_DOORWAY_MIN_Z,
                CONSOLE_DOORWAY_X, CONSOLE_DOORWAY_MAX_Y, CONSOLE_DOORWAY_MAX_Z);
    }

    /**
     * Plot-local box of a placed piece: its rotated footprint ({@code × CELL_SIZE}) from the anchor cell's min
     * corner, {@code height} blocks up from {@link ArsGrid#FLOOR_Y}.
     */
    public static BoundingBox pieceBox(ArsPlacedPiece piece, ArsPieceDefinition definition, int height) {
        int width = ArsGrid.rotatedWidth(definition.width(), definition.depth(), piece.rotation);
        int depth = ArsGrid.rotatedDepth(definition.width(), definition.depth(), piece.rotation);
        int minX = ArsGrid.cellMinBlockX(piece.cellX);
        int minZ = ArsGrid.cellMinBlockZ(piece.cellZ);
        return new BoundingBox(
                minX, ArsGrid.FLOOR_Y, minZ,
                minX + width * ArsGrid.CELL_SIZE - 1,
                ArsGrid.FLOOR_Y + height - 1,
                minZ + depth * ArsGrid.CELL_SIZE - 1);
    }

    /**
     * Plot-local aperture on a cell's outermost block layer facing {@code side}: the 4 blocks at offsets
     * {@code 2..5} along the face, {@code y = 1..4}. This is where a piece's doorway plug sits.
     */
    public static BoundingBox apertureBox(ArsCell cell, Direction side) {
        int minX = ArsGrid.cellMinBlockX(cell.x());
        int minZ = ArsGrid.cellMinBlockZ(cell.z());
        int last = ArsGrid.CELL_SIZE - 1;
        int minY = ArsGrid.APERTURE_MIN_Y;
        int maxY = ArsGrid.APERTURE_MAX_Y;
        int lo = ArsGrid.APERTURE_MIN_OFFSET;
        int hi = ArsGrid.APERTURE_MAX_OFFSET;
        return switch (side) {
            case NORTH -> new BoundingBox(minX + lo, minY, minZ, minX + hi, maxY, minZ);
            case SOUTH -> new BoundingBox(minX + lo, minY, minZ + last, minX + hi, maxY, minZ + last);
            case WEST -> new BoundingBox(minX, minY, minZ + lo, minX, maxY, minZ + hi);
            case EAST -> new BoundingBox(minX + last, minY, minZ + lo, minX + last, maxY, minZ + hi);
            case UP, DOWN -> throw new IllegalArgumentException("ARS apertures are horizontal, got " + side);
        };
    }

    /**
     * Plot-local doorway of an open link: both wall layers, the link cell's own aperture and the facing
     * aperture of the cell across the link. For the root link the second layer is the annex doorway.
     */
    public static BoundingBox doorwayBox(ArsLayoutLogic.Link link) {
        return BoundingBox.encapsulating(
                apertureBox(link.cell(), link.side()),
                apertureBox(link.cell().relative(link.side()), link.side().getOpposite()));
    }

    /**
     * The {@code placeInWorld} position for a template of size {@code sizeX × sizeZ} rotated by
     * {@code rotation}, so that its rotated bounding box min corner lands on {@code anchorMin}. Use it as
     * both the position and the reference position, with the default (zero) rotation pivot.
     */
    public static BlockPos templatePosition(BlockPos anchorMin, int rotation, int sizeX, int sizeZ) {
        return StructureTemplate.getZeroPositionWithTransform(anchorMin, Mirror.NONE, rotation(rotation), sizeX, sizeZ);
    }

    /** {@code box} moved from plot-local to world coordinates. */
    public static BoundingBox toWorld(BoundingBox local, BlockPos plotOrigin) {
        return local.moved(plotOrigin.getX(), plotOrigin.getY(), plotOrigin.getZ());
    }

    /** Inclusive chunk bounds {@code [minChunkX, maxChunkX, minChunkZ, maxChunkZ]} of a world-space box. */
    public static int[] chunkBounds(BoundingBox worldBox) {
        return new int[]{
                SectionPos.blockToSectionCoord(worldBox.minX()),
                SectionPos.blockToSectionCoord(worldBox.maxX()),
                SectionPos.blockToSectionCoord(worldBox.minZ()),
                SectionPos.blockToSectionCoord(worldBox.maxZ())
        };
    }
}
