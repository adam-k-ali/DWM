package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.world.structure.ThalSettlementLayout.Cell;
import com.adamkali.dwm.world.structure.ThalSettlementLayout.Kind;
import com.adamkali.dwm.world.structure.ThalSettlementLayout.Layout;
import com.adamkali.dwm.world.structure.ThalSettlementLayout.PlannedPiece;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThalSettlementLayoutTest {
    private static final int SEEDS = 500;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void everySeedHasTheRequiredPieces() {
        for (long seed = 0; seed < SEEDS; seed++) {
            Layout layout = ThalSettlementLayout.plan(seed);
            assertEquals(1, layout.ofKind(Kind.SHELTER).size(), "shelters, seed " + seed);
            int huts = layout.ofKind(Kind.HUT).size();
            assertTrue(huts >= ThalSettlementLayout.HUT_MIN && huts <= ThalSettlementLayout.HUT_MAX, "huts " + huts + ", seed " + seed);
            assertEquals(ThalSettlementLayout.FIELD_COUNT, layout.ofKind(Kind.FIELD).size(), "fields, seed " + seed);
            assertEquals(1, layout.ofKind(Kind.WATCH_POST).size(), "watch posts, seed " + seed);
            assertEquals(Kind.SHELTER, layout.shelter().kind(), "shelter is the reference piece, seed " + seed);
        }
    }

    @Test
    void residentsAreAssignedToHutsAndShelterOnly() {
        for (long seed = 0; seed < SEEDS; seed++) {
            Layout layout = ThalSettlementLayout.plan(seed);
            for (PlannedPiece piece : layout.pieces()) {
                switch (piece.kind()) {
                    case SHELTER -> assertTrue(piece.residents() >= 2 && piece.residents() <= 3, "seed " + seed);
                    case HUT -> assertTrue(piece.residents() >= 1 && piece.residents() <= 2, "seed " + seed);
                    default -> assertEquals(0, piece.residents(), "seed " + seed);
                }
            }
        }
    }

    @Test
    void footprintsDoNotOverlapEachOtherOrPaths() {
        for (long seed = 0; seed < SEEDS; seed++) {
            Layout layout = ThalSettlementLayout.plan(seed);
            List<PlannedPiece> pieces = layout.pieces();
            for (int i = 0; i < pieces.size(); i++) {
                ThalFrame a = pieces.get(i).frame();
                for (int j = i + 1; j < pieces.size(); j++) {
                    ThalFrame b = pieces.get(j).frame();
                    boolean apartX = a.minX() > b.maxX() + ThalSettlementLayout.GAP || a.maxX() < b.minX() - ThalSettlementLayout.GAP;
                    boolean apartZ = a.minZ() > b.maxZ() + ThalSettlementLayout.GAP || a.maxZ() < b.minZ() - ThalSettlementLayout.GAP;
                    assertTrue(apartX || apartZ, "pieces " + i + " and " + j + " too close, seed " + seed);
                }
                for (Cell path : layout.pathCells()) {
                    assertFalse(a.contains(path.x(), path.z()), "path " + path + " inside piece " + i + ", seed " + seed);
                }
                assertTrue(
                        Math.abs(a.minX()) <= ThalSettlementLayout.BOUND && Math.abs(a.maxX()) <= ThalSettlementLayout.BOUND
                                && Math.abs(a.minZ()) <= ThalSettlementLayout.BOUND && Math.abs(a.maxZ()) <= ThalSettlementLayout.BOUND,
                        "piece " + i + " out of bounds, seed " + seed);
            }
        }
    }

    @Test
    void widenedPathsCoverThreeWideAndNeverEnterFootprints() {
        for (long seed = 0; seed < SEEDS; seed++) {
            Layout layout = ThalSettlementLayout.plan(seed);
            Set<Cell> widened = ThalSettlementLayout.widenedPathCells(layout);
            for (Cell path : layout.pathCells()) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Cell near = new Cell(path.x() + dx, path.z() + dz);
                        boolean inFootprint = layout.pieces().stream().anyMatch(p -> p.frame().contains(near.x(), near.z()));
                        assertEquals(!inFootprint, widened.contains(near), "cell " + near + " around " + path + ", seed " + seed);
                    }
                }
            }
            for (Cell cell : widened) {
                for (PlannedPiece piece : layout.pieces()) {
                    assertFalse(piece.frame().contains(cell.x(), cell.z()), "widened path " + cell + " inside footprint, seed " + seed);
                }
            }
        }
    }

    @Test
    void everyDoorIsReachableFromTheShelterDoorThroughPaths() {
        for (long seed = 0; seed < SEEDS; seed++) {
            assertAllEntrancesConnected(ThalSettlementLayout.plan(seed), "seed " + seed);
        }
    }

    @Test
    void doorsSitOnTheirFootprintEdgeAndEntrancesAreOutside() {
        for (long seed = 0; seed < 100; seed++) {
            for (PlannedPiece piece : ThalSettlementLayout.plan(seed).pieces()) {
                ThalFrame frame = piece.frame();
                Cell door = piece.doorCell();
                Cell entrance = piece.entranceCell();
                assertTrue(frame.contains(door.x(), door.z()), "door inside footprint, seed " + seed);
                assertFalse(frame.contains(entrance.x(), entrance.z()), "entrance outside footprint, seed " + seed);
                assertEquals(entrance, door.relative(frame.facing()), "seed " + seed);
            }
        }
    }

    @Test
    void sameSeedGivesTheSameLayout() {
        for (long seed = 0; seed < 50; seed++) {
            assertEquals(ThalSettlementLayout.plan(seed), ThalSettlementLayout.plan(seed), "seed " + seed);
            assertEquals(
                    ThalSettlementLayout.plan(RandomSource.create(seed)),
                    ThalSettlementLayout.plan(RandomSource.create(seed)),
                    "seed " + seed);
        }
    }

    @Test
    void differentSeedsGiveDifferentLayouts() {
        Set<Layout> distinct = new HashSet<>();
        for (long seed = 0; seed < 50; seed++) {
            distinct.add(ThalSettlementLayout.plan(seed));
        }
        assertTrue(distinct.size() > 40, "layouts should vary by seed, got " + distinct.size());
    }

    @Test
    void randomPlanningNeverNeedsTheFallback() {
        for (long seed = 0; seed < SEEDS; seed++) {
            assertNotNull(ThalSettlementLayout.tryPlan(RandomSource.create(seed)), "first attempt failed for seed " + seed
                    + " (the retry loop would still cover it, but the failure rate should stay low)");
        }
    }

    @Test
    void fallbackLayoutIsValid() {
        Layout layout = ThalSettlementLayout.fallback();
        assertEquals(1, layout.ofKind(Kind.SHELTER).size());
        assertEquals(3, layout.ofKind(Kind.HUT).size());
        assertEquals(2, layout.ofKind(Kind.FIELD).size());
        assertEquals(1, layout.ofKind(Kind.WATCH_POST).size());
        assertAllEntrancesConnected(layout, "fallback");
    }

    @Test
    void frameMapsLocalCellsBijectivelyOntoTheFootprintForEveryFacing() {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            ThalFrame frame = new ThalFrame(-3, 5, 7, 5, facing);
            Set<Cell> cells = new HashSet<>();
            for (int u = 0; u < frame.width(); u++) {
                for (int v = 0; v < frame.depth(); v++) {
                    Cell cell = new Cell(frame.worldX(u, v), frame.worldZ(u, v));
                    assertTrue(frame.contains(cell.x(), cell.z()), facing + " " + u + "," + v);
                    cells.add(cell);
                }
            }
            assertEquals(frame.width() * frame.depth(), cells.size(), facing.toString());
            // v = 0 is the door wall: stepping outward from it leaves the footprint.
            for (int u = 0; u < frame.width(); u++) {
                Cell front = new Cell(frame.worldX(u, 0), frame.worldZ(u, 0));
                Cell outside = front.relative(facing);
                assertFalse(frame.contains(outside.x(), outside.z()), facing + " front u=" + u);
            }
            // The door cell is the centre of the front wall.
            assertEquals(new Cell(frame.worldX(frame.doorU(), 0), frame.worldZ(frame.doorU(), 0)), frame.doorCell());
            // uDirection really is the direction of increasing u.
            Cell u0 = new Cell(frame.worldX(0, 0), frame.worldZ(0, 0));
            assertEquals(u0.relative(frame.uDirection()), new Cell(frame.worldX(1, 0), frame.worldZ(1, 0)), facing.toString());
            // inward really is the direction of increasing v.
            assertEquals(u0.relative(frame.inward()), new Cell(frame.worldX(0, 1), frame.worldZ(0, 1)), facing.toString());
        }
    }

    @Test
    void siteHeightPolicy() {
        assertTrue(ThalSettlementStructure.chooseBaseY(List.of()).isEmpty());
        assertTrue(ThalSettlementStructure.chooseBaseY(List.of(70, 70, 77)).isEmpty(), "spread 7 rejects");
        assertEquals(OptionalInt.of(71), ThalSettlementStructure.chooseBaseY(List.of(70, 71, 76)), "median, spread 6 accepts");
        assertEquals(OptionalInt.of(64), ThalSettlementStructure.chooseBaseY(List.of(64)));
    }

    @Test
    void siteWaterPolicy() {
        assertFalse(ThalSettlementStructure.isTooWet(false, 0, 50), "dry site");
        assertFalse(ThalSettlementStructure.isTooWet(false, 12, 50), "a quarter wet is tolerated");
        assertTrue(ThalSettlementStructure.isTooWet(false, 13, 50), "more than a quarter wet rejects");
        assertTrue(ThalSettlementStructure.isTooWet(true, 1, 50), "a wet centre always rejects");
    }

    private static void assertAllEntrancesConnected(Layout layout, String label) {
        Cell root = layout.shelter().entranceCell();
        assertTrue(layout.pathCells().contains(root), "shelter entrance must be a path cell, " + label);

        Set<Cell> reached = new HashSet<>();
        ArrayDeque<Cell> queue = new ArrayDeque<>();
        reached.add(root);
        queue.add(root);
        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            for (Direction step : Direction.Plane.HORIZONTAL) {
                Cell next = current.relative(step);
                if (layout.pathCells().contains(next) && reached.add(next)) {
                    queue.add(next);
                }
            }
        }
        for (PlannedPiece piece : layout.pieces()) {
            assertTrue(layout.pathCells().contains(piece.entranceCell()), piece.kind() + " entrance not a path cell, " + label);
            assertTrue(reached.contains(piece.entranceCell()), piece.kind() + " entrance " + piece.entranceCell() + " unreachable, " + label);
        }
        assertEquals(layout.pathCells().size(), reached.size(), "every path cell is connected to the shelter, " + label);
    }
}
