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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArsLayoutLogicTest {
    private static final String HALL = "dwm:hall";
    private static final String STRAIGHT = "dwm:straight";
    private static final String CROSS = "dwm:cross";
    private static final String DEAD_END = "dwm:dead_end";

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

    private static final Map<Identifier, ArsPieceDefinition> DEFINITIONS = Map.of(
            Identifier.parse(HALL), HALL_DEF,
            Identifier.parse(STRAIGHT), STRAIGHT_DEF,
            Identifier.parse(CROSS), CROSS_DEF,
            Identifier.parse(DEAD_END), DEAD_END_DEF
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
    void canPlace_connectsToRootLink() {
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(STRAIGHT, 2, 1, 0), 500, false));
        // Hall's west door on its (0,0) cell lands on the root cell.
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(HALL, 2, 1, 0), 500, false));
        // Rotated 180 its east door turns west on the anchor cell, here (2,0): faces the console, not the root link.
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(HALL, 2, 0, 2), 500, false));
        // One row down that west door sits on the root cell (2,1).
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(HALL, 2, 1, 2), 500, false));
    }

    @Test
    void canPlace_notConnectedWhenNoConnectorMates() {
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(STRAIGHT, 5, 5, 0), 500, false));
        // Root cell, but a north-only dead end does not face the console.
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(DEAD_END, 2, 1, 0), 500, false));
        // Rotated three quarter turns its door faces west onto the root link.
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(DEAD_END, 2, 1, 3), 500, false));
    }

    @Test
    void canPlace_connectsToExistingPieceOnlyThroughMatchingConnector() {
        List<ArsPlacedPiece> layout = List.of(piece(STRAIGHT, 2, 1, 0));

        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 3, 1, 0), 500, false));
        // Adjacent, but the dead end's north door faces (3,0), not the corridor.
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(DEAD_END, 3, 1, 0), 500, false));
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(DEAD_END, 3, 1, 3), 500, false));
        // A cross north of the straight corridor: the corridor has no north door to mate.
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(CROSS, 2, 0, 0), 500, false));
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
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(HALL, 1, 2, 0), 500, true));
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(CROSS, 0, 3, 0), 500, true));
    }

    @Test
    void canPlace_occupied() {
        List<ArsPlacedPiece> layout = List.of(piece(HALL, 2, 1, 0));

        assertEquals(PlaceResult.OCCUPIED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 4, 2, 0), 500, false));
        assertEquals(PlaceResult.OCCUPIED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(HALL, 4, 2, 1), 500, false));
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 5, 2, 0), 500, false));
    }

    @Test
    void canPlace_artronAndInstabuild() {
        ArsPlacedPiece candidate = piece(STRAIGHT, 2, 1, 0);

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
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece("Not An Id!", 2, 1, 0), 500, false));
        assertEquals(PlaceResult.UNKNOWN_PIECE,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(null, 2, 1, 0), 500, false));
        // Invalid rotation beats out of bounds.
        assertEquals(PlaceResult.INVALID_ROTATION,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(STRAIGHT, 9, 9, 4), 0, false));
        assertEquals(PlaceResult.INVALID_ROTATION,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(STRAIGHT, 2, 1, -1), 500, false));
        // Out of bounds beats reserved (footprint spans x = -1..1, inside the console).
        assertEquals(PlaceResult.OUT_OF_BOUNDS,
                ArsLayoutLogic.canPlace(List.of(), DEFS, piece(HALL, -1, 0, 0), 0, false));
        // Reserved beats occupied and not-connected.
        List<ArsPlacedPiece> layout = List.of(piece(STRAIGHT, 2, 1, 0));
        assertEquals(PlaceResult.RESERVED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(HALL, 0, 1, 0), 0, false));
        // Occupied beats not-connected and artron.
        assertEquals(PlaceResult.OCCUPIED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(DEAD_END, 2, 1, 0), 0, false));
        // Not-connected beats artron.
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 6, 6, 0), 0, false));
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
                piece(STRAIGHT, 2, 1, 0),
                piece(STRAIGHT, 3, 1, 0),
                // Cross north of the second corridor: walls touch but the corridor has no north door.
                piece(CROSS, 3, 0, 0)
        );

        Set<Link> links = ArsLayoutLogic.openLinks(layout, DEFS);

        assertEquals(Set.of(ArsLayoutLogic.ROOT_LINK, new Link(new ArsCell(3, 1), Direction.WEST)), links);
        assertTrue(ArsLayoutLogic.ROOT_LINK.isRoot());
        assertEquals(new Link(ArsGrid.rootCell(), ArsGrid.ROOT_SIDE), ArsLayoutLogic.ROOT_LINK);
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
                piece(STRAIGHT, 2, 1, 0),
                piece(STRAIGHT, 3, 1, 0),
                piece(STRAIGHT, 4, 1, 0)
        );

        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 4, 1));
        assertEquals(RemoveResult.WOULD_DISCONNECT, ArsLayoutLogic.canRemove(layout, DEFS, 3, 1));
        assertEquals(RemoveResult.WOULD_DISCONNECT, ArsLayoutLogic.canRemove(layout, DEFS, 2, 1));
        assertEquals(RemoveResult.NO_PIECE, ArsLayoutLogic.canRemove(layout, DEFS, 5, 1));
    }

    @Test
    void canRemove_loopLeavesAlternatePath() {
        List<ArsPlacedPiece> layout = List.of(
                piece(CROSS, 2, 1, 0),
                piece(CROSS, 3, 1, 0),
                piece(CROSS, 2, 2, 0),
                piece(CROSS, 3, 2, 0)
        );

        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 3, 1));
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 2, 2));
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 3, 2));
        // The only piece on the root link carries everything else.
        assertEquals(RemoveResult.WOULD_DISCONNECT, ArsLayoutLogic.canRemove(layout, DEFS, 2, 1));
    }

    @Test
    void canRemove_findsMultiCellPieceByAnyCell() {
        List<ArsPlacedPiece> layout = List.of(piece(HALL, 2, 1, 0));

        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 4, 2));
        assertEquals(RemoveResult.NO_PIECE, ArsLayoutLogic.canRemove(layout, DEFS, 5, 2));
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
        ArsPlacedPiece orphan = piece("dwm:removed_by_datapack", 3, 1, 0);
        List<ArsPlacedPiece> layout = List.of(
                piece(STRAIGHT, 2, 1, 0),
                orphan,
                piece(STRAIGHT, 4, 1, 0)
        );

        assertNull(ArsLayoutLogic.definitionOf(orphan, DEFS));
        assertEquals(Optional.empty(), ArsLayoutLogic.pieceAt(layout, DEFS, 3, 1));
        assertEquals(Set.of(ArsLayoutLogic.ROOT_LINK), ArsLayoutLogic.openLinks(layout, DEFS));
        // Its cell counts as free.
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 3, 1, 0), 500, false));
        assertEquals(RemoveResult.NO_PIECE, ArsLayoutLogic.canRemove(layout, DEFS, 3, 1));
        // (4,1) is already cut off by the orphan, so it does not block removing the root corridor.
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 2, 1));
        assertEquals(RemoveResult.OK, ArsLayoutLogic.canRemove(layout, DEFS, 4, 1));
    }

    @Test
    void canPlace_refusesCandidateMatingOnlyAnIsland() {
        // (4,1) is cut off: its only path to the root ran through the orphan at (3,1).
        List<ArsPlacedPiece> layout = List.of(
                piece(STRAIGHT, 2, 1, 0),
                piece("dwm:removed_by_datapack", 3, 1, 0),
                piece(STRAIGHT, 4, 1, 0)
        );

        // East of the island: mates only the island's east door.
        assertEquals(PlaceResult.NOT_CONNECTED,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 5, 1, 0), 500, false));
        // Same candidate mating a reachable piece is accepted.
        List<ArsPlacedPiece> reachableLayout = List.of(
                piece(STRAIGHT, 2, 1, 0),
                piece(STRAIGHT, 3, 1, 0),
                piece(STRAIGHT, 4, 1, 0)
        );
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(reachableLayout, DEFS, piece(STRAIGHT, 5, 1, 0), 500, false));
        // Filling the gap mates both the reachable piece and the island: accepted.
        assertEquals(PlaceResult.OK,
                ArsLayoutLogic.canPlace(layout, DEFS, piece(STRAIGHT, 3, 1, 0), 500, false));
    }

    @Test
    void savedPieceWithInvalidRotationIsIgnored() {
        List<ArsPlacedPiece> layout = new ArrayList<>(List.of(piece(STRAIGHT, 2, 1, 5)));

        assertEquals(Optional.empty(), ArsLayoutLogic.pieceAt(layout, DEFS, 2, 1));
        assertEquals(Set.of(), ArsLayoutLogic.openLinks(layout, DEFS));
        assertEquals(RemoveResult.NO_PIECE, ArsLayoutLogic.canRemove(layout, DEFS, 2, 1));
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
