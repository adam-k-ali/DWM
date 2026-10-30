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

    public static final String SEGMENT_POOL = "dwm:skaro/dalek_trench/segments";
    public static final String BUNKER_POOL = "dwm:skaro/dalek_trench/bunkers";
    public static final String LINK = "dwm:dalek_trench_link";
    public static final String BUNKER_LINK = "dwm:dalek_trench_bunker";

    private DalekTrenchTemplates() {
    }

    public static List<TemplateLayout> all() {
        return List.of(checkpoint(), segment(), junction(), bunker(), endCap());
    }

    /** 11×5×11 walled yard with the structure's only loot chest and a 3-wide gate on the north and south faces. */
    public static TemplateLayout checkpoint() {
        return base(CHECKPOINT)
                .legend('n', jigsaw(FrontAndTop.NORTH_UP), segmentConnector())
                .legend('o', jigsaw(FrontAndTop.SOUTH_UP), segmentConnector())
                .legend('C', Blocks.CHEST.defaultBlockState()
                        .setValue(ChestBlock.FACING, Direction.SOUTH),
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
                // y = 1: perimeter wall, gates, pillars and the chest
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
                // y = 2: grille windows on the side walls
                .layer(
                        "rSSS...SSSr",
                        "S.........S",
                        "S.........S",
                        "S..r...r..S",
                        "S.........S",
                        "I.........I",
                        "S.........S",
                        "S..r...r..S",
                        "S.........S",
                        "S.........S",
                        "rSSS...SSSr")
                // y = 3
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
                // y = 4: riveted cap with lamps on the pillars; the gates stay open to the sky
                .layer(
                        "rRRR...RRRr",
                        "R.........R",
                        "R.........R",
                        "R..L...L..R",
                        "R.........R",
                        "R.........R",
                        "R.........R",
                        "R..L...L..R",
                        "R.........R",
                        "R.........R",
                        "rRRR...RRRr")
                .build();
    }

    /** 5×4×8 straight trench: a three-wide lane between three-high parapets with iron-bar gunports. */
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
                        "I...I",
                        "S...S",
                        "S...S",
                        "S...S",
                        "I...I",
                        "S...S",
                        "r...r")
                .layer(
                        "r...r",
                        "R...R",
                        "R...R",
                        "R...R",
                        "R...R",
                        "R...R",
                        "R...R",
                        "r...r")
                .build();
    }

    /** 5×4×8 trench segment with a doorway in each side wall that opens a bunker. */
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
                        "R...R",
                        "R...R",
                        "R...R",
                        "R...R",
                        "R...R",
                        "R...R",
                        "r...r")
                .build();
    }

    /** 9×6×9 roofed bunker with gunports on its north face and a 3-wide doorway on the south face. */
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
                        "rSSSSSSSr",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "S.......S",
                        "rSSSSSSSr")
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
                        "rRRRr",
                        "R...R",
                        "r...r")
                .build();
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
