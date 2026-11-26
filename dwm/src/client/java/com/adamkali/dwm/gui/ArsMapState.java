package com.adamkali.dwm.gui;

import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.ars.ArsCell;
import com.adamkali.dwm.tardis.interior.ars.ArsConnector;
import com.adamkali.dwm.tardis.interior.ars.ArsGrid;
import com.adamkali.dwm.tardis.interior.ars.ArsPieceDefinition;
import com.adamkali.dwm.tardis.interior.ars.ArsPieceKind;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Everything the ARS map screen decides that is not drawing: map hit-testing, palette order, the placement
 * ghost, and the palette/jettison selection. Pure, so it is unit-tested without a client.
 *
 * <p>The map is drawn north-up: cell x grows to the right (east) and cell z grows downward (south).
 */
public final class ArsMapState {
    private List<ArsPlacedPiece> layout = List.of();
    private int artron;
    private @Nullable Identifier selectedPiece;
    private int rotation;
    private @Nullable ArsCell selectedCell;
    private double scrollX;
    private double scrollY;

    /** Cells shown along each edge of the map viewport. */
    public static final int VIEW_CELLS = 8;
    /** How far (GUI pixels) the cursor must move with a button held before the press pans instead of clicking. */
    public static final double DRAG_THRESHOLD = 3.0;

    /**
     * Map placement on screen: the viewport's top-left corner and cell edge length in GUI pixels, and the scroll: the
     * GUI-pixel offset of the viewport's top-left corner from cell (0, 0)'s. At scroll (0, 0) (the home view) the
     * viewport shows cells (0..7, 0..7) with the console room in the middle.
     */
    public record MapGeometry(int left, int top, int cellSize, int scrollX, int scrollY) {
        public MapGeometry(int left, int top, int cellSize) {
            this(left, top, cellSize, 0, 0);
        }

        /** Viewport edge length in GUI pixels. */
        public int size() {
            return cellSize * VIEW_CELLS;
        }

        /** Whether a GUI point is inside the viewport. */
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= left && mouseY >= top && mouseX < left + size() && mouseY < top + size();
        }

        /** The grid cell under a GUI point, or {@code null} outside the viewport or the grid. */
        public @Nullable ArsCell cellAt(double mouseX, double mouseY) {
            if (!contains(mouseX, mouseY)) {
                return null;
            }
            int cx = Math.floorDiv((int) Math.floor(mouseX - left) + scrollX, cellSize);
            int cz = Math.floorDiv((int) Math.floor(mouseY - top) + scrollY, cellSize);
            return ArsGrid.inBounds(cx, cz) ? new ArsCell(cx, cz) : null;
        }

        public int cellLeft(int cellX) {
            return left + cellX * cellSize - scrollX;
        }

        public int cellTop(int cellZ) {
            return top + cellZ * cellSize - scrollY;
        }

        /** First (westmost) cell column at least partly in the viewport, clamped to the grid. */
        public int firstVisibleCellX() {
            return Math.max(ArsGrid.MIN_CELL, Math.floorDiv(scrollX, cellSize));
        }

        /** Last (eastmost) cell column at least partly in the viewport, clamped to the grid. */
        public int lastVisibleCellX() {
            return Math.min(ArsGrid.MAX_CELL, Math.floorDiv(scrollX + size() - 1, cellSize));
        }

        /** First (northmost) cell row at least partly in the viewport, clamped to the grid. */
        public int firstVisibleCellZ() {
            return Math.max(ArsGrid.MIN_CELL, Math.floorDiv(scrollY, cellSize));
        }

        /** Last (southmost) cell row at least partly in the viewport, clamped to the grid. */
        public int lastVisibleCellZ() {
            return Math.min(ArsGrid.MAX_CELL, Math.floorDiv(scrollY + size() - 1, cellSize));
        }

        /** Screen x of a grid-local block x (blocks scale to {@code cellSize / CELL_SIZE} pixels). */
        public double blockX(double gridLocalX) {
            return left + gridLocalX * cellSize / ArsGrid.CELL_SIZE - scrollX;
        }

