package com.adamkali.dwm.tardis.interior.ars;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * Pure block geometry for placing ARS pieces in an interior plot: piece boxes, doorway apertures, the console
 * lobbies and console doorways, and template placement positions. Boxes are grid-local (relative to the grid
 * origin, {@link ArsGrid#gridOrigin}, inclusive bounds) unless a method says otherwise; {@link BoundingBox} is
 * mutable, so every method returns a fresh box.
 */
public final class ArsGeometry {
    /** Console room doorways (both walls): grid-local z and y, 2 wide and 3 high. Anchor-local z 7..8. */
    public static final int CONSOLE_DOORWAY_MIN_Z = ArsGrid.CONSOLE_ORIGIN.getZ() + 7;
    public static final int CONSOLE_DOORWAY_MAX_Z = CONSOLE_DOORWAY_MIN_Z + 1;
    public static final int CONSOLE_DOORWAY_MIN_Y = 1;
    public static final int CONSOLE_DOORWAY_MAX_Y = 3;
    /** Grid-local x of the console room's east and west walls. */
    public static final int CONSOLE_EAST_WALL_X = ArsGrid.CONSOLE_ORIGIN.getX() + FirstDoctorConsoleRoomLayout.SIZE_X - 1;
    public static final int CONSOLE_WEST_WALL_X = ArsGrid.CONSOLE_ORIGIN.getX();

    /**
     * Lobby cross-section, shared by both lobbies: walls at the aperture's offsets 1 and 6 of the root row
     * ({@code z = 33} and {@code 38}), floor {@code y = 0} and ceiling {@code y = 5}. Open at both x ends.
     */
    public static final int LOBBY_MIN_Z = ArsGrid.cellMinBlockZ(ArsGrid.EAST_ROOT.cell().z()) + ArsGrid.APERTURE_MIN_OFFSET - 1;
    public static final int LOBBY_SIZE_Y = 6;
    public static final int LOBBY_SIZE_Z = ArsGrid.APERTURE_MAX_OFFSET - ArsGrid.APERTURE_MIN_OFFSET + 3;

    /**
     * Where the console room meets the grid on one side: the root link, the doorway carved through the console
     * room's wall while that link is open, and the fixed lobby (not a registry piece) that joins the two, placed
     * unrotated at {@code lobbyMinX, FLOOR_Y, LOBBY_MIN_Z}.
     */
    public record RootEntrance(ArsLayoutLogic.Link link, int doorwayX, Identifier lobbyTemplate, int lobbyMinX, int lobbySizeX) {
        /** Grid-local doorway through the console room's wall (2 wide, 3 high). */
        public BoundingBox consoleDoorwayBox() {
            return new BoundingBox(
                    doorwayX, CONSOLE_DOORWAY_MIN_Y, CONSOLE_DOORWAY_MIN_Z,
                    doorwayX, CONSOLE_DOORWAY_MAX_Y, CONSOLE_DOORWAY_MAX_Z);
        }

        /** Grid-local min corner of the lobby template. */
        public BlockPos lobbyOrigin() {
            return new BlockPos(lobbyMinX, ArsGrid.FLOOR_Y, LOBBY_MIN_Z);
        }

        /** Grid-local lobby box. */
        public BoundingBox lobbyBox() {
            return new BoundingBox(
                    lobbyMinX, ArsGrid.FLOOR_Y, LOBBY_MIN_Z,
                    lobbyMinX + lobbySizeX - 1, ArsGrid.FLOOR_Y + LOBBY_SIZE_Y - 1, LOBBY_MIN_Z + LOBBY_SIZE_Z - 1);
        }
    }

    /** East: from the console room's east wall (x = 37) to the west face of cell (5, 4) (x = 40). Lobby x 38..39. */
    public static final RootEntrance EAST_ROOT = new RootEntrance(
            ArsLayoutLogic.EAST_ROOT_LINK,
            CONSOLE_EAST_WALL_X,
            lobby("console_lobby_east"),
            CONSOLE_EAST_WALL_X + 1,
            ArsGrid.cellMinBlockX(ArsGrid.EAST_ROOT.cell().x()) - CONSOLE_EAST_WALL_X - 1);
    /** West: from the east face of cell (2, 4) (x = 23) to the console room's west wall (x = 27). Lobby x 24..26. */
    public static final RootEntrance WEST_ROOT = new RootEntrance(
            ArsLayoutLogic.WEST_ROOT_LINK,
            CONSOLE_WEST_WALL_X,
            lobby("console_lobby_west"),
            ArsGrid.cellMinBlockX(ArsGrid.WEST_ROOT.cell().x() + 1),
            CONSOLE_WEST_WALL_X - ArsGrid.cellMinBlockX(ArsGrid.WEST_ROOT.cell().x() + 1));
    /** Both root entrances, in {@link ArsLayoutLogic#ROOT_LINKS} order. */
    public static final List<RootEntrance> ROOT_ENTRANCES = List.of(EAST_ROOT, WEST_ROOT);

    private ArsGeometry() {
    }

    private static Identifier lobby(String name) {
        return Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis/ars/" + name);
    }

    /** Vanilla rotation for an ARS rotation {@code 0..3}. */
    public static Rotation rotation(int rotation) {
        if (!ArsGrid.isValidRotation(rotation)) {
            throw new IllegalArgumentException("Invalid ARS rotation: " + rotation);
        }
        return Rotation.values()[rotation];
    }

    /**
     * Grid-local box of a placed piece: its rotated footprint ({@code × CELL_SIZE}) from the anchor cell's min
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
     * Grid-local aperture on a cell's outermost block layer facing {@code side}: the 4 blocks at offsets
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
     * Grid-local doorway of an open link: both wall layers, the link cell's own aperture and the facing
     * aperture of the cell across the link. For a root link the console room's side is the open end of its lobby.
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

    /**
     * {@code local} with its x and z clamped to the plot window ({@link ArsGrid#windowMinBlock()} ..
     * {@link ArsGrid#windowMaxBlock()}), so chunk work around a piece on the grid edge never reaches a neighbouring
     * TARDIS's window. Y is unchanged.
     */
    public static BoundingBox clampToWindow(BoundingBox local) {
        int first = ArsGrid.windowMinBlock();
        int last = ArsGrid.windowMaxBlock();
        return new BoundingBox(
                Math.clamp(local.minX(), first, last), local.minY(), Math.clamp(local.minZ(), first, last),
                Math.clamp(local.maxX(), first, last), local.maxY(), Math.clamp(local.maxZ(), first, last));
    }

    /** {@code box} moved from grid-local to world coordinates ({@code gridOrigin} from {@link ArsGrid#gridOrigin}). */
    public static BoundingBox toWorld(BoundingBox local, BlockPos gridOrigin) {
        return local.moved(gridOrigin.getX(), gridOrigin.getY(), gridOrigin.getZ());
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
