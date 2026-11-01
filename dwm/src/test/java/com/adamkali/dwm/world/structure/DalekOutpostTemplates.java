package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Layouts for the modular Dalek outpost (DWM-069). Regenerate the committed templates with
 * {@code ./gradlew generateStructureTemplates}; {@link StructureTemplateGeneratorTest} fails if they drift.
 *
 * <p>The core checkpoint owns the outpost's only loot chest and three outward floor connectors
 * (north, east, west). Each wing piece has one connector on its south edge that faces back toward
 * the core, so jigsaw rotates wings to sit flush against the core's apron.
 */
public final class DalekOutpostTemplates {
    public static final String CORE = "skaro/dalek_outpost/core";
    public static final String WATCHTOWER = "skaro/dalek_outpost/watchtower";
    public static final String BARRICADE = "skaro/dalek_outpost/barricade";
    public static final String SENSOR_MAST = "skaro/dalek_outpost/sensor_mast";

    public static final String WING_POOL = "dwm:skaro/dalek_outpost/wings";
    public static final String WING_JIGSAW_NAME = "dwm:dalek_outpost_wing";
    public static final String LOOT_TABLE = "dwm:chests/skaro/dalek_outpost";

    private DalekOutpostTemplates() {
    }

    public static List<TemplateLayout> all() {
        return List.of(core(), watchtower(), barricade(), sensorMast());
    }

