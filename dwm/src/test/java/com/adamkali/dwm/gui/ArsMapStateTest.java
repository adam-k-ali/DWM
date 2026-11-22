package com.adamkali.dwm.gui;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.ars.ArsCell;
import com.adamkali.dwm.tardis.interior.ars.ArsConnector;
import com.adamkali.dwm.tardis.interior.ars.ArsGrid;
import com.adamkali.dwm.tardis.interior.ars.ArsPieceDefinition;
import com.adamkali.dwm.tardis.interior.ars.ArsPieceKind;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic.PlaceResult;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic.RemoveResult;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArsMapStateTest {
    private static final Identifier STRAIGHT = Identifier.fromNamespaceAndPath("dwm", "corridor_straight");
    private static final Identifier CROSS = Identifier.fromNamespaceAndPath("dwm", "corridor_cross");
    private static final Identifier BEDROOM = Identifier.fromNamespaceAndPath("dwm", "bedroom");
    private static final Identifier LIBRARY = Identifier.fromNamespaceAndPath("dwm", "library");
    private static final Identifier EXTRA_ROOM = Identifier.fromNamespaceAndPath("aaa", "zz_room");

    /** Shipped shapes: straight runs north-south at rotation 0; bedroom is 2x1 with a north door on (0,0). */
    private static final Map<Identifier, ArsPieceDefinition> DEFINITIONS = Map.of(
            STRAIGHT, def(ArsPieceKind.CORRIDOR, 1, 1, 5, conn(0, 0, Direction.NORTH), conn(0, 0, Direction.SOUTH)),
            CROSS, def(ArsPieceKind.CORRIDOR, 1, 1, 5, conn(0, 0, Direction.NORTH), conn(0, 0, Direction.EAST),
                    conn(0, 0, Direction.SOUTH), conn(0, 0, Direction.WEST)),
            BEDROOM, def(ArsPieceKind.ROOM, 2, 1, 20, conn(0, 0, Direction.NORTH)),
            LIBRARY, def(ArsPieceKind.ROOM, 2, 2, 40, conn(0, 0, Direction.NORTH), conn(1, 1, Direction.EAST)),
            EXTRA_ROOM, def(ArsPieceKind.ROOM, 1, 1, 1, conn(0, 0, Direction.NORTH))
    );
    private static final Function<Identifier, ArsPieceDefinition> DEFS = DEFINITIONS::get;
    private static final ArsMapState.MapGeometry GEO = new ArsMapState.MapGeometry(21, 29, 20);

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void entranceRect_marksTheMainDoorsOnTheConsoleRoomsNorthWall() {
        ArsMapState.Rect door = GEO.entranceRect();
        // Door bank: anchor-local x 4..6, z 1, so grid-local x 31..33, z 28, at 2.5 px per block from (21, 29).
        assertEquals(new ArsMapState.Rect(99, 99, 106, 102), door);
        // Inside the console room's top cells, nearer the north wall than the console.
        assertTrue(door.left() >= GEO.cellLeft(ArsGrid.RESERVED_MIN_X));
        assertTrue(door.right() <= GEO.cellLeft(ArsGrid.RESERVED_MAX_X + 1));
        assertTrue(door.top() >= GEO.cellTop(ArsGrid.RESERVED_MIN_Z));
        assertTrue(door.bottom() <= GEO.cellTop(ArsGrid.RESERVED_MIN_Z + 1));
        assertTrue(door.bottom() < GEO.anchorBlockZ(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE.getZ()));
        assertTrue(door.contains(102, 100, 0));
        assertTrue(door.contains(98, 98, 2));
        assertFalse(door.contains(98, 98, 0));
        assertFalse(door.contains(102, 108, 2));
    }

    @Test
    void consoleRoom_isCentredOnTheMap() {
        // Reserved cells 3..4 x 3..5: three free columns on each side, three rows north and two south.
        int roomLeft = GEO.cellLeft(ArsGrid.RESERVED_MIN_X);
        int roomRight = GEO.cellLeft(ArsGrid.RESERVED_MAX_X + 1);
        assertEquals(GEO.left() + GEO.size() - roomRight, roomLeft - GEO.left());
        // The console (anchor-local x 5) sits on the map's vertical centre line.
        assertEquals(GEO.left() + GEO.size() / 2.0, GEO.anchorBlockX(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE.getX() + 0.5),
                2.5);
        assertEquals(GEO.blockX(ArsGrid.CONSOLE_ORIGIN.getX()), GEO.anchorBlockX(0), 1e-9);
        assertEquals(GEO.blockZ(ArsGrid.CONSOLE_ORIGIN.getZ() + 7), GEO.anchorBlockZ(7), 1e-9);
    }

    @Test
    void cellAt_mapsMouseToNorthUpGrid() {
        assertEquals(new ArsCell(0, 0), GEO.cellAt(21, 29));
        assertEquals(new ArsCell(0, 0), GEO.cellAt(40.9, 48.9));
        assertEquals(new ArsCell(1, 0), GEO.cellAt(41, 29));
        assertEquals(new ArsCell(0, 1), GEO.cellAt(21, 49));
        assertEquals(new ArsCell(7, 7), GEO.cellAt(21 + 159.5, 29 + 159.5));
        assertEquals(new ArsCell(2, 1), GEO.cellAt(GEO.cellLeft(2) + 10, GEO.cellTop(1) + 10));
    }

    @Test
    void cellAt_isNullOutsideTheMap() {
        assertNull(GEO.cellAt(20.9, 40));
        assertNull(GEO.cellAt(40, 28.9));
        assertNull(GEO.cellAt(21 + 160, 40));
        assertNull(GEO.cellAt(40, 29 + 160));
        assertNull(GEO.cellAt(-1000, -1000));
    }

    @Test
    void geometry_mapsPlotBlocksToPixels() {
        assertEquals(160, GEO.size());
        assertEquals(21 + 40, GEO.cellLeft(2));
        assertEquals(29 + 20, GEO.cellTop(1));
        // Block x 16 is the west edge of cell 2; z 8 is the north edge of cell 1.
        assertEquals(GEO.cellLeft(2), GEO.blockX(16), 1e-9);
        assertEquals(GEO.cellTop(1), GEO.blockZ(8), 1e-9);
    }

    @Test
    void scrolledGeometry_movesCellsAndBlocksUnderTheViewport() {
        // Panned one and a half cells east and two cells north of home.
        ArsMapState.MapGeometry geo = new ArsMapState.MapGeometry(21, 29, 20, 30, -40);
        assertEquals(21 - 30, geo.cellLeft(0));
        assertEquals(29 + 40, geo.cellTop(0));
        assertEquals(new ArsCell(1, -2), geo.cellAt(21, 29));
        assertEquals(new ArsCell(2, -2), geo.cellAt(21 + 10, 29));
        assertEquals(new ArsCell(9, 5), geo.cellAt(21 + 159.5, 29 + 159.5));
        assertEquals(new ArsCell(4, 3), geo.cellAt(geo.cellLeft(4) + 10, geo.cellTop(3) + 10));
        assertEquals(geo.cellLeft(2), geo.blockX(16), 1e-9);
        assertEquals(geo.cellTop(-1), geo.blockZ(-8), 1e-9);
        // The viewport does not move with the scroll.
        assertTrue(geo.contains(21, 29));
        assertFalse(geo.contains(21 + 160, 40));
        assertNull(geo.cellAt(21 + 160, 40));
        assertEquals(1, geo.firstVisibleCellX());
        assertEquals(9, geo.lastVisibleCellX());
        assertEquals(-2, geo.firstVisibleCellZ());
        assertEquals(5, geo.lastVisibleCellZ());
    }

    @Test
    void scrolledGeometry_reachesNegativeAndFarCellsButNotPastTheGrid() {
        int cell = 20;
        ArsMapState.MapGeometry west = new ArsMapState.MapGeometry(0, 0, cell,
                (int) ArsMapState.minScroll(cell), (int) ArsMapState.minScroll(cell));
        assertEquals(new ArsCell(ArsGrid.MIN_CELL, ArsGrid.MIN_CELL), west.cellAt(0, 0));
        assertEquals(ArsGrid.MIN_CELL, west.firstVisibleCellX());
        ArsMapState.MapGeometry east = new ArsMapState.MapGeometry(0, 0, cell,
                (int) ArsMapState.maxScroll(cell), (int) ArsMapState.maxScroll(cell));
        assertEquals(new ArsCell(ArsGrid.MAX_CELL, ArsGrid.MAX_CELL), east.cellAt(159.5, 159.5));
        assertEquals(ArsGrid.MAX_CELL, east.lastVisibleCellZ());
        // Beyond the grid there is no cell, even if a geometry is built scrolled past it.
        ArsMapState.MapGeometry past = new ArsMapState.MapGeometry(0, 0, cell,
                (int) ArsMapState.maxScroll(cell) + cell, 0);
        assertNull(past.cellAt(159.5, 0));
        assertEquals(ArsGrid.MAX_CELL, past.lastVisibleCellX());
    }

    @Test
    void panBy_clampsToTheGridEdgesAndCentreReturnsHome() {
        ArsMapState state = new ArsMapState();
        int cell = 20;
        assertTrue(state.isHome());
        assertEquals(new ArsCell(0, 0), state.geometry(21, 29, cell).cellAt(21, 29));

        state.panCells(3, -2, cell);
        assertFalse(state.isHome());
        assertEquals(new ArsCell(3, -2), state.geometry(21, 29, cell).cellAt(21, 29));
        state.panBy(-12.5, 7.25, cell);
        assertEquals(47.5, state.scrollX(), 1e-9);
        assertEquals(-32.75, state.scrollY(), 1e-9);

        state.panBy(-1.0e9, -1.0e9, cell);
        assertEquals(ArsMapState.minScroll(cell), state.scrollX(), 1e-9);
        assertEquals(ArsMapState.minScroll(cell), state.scrollY(), 1e-9);
        assertEquals(new ArsCell(ArsGrid.MIN_CELL, ArsGrid.MIN_CELL), state.geometry(0, 0, cell).cellAt(0, 0));
        state.panBy(1.0e9, 1.0e9, cell);
        assertEquals(ArsMapState.maxScroll(cell), state.scrollX(), 1e-9);
        assertEquals(new ArsCell(ArsGrid.MAX_CELL, ArsGrid.MAX_CELL), state.geometry(0, 0, cell).cellAt(159, 159));
        // The whole grid is reachable: 1250 cells less the 8 on screen.
        assertEquals((ArsGrid.GRID_SIZE - ArsMapState.VIEW_CELLS) * cell,
                ArsMapState.maxScroll(cell) - ArsMapState.minScroll(cell), 1e-9);

        state.centre();
        assertTrue(state.isHome());
        assertEquals(new ArsCell(0, 0), state.geometry(21, 29, cell).cellAt(21, 29));
    }

    @Test
    void isDrag_needsTheCursorToMoveAFewPixels() {
        assertFalse(ArsMapState.isDrag(0, 0));
        assertFalse(ArsMapState.isDrag(2, 2));
        assertFalse(ArsMapState.isDrag(-2.9, 0));
        assertTrue(ArsMapState.isDrag(3, 0));
        assertTrue(ArsMapState.isDrag(0, -3));
        assertTrue(ArsMapState.isDrag(2.2, 2.2));
    }

    @Test
    void update_keepsTheView() {
        ArsMapState state = new ArsMapState();
        state.panCells(-5, 9, 20);
        state.update(layout(), 100, DEFS);
        assertEquals(-100, state.scrollX(), 1e-9);
        assertEquals(180, state.scrollY(), 1e-9);
    }

    @Test
    void paletteOrder_putsCorridorsFirstThenRoomsById() {
        assertEquals(List.of(CROSS, STRAIGHT, EXTRA_ROOM, BEDROOM, LIBRARY), ArsMapState.paletteOrder(DEFINITIONS));
    }

    @Test
    void ghost_rotatesFootprintAndConnectorsAroundTheAnchor() {
        ArsMapState.Ghost bedroom = ArsMapState.ghost(BEDROOM, DEFINITIONS.get(BEDROOM), new ArsCell(3, 2), 1);

        assertEquals(new ArsPlacedPiece(BEDROOM.toString(), 3, 2, 1), bedroom.candidate());
        assertEquals(Set.of(new ArsCell(3, 2), new ArsCell(3, 3)), new HashSet<>(bedroom.cells()));
        assertEquals(List.of(new ArsConnector(new ArsCell(3, 2), Direction.EAST)), bedroom.connectors());

        ArsMapState.Ghost straight = ArsMapState.ghost(STRAIGHT, DEFINITIONS.get(STRAIGHT), new ArsCell(2, 1), 1);
        assertEquals(Set.of(Direction.EAST, Direction.WEST),
                Set.of(straight.connectors().get(0).side(), straight.connectors().get(1).side()));
    }

    @Test
    void click_withPaletteGrowsOnlyWhereTheRulesAllow() {
        ArsMapState state = new ArsMapState();
        state.update(List.of(), 100, DEFS);
        state.togglePalette(STRAIGHT);

        // Rotation 0 runs north-south and does not meet the east root link.
        ArsMapState.Click refused = state.click(new ArsCell(5, 4), false, DEFS, false);
        assertEquals(ArsMapState.ClickKind.REFUSED, refused.kind());
        assertEquals(PlaceResult.NOT_CONNECTED, refused.refusal());

        state.rotateClockwise();
        ArsMapState.Click grow = state.click(new ArsCell(5, 4), false, DEFS, false);
        assertEquals(ArsMapState.ClickKind.GROW, grow.kind());
        assertEquals(new ArsPlacedPiece(STRAIGHT.toString(), 5, 4, 1), grow.grow());
        // The west root link takes the same corridor on the other side of the console room.
        assertEquals(ArsMapState.ClickKind.GROW, state.click(new ArsCell(2, 4), false, DEFS, false).kind());

        assertEquals(PlaceResult.RESERVED, state.click(new ArsCell(4, 4), false, DEFS, false).refusal());
        assertEquals(ArsMapState.ClickKind.NONE, state.click(null, false, DEFS, false).kind());
    }

    @Test
    void click_reportsMissingArtronUnlessInstabuild() {
        ArsMapState state = new ArsMapState();
        state.update(List.of(new ArsPlacedPiece(CROSS.toString(), 5, 4, 0)), 10, DEFS);
        state.togglePalette(LIBRARY);

        assertEquals(PlaceResult.NOT_ENOUGH_ARTRON, state.click(new ArsCell(5, 5), false, DEFS, false).refusal());
        assertEquals(ArsMapState.ClickKind.GROW, state.click(new ArsCell(5, 5), false, DEFS, true).kind());
    }

    @Test
    void click_withoutPaletteSelectsAndClearsPlacedPiece() {
        ArsMapState state = new ArsMapState();
        state.update(layout(), 100, DEFS);

        assertEquals(ArsMapState.ClickKind.SELECTED, state.click(new ArsCell(6, 4), false, DEFS, false).kind());
        assertEquals(new ArsPlacedPiece(CROSS.toString(), 6, 4, 0), state.selectedPlaced(DEFS).orElseThrow());
        assertEquals(RemoveResult.WOULD_DISCONNECT, state.removeCheck(DEFS));

        assertEquals(ArsMapState.ClickKind.SELECTED, state.click(new ArsCell(7, 5), false, DEFS, false).kind());
        assertEquals(RemoveResult.OK, state.removeCheck(DEFS));

        assertEquals(ArsMapState.ClickKind.DESELECTED, state.click(new ArsCell(6, 6), false, DEFS, false).kind());
        assertTrue(state.selectedPlaced(DEFS).isEmpty());
        assertEquals(RemoveResult.NO_PIECE, state.removeCheck(DEFS));
        assertEquals(ArsMapState.ClickKind.NONE, state.click(new ArsCell(6, 6), false, DEFS, false).kind());
    }

    @Test
    void rightClick_selectsPlacedPieceAndDropsPaletteSelection() {
        ArsMapState state = new ArsMapState();
        state.update(layout(), 100, DEFS);
        state.togglePalette(BEDROOM);

        assertEquals(ArsMapState.ClickKind.SELECTED, state.click(new ArsCell(5, 4), true, DEFS, false).kind());
        assertNull(state.selectedPiece());
        assertEquals(new ArsCell(5, 4), state.selectedCell());
    }

    @Test
    void togglePalette_switchesBetweenPlacingAndJettisoning() {
        ArsMapState state = new ArsMapState();
        state.update(layout(), 100, DEFS);
        state.click(new ArsCell(6, 4), false, DEFS, false);

        state.togglePalette(BEDROOM);
        assertEquals(BEDROOM, state.selectedPiece());
        assertNull(state.selectedCell());

        state.togglePalette(BEDROOM);
        assertNull(state.selectedPiece());
    }

    @Test
    void update_keepsPaletteAndRotationButDropsVanishedSelections() {
        ArsMapState state = new ArsMapState();
        state.update(layout(), 100, DEFS);
        state.click(new ArsCell(7, 5), false, DEFS, false);
        state.togglePalette(LIBRARY);
        state.rotateClockwise();
        state.rotateClockwise();
        state.click(new ArsCell(7, 5), true, DEFS, false);
        state.togglePalette(LIBRARY);
        assertEquals(LIBRARY, state.selectedPiece());

        state.update(layout(), 60, DEFS);
        assertEquals(LIBRARY, state.selectedPiece());
        assertEquals(2, state.rotation());
        assertEquals(60, state.artron());

        // Palette entry whose definition disappeared (datapack reload) is dropped.
        state.update(layout(), 60, id -> LIBRARY.equals(id) ? null : DEFS.apply(id));
        assertNull(state.selectedPiece());

        // A jettison selection survives only while a piece still covers that cell.
        state.click(new ArsCell(7, 5), false, DEFS, false);
        assertEquals(new ArsCell(7, 5), state.selectedCell());
        state.update(layout().subList(0, 2), 60, DEFS);
        assertNull(state.selectedCell());
    }

    @Test
    void receive_refreshOnlyUpdatesTheOpenScreenForThatTardis() {
        UUID tardis = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        assertEquals(ArsMapState.Receive.UPDATE, ArsMapState.receive(tardis, tardis, true));
        assertEquals(ArsMapState.Receive.UPDATE, ArsMapState.receive(tardis, tardis, false));
        // Closed (or another screen showing): the grow/jettison reply must not reopen the screen.
        assertEquals(ArsMapState.Receive.IGNORE, ArsMapState.receive(null, tardis, true));
        assertEquals(ArsMapState.Receive.IGNORE, ArsMapState.receive(other, tardis, true));
        // The console control opens it.
        assertEquals(ArsMapState.Receive.OPEN, ArsMapState.receive(null, tardis, false));
        assertEquals(ArsMapState.Receive.OPEN, ArsMapState.receive(other, tardis, false));
    }

    @Test
    void rotateClockwise_wrapsAfterFourTurns() {
        ArsMapState state = new ArsMapState();
        for (int i = 1; i <= 4; i++) {
            state.rotateClockwise();
            assertEquals(i % 4, state.rotation());
        }
    }

    @Test
    void ghostAt_needsAPaletteSelectionAndAKnownDefinition() {
        ArsMapState state = new ArsMapState();
        state.update(List.of(), 0, DEFS);
        assertTrue(state.ghostAt(new ArsCell(2, 1), DEFS).isEmpty());
        state.togglePalette(STRAIGHT);
        assertTrue(state.ghostAt(null, DEFS).isEmpty());
        assertTrue(state.ghostAt(new ArsCell(2, 1), id -> null).isEmpty());
        assertEquals(List.of(new ArsCell(2, 1)), state.ghostAt(new ArsCell(2, 1), DEFS).orElseThrow().cells());
    }

    /** Straight corridor on the east root cell, crossroads east of it, bedroom south of the crossroads. */
    private static List<ArsPlacedPiece> layout() {
        return List.of(
                new ArsPlacedPiece(STRAIGHT.toString(), 5, 4, 1),
                new ArsPlacedPiece(CROSS.toString(), 6, 4, 0),
                new ArsPlacedPiece(BEDROOM.toString(), 6, 5, 0));
    }

    private static ArsPieceDefinition def(ArsPieceKind kind, int width, int depth, int cost, ArsConnector... connectors) {
        ArsPieceDefinition definition = new ArsPieceDefinition(kind, Identifier.fromNamespaceAndPath("dwm", "test"),
                width, depth, List.of(connectors), cost, 0x808080);
        ArsPieceDefinition.validate(definition).getOrThrow();
        return definition;
    }

    private static ArsConnector conn(int x, int z, Direction side) {
        return new ArsConnector(new ArsCell(x, z), side);
    }
}
