package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.interior.ars.ArsCell;
import com.adamkali.dwm.tardis.interior.ars.ArsConnector;
import com.adamkali.dwm.tardis.interior.ars.ArsGrid;
import com.adamkali.dwm.tardis.interior.ars.ArsPieceDefinition;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Pure Architectural Reconfiguration System layout rules: footprints, connectors, links, and whether a
 * piece may be placed or removed. No world access, so it is shared by server code and the console
 * screen.
 *
 * <p>Definitions come through a lookup that returns {@code null} for unknown ids (pass
 * {@code map::get} for a {@link Map}).
 *
 * <p><b>Unresolved pieces.</b> A placed piece whose {@code pieceId} does not parse, whose definition
 * no longer exists (datapack removed), or whose rotation is outside {@code 0..3} is
 * <em>ignored by every rule</em>: it occupies no cells, has no connectors, forms no links,
 * {@link #pieceAt} never returns it, and {@link #canRemove} on its cells answers
 * {@link RemoveResult#NO_PIECE}. Its cells therefore count as free for {@link #canPlace}; world
 * placement must deal with any blocks it left behind. Pieces reached only through it become
 * unreachable from the root, which {@link #canRemove} tolerates (see there).
 */
public final class ArsLayoutLogic {
    public static final String UNKNOWN_PIECE_KEY = "dwm.ars.unknown_piece";
    public static final String INVALID_ROTATION_KEY = "dwm.ars.invalid_rotation";
    public static final String OUT_OF_BOUNDS_KEY = "dwm.ars.out_of_bounds";
    public static final String RESERVED_KEY = "dwm.ars.reserved";
    public static final String OCCUPIED_KEY = "dwm.ars.occupied";
    public static final String NOT_CONNECTED_KEY = "dwm.ars.not_connected";
    public static final String NOT_ENOUGH_ARTRON_KEY = "dwm.ars.not_enough_artron";
    public static final String NO_PIECE_KEY = "dwm.ars.no_piece";
    public static final String WOULD_DISCONNECT_KEY = "dwm.ars.would_disconnect";

    /** Graph node id for the console room in reachability searches. */
    private static final int ROOT_NODE = -1;
    /** Sentinel for {@link Graph#reachableFromRoot} meaning no piece is excluded. */
    private static final int NO_EXCLUSION = -2;

    /** Outcome of {@link #canPlace}, checked in declaration order. */
    public enum PlaceResult {
        OK(null),
        UNKNOWN_PIECE(UNKNOWN_PIECE_KEY),
        INVALID_ROTATION(INVALID_ROTATION_KEY),
        OUT_OF_BOUNDS(OUT_OF_BOUNDS_KEY),
        RESERVED(RESERVED_KEY),
        OCCUPIED(OCCUPIED_KEY),
        NOT_CONNECTED(NOT_CONNECTED_KEY),
        NOT_ENOUGH_ARTRON(NOT_ENOUGH_ARTRON_KEY);

        private final @Nullable String translationKey;

        PlaceResult(@Nullable String translationKey) {
            this.translationKey = translationKey;
        }

        /** Player-facing refusal key; {@code null} for {@link #OK}. */
        public @Nullable String translationKey() {
            return translationKey;
        }
    }

    /** Outcome of {@link #canRemove}, checked in declaration order. */
    public enum RemoveResult {
        OK(null),
        NO_PIECE(NO_PIECE_KEY),
        WOULD_DISCONNECT(WOULD_DISCONNECT_KEY);

        private final @Nullable String translationKey;

        RemoveResult(@Nullable String translationKey) {
            this.translationKey = translationKey;
        }

        /** Player-facing refusal key; {@code null} for {@link #OK}. */
        public @Nullable String translationKey() {
            return translationKey;
        }
    }

    /**
     * One matched connector pair: the shared wall between {@link #cell} and the cell across
     * {@link #side}. Canonical form puts the wall on the cell with the larger coordinate, so
     * {@link #side} is always {@link Direction#NORTH} or {@link Direction#WEST} and each wall appears
     * once. The root link is {@code Link(ArsGrid.rootCell(), ArsGrid.ROOT_SIDE)}.
     */
    public record Link(ArsCell cell, Direction side) {
        public static Link of(ArsConnector connector) {
            Direction side = connector.side();
            if (side == Direction.NORTH || side == Direction.WEST) {
                return new Link(connector.cell(), side);
            }
            return new Link(connector.facingCell(), side.getOpposite());
        }

        public boolean isRoot() {
            return this.equals(ROOT_LINK);
        }
    }

    /** The console room's permanent link to the grid. */
    public static final Link ROOT_LINK = Link.of(new ArsConnector(ArsGrid.rootCell(), ArsGrid.ROOT_SIDE));

    /** The console room's virtual connector, facing back across the root link into the grid. */
    public static final ArsConnector CONSOLE_CONNECTOR =
            new ArsConnector(ArsGrid.rootCell(), ArsGrid.ROOT_SIDE).mate();

    private ArsLayoutLogic() {
    }

    /**
     * The definition for a placed piece, or {@code null} when its id does not parse or is unknown.
     */
    public static @Nullable ArsPieceDefinition definitionOf(
            ArsPlacedPiece piece,
            Function<Identifier, ArsPieceDefinition> definitions
    ) {
        if (piece == null || piece.pieceId == null) {
            return null;
        }
        Identifier id = Identifier.tryParse(piece.pieceId);
        return id == null ? null : definitions.apply(id);
    }

    /** Absolute grid cells covered by {@code piece}, applying its rotation and anchor. */
    public static List<ArsCell> occupiedCells(ArsPlacedPiece piece, ArsPieceDefinition definition) {
        List<ArsCell> cells = new ArrayList<>(definition.width() * definition.depth());
        for (int cx = 0; cx < definition.width(); cx++) {
            for (int cz = 0; cz < definition.depth(); cz++) {
                cells.add(toAbsolute(piece, definition, cx, cz));
            }
        }
        return cells;
    }

    /** Absolute connectors of {@code piece}, applying its rotation and anchor. */
    public static List<ArsConnector> connectors(ArsPlacedPiece piece, ArsPieceDefinition definition) {
        List<ArsConnector> result = new ArrayList<>(definition.connectors().size());
        for (ArsConnector local : definition.connectors()) {
            result.add(new ArsConnector(
                    toAbsolute(piece, definition, local.cell().x(), local.cell().z()),
                    ArsGrid.rotateSide(local.side(), piece.rotation)
            ));
        }
        return result;
    }

    /** The resolved piece covering a cell, if any. Unresolved pieces are never returned. */
    public static Optional<ArsPlacedPiece> pieceAt(
            List<ArsPlacedPiece> layout,
            Function<Identifier, ArsPieceDefinition> definitions,
            int cellX,
            int cellZ
    ) {
        ArsCell target = new ArsCell(cellX, cellZ);
        for (Resolved resolved : resolve(layout, definitions)) {
            if (resolved.cells.contains(target)) {
                return Optional.of(resolved.piece);
            }
        }
        return Optional.empty();
    }

    /** Every matched connector pair in the layout, including {@link #ROOT_LINK} when a piece mates it. */
    public static Set<Link> openLinks(
            List<ArsPlacedPiece> layout,
            Function<Identifier, ArsPieceDefinition> definitions
    ) {
        Graph graph = Graph.build(resolve(layout, definitions));
        return graph.links;
    }

    /**
     * Whether {@code candidate} may be added to {@code layout}. Checks, in order: definition exists,
     * rotation is {@code 0..3}, footprint inside the grid, footprint clear of the console cells,
     * footprint clear of resolved pieces, at least one connector mates an existing piece's connector
     * or the root link, and (unless {@code instabuild}) {@code artron} covers the cost.
     */
    public static PlaceResult canPlace(
            List<ArsPlacedPiece> layout,
            Function<Identifier, ArsPieceDefinition> definitions,
            ArsPlacedPiece candidate,
            int artron,
            boolean instabuild
    ) {
        ArsPieceDefinition definition = definitionOf(candidate, definitions);
        if (definition == null) {
            return PlaceResult.UNKNOWN_PIECE;
        }
        if (!ArsGrid.isValidRotation(candidate.rotation)) {
            return PlaceResult.INVALID_ROTATION;
        }
        List<ArsCell> cells = occupiedCells(candidate, definition);
        for (ArsCell cell : cells) {
            if (!ArsGrid.inBounds(cell.x(), cell.z())) {
                return PlaceResult.OUT_OF_BOUNDS;
            }
        }
        for (ArsCell cell : cells) {
            if (ArsGrid.isReserved(cell.x(), cell.z())) {
                return PlaceResult.RESERVED;
            }
        }
        Graph graph = Graph.build(resolve(layout, definitions));
        for (ArsCell cell : cells) {
            if (graph.cellOwners.containsKey(cell)) {
                return PlaceResult.OCCUPIED;
            }
        }
        boolean connected = false;
        for (ArsConnector connector : connectors(candidate, definition)) {
            if (graph.connectorOwners.containsKey(connector.mate())) {
                connected = true;
                break;
            }
        }
        if (!connected) {
            return PlaceResult.NOT_CONNECTED;
        }
        if (!instabuild && artron < definition.artronCost()) {
            return PlaceResult.NOT_ENOUGH_ARTRON;
        }
        return PlaceResult.OK;
    }

    /**
     * Whether the resolved piece covering a cell may be jettisoned. Refuses with
     * {@link RemoveResult#WOULD_DISCONNECT} when some other piece that is reachable from the root now
     * would no longer be reachable after removal. Pieces that are already unreachable (for example cut
     * off by an unresolved piece) do not block removal. Whether a player stands inside the piece is a
     * world check and not handled here.
     */
    public static RemoveResult canRemove(
            List<ArsPlacedPiece> layout,
            Function<Identifier, ArsPieceDefinition> definitions,
            int cellX,
            int cellZ
    ) {
        List<Resolved> resolved = resolve(layout, definitions);
        ArsCell target = new ArsCell(cellX, cellZ);
        int targetIndex = -1;
        for (int i = 0; i < resolved.size(); i++) {
            if (resolved.get(i).cells.contains(target)) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex < 0) {
            return RemoveResult.NO_PIECE;
        }
        Graph graph = Graph.build(resolved);
        Set<Integer> before = graph.reachableFromRoot(NO_EXCLUSION);
        Set<Integer> after = graph.reachableFromRoot(targetIndex);
        for (int index : before) {
            if (index != targetIndex && !after.contains(index)) {
                return RemoveResult.WOULD_DISCONNECT;
            }
        }
        return RemoveResult.OK;
    }

    /** Artron returned when jettisoning a piece: half its cost, rounded down. */
    public static int refundFor(ArsPieceDefinition definition) {
        return Math.max(0, definition.artronCost()) / 2;
    }

    private static ArsCell toAbsolute(ArsPlacedPiece piece, ArsPieceDefinition definition, int localX, int localZ) {
        ArsCell rotated = ArsGrid.rotateCell(localX, localZ, definition.width(), definition.depth(), piece.rotation);
        return new ArsCell(piece.cellX + rotated.x(), piece.cellZ + rotated.z());
    }

    private static List<Resolved> resolve(
            List<ArsPlacedPiece> layout,
            Function<Identifier, ArsPieceDefinition> definitions
    ) {
        List<Resolved> resolved = new ArrayList<>(layout.size());
        for (ArsPlacedPiece piece : layout) {
            if (piece == null || !ArsGrid.isValidRotation(piece.rotation)) {
                continue;
            }
            ArsPieceDefinition definition = definitionOf(piece, definitions);
            if (definition == null) {
                continue;
            }
            resolved.add(new Resolved(
                    piece,
                    new HashSet<>(occupiedCells(piece, definition)),
                    connectors(piece, definition)
            ));
        }
        return resolved;
    }

    private record Resolved(ArsPlacedPiece piece, Set<ArsCell> cells, List<ArsConnector> connectors) {
    }

    /** Cell/connector indexes and matched links over resolved pieces (index = position in list). */
    private static final class Graph {
        final Map<ArsCell, Integer> cellOwners = new HashMap<>();
        final Map<ArsConnector, Integer> connectorOwners = new HashMap<>();
        final Map<Integer, List<Integer>> adjacency = new HashMap<>();
        final Set<Link> links = new LinkedHashSet<>();

        static Graph build(List<Resolved> resolved) {
            Graph graph = new Graph();
            graph.connectorOwners.put(CONSOLE_CONNECTOR, ROOT_NODE);
            for (int i = 0; i < resolved.size(); i++) {
                for (ArsCell cell : resolved.get(i).cells) {
                    graph.cellOwners.putIfAbsent(cell, i);
                }
                for (ArsConnector connector : resolved.get(i).connectors) {
                    graph.connectorOwners.putIfAbsent(connector, i);
                }
            }
            for (int i = 0; i < resolved.size(); i++) {
                for (ArsConnector connector : resolved.get(i).connectors) {
                    Integer other = graph.connectorOwners.get(connector.mate());
                    if (other == null || other == i || graph.connectorOwners.get(connector) != i) {
                        continue;
                    }
                    graph.links.add(Link.of(connector));
                    graph.adjacency.computeIfAbsent(i, k -> new ArrayList<>()).add(other);
                    graph.adjacency.computeIfAbsent(other, k -> new ArrayList<>()).add(i);
                }
            }
            return graph;
        }

        /** Piece indexes reachable from the root, treating {@code excluded} as absent. */
        Set<Integer> reachableFromRoot(int excluded) {
            Set<Integer> seen = new HashSet<>();
            Deque<Integer> queue = new ArrayDeque<>();
            queue.add(ROOT_NODE);
            while (!queue.isEmpty()) {
                int node = queue.poll();
                for (int next : adjacency.getOrDefault(node, List.of())) {
                    if (next != excluded && next != ROOT_NODE && seen.add(next)) {
                        queue.add(next);
                    }
                }
            }
            return seen;
        }
    }
}
