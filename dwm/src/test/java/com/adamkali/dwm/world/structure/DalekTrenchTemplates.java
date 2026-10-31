package com.adamkali.dwm.world.structure;

import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Layouts for the Dalek battlefield trench (DWM-069). Regenerate the committed templates with
 * {@code ./gradlew generateStructureTemplates}; {@link StructureTemplateGeneratorTest} fails if they drift.
 *
 * <p>A trench is a chain: a walled checkpoint yard opens a trench north and south; straight segments
 * (some with side doorways to bunkers) follow the terrain until an end cap. Every piece's open end
 * is a {@code SOUTH_UP} connector named {@link #LINK}, so jigsaw rotates pieces to line up. Only the
 * checkpoint holds a loot chest.
 */
public final class DalekTrenchTemplates {
    public static final String CHECKPOINT = "skaro/dalek_trench/checkpoint";
    public static final String SEGMENT = "skaro/dalek_trench/segment";
    public static final String JUNCTION = "skaro/dalek_trench/junction";
    public static final String BUNKER = "skaro/dalek_trench/bunker";
    public static final String END_CAP = "skaro/dalek_trench/end_cap";
    public static final String ZIGZAG = "skaro/dalek_trench/zigzag";
    public static final String ZIGZAG_MIRRORED = "skaro/dalek_trench/zigzag_mirrored";

    /** Footprint of a zig-zag piece; entry (south) and exit (north) lanes are centred on {@link #ZIGZAG_LANE_X}. */
    public static final int ZIGZAG_WIDTH = 9;
    public static final int ZIGZAG_DEPTH = 18;
    public static final int ZIGZAG_LANE_X = 2;
    /**
     * Lane cells of a zig-zag piece, north (z = 0) to south. A bay on the west, a traverse east, a bay on the east, a
     * traverse back west and a last bay: the lane turns four times, so a blast or a shot cannot run down the line.
     */
    private static final String[] ZIGZAG_PLAN = {
            " ...     ",
            " ...     ",
            " ....... ",
            " ....... ",
            " ....... ",
            "     ... ",
            "     ... ",
            "     ... ",
            "     ... ",
            "     ... ",
            "     ... ",
            " ....... ",
            " ....... ",
            " ....... ",
            " ...     ",
            " ...     ",
            " ...     ",
            " ...     "
    };

    public static final String SEGMENT_POOL = "dwm:skaro/dalek_trench/segments";
    public static final String BUNKER_POOL = "dwm:skaro/dalek_trench/bunkers";
    public static final String LINK = "dwm:dalek_trench_link";
    public static final String BUNKER_LINK = "dwm:dalek_trench_bunker";

    private DalekTrenchTemplates() {
    }

    public static List<TemplateLayout> all() {
        return List.of(checkpoint(), segment(), junction(), bunker(), endCap(), zigzag(ZIGZAG, false), zigzag(ZIGZAG_MIRRORED, true));
    }

    /**
     * 11×5×11 sunken yard with the structure's only loot chest and a 3-wide, 3-high gate on the north and south
     * faces. Layers 1-2 are underground lining; the parapet (layer 3) sits at ground level with gaps to step in by.
     */
    public static TemplateLayout checkpoint() {
        return base(CHECKPOINT)
                .legend('n', jigsaw(FrontAndTop.NORTH_UP), segmentConnector())
                .legend('o', jigsaw(FrontAndTop.SOUTH_UP), segmentConnector())
                .legend('C', Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH),
                        DalekOutpostTemplates.lootChest())
                // y = 0: floor, connectors in the middle of the north and south gates
                .layer(
                        "BBBBBnBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBoBBBBB")
                // y = 1: underground lining, gates, pillars and the chest
                .layer(
                        "rSSS...SSSr",
                        "S.........S",
                        "S.........S",
                        "S..r...r..S",
                        "S.........S",
                        "S....C....S",
                        "S.........S",
                        "S..r...r..S",
                        "S.........S",
                        "S.........S",
                        "rSSS...SSSr")
                // y = 2: lining continues to ground level
                .layer(
                        "rSSS...SSSr",
                        "S.........S",
                        "S.........S",
                        "S..r...r..S",
                        "S.........S",
                        "S.........S",
                        "S.........S",
                        "S..r...r..S",
                        "S.........S",
                        "S.........S",
                        "rSSS...SSSr")
                // y = 3: ground-level parapet: grille windows with open gaps on the side walls
                .layer(
                        "rISI...ISIr",
                        "I.........I",
                        "S.........S",
                        "S..r...r..S",
                        "...........",
                        "I.........I",
                        "...........",
                        "S..r...r..S",
                        "S.........S",
                        "I.........I",
                        "rISI...ISIr")
                // y = 4: lamps on the pillars and corner posts; the yard stays open to the sky
                .layer(
                        "r.........r",
                        "...........",
                        "...........",
                        "...L...L...",
                        "...........",
                        "...........",
                        "...........",
                        "...L...L...",
                        "...........",
                        "...........",
                        "r.........r")
                .build();
    }

    /** 5×4×8 straight trench: a three-wide lane lined underground, with a ground-level bar parapet and gaps to drop in by. */
    public static TemplateLayout segment() {
        return base(SEGMENT)
                .legend('n', jigsaw(FrontAndTop.NORTH_UP), segmentConnector())
                .legend('s', jigsaw(FrontAndTop.SOUTH_UP), linkConnector())
                .layer(
                        "BBnBB",
                        "BBBBB",
                        "BBBBB",
                        "BBBBB",
                        "BBBBB",
                        "BBBBB",
                        "BBBBB",
                        "BBsBB")
                .layer(
                        "r...r",
                        "S...S",
                        "S...S",
                        "S...S",
                        "S...S",
                        "S...S",
                        "S...S",
                        "r...r")
                .layer(
                        "r...r",
                        "S...S",
                        "S...S",
                        "S...S",
                        "S...S",
                        "S...S",
                        "S...S",
                        "r...r")
                .layer(
                        "r...r",
                        "I...I",
                        "S...S",
                        ".....",
                        ".....",
                        "S...S",
                        "I...I",
                        "r...r")
                .build();
    }

    /** 5×4×8 trench segment with a 3-high doorway in each side wall that opens a bunker. */
    public static TemplateLayout junction() {
        return base(JUNCTION)
                .legend('n', jigsaw(FrontAndTop.NORTH_UP), segmentConnector())
                .legend('s', jigsaw(FrontAndTop.SOUTH_UP), linkConnector())
                .legend('e', jigsaw(FrontAndTop.EAST_UP), bunkerConnector())
                .legend('w', jigsaw(FrontAndTop.WEST_UP), bunkerConnector())
                .layer(
                        "BBnBB",
                        "BBBBB",
                        "BBBBB",
                        "wBBBe",
                        "BBBBB",
                        "BBBBB",
                        "BBBBB",
                        "BBsBB")
                .layer(
                        "r...r",
                        "S...S",
                        "S...S",
                        ".....",
                        "S...S",
                        "S...S",
                        "S...S",
                        "r...r")
                .layer(
                        "r...r",
                        "S...S",
                        "S...S",
                        ".....",
                        "S...S",
                        "S...S",
                        "S...S",
                        "r...r")
                .layer(
                        "r...r",
                        "I...I",
                        "S...S",
                        ".....",
                        "S...S",
                        "S...S",
                        "I...I",
                        "r...r")
                .build();
    }

    /** 9×6×9 half-buried bunker: gunports at ground level on the north face, a 3-wide, 3-high doorway on the south face. */
    public static TemplateLayout bunker() {
        return base(BUNKER)
                .legend('s', jigsaw(FrontAndTop.SOUTH_UP), bunkerLinkConnector())
                .layer(
                        "BBBBBBBBB",
                        "BBBBBBBBB",
                        "BBBBBBBBB",
                        "BBBBBBBBB",
                        "BBBBBBBBB",
                        "BBBBBBBBB",
                        "BBBBBBBBB",
                        "BBBBBBBBB",
                        "BBBBsBBBB")
                .layer(
                        "rSSSSSSSr",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "rSS...SSr")
                .layer(
                        "rSSSSSSSr",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "rSS...SSr")
                .layer(
                        "rSISISISr",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "rSS...SSr")
                .layer(
                        "rRRRRRRRr",
                        "R.......R",
                        "R.......R",
                        "R.......R",
                        "R.......R",
                        "R.......R",
                        "R.......R",
                        "R.......R",
                        "rRRRRRRRr")
                .layer(
                        "SSSSSSSSS",
                        "SSSSSSSSS",
                        "SSLSSSLSS",
                        "SSSSSSSSS",
                        "SSSSLSSSS",
                        "SSSSSSSSS",
                        "SSLSSSLSS",
                        "SSSSSSSSS",
                        "SSSSSSSSS")
                .build();
    }

    /** 5×4×3 closed trench end; its open side faces the trench. */
    public static TemplateLayout endCap() {
        return base(END_CAP)
                .legend('s', jigsaw(FrontAndTop.SOUTH_UP), linkConnector())
                .layer(
                        "BBBBB",
                        "BBBBB",
                        "BBsBB")
                .layer(
                        "rSSSr",
                        "S...S",
                        "r...r")
                .layer(
                        "rSSSr",
                        "S...S",
                        "r...r")
                .layer(
                        "rISIr",
                        "I...I",
                        "r...r")
                .build();
    }

    /**
     * 9×4×18 zig-zag trench: three bays joined by two traverses, entering at the south end and leaving at the north end
     * on the same axis so it chains like a straight segment. Only the lane and a one-block lining around it are
     * placed; the rest is structure void so terrain stays. The mirror flips the plan east to west.
     */
    public static TemplateLayout zigzag(String path, boolean mirrored) {
        int laneX = mirrored ? ZIGZAG_WIDTH - 1 - ZIGZAG_LANE_X : ZIGZAG_LANE_X;
        TemplateLayout.Builder builder = base(path)
                .legend('n', jigsaw(FrontAndTop.NORTH_UP), segmentConnector())
                .legend('s', jigsaw(FrontAndTop.SOUTH_UP), linkConnector());
        for (int layer = 0; layer < 4; layer++) {
            String[] rows = new String[ZIGZAG_DEPTH];
            for (int z = 0; z < ZIGZAG_DEPTH; z++) {
                StringBuilder row = new StringBuilder();
                for (int x = 0; x < ZIGZAG_WIDTH; x++) {
                    row.append(zigzagCell(layer, x, z, mirrored, laneX));
                }
                rows[z] = row.toString();
            }
            builder.layer(rows);
        }
        return builder.build();
    }

    private static char zigzagCell(int layer, int x, int z, boolean mirrored, int laneX) {
        boolean endRow = z == 0 || z == ZIGZAG_DEPTH - 1;
        if (zigzagLane(x, z, mirrored)) {
            if (layer == 0) {
                return x == laneX && endRow ? (z == 0 ? 'n' : 's') : 'B';
            }
            return '.';
        }
        boolean lining = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                lining |= zigzagLane(x + dx, z + dz, mirrored);
            }
        }
        if (!lining) {
            return ' ';
        }
        return switch (layer) {
            case 0 -> 'B';
            case 1, 2 -> endRow ? 'r' : 'S';
            // Ground-level parapet: bars and solid blocks with an open gap every fourth cell to drop in by.
            default -> endRow ? 'r' : new char[] {'I', 'S', 'S', '.'}[(x + z) % 4];
        };
    }

    /** True when plan cell (x, z) is lane; cells outside the footprint are not. */
    public static boolean zigzagLane(int x, int z, boolean mirrored) {
        if (x < 0 || x >= ZIGZAG_WIDTH || z < 0 || z >= ZIGZAG_DEPTH) {
            return false;
        }
        int planX = mirrored ? ZIGZAG_WIDTH - 1 - x : x;
        return ZIGZAG_PLAN[z].charAt(planX) == '.';
    }

    private static TemplateLayout.Builder base(String path) {
        return DalekOutpostTemplates.palette(path);
    }

    private static BlockState jigsaw(FrontAndTop orientation) {
        return DalekOutpostTemplates.jigsaw(orientation);
    }

    /** Parent-side connector: pulls the next trench piece from the segment pool. */
    private static CompoundTag segmentConnector() {
        return DalekOutpostTemplates.jigsawNbt("minecraft:empty", LINK, SEGMENT_POOL);
    }

    /** Parent-side connector: pulls a bunker from the bunker pool. */
    private static CompoundTag bunkerConnector() {
        return DalekOutpostTemplates.jigsawNbt("minecraft:empty", BUNKER_LINK, BUNKER_POOL);
    }

    /** Child-side connector: the target of a segment connector; it opens no further pieces. */
    private static CompoundTag linkConnector() {
        return DalekOutpostTemplates.jigsawNbt(LINK, "minecraft:empty", "minecraft:empty");
    }

    private static CompoundTag bunkerLinkConnector() {
        return DalekOutpostTemplates.jigsawNbt(BUNKER_LINK, "minecraft:empty", "minecraft:empty");
    }
}