    /** 11×7×11 checkpoint room on a one-block apron; 3-wide doorway on the south face. */
    public static TemplateLayout core() {
        return palette(CORE)
                .legend('C', Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), lootChest())
                .legend('n', jigsaw(FrontAndTop.NORTH_UP), coreConnector())
                .legend('e', jigsaw(FrontAndTop.EAST_UP), coreConnector())
                .legend('w', jigsaw(FrontAndTop.WEST_UP), coreConnector())
                // y = 0: floor and apron with connectors on the north, east and west edges
                .layer(
                        "BBBBBnBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "wBBBBBBBBBe",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB",
                        "BBBBBBBBBBB")
                // y = 1: walls, doorway, loot chest; apron carved so the approach stays walkable
                .layer(
                        "...........",
                        ".rSSSSSSSr.",
                        ".S.......S.",
                        ".S...C...S.",
                        ".S.......S.",
                        ".S.......S.",
                        ".S.......S.",
                        ".S.......S.",
                        ".S.......S.",
                        ".rSS...SSr.",
                        "...........")
                // y = 2: grille windows on the north, east and west faces
                .layer(
                        "...........",
                        ".rSSSISSSr.",
                        ".S.......S.",
                        ".S.......S.",
                        ".S.......S.",
                        ".I.......I.",
                        ".S.......S.",
                        ".S.......S.",
                        ".S.......S.",
                        ".rSS...SSr.",
                        "...........")
                // y = 3: lintel over the doorway
                .layer(
                        "           ",
                        " rSSSSSSSr ",
                        " S.......S ",
                        " S.......S ",
                        " S.......S ",
                        " S.......S ",
                        " S.......S ",
                        " S.......S ",
                        " S.......S ",
                        " rSSSSSSSr ",
                        "           ")
                // y = 4: riveted band
                .layer(
                        "           ",
                        " rRRRRRRRr ",
                        " R.......R ",
                        " R.......R ",
                        " R.......R ",
                        " R.......R ",
                        " R.......R ",
                        " R.......R ",
                        " R.......R ",
                        " rRRRRRRRr ",
                        "           ")
                // y = 5: roof with recessed lights
                .layer(
                        "           ",
                        " SSSSSSSSS ",
                        " SSSSSSSSS ",
                        " SSLSSSLSS ",
                        " SSSSSSSSS ",
                        " SSSSLSSSS ",
                        " SSSSSSSSS ",
                        " SSLSSSLSS ",
                        " SSSSSSSSS ",
                        " SSSSSSSSS ",
                        "           ")
                // y = 6: roof parapet
                .layer(
                        "           ",
                        " rIIIIIIIr ",
                        " I.......I ",
                        " I.......I ",
                        " I.......I ",
                        " I.......I ",
                        " I.......I ",
                        " I.......I ",
                        " I.......I ",
                        " rIIIIIIIr ",
                        "           ")
                .build();
    }

    /** 5×14×5 observation post: open legs, ladder up a bronze pillar, railed platform and roof. */
    public static TemplateLayout watchtower() {
        TemplateLayout.Builder builder = palette(WATCHTOWER)
                .legend('s', jigsaw(FrontAndTop.SOUTH_UP), wingConnector())
                .legend('H', Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH))
                .layer(
                        "BBBBB",
                        "BBBBB",
                        "BBBBB",
                        "BBBBB",
                        "BBsBB");
        for (int y = 1; y <= 7; y++) {
            builder.layer(
                    "r...r",
                    "..r..",
                    "..H..",
                    ".....",
                    "r...r");
        }
        return builder
                // y = 8: platform; the ladder continues through a hatch against the bronze pillar
                .layer(
                        "SSSSS",
                        "SSrSS",
                        "SSHSS",
                        "SSSSS",
                        "SSSSS")
                // y = 9-12: railing and four blocks of headroom (a Dalek is taller than two blocks)
                .layer(
                        "rIIIr",
                        "I...I",
                        "I...I",
                        "I...I",
                        "rIIIr")
                .layer(
                        "r...r",
                        ".....",
                        ".....",
                        ".....",
                        "r...r")
                .layer(
                        "r...r",
                        ".....",
                        ".....",
                        ".....",
                        "r...r")
                .layer(
                        "r...r",
                        ".....",
                        ".....",
                        ".....",
                        "r...r")
                // y = 13: roof with a lamp
                .layer(
                        "SSSSS",
                        "SSSSS",
                        "SSLSS",
                        "SSSSS",
                        "SSSSS")
                .build();
    }

    /** 7×3×3 low barricade that follows the terrain (terrain_matching projection). */
    public static TemplateLayout barricade() {
        return palette(BARRICADE)
                .legend('s', jigsaw(FrontAndTop.SOUTH_UP), wingConnector())
                .layer(
                        "BBBBBBB",
                        "BBBBBBB",
                        "BBBsBBB")
                .layer(
                        "rSSRSSr",
                        ".......",
                        ".......")
                .layer(
                        "rIIrIIr",
                        ".......",
                        ".......")
                .build();
    }

    /** 3×8×3 sensor mast: a pad, a riveted plinth, a chain mast with a crossbar, lamp and rod. */
    public static TemplateLayout sensorMast() {
        return palette(SENSOR_MAST)
                .legend('s', jigsaw(FrontAndTop.SOUTH_UP), wingConnector())
                .legend('K', Blocks.IRON_CHAIN.defaultBlockState())
                .legend('T', Blocks.LIGHTNING_ROD.waxed().oxidized().defaultBlockState())
                .layer("BBB", "BBB", "BsB")
                .layer("...", ".r.", "...")
                .layer("   ", " r ", "   ")
                .layer("   ", " K ", "   ")
                .layer("   ", "IKI", "   ")
                .layer("   ", " K ", "   ")
                .layer("   ", " L ", "   ")
                .layer("   ", " T ", "   ")
                .build();
    }

    /** Shared Dalek palette: bronze floors and structure, silver walls (the ruin processor rots silver). */
    static TemplateLayout.Builder palette(String path) {
        return TemplateLayout.builder(path)
                .legend('.', Blocks.AIR.defaultBlockState())
                .legend('B', DWMBlocks.BRONZE_DALEKANIUM_PANEL.defaultBlockState())
                .legend('r', DWMBlocks.BRONZE_DALEKANIUM_RIVETED_WALL.defaultBlockState())
                .legend('S', DWMBlocks.SILVER_DALEKANIUM_PANEL.defaultBlockState())
                .legend('R', DWMBlocks.SILVER_DALEKANIUM_RIVETED_WALL.defaultBlockState())
                .legend('I', Blocks.IRON_BARS.defaultBlockState())
                .legend('L', Blocks.SEA_LANTERN.defaultBlockState());
    }

    static BlockState jigsaw(FrontAndTop orientation) {
        return Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, orientation);
    }

    static CompoundTag lootChest() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", "minecraft:chest");
        nbt.putString("LootTable", LOOT_TABLE);
        return nbt;
    }

    /** Core-side connector: pulls a wing from the wing pool and becomes floor once placed. */
    private static CompoundTag coreConnector() {
        return jigsawNbt("minecraft:empty", WING_JIGSAW_NAME, WING_POOL);
    }

    /** Wing-side connector: the target of a core connector; it opens no further pieces. */
    private static CompoundTag wingConnector() {
        return jigsawNbt(WING_JIGSAW_NAME, "minecraft:empty", "minecraft:empty");
    }

    static CompoundTag jigsawNbt(String name, String target, String pool) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", "minecraft:jigsaw");
        nbt.putString("name", name);
        nbt.putString("target", target);
        nbt.putString("pool", pool);
        nbt.putString("final_state", blockId(DWMBlocks.BRONZE_DALEKANIUM_PANEL));
        nbt.putString("joint", "aligned");
        nbt.putInt("placement_priority", 0);
        nbt.putInt("selection_priority", 0);
        return nbt;
    }

    private static String blockId(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }
}
