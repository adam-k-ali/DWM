package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.interior.ars.ArsGrid;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Layouts for the TARDIS Architectural Reconfiguration System (ARS) pieces and the console annex. Regenerate the
 * committed templates with {@code ./gradlew generateStructureTemplates}; {@link StructureTemplateGeneratorTest}
 * fails if they drift.
 *
 * <p>Every piece is a closed shell sized {@code cells × }{@value ArsGrid#CELL_SIZE} on X and Z, floor at
 * {@code y = 0}. Each connector's 4×4 doorway (offsets {@value ArsGrid#APERTURE_MIN_OFFSET}..
 * {@value ArsGrid#APERTURE_MAX_OFFSET}, {@code y = }{@value ArsGrid#APERTURE_MIN_Y}..{@value ArsGrid#APERTURE_MAX_Y})
 * is authored closed by a plug of wall-palette blocks with air directly behind it, so the ARS can carve and
 * re-seal it. {@code ArsPieceResourceTest} checks the contract.
 *
 * <p>Layers are computed on a {@link Canvas} rather than written as strings: the rooms are too large to review
 * as character grids, and the shell, doorways and cove are shared between them.
 */
public final class TardisArsTemplates {
    public static final String PREFIX = "tardis/ars/";
    public static final String CORRIDOR_STRAIGHT = PREFIX + "corridor_straight";
    public static final String CORRIDOR_CORNER = PREFIX + "corridor_corner";
    public static final String CORRIDOR_T = PREFIX + "corridor_t";
    public static final String CORRIDOR_CROSS = PREFIX + "corridor_cross";
    public static final String CORRIDOR_DEAD_END = PREFIX + "corridor_dead_end";
    public static final String BEDROOM = PREFIX + "bedroom";
    public static final String LIBRARY = PREFIX + "library";
    public static final String ARS_CHAMBER = PREFIX + "ars_chamber";
    /** Fixed lobby joining the console room to the grid; not a registry piece. */
    public static final String CONSOLE_ANNEX = PREFIX + "console_annex";

    public static final int CORRIDOR_HEIGHT = 6;
    public static final int BEDROOM_HEIGHT = 6;
    public static final int LIBRARY_HEIGHT = 10;
    public static final int ARS_CHAMBER_HEIGHT = 14;

    public static final int ANNEX_SIZE_X = 5;
    public static final int ANNEX_SIZE_Y = 6;
    public static final int ANNEX_SIZE_Z = 9;
    /** Annex-local z range of the permanently open east doorway (lines up with the root cell's west aperture). */
    public static final int ANNEX_DOOR_MIN_Z = 4;
    public static final int ANNEX_DOOR_MAX_Z = 7;

    private static final int CELL = ArsGrid.CELL_SIZE;
    private static final int AP_MIN = ArsGrid.APERTURE_MIN_OFFSET;
    private static final int AP_MAX = ArsGrid.APERTURE_MAX_OFFSET;
    private static final int AP_MIN_Y = ArsGrid.APERTURE_MIN_Y;
    private static final int AP_MAX_Y = ArsGrid.APERTURE_MAX_Y;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private record Palette(Block wall, Block roundelA, Block roundelB, Block bigA, Block bigB) {
        BlockState plain() {
            return wall.defaultBlockState();
        }

        BlockState roundel(int along) {
            return ((along & 1) == 0 ? roundelA : roundelB).defaultBlockState();
        }

        BlockState big(int along) {
            return ((along & 1) == 0 ? bigA : bigB).defaultBlockState();
        }
    }

    private static final Palette WHITE = new Palette(DWMBlocks.WHITE_TARDIS_WALL,
            DWMBlocks.WHITE_ROUNDEL_A, DWMBlocks.WHITE_ROUNDEL_B, DWMBlocks.WHITE_BIG_ROUNDEL_A, DWMBlocks.WHITE_BIG_ROUNDEL_B);
    private static final Palette LIGHT_BLUE = new Palette(DWMBlocks.LIGHT_BLUE_TARDIS_WALL,
            DWMBlocks.LIGHT_BLUE_ROUNDEL_A, DWMBlocks.LIGHT_BLUE_ROUNDEL_B,
            DWMBlocks.LIGHT_BLUE_BIG_ROUNDEL_A, DWMBlocks.LIGHT_BLUE_BIG_ROUNDEL_B);
    private static final Palette BROWN = new Palette(DWMBlocks.BROWN_TARDIS_WALL,
            DWMBlocks.BROWN_ROUNDEL_A, DWMBlocks.BROWN_ROUNDEL_B, DWMBlocks.BROWN_BIG_ROUNDEL_A, DWMBlocks.BROWN_BIG_ROUNDEL_B);
    private static final Palette YELLOW = new Palette(DWMBlocks.YELLOW_TARDIS_WALL,
            DWMBlocks.YELLOW_ROUNDEL_A, DWMBlocks.YELLOW_ROUNDEL_B, DWMBlocks.YELLOW_BIG_ROUNDEL_A, DWMBlocks.YELLOW_BIG_ROUNDEL_B);

    private TardisArsTemplates() {
    }

    public static List<TemplateLayout> all() {
        return List.of(
                corridor(CORRIDOR_STRAIGHT, Direction.NORTH, Direction.SOUTH),
                corridor(CORRIDOR_CORNER, Direction.NORTH, Direction.EAST),
                corridor(CORRIDOR_T, Direction.WEST, Direction.NORTH, Direction.EAST),
                corridor(CORRIDOR_CROSS, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST),
                corridorDeadEnd(),
                bedroom(),
                library(),
                arsChamber(),
                consoleAnnex());
    }

    // ---- Corridors ---------------------------------------------------------------------------------------------

    /**
     * One-cell corridor: a 4-wide, 4-high passage (offsets 2..5, {@code y = 1..4}) through a central hub, with an
     * arm toward each listed side. Walls (offsets 1 and 6, and the end walls on the cell's outer layer) wrap the
     * air exactly; everything else in the cell is structure void.
     */
    static TemplateLayout corridor(String path, Direction... arms) {
        boolean[][] open = new boolean[CELL][CELL];
        mark(open, AP_MIN, AP_MIN, AP_MAX, AP_MAX);
        for (Direction arm : arms) {
            switch (arm) {
                case NORTH -> mark(open, AP_MIN, 1, AP_MAX, AP_MIN - 1);
                case SOUTH -> mark(open, AP_MIN, AP_MAX + 1, AP_MAX, CELL - 2);
                case WEST -> mark(open, 1, AP_MIN, AP_MIN - 1, AP_MAX);
                case EAST -> mark(open, AP_MAX + 1, AP_MIN, CELL - 2, AP_MAX);
                default -> throw new IllegalArgumentException(path + ": corridor arms are horizontal, got " + arm);
            }
        }
        Canvas canvas = passage(open);
        ceilingVents(canvas, 3, 4, 3, 4, CORRIDOR_HEIGHT - 2);
        return canvas.build(path);
    }

    /**
     * Dead-end stub: a short passage from the north doorway that stops at a feature wall of big white roundels,
     * flanked by two columns with the TARDIS globe between them.
     */
    static TemplateLayout corridorDeadEnd() {
        boolean[][] open = new boolean[CELL][CELL];
        mark(open, AP_MIN, 1, AP_MAX, 4);
        Canvas canvas = passage(open);
        for (int x = AP_MIN; x <= AP_MAX; x++) {
            for (int y = 1; y <= CORRIDOR_HEIGHT - 2; y++) {
                canvas.set(x, y, 5, WHITE.big(x + y));
            }
        }
        canvas.set(AP_MIN, 1, 4, decor(DWMBlocks.DECORATIONAL_COLUMN, Direction.NORTH));
        canvas.set(AP_MAX, 1, 4, decor(DWMBlocks.DECORATIONAL_COLUMN, Direction.NORTH));
        canvas.set(3, 1, 4, decor(DWMBlocks.TARDIS_GLOBE, Direction.NORTH));
        ceilingVents(canvas, 3, 4, 2, 3, CORRIDOR_HEIGHT - 2);
        return canvas.build(CORRIDOR_DEAD_END);
    }

    private static void mark(boolean[][] open, int x0, int z0, int x1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                open[x][z] = true;
            }
        }
    }

    /**
     * White corridor shell around an open plan: air over the open cells, a wall wherever a cell touches the open
     * area (including diagonally), floor and ceiling under and over both. Wall faces are roundel columns; cells
     * that only touch the passage at a corner are plain wall.
     */
    private static Canvas passage(boolean[][] open) {
        Canvas canvas = new Canvas(CELL, CORRIDOR_HEIGHT, CELL);
        int top = CORRIDOR_HEIGHT - 1;
        for (int x = 0; x < CELL; x++) {
            for (int z = 0; z < CELL; z++) {
                boolean face = false;
                boolean touch = false;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        int nx = x + dx;
                        int nz = z + dz;
                        if ((dx != 0 || dz != 0) && nx >= 0 && nx < CELL && nz >= 0 && nz < CELL && open[nx][nz]) {
                            touch = true;
                            face |= dx == 0 || dz == 0;
                        }
                    }
                }
                if (!open[x][z] && !touch) {
                    continue;
                }
                canvas.set(x, 0, z, WHITE.plain());
                canvas.set(x, top, z, WHITE.plain());
                for (int y = 1; y < top; y++) {
                    canvas.set(x, y, z, open[x][z] ? AIR : face ? WHITE.roundel(x + z) : WHITE.plain());
                }
            }
        }
        return canvas;
    }

    // ---- Rooms ---------------------------------------------------------------------------------------------------

    /**
     * 16×6×8 crew quarters in light blue. A glass partition with two columns splits the entry and desk end from
     * the sleeping end: a double bed against the east wall between barrel nightstands with lanterns, a chest and a
     * soft blue floor; the desk end has a lamp-lit writing desk between short bookcases, a chair, an armchair and a
     * second chest.
     */
    static TemplateLayout bedroom() {
        int sx = 2 * CELL;
        int sz = CELL;
        Canvas canvas = shell(sx, BEDROOM_HEIGHT, sz, LIGHT_BLUE);
        Doorway north = Doorway.of(0, 0, Direction.NORTH);
        doorway(canvas, north, LIGHT_BLUE);
        cove(canvas, BEDROOM_HEIGHT - 2, north);

        // Sleeping end: blue floor, a double bed (two beds side by side) heads-east between lantern-lit nightstands.
        for (int x = 11; x <= sx - 2; x++) {
            for (int z = 1; z <= sz - 2; z++) {
                canvas.set(x, 0, z, LIGHT_BLUE.plain());
            }
        }
        bed(canvas, 13, 1, 3, Direction.EAST, DyeColor.LIGHT_BLUE);
        bed(canvas, 13, 1, 4, Direction.EAST, DyeColor.LIGHT_BLUE);
        for (int z : new int[] {2, 5}) {
            canvas.set(14, 1, z, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            canvas.set(14, 2, z, Blocks.LANTERN.defaultBlockState());
        }
        canvas.set(11, 1, 6, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH));

        // Partition: glass panes against the long walls, columns framing a 2-wide opening.
        for (int y = 1; y <= BEDROOM_HEIGHT - 2; y++) {
            canvas.set(10, y, 1, Blocks.GLASS_PANE.defaultBlockState());
            canvas.set(10, y, sz - 2, Blocks.GLASS_PANE.defaultBlockState());
        }
        canvas.set(10, 1, 2, decor(DWMBlocks.DECORATIONAL_COLUMN, Direction.WEST));
        canvas.set(10, 1, 5, decor(DWMBlocks.DECORATIONAL_COLUMN, Direction.WEST));

        // Desk end: a writing desk with a lamp between two short bookcases on the south wall, a chair at the desk,
        // an armchair and a chest in the west corner.
        canvas.set(6, 1, 6, slab(Blocks.DARK_OAK_SLAB, SlabType.TOP));
        canvas.set(7, 1, 6, slab(Blocks.DARK_OAK_SLAB, SlabType.TOP));
        canvas.set(6, 2, 6, Blocks.LANTERN.defaultBlockState());
        for (int y = 1; y <= 2; y++) {
            canvas.set(5, y, 6, Blocks.BOOKSHELF.defaultBlockState());
            canvas.set(8, y, 6, Blocks.BOOKSHELF.defaultBlockState());
        }
        canvas.set(7, 1, 5, decor(DWMBlocks.TARDIS_CHAIR_SMALL, Direction.NORTH));
        canvas.set(1, 1, 6, decor(DWMBlocks.TARDIS_CHAIR_LARGE, Direction.NORTH));
        canvas.set(1, 1, 4, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST));

        ceilingVents(canvas, 5, 6, 3, 4, BEDROOM_HEIGHT - 2);
        ceilingVents(canvas, 12, 12, 3, 4, BEDROOM_HEIGHT - 2);
        return canvas.build(BEDROOM);
    }

    /**
     * 16×10×16 library in brown. Walls are bays of bookshelves framed by roundel pilasters and roundel bands at
     * {@code y = 4} and {@code y = 8};
     * two free-standing shelf rows fill the north-east, a reading table with chairs and lecterns sits on a brown
     * rug in the middle, and a reading nook with the globe is in the south-west. Hanging lanterns and ceiling
     * vents light the floor. Doorways: north on cell (0, 0) and east on cell (1, 1).
     */
    static TemplateLayout library() {
        int size = 2 * CELL;
        int top = LIBRARY_HEIGHT - 1;
        Canvas canvas = shell(size, LIBRARY_HEIGHT, size, BROWN);
        // Bookshelf walls between the roundel bands, split into three bays by roundel pilasters.
        for (int y = 1; y < top; y++) {
            if (y == 4 || y == 8) {
                continue;
            }
            for (int i = 1; i < size - 1; i++) {
                if (i == 5 || i == 10) {
                    continue;
                }
                canvas.set(i, y, 0, Blocks.BOOKSHELF.defaultBlockState());
                canvas.set(i, y, size - 1, Blocks.BOOKSHELF.defaultBlockState());
                canvas.set(0, y, i, Blocks.BOOKSHELF.defaultBlockState());
                canvas.set(size - 1, y, i, Blocks.BOOKSHELF.defaultBlockState());
            }
        }
        Doorway north = Doorway.of(0, 0, Direction.NORTH);
        Doorway east = Doorway.of(1, 1, Direction.EAST);
        doorway(canvas, north, BROWN);
        doorway(canvas, east, BROWN);
        cove(canvas, top - 1, north, east);

        // Rug under the reading table.
        for (int x = 4; x <= 11; x++) {
            for (int z = 7; z <= 11; z++) {
                canvas.set(x, 0, z, BROWN.plain());
            }
        }
        // Reading table (dark oak top slabs) with chairs on both long sides and a lectern at each end.
        for (int x = 6; x <= 9; x++) {
            canvas.set(x, 1, 9, slab(Blocks.DARK_OAK_SLAB, SlabType.TOP));
        }
        for (int x = 6; x <= 9; x += 3) {
            canvas.set(x, 1, 8, decor(DWMBlocks.TARDIS_CHAIR_SMALL, Direction.NORTH));
            canvas.set(x, 1, 10, decor(DWMBlocks.TARDIS_CHAIR_SMALL, Direction.SOUTH));
        }
        canvas.set(5, 1, 9, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.EAST));
        canvas.set(10, 1, 9, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.WEST));

        // Free-standing shelf rows, capped with a slab, columns at their ends.
        for (int z : new int[] {3, 5}) {
            for (int x = 8; x <= 12; x++) {
                for (int y = 1; y <= 3; y++) {
                    canvas.set(x, y, z, Blocks.BOOKSHELF.defaultBlockState());
                }
                canvas.set(x, 4, z, slab(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
            }
        }

        // Reading nook: armchair beside the globe.
        canvas.set(2, 1, 13, decor(DWMBlocks.TARDIS_CHAIR_LARGE, Direction.WEST));
        canvas.set(2, 1, 11, decor(DWMBlocks.TARDIS_GLOBE, Direction.EAST));
        canvas.set(1, 1, 1, decor(DWMBlocks.DECORATIONAL_COLUMN, Direction.SOUTH));
        canvas.set(size - 2, 1, size - 2, decor(DWMBlocks.DECORATIONAL_COLUMN, Direction.NORTH));

        // Light: hanging lanterns on chains, vents over the table.
        for (int[] at : new int[][] {{4, 4}, {11, 4}, {4, 11}, {11, 11}}) {
            hangingLantern(canvas, at[0], at[1], top, 2);
        }
        ceilingVents(canvas, 7, 8, 8, 10, top - 1);
        return canvas.build(LIBRARY);
    }

    /**
     * 24×14×24 Architectural Reconfiguration chamber in yellow: "a machine that makes machines". A waxed copper
     * tree rises from a raised quartz dais ringed by copper bars: a 2×2 trunk with root flares, eight branches
     * spiralling out and up, glowing froglight and sea lantern orbs hung from them on chains, end rods budding from
     * the branch tips and a sea-lantern heart on top. A yellow ring is inlaid in the floor around the dais.
     * Doorways: north on cell (1, 0) and south on cell (1, 2).
     */
    static TemplateLayout arsChamber() {
        int size = 3 * CELL;
        int top = ARS_CHAMBER_HEIGHT - 1;
        Canvas canvas = shell(size, ARS_CHAMBER_HEIGHT, size, YELLOW);
        // Upper walls: a white band, then big roundels.
        for (int i = 1; i < size - 1; i++) {
            for (int y = 5; y < top; y++) {
                BlockState state = y == 5 ? WHITE.plain() : YELLOW.big(i);
                canvas.set(i, y, 0, state);
                canvas.set(i, y, size - 1, state);
                canvas.set(0, y, i, state);
                canvas.set(size - 1, y, i, state);
            }
        }
        Doorway north = Doorway.of(1, 0, Direction.NORTH);
        Doorway south = Doorway.of(1, 2, Direction.SOUTH);
        doorway(canvas, north, YELLOW);
        doorway(canvas, south, YELLOW);
        cove(canvas, top - 1, north, south);

        double c = (size - 1) / 2.0;
        for (int x = 1; x < size - 1; x++) {
            for (int z = 1; z < size - 1; z++) {
                double d = Math.hypot(x - c, z - c);
                if (d >= 7.0 && d < 8.0) {
                    canvas.set(x, 0, z, YELLOW.plain());
                }
                if (onDais(x, z, c)) {
                    canvas.set(x, 1, z, Blocks.SMOOTH_QUARTZ.defaultBlockState());
                } else if (d <= 5.6) {
                    canvas.set(x, 1, z, slab(Blocks.SMOOTH_QUARTZ_SLAB, SlabType.BOTTOM));
                }
            }
        }
        // Railing on the dais rim (dais blocks with a side open to the step ring), so the bars join up; it is open
        // where the doorway axis crosses it.
        for (int x = 1; x < size - 1; x++) {
            for (int z = 1; z < size - 1; z++) {
                boolean onAxis = x >= 10 && x <= 13;
                if (!onAxis && onDais(x, z, c) && !(onDais(x - 1, z, c) && onDais(x + 1, z, c)
                        && onDais(x, z - 1, c) && onDais(x, z + 1, c))) {
                    canvas.set(x, 2, z, Blocks.COPPER_BARS.waxed().unaffected().defaultBlockState());
                }
            }
        }

        // Trunk with chiselled bands and root flares.
        BlockState copper = Blocks.COPPER_BLOCK.waxed().unaffected().defaultBlockState();
        BlockState chiseled = Blocks.CHISELED_COPPER.waxed().unaffected().defaultBlockState();
        for (int y = 2; y <= 10; y++) {
            for (int x = 11; x <= 12; x++) {
                for (int z = 11; z <= 12; z++) {
                    canvas.set(x, y, z, y == 4 || y == 7 ? chiseled : copper);
                }
            }
        }
        Block cutStairs = Blocks.CUT_COPPER_STAIRS.waxed().unaffected();
        for (int i = 11; i <= 12; i++) {
            canvas.set(10, 2, i, stairs(cutStairs, Direction.EAST, Half.BOTTOM));
            canvas.set(13, 2, i, stairs(cutStairs, Direction.WEST, Half.BOTTOM));
            canvas.set(i, 2, 10, stairs(cutStairs, Direction.SOUTH, Half.BOTTOM));
            canvas.set(i, 2, 13, stairs(cutStairs, Direction.NORTH, Half.BOTTOM));
        }
        for (int x = 11; x <= 12; x++) {
            for (int z = 11; z <= 12; z++) {
                canvas.set(x, 11, z, Blocks.SEA_LANTERN.defaultBlockState());
            }
        }

        // Branches: four diagonals from the trunk corners and four cardinals in a pinwheel.
        BlockState cut = Blocks.CUT_COPPER.waxed().unaffected().defaultBlockState();
        BlockState chain = Blocks.COPPER_CHAIN.waxed().unaffected().defaultBlockState();
        BlockState[] orbs = {
                Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState(),
                Blocks.OCHRE_FROGLIGHT.defaultBlockState(),
                Blocks.SEA_LANTERN.defaultBlockState(),
                Blocks.VERDANT_FROGLIGHT.defaultBlockState()
        };
        int[][] branches = {
                // startX, startZ, dx, dz
                {11, 11, -1, -1}, {12, 11, 1, -1}, {12, 12, 1, 1}, {11, 12, -1, 1},
                {11, 11, 0, -1}, {12, 11, 1, 0}, {12, 12, 0, 1}, {11, 12, -1, 0}
        };
        int[] diagonalRise = {8, 9, 10, 10};
        int[] cardinalRise = {7, 8, 9, 9, 10};
        for (int b = 0; b < branches.length; b++) {
            int[] branch = branches[b];
            boolean diagonal = branch[2] != 0 && branch[3] != 0;
            int[] rise = diagonal ? diagonalRise : cardinalRise;
            for (int k = 0; k < rise.length; k++) {
                int x = branch[0] + branch[2] * (k + 1);
                int z = branch[1] + branch[3] * (k + 1);
                canvas.set(x, rise[k], z, cut);
                boolean tip = k == rise.length - 1;
                if (tip) {
                    canvas.set(x, rise[k] + 1, z, Blocks.END_ROD.defaultBlockState());
                }
                // Orbs hang under every other step, on chains of varying length.
                if (k % 2 == 1 || tip) {
                    int drop = 1 + (b + k) % 3;
                    for (int i = 1; i <= drop; i++) {
                        canvas.set(x, rise[k] - i, z, chain);
                    }
                    canvas.set(x, rise[k] - drop - 1, z, orbs[(b + k) % orbs.length]);
                }
            }
        }

        // Columns at the four quarters of the floor ring and vents high around the room.
        for (int[] at : new int[][] {{5, 5}, {18, 5}, {5, 18}, {18, 18}}) {
            canvas.set(at[0], 1, at[1], decor(DWMBlocks.DECORATIONAL_COLUMN, Direction.NORTH));
        }
        for (int[] at : new int[][] {{4, 11}, {4, 12}, {19, 11}, {19, 12}, {11, 4}, {12, 4}, {11, 19}, {12, 19}}) {
            canvas.set(at[0], top - 1, at[1], decor(DWMBlocks.TARDIS_CEILING_VENT, Direction.NORTH));
        }
        return canvas.build(ARS_CHAMBER);
    }

    private static boolean onDais(int x, int z, double centre) {
        return Math.hypot(x - centre, z - centre) <= 4.5;
    }

    /**
     * 5×6×9 lobby east of the console room. The console room's east wall closes its open west side; the east wall
     * has a permanently open 4×4 doorway at {@code z = }{@value #ANNEX_DOOR_MIN_Z}..{@value #ANNEX_DOOR_MAX_Z},
     * {@code y = 1..4}, framed in plain white like the ARS doorways.
     */
    static TemplateLayout consoleAnnex() {
        Canvas canvas = new Canvas(ANNEX_SIZE_X, ANNEX_SIZE_Y, ANNEX_SIZE_Z);
        int top = ANNEX_SIZE_Y - 1;
        int east = ANNEX_SIZE_X - 1;
        int south = ANNEX_SIZE_Z - 1;
        for (int x = 0; x < ANNEX_SIZE_X; x++) {
            for (int z = 0; z < ANNEX_SIZE_Z; z++) {
                canvas.set(x, 0, z, WHITE.plain());
                canvas.set(x, top, z, WHITE.plain());
                for (int y = 1; y < top; y++) {
                    boolean wall = z == 0 || z == south || x == east;
                    boolean corner = x == east && (z == 0 || z == south);
                    boolean doorway = x == east && z >= ANNEX_DOOR_MIN_Z && z <= ANNEX_DOOR_MAX_Z;
                    boolean frame = x == east && z == ANNEX_DOOR_MIN_Z - 1;
                    BlockState state;
                    if (!wall || doorway) {
                        state = AIR;
                    } else if (corner || frame) {
                        state = WHITE.plain();
                    } else {
                        state = WHITE.roundel(x + z);
                    }
                    canvas.set(x, y, z, state);
                }
            }
        }
        return canvas.build(CONSOLE_ANNEX);
    }

    // ---- Room helpers --------------------------------------------------------------------------------------------

    /** A connector face in template-local blocks: footprint cell and side, as in the piece JSON. */
    private record Doorway(int cellX, int cellZ, Direction side) {
        static Doorway of(int cellX, int cellZ, Direction side) {
            return new Doorway(cellX, cellZ, side);
        }

        /** Block x of the face at {@code along} (0..7) and {@code depth} (0 = outer layer, 1 = one step in). */
        int x(int along, int depth) {
            int min = cellX * CELL;
            return switch (side) {
                case WEST -> min + depth;
                case EAST -> min + CELL - 1 - depth;
                default -> min + along;
            };
        }

        int z(int along, int depth) {
            int min = cellZ * CELL;
            return switch (side) {
                case NORTH -> min + depth;
                case SOUTH -> min + CELL - 1 - depth;
                default -> min + along;
            };
        }

        boolean innerZone(int x, int y, int z) {
            if (y < AP_MIN_Y || y > AP_MAX_Y) {
                return false;
            }
            for (int along = AP_MIN; along <= AP_MAX; along++) {
                if (x(along, 1) == x && z(along, 1) == z) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Air box with a floor, ceiling and roundel-column walls: roundels alternate A/B along each wall, the four
     * corner columns are plain wall, floor and ceiling are plain white.
     */
    private static Canvas shell(int sx, int sy, int sz, Palette accent) {
        Canvas canvas = new Canvas(sx, sy, sz);
        for (int x = 0; x < sx; x++) {
            for (int z = 0; z < sz; z++) {
                canvas.set(x, 0, z, WHITE.plain());
                canvas.set(x, sy - 1, z, WHITE.plain());
                boolean edgeX = x == 0 || x == sx - 1;
                boolean edgeZ = z == 0 || z == sz - 1;
                for (int y = 1; y < sy - 1; y++) {
                    BlockState state;
                    if (edgeX && edgeZ) {
                        state = WHITE.plain();
                    } else if (edgeX) {
                        state = accent.roundel(z);
                    } else if (edgeZ) {
                        state = accent.roundel(x);
                    } else {
                        state = AIR;
                    }
                    canvas.set(x, y, z, state);
                }
            }
        }
        return canvas;
    }

    /**
     * Closed doorway on the outer wall: the 4×4 aperture is a plug of the wall's own roundels (offsets 2..5 keep the
     * wall's A/B rhythm), framed by plain white jambs at offsets 1 and 6 and a lintel one block above it.
     */
    private static void doorway(Canvas canvas, Doorway door, Palette accent) {
        int lintel = Math.min(AP_MAX_Y + 1, canvas.sizeY - 2);
        boolean northSouth = door.side().getAxis() == Direction.Axis.Z;
        for (int along = AP_MIN - 1; along <= AP_MAX + 1; along++) {
            int x = door.x(along, 0);
            int z = door.z(along, 0);
            for (int y = AP_MIN_Y; y <= lintel; y++) {
                boolean jamb = along < AP_MIN || along > AP_MAX || y > AP_MAX_Y;
                canvas.set(x, y, z, jamb ? WHITE.plain() : accent.roundel(northSouth ? x : z));
            }
        }
    }

    /**
     * Quartz cove: top-half quartz stairs against every wall at {@code y}, as in the console room. Placement updates
     * the corner shapes. Doorways keep their inner 4×4 clear.
     */
    private static void cove(Canvas canvas, int y, Doorway... doors) {
        int maxX = canvas.sizeX - 2;
        int maxZ = canvas.sizeZ - 2;
        for (int x = 1; x <= maxX; x++) {
            for (int z = 1; z <= maxZ; z++) {
                Direction facing = x == 1 ? Direction.WEST : x == maxX ? Direction.EAST
                        : z == 1 ? Direction.NORTH : z == maxZ ? Direction.SOUTH : null;
                if (facing == null || !canvas.get(x, y, z).isAir()) {
                    continue;
                }
                boolean clear = false;
                for (Doorway door : doors) {
                    clear |= door.innerZone(x, y, z);
                }
                if (!clear) {
                    canvas.set(x, y, z, stairs(Blocks.QUARTZ_STAIRS, facing, Half.TOP));
                }
            }
        }
    }

    private static void ceilingVents(Canvas canvas, int x0, int x1, int z0, int z1, int y) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                canvas.set(x, y, z, decor(DWMBlocks.TARDIS_CEILING_VENT, Direction.NORTH));
            }
        }
    }

    /** Lantern hanging {@code chainLength} chains below the ceiling at {@code ceilingY}. */
    private static void hangingLantern(Canvas canvas, int x, int z, int ceilingY, int chainLength) {
        for (int i = 1; i <= chainLength; i++) {
            canvas.set(x, ceilingY - i, z, Blocks.IRON_CHAIN.defaultBlockState());
        }
        canvas.set(x, ceilingY - chainLength - 1, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    /** Two-block bed with its foot at {@code (x, y, z)} and its head one block toward {@code facing}. */
    private static void bed(Canvas canvas, int x, int y, int z, Direction facing, DyeColor color) {
        BlockState foot = Blocks.BED.pick(color).defaultBlockState()
                .setValue(BedBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.FOOT);
        canvas.set(x, y, z, foot);
        canvas.set(x + facing.getStepX(), y, z + facing.getStepZ(), foot.setValue(BedBlock.PART, BedPart.HEAD));
    }

    private static BlockState decor(Block block, Direction facing) {
        return block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
    }

    private static BlockState slab(Block block, SlabType type) {
        return block.defaultBlockState().setValue(SlabBlock.TYPE, type);
    }

    private static BlockState stairs(Block block, Direction facing, Half half) {
        return block.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }

    /**
     * A {@code sizeX × sizeY × sizeZ} grid of block states ({@code null} = structure void) that converts to a
     * {@link TemplateLayout}. Legend symbols are assigned in scan order from U+0100 upward, so the output is
     * deterministic and never runs out of symbols.
     */
    static final class Canvas {
        final int sizeX;
        final int sizeY;
        final int sizeZ;
        private final BlockState[][][] blocks;

        Canvas(int sizeX, int sizeY, int sizeZ) {
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.blocks = new BlockState[sizeY][sizeZ][sizeX];
        }

        void set(int x, int y, int z, BlockState state) {
            if (x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) {
                throw new IndexOutOfBoundsException("(" + x + ", " + y + ", " + z + ") is outside "
                        + sizeX + "x" + sizeY + "x" + sizeZ);
            }
            blocks[y][z][x] = state;
        }

        BlockState get(int x, int y, int z) {
            BlockState state = blocks[y][z][x];
            return state == null ? AIR : state;
        }

        TemplateLayout build(String path) {
            Map<BlockState, Character> symbols = new LinkedHashMap<>();
            TemplateLayout.Builder builder = TemplateLayout.builder(path);
            for (int y = 0; y < sizeY; y++) {
                String[] rows = new String[sizeZ];
                for (int z = 0; z < sizeZ; z++) {
                    StringBuilder row = new StringBuilder(sizeX);
                    for (int x = 0; x < sizeX; x++) {
                        BlockState state = blocks[y][z][x];
                        row.append(state == null ? ' '
                                : symbols.computeIfAbsent(state, s -> (char) (0x100 + symbols.size())));
                    }
                    rows[z] = row.toString();
                }
                builder.layer(rows);
            }
            symbols.forEach((state, symbol) -> builder.legend(symbol, state));
            return builder.build();
        }
    }
}
