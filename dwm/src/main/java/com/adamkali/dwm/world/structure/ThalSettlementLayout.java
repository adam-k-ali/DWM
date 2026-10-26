package com.adamkali.dwm.world.structure;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Pure, deterministic planner for a Thal plateau settlement. It never touches a level: given a
 * {@link RandomSource} it returns an immutable plan in a local 2D grid relative to the settlement centre.
 *
 * <p>The plan holds one communal {@link Kind#SHELTER}, three to five {@link Kind#HUT}s, two
 * {@link Kind#FIELD}s and one {@link Kind#WATCH_POST}, each with a rectangular footprint and a door facing
 * the shelter, plus a set of path cells. Every building's entrance cell (the cell outside its door) is
 * connected to the shelter's entrance through 4-connected path cells, and no path cell lies inside a
 * footprint. Footprints are kept {@link #GAP} cells apart so paths always have room to route.
 */
public final class ThalSettlementLayout {
    /** Buildings and paths stay within {@code [-BOUND, BOUND]} on both grid axes. */
    public static final int BOUND = 30;
    /** Minimum number of empty cells between any two footprints. */
    public static final int GAP = 2;
    public static final int HUT_MIN = 3;
    public static final int HUT_MAX = 5;
    public static final int FIELD_COUNT = 2;

    private static final int PLACEMENT_ATTEMPTS = 300;
    private static final int LAYOUT_ATTEMPTS = 64;
    private static final Direction[] STEPS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public enum Kind {
        SHELTER,
        HUT,
        FIELD,
        WATCH_POST
    }

    /** A cell of the local settlement grid. */
    public record Cell(int x, int z) implements Comparable<Cell> {
        @Override
        public int compareTo(Cell other) {
            int byX = Integer.compare(x, other.x);
            return byX != 0 ? byX : Integer.compare(z, other.z);
        }

        public Cell relative(Direction direction) {
            return new Cell(x + direction.getStepX(), z + direction.getStepZ());
        }
    }

    /** One planned building: its kind, footprint/door frame and the number of Thal residents it houses. */
    public record PlannedPiece(Kind kind, ThalFrame frame, int residents) {
        public Cell doorCell() {
            return frame.doorCell();
        }

        public Cell entranceCell() {
            return frame.entranceCell();
        }
    }

    /** The complete plan: pieces (shelter first) and the connecting path cells in sorted order. */
    public record Layout(List<PlannedPiece> pieces, Set<Cell> pathCells) {
        public Layout {
            pieces = List.copyOf(pieces);
            pathCells = java.util.Collections.unmodifiableSet(new TreeSet<>(pathCells));
        }

        public PlannedPiece shelter() {
            return pieces.get(0);
        }

        public List<PlannedPiece> ofKind(Kind kind) {
            return pieces.stream().filter(piece -> piece.kind() == kind).toList();
        }
    }

    private ThalSettlementLayout() {
    }

    public static Layout plan(long seed) {
        return plan(RandomSource.create(seed));
    }

    /**
     * Plans a settlement. Retries a bounded number of times with the same random stream, then falls back to a
     * hand-authored layout, so this always returns a valid plan.
     */
    public static Layout plan(RandomSource random) {
        for (int attempt = 0; attempt < LAYOUT_ATTEMPTS; attempt++) {
            Layout layout = tryPlan(random);
            if (layout != null) {
                return layout;
            }
        }
        return fallback();
    }

    /** One randomized attempt; {@code null} if placement or path routing failed. Visible for tests. */
    static Layout tryPlan(RandomSource random) {
        List<PlannedPiece> placed = new ArrayList<>();
        placed.add(new PlannedPiece(Kind.SHELTER, shelterFrame(random), 2 + random.nextInt(2)));

        for (int i = 0; i < FIELD_COUNT; i++) {
            if (!place(placed, random, Kind.FIELD, 9, 7, 13, 22, 0)) {
                return null;
            }
        }
        int huts = HUT_MIN + random.nextInt(HUT_MAX - HUT_MIN + 1);
        for (int i = 0; i < huts; i++) {
            int width = random.nextBoolean() ? 5 : 7;
            if (!place(placed, random, Kind.HUT, width, 5, 12, 20, 1 + random.nextInt(2))) {
                return null;
            }
        }
        if (!place(placed, random, Kind.WATCH_POST, 3, 3, 19, 25, 0)) {
            return null;
        }
        return connect(placed);
    }

    private static ThalFrame shelterFrame(RandomSource random) {
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        ThalFrame probe = new ThalFrame(0, 0, 9, 7, facing);
        return new ThalFrame(-(probe.sizeX() / 2), -(probe.sizeZ() / 2), 9, 7, facing);
    }

    private static boolean place(
            List<PlannedPiece> placed, RandomSource random, Kind kind, int width, int depth,
            int minRadius, int maxRadius, int residents
    ) {
        for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);
            int centreX = (int) Math.round(Math.cos(angle) * radius);
            int centreZ = (int) Math.round(Math.sin(angle) * radius);

            Direction facing = towardCentre(centreX, centreZ);
            if (random.nextInt(4) == 0) {
                facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            }
            ThalFrame probe = new ThalFrame(0, 0, width, depth, facing);
            ThalFrame frame = new ThalFrame(
                    centreX - probe.sizeX() / 2, centreZ - probe.sizeZ() / 2, width, depth, facing);
            if (fits(placed, frame)) {
                placed.add(new PlannedPiece(kind, frame, residents));
                return true;
            }
        }
        return false;
    }

    private static Direction towardCentre(int x, int z) {
        if (Math.abs(x) >= Math.abs(z)) {
            return x > 0 ? Direction.WEST : Direction.EAST;
        }
        return z > 0 ? Direction.NORTH : Direction.SOUTH;
    }

    /** In bounds (including the entrance cell) and at least {@link #GAP} empty cells from every other footprint. */
    private static boolean fits(List<PlannedPiece> placed, ThalFrame frame) {
        if (frame.minX() - 1 < -BOUND || frame.maxX() + 1 > BOUND
                || frame.minZ() - 1 < -BOUND || frame.maxZ() + 1 > BOUND) {
            return false;
        }
        for (PlannedPiece other : placed) {
            ThalFrame o = other.frame();
            boolean apartX = frame.minX() > o.maxX() + GAP || frame.maxX() < o.minX() - GAP;
            boolean apartZ = frame.minZ() > o.maxZ() + GAP || frame.maxZ() < o.minZ() - GAP;
            if (!apartX && !apartZ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Grows the path network from the shelter entrance: each other building (nearest first) is joined to the
     * network by a shortest 4-connected route around footprints, so the result is a connected tree-like web.
     */
    private static Layout connect(List<PlannedPiece> placed) {
        Cell root = placed.get(0).entranceCell();
        Set<Cell> paths = new TreeSet<>();
        paths.add(root);

        List<PlannedPiece> order = new ArrayList<>(placed.subList(1, placed.size()));
        order.sort(Comparator.comparingInt((PlannedPiece piece) -> manhattan(piece.entranceCell(), root)));

        for (PlannedPiece piece : order) {
            List<Cell> route = route(placed, paths, piece.entranceCell());
            if (route == null) {
                return null;
            }
            paths.addAll(route);
        }
        return new Layout(placed, paths);
    }

    private static int manhattan(Cell a, Cell b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.z() - b.z());
    }

    /** BFS from {@code start} to the nearest existing path cell, avoiding all footprints; null if unreachable. */
    private static List<Cell> route(List<PlannedPiece> placed, Set<Cell> paths, Cell start) {
        if (blocked(placed, start)) {
            return null;
        }
        Map<Cell, Cell> parent = new HashMap<>();
        ArrayDeque<Cell> queue = new ArrayDeque<>();
        parent.put(start, start);
        queue.add(start);
        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            if (paths.contains(current)) {
                List<Cell> route = new ArrayList<>();
                for (Cell at = current; ; at = parent.get(at)) {
                    route.add(at);
                    if (at.equals(start)) {
                        break;
                    }
                }
                return route;
            }
            for (Direction step : STEPS) {
                Cell next = current.relative(step);
                if (Math.abs(next.x()) > BOUND || Math.abs(next.z()) > BOUND
                        || parent.containsKey(next) || blocked(placed, next)) {
                    continue;
                }
                parent.put(next, current);
                queue.add(next);
            }
        }
        return null;
    }

    private static boolean blocked(List<PlannedPiece> placed, Cell cell) {
        for (PlannedPiece piece : placed) {
            if (piece.frame().contains(cell.x(), cell.z())) {
                return true;
            }
        }
        return false;
    }

    /** Hand-authored layout used only if every randomized attempt failed. Always valid (see unit tests). */
    static Layout fallback() {
        List<PlannedPiece> placed = new ArrayList<>();
        placed.add(new PlannedPiece(Kind.SHELTER, new ThalFrame(-4, -3, 9, 7, Direction.SOUTH), 2));
        placed.add(new PlannedPiece(Kind.FIELD, new ThalFrame(-24, -4, 9, 7, Direction.EAST), 0));
        placed.add(new PlannedPiece(Kind.FIELD, new ThalFrame(16, -4, 9, 7, Direction.WEST), 0));
        placed.add(new PlannedPiece(Kind.HUT, new ThalFrame(-12, 10, 5, 5, Direction.NORTH), 1));
        placed.add(new PlannedPiece(Kind.HUT, new ThalFrame(-2, 12, 5, 5, Direction.NORTH), 1));
        placed.add(new PlannedPiece(Kind.HUT, new ThalFrame(8, 10, 5, 5, Direction.NORTH), 1));
        placed.add(new PlannedPiece(Kind.WATCH_POST, new ThalFrame(-1, -22, 3, 3, Direction.SOUTH), 0));
        Layout layout = connect(placed);
        if (layout == null) {
            throw new IllegalStateException("Fallback Thal settlement layout must be connectable");
        }
        return layout;
    }
}
