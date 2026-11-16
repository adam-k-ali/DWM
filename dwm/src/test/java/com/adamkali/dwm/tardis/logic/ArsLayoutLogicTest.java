package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.interior.ars.ArsCell;
import com.adamkali.dwm.tardis.interior.ars.ArsConnector;
import com.adamkali.dwm.tardis.interior.ars.ArsGrid;
import com.adamkali.dwm.tardis.interior.ars.ArsPieceDefinition;
import com.adamkali.dwm.tardis.interior.ars.ArsPieceKind;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic.Link;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic.PlaceResult;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic.RemoveResult;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArsLayoutLogicTest {
    private static final String HALL = "dwm:hall";
    private static final String STRAIGHT = "dwm:straight";
    private static final String CROSS = "dwm:cross";
    private static final String DEAD_END = "dwm:dead_end";
    private static final String GALLERY = "dwm:gallery";

    /** 3x2 room: doors on the west of (0,0), east of (2,1), north of (1,0). */
    private static final ArsPieceDefinition HALL_DEF = def(ArsPieceKind.ROOM, 3, 2, 41,
            new ArsConnector(new ArsCell(0, 0), Direction.WEST),
            new ArsConnector(new ArsCell(2, 1), Direction.EAST),
            new ArsConnector(new ArsCell(1, 0), Direction.NORTH));
    /** 1x1 east-west corridor. */
    private static final ArsPieceDefinition STRAIGHT_DEF = def(ArsPieceKind.CORRIDOR, 1, 1, 10,
            new ArsConnector(new ArsCell(0, 0), Direction.WEST),
            new ArsConnector(new ArsCell(0, 0), Direction.EAST));
    /** 1x1 junction open on all four sides. */
    private static final ArsPieceDefinition CROSS_DEF = def(ArsPieceKind.CORRIDOR, 1, 1, 12,
            new ArsConnector(new ArsCell(0, 0), Direction.NORTH),
            new ArsConnector(new ArsCell(0, 0), Direction.EAST),
            new ArsConnector(new ArsCell(0, 0), Direction.SOUTH),
            new ArsConnector(new ArsCell(0, 0), Direction.WEST));
    /** 1x1 dead end opening north only. */
    private static final ArsPieceDefinition DEAD_END_DEF = def(ArsPieceKind.CORRIDOR, 1, 1, 5,
            new ArsConnector(new ArsCell(0, 0), Direction.NORTH));
    /** 6x1 gallery with a west door, long enough to span the console room's cells and the grid edge. */
    private static final ArsPieceDefinition GALLERY_DEF = def(ArsPieceKind.ROOM, 6, 1, 30,
            new ArsConnector(new ArsCell(0, 0), Direction.WEST));

    private static final Map<Identifier, ArsPieceDefinition> DEFINITIONS = Map.of(
            Identifier.parse(HALL), HALL_DEF,
            Identifier.parse(STRAIGHT), STRAIGHT_DEF,
            Identifier.parse(CROSS), CROSS_DEF,
            Identifier.parse(DEAD_END), DEAD_END_DEF,
            Identifier.parse(GALLERY), GALLERY_DEF
    );
    private static final Function<Identifier, ArsPieceDefinition> DEFS = DEFINITIONS::get;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    // --- footprint and connector transforms -------------------------------------------------

    @Test
    void occupiedCells_allRotationsOfNonSquarePiece() {
        assertEquals(cells(3, 3, 4, 3, 5, 3, 3, 4, 4, 4, 5, 4),
                new HashSet<>(ArsLayoutLogic.occupiedCells(piece(HALL, 3, 3, 0), HALL_DEF)));
        assertEquals(cells(3, 3, 4, 3, 3, 4, 4, 4, 3, 5, 4, 5),
                new HashSet<>(ArsLayoutLogic.occupiedCells(piece(HALL, 3, 3, 1), HALL_DEF)));
        assertEquals(cells(3, 3, 4, 3, 5, 3, 3, 4, 4, 4, 5, 4),
                new HashSet<>(ArsLayoutLogic.occupiedCells(piece(HALL, 3, 3, 2), HALL_DEF)));
        assertEquals(cells(3, 3, 4, 3, 3, 4, 4, 4, 3, 5, 4, 5),
                new HashSet<>(ArsLayoutLogic.occupiedCells(piece(HALL, 3, 3, 3), HALL_DEF)));
    }

    @Test
    void connectors_allRotationsOfNonSquarePiece() {
        assertEquals(List.of(
                conn(3, 3, Direction.WEST), conn(5, 4, Direction.EAST), conn(4, 3, Direction.NORTH)
        ), ArsLayoutLogic.connectors(piece(HALL, 3, 3, 0), HALL_DEF));
        assertEquals(List.of(
                conn(4, 3, Direction.NORTH), conn(3, 5, Direction.SOUTH), conn(4, 4, Direction.EAST)
        ), ArsLayoutLogic.connectors(piece(HALL, 3, 3, 1), HALL_DEF));
        assertEquals(List.of(
                conn(5, 4, Direction.EAST), conn(3, 3, Direction.WEST), conn(4, 4, Direction.SOUTH)
        ), ArsLayoutLogic.connectors(piece(HALL, 3, 3, 2), HALL_DEF));
        assertEquals(List.of(
                conn(3, 5, Direction.SOUTH), conn(4, 3, Direction.NORTH), conn(3, 4, Direction.WEST)
        ), ArsLayoutLogic.connectors(piece(HALL, 3, 3, 3), HALL_DEF));
    }

    @Test
    void pieceAt_findsRotatedPieceByAnyCell() {
        ArsPlacedPiece hall = piece(HALL, 3, 3, 1);
        List<ArsPlacedPiece> layout = List.of(hall);

        assertEquals(Optional.of(hall), ArsLayoutLogic.pieceAt(layout, DEFS, 4, 5));
        assertEquals(Optional.of(hall), ArsLayoutLogic.pieceAt(layout, DEFS, 3, 3));
        assertEquals(Optional.empty(), ArsLayoutLogic.pieceAt(layout, DEFS, 5, 3));
    }

    // --- canPlace -------------------------------------------------------------------------------

    @Test
    void canPlace_connectsToEastRootLink() {
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(STRAIGHT, 5, 4, 0), 500, false));
        // Hall's west door on its (0,0) cell lands on the east root cell.
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(HALL, 5, 4, 0), 500, false));
        // Rotated 180 its east door turns west on the anchor cell, here (5,3): faces the console, not a root link.
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(HALL, 5, 3, 2), 500, false));
        // One row down that west door sits on the east root cell (5,4).
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(HALL, 5, 4, 2), 500, false));
    }

    @Test
    void canPlace_connectsToWestRootLink() {
        // The east door of a straight corridor on (2,4) faces the console room's west doorway.
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(STRAIGHT, 2, 4, 0), 500, false));
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(STRAIGHT, 2, 4, 1), 500, false));
        // Hall's east door on its (2,1) cell lands on the west root cell (2,4).
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(HALL, 0, 3, 0), 500, false));
        // A dead end turned once opens east onto the west root link.
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(DEAD_END, 2, 4, 1), 500, false));
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(DEAD_END, 2, 4, 0), 500, false));
    }

    @Test
    void canPlace_notConnectedWhenNoConnectorMates() {
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(STRAIGHT, 7, 7, 0), 500, false));
        // Root cell, but a north-only dead end does not face the console.
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(DEAD_END, 5, 4, 0), 500, false));
        // Rotated three quarter turns its door faces west onto the east root link.
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(DEAD_END, 5, 4, 3), 500, false));
        // Next to the console room but not at a root: (5,5) faces the console's east wall without a doorway.
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(STRAIGHT, 5, 5, 0), 500, false));
    }

    @Test
    void canPlace_connectsToExistingPieceOnlyThroughMatchingConnector() {
        List<ArsPlacedPiece> layout = List.of(piece(STRAIGHT, 5, 4, 0));

        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 6, 4, 0), 500, false));
        // Adjacent, but the dead end's north door faces (6,3), not the corridor.
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(DEAD_END, 6, 4, 0), 500, false));
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(DEAD_END, 6, 4, 3), 500, false));
        // A cross north of the straight corridor: the corridor has no north door to mate.
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(CROSS, 5, 3, 0), 500, false));
    }

    @Test
    void canPlace_outOfBounds() {
        assertEquals(PlaceResult.OUT_OF_BOUNDS,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(HALL, 6, 0, 0), 500, false));
        assertEquals(PlaceResult.OUT_OF_BOUNDS,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(HALL, 6, 6, 1), 500, false));
        assertEquals(PlaceResult.OUT_OF_BOUNDS,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(STRAIGHT, 8, 3, 0), 500, false));
        assertEquals(PlaceResult.OUT_OF_BOUNDS,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(STRAIGHT, 3, -1, 0), 500, false));
    }

    @Test
    void canPlace_reservedConsoleCells() {
        for (int x = ArsGrid.RESERVED_MIN_X; x <= ArsGrid.RESERVED_MAX_X; x++) {
            for (int z = ArsGrid.RESERVED_MIN_Z; z <= ArsGrid.RESERVED_MAX_Z; z++) {
                assertEquals(PlaceResult.RESERVED,
                        ArsLayoutLogic.canPlace(List.of(), DEFS, piece(CROSS, x, z, 0), 500, true));
            }
        }
        // Wide piece overlapping the console's south-east corner.
        assertEquals(PlaceResult.RESERVED,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(HALL, 4, 5, 0), 500, true));
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(CROSS, 0, 3, 0), 500, true));
    }

    @Test
    void canPlace_occupied() {
        List<ArsPlacedPiece> layout = List.of(piece(HALL, 5, 4, 0));

        assertEquals(PlaceResult.OCCUPIED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 7, 5, 0), 500, false));
        assertEquals(PlaceResult.OCCUPIED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(HALL, 6, 4, 1), 500, false));
        // North of the hall's north door, a north-south corridor joins it.
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 6, 3, 1), 500, false));
    }

    @Test
    void canPlace_artronAndInstabuild() {
        ArsPlacedPiece candidate = piece(STRAIGHT, 5, 4, 0);

        assertEquals(PlaceResult.NOT_ENOUGH_ARTRON,
                ArsLayoutLogic.canPlace(List.of(), DEFS, candidate, 9, false));
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(List.of(), DEFS, candidate, 10, false));
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(List.of(), DEFS, candidate, 0, true));
    }

    @Test
    void canPlace_checksInDocumentedOrder() {
        // Unknown beats invalid rotation.
        assertEquals(PlaceResult.UNKNOWN_PIECE,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece("dwm:missing", 9, 9, 7), 0, false));
        assertEquals(PlaceResult.UNKNOWN_PIECE,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece("Not An Id!", 5, 4, 0), 500, false));
        assertEquals(PlaceResult.UNKNOWN_PIECE,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(null, 5, 4, 0), 500, false));
        // Invalid rotation beats out of bounds.
        assertEquals(PlaceResult.INVALID_ROTATION,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(STRAIGHT, 9, 9, 4), 0, false));
        assertEquals(PlaceResult.INVALID_ROTATION,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(STRAIGHT, 5, 4, -1), 500, false));
        // Out of bounds beats reserved (footprint spans x = 3..8, through the console room and off the grid).
        assertEquals(PlaceResult.OUT_OF_BOUNDS,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(GALLERY, 3, 4, 0), 0, false));
        // Reserved beats occupied and not-connected (footprint x = 4..6 covers (4,3) and the corridor's (5,4)).
        List<ArsPlacedPiece> layout = List.of(piece(STRAIGHT, 5, 4, 0));
        assertEquals(PlaceResult.RESERVED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(HALL, 4, 3, 0), 0, false));
        // Occupied beats not-connected and artron.
        assertEquals(PlaceResult.OCCUPIED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(DEAD_END, 5, 4, 0), 0, false));
        // Not-connected beats artron.
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 7, 7, 0), 0, false));
    }

    @Test
    void resultsCarryTranslationKeys() {
        assertNull(PlaceResult.OK.translationKey());
        assertNull(RemoveResult.OK.translationKey());
        for (PlaceResult result : PlaceResult.values()) {
            if (result != PlaceResult.OK) {
                assertNotNull(result.translationKey());
                assertTrue(result.translationKey().startsWith("dwm.ars."), result.name());
            }
        }
        for (RemoveResult result : RemoveResult.values()) {
            if (result != RemoveResult.OK) {
                assertNotNull(result.translationKey());
                assertTrue(result.translationKey().startsWith("dwm.ars."), result.name());
            }
        }
        assertEquals(ArsLayoutLogic.NOT_CONNECTED_KEY, PlaceResult.NOT_CONNECTED.translationKey());
        assertEquals(ArsLayoutLogic.WOULD_DISCONNECT_KEY, RemoveResult.WOULD_DISCONNECT.translationKey());
    }

    // --- openLinks ------------------------------------------------------------------------------

    @Test
    void openLinks_rootAndMatchedPairsOnce() {
        List<ArsPlacedPiece> layout = List.of(
                piece(STRAIGHT, 5, 4, 0),
                piece(STRAIGHT, 6, 4, 0),
                // Cross north of the second corridor: walls touch but the corridor has no north door.
                piece(CROSS, 6, 3, 0)
        );

        Set<Link> links = ArsLayoutLogic.openLinks(layout, DEFS);

        assertEquals(Set.of(ArsLayoutLogic.EAST_ROOT_LINK, new Link(new ArsCell(6, 4), Direction.WEST)), links);
        assertTrue(ArsLayoutLogic.EAST_ROOT_LINK.isRoot());
        assertEquals(new Link(new ArsCell(5, 4), Direction.WEST), ArsLayoutLogic.EAST_ROOT_LINK);
    }

    @Test
    void rootLinks_areCanonicalAndHaveOneConsoleConnectorEach() {
        // The west root is the east face of (2,4), canonically the west face of the console cell (3,4).
        assertEquals(new Link(new ArsCell(3, 4), Direction.WEST), ArsLayoutLogic.WEST_ROOT_LINK);
        assertEquals(Link.of(ArsGrid.WEST_ROOT), ArsLayoutLogic.WEST_ROOT_LINK);
        assertEquals(List.of(ArsLayoutLogic.EAST_ROOT_LINK, ArsLayoutLogic.WEST_ROOT_LINK), ArsLayoutLogic.ROOT_LINKS);
        assertTrue(ArsLayoutLogic.WEST_ROOT_LINK.isRoot());
        assertFalse(new Link(new ArsCell(5, 5), Direction.WEST).isRoot());
        assertEquals(List.of(conn(4, 4, Direction.EAST), conn(3, 4, Direction.WEST)), ArsLayoutLogic.CONSOLE_CONNECTORS);
        assertEquals(Set.of(ArsLayoutLogic.WEST_ROOT_LINK),
                ArsLayoutLogic.openLinks(List.of(piece(STRAIGHT, 2, 4, 0)), DEFS));
        assertEquals(Set.copyOf(ArsLayoutLogic.ROOT_LINKS),
                ArsLayoutLogic.openLinks(List.of(piece(STRAIGHT, 2, 4, 0), piece(STRAIGHT, 5, 4, 0)), DEFS));
    }

    @Test
    void openLinks_canonicalisesBothFacesOfAWall() {
        ArsConnector east = conn(4, 4, Direction.EAST);
        ArsConnector west = conn(5, 4, Direction.WEST);
        ArsConnector south = conn(4, 4, Direction.SOUTH);
        ArsConnector north = conn(4, 5, Direction.NORTH);

        assertEquals(Link.of(east), Link.of(west));
        assertEquals(new Link(new ArsCell(5, 4), Direction.WEST), Link.of(east));
        assertEquals(Link.of(south), Link.of(north));
        assertEquals(new Link(new ArsCell(4, 5), Direction.NORTH), Link.of(south));
    }

    @Test
    void openLinks_emptyLayoutHasNoLinks() {
        assertEquals(Set.of(), ArsLayoutLogic.openLinks(List.of(), DEFS));
    }

    // --- canRemove ------------------------------------------------------------------------------

    @Test
    void canRemove_leafOkButCutVertexWouldDisconnect() {
        List<ArsPlacedPiece> layout = List.of(
                piece(STRAIGHT, 5, 4, 0),
                piece(STRAIGHT, 6, 4, 0),
                piece(STRAIGHT, 7, 4, 0)
        );

        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 7, 4));
        assertEquals(RemoveResult.WOULD_DISCONNECT, ArsLayoutLogic.canRemove(layout, DEFS, 6, 4));
        assertEquals(RemoveResult.WOULD_DISCONNECT, ArsLayoutLogic.canRemove(layout, DEFS, 5, 4));
        assertEquals(RemoveResult.NO_PIECE, ArsLayoutLogic.canRemove(layout, DEFS, 5, 5));
    }

    @Test
    void canRemove_loopLeavesAlternatePath() {
        List<ArsPlacedPiece> layout = List.of(
                piece(CROSS, 5, 4, 0),
                piece(CROSS, 6, 4, 0),
                piece(CROSS, 5, 5, 0),
                piece(CROSS, 6, 5, 0)
        );

        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 6, 4));
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 5, 5));
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 6, 5));
        // The only piece on the root link carries everything else.
        assertEquals(RemoveResult.WOULD_DISCONNECT, ArsLayoutLogic.canRemove(layout, DEFS, 5, 4));
    }

    @Test
    void canRemove_eachSideIsItsOwnTreeUnlessJoined() {
        // West: two corridors in a row; east: one corridor. The sides only meet through the console room.
        List<ArsPlacedPiece> layout = List.of(
                piece(STRAIGHT, 2, 4, 0),
                piece(STRAIGHT, 1, 4, 0),
                piece(STRAIGHT, 5, 4, 0)
        );
        assertEquals(RemoveResult.WOULD_DISCONNECT, ArsLayoutLogic.canRemove(layout, DEFS, 2, 4));
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 1, 4));
        // Removing the only piece on the east side closes the east root only.
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 5, 4));
        List<ArsPlacedPiece> westOnly = layout.subList(0, 2);
        Set<Link> links = ArsLayoutLogic.openLinks(westOnly, DEFS);
        assertTrue(links.contains(ArsLayoutLogic.WEST_ROOT_LINK));
        assertFalse(links.contains(ArsLayoutLogic.EAST_ROOT_LINK));
        // The west side still grows from the console room.
        assertEquals(PlaceResult.OK, ArsLayoutLogic.canPlace(westOnly, DEFS, piece(STRAIGHT, 0, 4, 0), 500, false));
    }

    @Test
    void canRemove_ringThroughBothRootsKeepsEverythingReachable() {
        // Crossroads from the west root up and over the console room's north side to the east root.
        List<ArsPlacedPiece> ring = List.of(
                piece(CROSS, 2, 4, 0), piece(CROSS, 2, 3, 0), piece(CROSS, 2, 2, 0), piece(CROSS, 3, 2, 0),
                piece(CROSS, 4, 2, 0), piece(CROSS, 5, 2, 0), piece(CROSS, 5, 3, 0), piece(CROSS, 5, 4, 0));
        Set<Link> links = ArsLayoutLogic.openLinks(ring, DEFS);
        assertTrue(links.containsAll(ArsLayoutLogic.ROOT_LINKS));
        // Each root piece is the only one on its root link, yet the ring reaches the rest from the other side.
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(ring, DEFS, 2, 4));
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(ring, DEFS, 5, 4));
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(ring, DEFS, 3, 2));
        // With the east root piece gone, everything hangs off the west root alone.
        List<ArsPlacedPiece> westRooted = ring.subList(0, 7);
        assertEquals(RemoveResult.WOULD_DISCONNECT, ArsLayoutLogic.canRemove(westRooted, DEFS, 2, 4));
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(westRooted, DEFS, 5, 3));
    }

    @Test
    void canRemove_findsMultiCellPieceByAnyCell() {
        List<ArsPlacedPiece> layout = List.of(piece(HALL, 5, 4, 0));

        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 7, 5));
        assertEquals(RemoveResult.NO_PIECE, ArsLayoutLogic.canRemove(layout, DEFS, 7, 6));
    }

    // --- refund ---------------------------------------------------------------------------------

    @Test
    void refundFor_isHalfRoundedDown() {
        assertEquals(20, ArsLayoutLogic.refundFor(HALL_DEF));
        assertEquals(5, ArsLayoutLogic.refundFor(STRAIGHT_DEF));
        assertEquals(2, ArsLayoutLogic.refundFor(DEAD_END_DEF));
        assertEquals(0, ArsLayoutLogic.refundFor(def(ArsPieceKind.ROOM, 1, 1, 1,
                new ArsConnector(new ArsCell(0, 0), Direction.WEST))));
        assertEquals(0, ArsLayoutLogic.refundFor(def(ArsPieceKind.ROOM, 1, 1, 0,
                new ArsConnector(new ArsCell(0, 0), Direction.WEST))));
    }

    // --- missing definitions --------------------------------------------------------------------

    @Test
    void missingDefinition_isIgnoredByAllRules() {
        ArsPlacedPiece orphan = piece("dwm:removed_by_datapack", 6, 4, 0);
        List<ArsPlacedPiece> layout = List.of(
                piece(STRAIGHT, 5, 4, 0),
                orphan,
                piece(STRAIGHT, 7, 4, 0)
        );

        assertNull(ArsLayoutLogic.definitionOf(orphan, DEFS));
        assertEquals(Optional.empty(), ArsLayoutLogic.pieceAt(layout, DEFS, 6, 4));
        assertEquals(Set.of(ArsLayoutLogic.EAST_ROOT_LINK), ArsLayoutLogic.openLinks(layout, DEFS));
        // Its cell counts as free.
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 6, 4, 0), 500, false));
        assertEquals(RemoveResult.NO_PIECE, ArsLayoutLogic.canRemove(layout, DEFS, 6, 4));
        // (7,4) is already cut off by the orphan, so it does not block removing the root corridor.
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 5, 4));
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 7, 4));
    }

    @Test
    void canPlace_refusesCandidateMatingOnlyAnIsland() {
        // (5,2) is cut off: its only path to the east root ran through the orphan at (5,3).
        List<ArsPlacedPiece> layout = List.of(
                piece(CROSS, 5, 4, 0),
                piece("dwm:removed_by_datapack", 5, 3, 1),
                piece(STRAIGHT, 5, 2, 1)
        );

        // North of the island: mates only the island's north door.
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 5, 1, 1), 500, false));
        // Same candidate mating a reachable piece is accepted.
        List<ArsPlacedPiece> reachableLayout = List.of(
                piece(CROSS, 5, 4, 0),
                piece(STRAIGHT, 5, 3, 1),
                piece(STRAIGHT, 5, 2, 1)
        );
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(reachableLayout, DEFS, piece(STRAIGHT, 5, 1, 1), 500, false));
        // Filling the gap mates both the reachable piece and the island: accepted.
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 5, 3, 1), 500, false));
    }

    @Test
    void savedPieceWithInvalidRotationIsIgnored() {
        List<ArsPlacedPiece> layout = new ArrayList<>(List.of(piece(STRAIGHT, 5, 4, 5)));

        assertEquals(Optional.empty(), ArsLayoutLogic.pieceAt(layout, DEFS, 5, 4));
        assertEquals(Set.of(), ArsLayoutLogic.openLinks(layout, DEFS));
        assertEquals(RemoveResult.NO_PIECE, ArsLayoutLogic.canRemove(layout, DEFS, 5, 4));
    }

    /** Values a hostile client could put in an ARS payload: the rules refuse them without throwing. */
    @Test
    void absurdCellsAndRotationsAreRefusedWithoutThrowing() {
        List<ArsPlacedPiece> layout = new ArrayList<>(List.of(piece(HALL, 5, 0, 0)));
        int[] extremes = {Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -1, ArsGrid.GRID_SIZE, Integer.MAX_VALUE - 1,
                Integer.MAX_VALUE};
        for (int value : extremes) {
            for (String id : List.of(HALL, STRAIGHT)) {
                for (int rotation = 0; rotation < ArsGrid.ROTATION_COUNT; rotation++) {
                    assertEquals(PlaceResult.OUT_OF_BOUNDS,
                            ArsLayoutLogic.canPlace(layout, DEFS, piece(id, value, 3, rotation), 500, true),
                            id + " at x=" + value + " r=" + rotation);
                    assertEquals(PlaceResult.OUT_OF_BOUNDS,
                            ArsLayoutLogic.canPlace(layout, DEFS, piece(id, 3, value, rotation), 500, true),
                            id + " at z=" + value + " r=" + rotation);
                }
            }
            assertEquals(PlaceResult.INVALID_ROTATION,
                    ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 3, 3, value), 500, true));
            assertEquals(RemoveResult.NO_PIECE, ArsLayoutLogic.canRemove(layout, DEFS, value, value));
            assertEquals(RemoveResult.NO_PIECE, ArsLayoutLogic.canRemove(layout, DEFS, 3, value));
            assertEquals(Optional.empty(), ArsLayoutLogic.pieceAt(layout, DEFS, value, 0));
        }
    }

    // --- helpers --------------------------------------------------------------------------------

    private static ArsPieceDefinition def(ArsPieceKind kind, int width, int depth, int cost, ArsConnector... connectors) {
        ArsPieceDefinition definition = new ArsPieceDefinition(
                kind,
                Identifier.fromNamespaceAndPath("dwm", "tardis/ars/test"),
                width,
                depth,
                Arrays.asList(connectors),
                cost,
                0x808080
        );
        // Keep in-test definitions honest against the codec rules.
        ArsPieceDefinition.validate(definition).getOrThrow();
        return definition;
    }

    private static ArsPlacedPiece piece(String id, int cellX, int cellZ, int rotation) {
        return new ArsPlacedPiece(id, cellX, cellZ, rotation);
    }

    private static ArsConnector conn(int x, int z, Direction side) {
        return new ArsConnector(new ArsCell(x, z), side);
    }

    private static Set<ArsCell> cells(int... xz) {
        Set<ArsCell> set = new HashSet<>();
        for (int i = 0; i < xz.length; i += 2) {
            set.add(new ArsCell(xz[i], xz[i + 1]));
        }
        return set;
    }
}