        /** Screen y of a grid-local block z. */
        public double blockZ(double gridLocalZ) {
            return top + gridLocalZ * cellSize / ArsGrid.CELL_SIZE - scrollY;
        }

        /**
         * Screen x of an anchor-local block x: a console room layout position ({@code FirstDoctorConsoleRoomLayout}),
         * moved to the grid by {@link ArsGrid#CONSOLE_ORIGIN}.
         */
        public double anchorBlockX(double anchorLocalX) {
            return blockX(ArsGrid.CONSOLE_ORIGIN.getX() + anchorLocalX);
        }

        /** Screen y of an anchor-local block z. */
        public double anchorBlockZ(double anchorLocalZ) {
            return blockZ(ArsGrid.CONSOLE_ORIGIN.getZ() + anchorLocalZ);
        }

        /**
         * The TARDIS's main doors (the 3-wide interior door bank on the console room's north wall, where players
         * enter and leave, at anchor-local {@code LOCAL_DOOR_ORIGIN}) as a screen rectangle, at least one pixel thick.
         */
        public Rect entranceRect() {
            int doorX = FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN.getX();
            int doorZ = FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN.getZ();
            int x0 = (int) Math.round(anchorBlockX(doorX));
            int x1 = (int) Math.round(anchorBlockX(doorX + TardisInteriorDoorBlock.BANK_WIDTH));
            int z0 = (int) Math.round(anchorBlockZ(doorZ));
            int z1 = (int) Math.round(anchorBlockZ(doorZ + 1));
            return new Rect(x0, z0, Math.max(x1, x0 + 1), Math.max(z1, z0 + 1));
        }
    }

    /** A screen rectangle, right and bottom exclusive. */
    public record Rect(int left, int top, int right, int bottom) {
        /** Whether a GUI point lies within this rectangle grown by {@code margin} pixels on every side. */
        public boolean contains(double x, double y, int margin) {
            return x >= left - margin && x < right + margin && y >= top - margin && y < bottom + margin;
        }
    }

    /** A candidate piece at an anchor cell: its rotated footprint and connectors (absolute cells). */
    public record Ghost(ArsPlacedPiece candidate, List<ArsCell> cells, List<ArsConnector> connectors) {
    }

    /** What a click on the map asks for. */
    public enum ClickKind {
        /** Nothing to do. */
        NONE,
        /** Send a grow request for {@link Click#grow()}. */
        GROW,
        /** The ghost is refused here; {@link Click#refusal()} says why. Nothing is sent. */
        REFUSED,
        /** A placed piece was selected for jettisoning. */
        SELECTED,
        /** The jettison selection was cleared. */
        DESELECTED
    }

    public record Click(ClickKind kind, @Nullable ArsPlacedPiece grow, @Nullable ArsLayoutLogic.PlaceResult refusal) {
        static Click of(ClickKind kind) {
            return new Click(kind, null, null);
        }
    }

    /** What the client does with an {@code OpenArsScreen} payload. */
    public enum Receive {
        /** Open a new ARS screen. */
        OPEN,
        /** Update the ARS screen already open for that TARDIS in place. */
        UPDATE,
        /** Drop it: a refresh with no ARS screen open for that TARDIS. */
        IGNORE
    }

    /**
     * Receiver decision for an {@code OpenArsScreen} payload. {@code openScreenTardisId} is the TARDIS of the ARS
     * screen currently showing, or {@code null} when another screen (or none) is showing. A matching open screen is
     * updated; otherwise a refresh is ignored and only the console control's payload opens the screen.
     */
    public static Receive receive(@Nullable UUID openScreenTardisId, UUID payloadTardisId, boolean refresh) {
        if (openScreenTardisId != null && openScreenTardisId.equals(payloadTardisId)) {
            return Receive.UPDATE;
        }
        return refresh ? Receive.IGNORE : Receive.OPEN;
    }

    /**
     * Palette order: corridors first, then rooms, each by id. {@link ArsPieceKind} declares rooms first, so the
     * kind order is spelled out here rather than taken from the enum.
     */
    public static List<Identifier> paletteOrder(Map<Identifier, ArsPieceDefinition> definitions) {
        List<Identifier> ids = new ArrayList<>(definitions.keySet());
        ids.sort(Comparator
                .comparingInt((Identifier id) -> definitions.get(id).kind() == ArsPieceKind.CORRIDOR ? 0 : 1)
                .thenComparing(Identifier::toString));
        return ids;
    }

    /** The ghost of {@code pieceId} anchored at {@code anchor} (min corner of the rotated footprint). */
    public static Ghost ghost(Identifier pieceId, ArsPieceDefinition definition, ArsCell anchor, int rotation) {
        ArsPlacedPiece candidate = new ArsPlacedPiece(pieceId.toString(), anchor.x(), anchor.z(), rotation);
        return new Ghost(
                candidate,
                ArsLayoutLogic.occupiedCells(candidate, definition),
                ArsLayoutLogic.connectors(candidate, definition));
    }

    /** Smallest scroll on either axis: the grid's west / north edge at the viewport's edge. */
    public static double minScroll(int cellSize) {
        return (double) ArsGrid.MIN_CELL * cellSize;
    }

    /** Largest scroll on either axis: the grid's east / south edge at the viewport's far edge. */
    public static double maxScroll(int cellSize) {
        return (double) (ArsGrid.MAX_CELL + 1) * cellSize - (double) VIEW_CELLS * cellSize;
    }

    /** Whether a press that has moved {@code (dx, dy)} GUI pixels has become a drag (pans) rather than a click. */
    public static boolean isDrag(double dx, double dy) {
        return Math.hypot(dx, dy) >= DRAG_THRESHOLD;
    }

    /** The map placement at {@code (left, top)} for the current scroll. */
    public MapGeometry geometry(int left, int top, int cellSize) {
        return new MapGeometry(left, top, cellSize, (int) Math.floor(scrollX), (int) Math.floor(scrollY));
    }

    public double scrollX() {
        return scrollX;
    }

    public double scrollY() {
        return scrollY;
    }

    /**
     * Moves the view by {@code (dx, dy)} GUI pixels (positive pans east / south), clamped so the viewport never leaves
     * the grid.
     */
    public void panBy(double dx, double dy, int cellSize) {
        scrollX = Math.clamp(scrollX + dx, minScroll(cellSize), maxScroll(cellSize));
        scrollY = Math.clamp(scrollY + dy, minScroll(cellSize), maxScroll(cellSize));
    }

    /** Pans by whole cells (keyboard). */
    public void panCells(int cellsX, int cellsZ, int cellSize) {
        panBy((double) cellsX * cellSize, (double) cellsZ * cellSize, cellSize);
    }

    /** Back to the home view: cells (0..7, 0..7), the console room in the middle. */
    public void centre() {
        scrollX = 0;
        scrollY = 0;
    }

    public boolean isHome() {
        return scrollX == 0 && scrollY == 0;
    }

    /** Replaces the layout and artron from a server snapshot, keeping palette selection, rotation and the view. */
    public void update(List<ArsPlacedPiece> layout, int artron, Function<Identifier, ArsPieceDefinition> definitions) {
        this.layout = List.copyOf(layout);
        this.artron = artron;
        if (selectedPiece != null && definitions.apply(selectedPiece) == null) {
            selectedPiece = null;
        }
        if (selectedCell != null && selectedPlaced(definitions).isEmpty()) {
            selectedCell = null;
        }
    }

    public List<ArsPlacedPiece> layout() {
        return layout;
    }

    public int artron() {
        return artron;
    }

    public @Nullable Identifier selectedPiece() {
        return selectedPiece;
    }

    public int rotation() {
        return rotation;
    }

    public @Nullable ArsCell selectedCell() {
        return selectedCell;
    }

    /** Selects a palette piece (or clears it when it is already selected) and drops the jettison selection. */
    public void togglePalette(Identifier pieceId) {
        selectedPiece = pieceId.equals(selectedPiece) ? null : pieceId;
        selectedCell = null;
    }

    public void clearPalette() {
        selectedPiece = null;
    }

    /** Turns the ghost a quarter turn clockwise. */
    public void rotateClockwise() {
        rotation = (rotation + 1) % ArsGrid.ROTATION_COUNT;
    }

    /** The ghost of the selected palette piece at {@code anchor}, if a piece is selected and known. */
    public Optional<Ghost> ghostAt(@Nullable ArsCell anchor, Function<Identifier, ArsPieceDefinition> definitions) {
        if (anchor == null || selectedPiece == null) {
            return Optional.empty();
        }
        ArsPieceDefinition definition = definitions.apply(selectedPiece);
        return definition == null ? Optional.empty() : Optional.of(ghost(selectedPiece, definition, anchor, rotation));
    }

    /** {@link ArsLayoutLogic#canPlace} for a ghost against the current layout and artron. */
    public ArsLayoutLogic.PlaceResult check(
            Ghost ghost,
            Function<Identifier, ArsPieceDefinition> definitions,
            boolean instabuild
    ) {
        return ArsLayoutLogic.canPlace(layout, definitions, ghost.candidate(), artron, instabuild);
    }

    /**
     * A click on map cell {@code cell}. With a palette piece selected, a left click grows it there when the
     * rules allow (else {@link ClickKind#REFUSED}). Otherwise, and on any right click, it selects the placed
     * piece under the cursor for jettisoning (right click also drops the palette selection), or clears the
     * jettison selection on an empty cell.
     */
    public Click click(
            @Nullable ArsCell cell,
            boolean rightClick,
            Function<Identifier, ArsPieceDefinition> definitions,
            boolean instabuild
    ) {
        if (cell == null) {
            return Click.of(ClickKind.NONE);
        }
        if (!rightClick && selectedPiece != null) {
            Optional<Ghost> ghost = ghostAt(cell, definitions);
            if (ghost.isEmpty()) {
                return new Click(ClickKind.REFUSED, null, ArsLayoutLogic.PlaceResult.UNKNOWN_PIECE);
            }
            ArsLayoutLogic.PlaceResult result = check(ghost.get(), definitions, instabuild);
            return result == ArsLayoutLogic.PlaceResult.OK
                    ? new Click(ClickKind.GROW, ghost.get().candidate(), null)
                    : new Click(ClickKind.REFUSED, null, result);
        }
        if (rightClick) {
            selectedPiece = null;
        }
        if (ArsLayoutLogic.pieceAt(layout, definitions, cell.x(), cell.z()).isPresent()) {
            selectedCell = cell;
            return Click.of(ClickKind.SELECTED);
        }
        if (selectedCell != null) {
            selectedCell = null;
            return Click.of(ClickKind.DESELECTED);
        }
        return Click.of(ClickKind.NONE);
    }

    /** The placed piece selected for jettisoning, if it still exists. */
    public Optional<ArsPlacedPiece> selectedPlaced(Function<Identifier, ArsPieceDefinition> definitions) {
        if (selectedCell == null) {
            return Optional.empty();
        }
        return ArsLayoutLogic.pieceAt(layout, definitions, selectedCell.x(), selectedCell.z());
    }

    /** Whether the selected placed piece may be jettisoned ({@link ArsLayoutLogic.RemoveResult#NO_PIECE} if none). */
    public ArsLayoutLogic.RemoveResult removeCheck(Function<Identifier, ArsPieceDefinition> definitions) {
        if (selectedCell == null) {
            return ArsLayoutLogic.RemoveResult.NO_PIECE;
        }
        return ArsLayoutLogic.canRemove(layout, definitions, selectedCell.x(), selectedCell.z());
    }
}
