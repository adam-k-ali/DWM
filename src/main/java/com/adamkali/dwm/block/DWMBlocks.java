package com.adamkali.dwm.block;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.ColoredFallingBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SignBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ColorCode;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class DWMBlocks {
    public static final Block BLACK_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "black_roundel_a");
    public static final Block BLUE_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "blue_roundel_a");
    public static final Block BROWN_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "brown_roundel_a");
    public static final Block CYAN_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "cyan_roundel_a");
    public static final Block GREEN_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "green_roundel_a");
    public static final Block LIGHT_BLUE_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "light_blue_roundel_a");
    public static final Block LIGHT_GRAY_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "light_gray_roundel_a");
    public static final Block LIME_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "lime_roundel_a");
    public static final Block MAGENTA_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "magenta_roundel_a");
    public static final Block ORANGE_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "orange_roundel_a");
    public static final Block PINK_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "pink_roundel_a");
    public static final Block RED_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "red_roundel_a");
    public static final Block WHITE_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "white_roundel_a");
    public static final Block YELLOW_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "yellow_roundel_a");
    public static final Block GRAY_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "gray_roundel_a");
    public static final Block PURPLE_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "purple_roundel_a");
    public static final Block TEAL_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "teal_roundel_a");

    public static final Block BLACK_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "black_roundel_b");
    public static final Block BLUE_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "blue_roundel_b");
    public static final Block BROWN_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "brown_roundel_b");
    public static final Block CYAN_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "cyan_roundel_b");
    public static final Block GREEN_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "green_roundel_b");
    public static final Block LIGHT_BLUE_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "light_blue_roundel_b");
    public static final Block LIGHT_GRAY_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "light_gray_roundel_b");
    public static final Block LIME_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "lime_roundel_b");
    public static final Block MAGENTA_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "magenta_roundel_b");
    public static final Block ORANGE_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "orange_roundel_b");
    public static final Block PINK_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "pink_roundel_b");
    public static final Block RED_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "red_roundel_b");
    public static final Block WHITE_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "white_roundel_b");
    public static final Block YELLOW_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "yellow_roundel_b");
    public static final Block GRAY_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "gray_roundel_b");
    public static final Block PURPLE_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "purple_roundel_b");
    public static final Block TEAL_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.nonOpaque(), "teal_roundel_b");

    public static final Block BLACK_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "black_big_roundel_a");
    public static final Block BLUE_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "blue_big_roundel_a");
    public static final Block BROWN_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "brown_big_roundel_a");
    public static final Block CYAN_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "cyan_big_roundel_a");
    public static final Block GREEN_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "green_big_roundel_a");
    public static final Block LIGHT_BLUE_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "light_blue_big_roundel_a");
    public static final Block LIGHT_GRAY_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "light_gray_big_roundel_a");
    public static final Block LIME_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "lime_big_roundel_a");
    public static final Block MAGENTA_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "magenta_big_roundel_a");
    public static final Block ORANGE_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "orange_big_roundel_a");
    public static final Block PINK_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "pink_big_roundel_a");
    public static final Block RED_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "red_big_roundel_a");
    public static final Block WHITE_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "white_big_roundel_a");
    public static final Block YELLOW_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "yellow_big_roundel_a");
    public static final Block GRAY_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "gray_big_roundel_a");
    public static final Block PURPLE_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "purple_big_roundel_a");
    public static final Block TEAL_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "teal_big_roundel_a");

    public static final Block BLACK_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "black_big_roundel_b");
    public static final Block BLUE_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "blue_big_roundel_b");
    public static final Block BROWN_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "brown_big_roundel_b");
    public static final Block CYAN_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "cyan_big_roundel_b");
    public static final Block GREEN_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "green_big_roundel_b");
    public static final Block LIGHT_BLUE_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "light_blue_big_roundel_b");
    public static final Block LIGHT_GRAY_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "light_gray_big_roundel_b");
    public static final Block LIME_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "lime_big_roundel_b");
    public static final Block MAGENTA_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "magenta_big_roundel_b");
    public static final Block ORANGE_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "orange_big_roundel_b");
    public static final Block PINK_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "pink_big_roundel_b");
    public static final Block RED_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "red_big_roundel_b");
    public static final Block WHITE_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "white_big_roundel_b");
    public static final Block YELLOW_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "yellow_big_roundel_b");
    public static final Block GRAY_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "gray_big_roundel_b");
    public static final Block PURPLE_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "purple_big_roundel_b");
    public static final Block TEAL_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "teal_big_roundel_b");

    public static final Block BLACK_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "black_tardis_wall");
    public static final Block BLUE_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "blue_tardis_wall");
    public static final Block BROWN_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "brown_tardis_wall");
    public static final Block CYAN_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "cyan_tardis_wall");
    public static final Block GREEN_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "green_tardis_wall");
    public static final Block LIGHT_BLUE_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "light_blue_tardis_wall");
    public static final Block LIGHT_GRAY_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "light_gray_tardis_wall");
    public static final Block LIME_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "lime_tardis_wall");
    public static final Block MAGENTA_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "magenta_tardis_wall");
    public static final Block ORANGE_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "orange_tardis_wall");
    public static final Block PINK_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "pink_tardis_wall");
    public static final Block RED_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "red_tardis_wall");
    public static final Block WHITE_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "white_tardis_wall");
    public static final Block YELLOW_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "yellow_tardis_wall");
    public static final Block GRAY_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "gray_tardis_wall");
    public static final Block PURPLE_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "purple_tardis_wall");
    public static final Block TEAL_TARDIS_WALL = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS, "teal_tardis_wall");

    public static final Block BLACK_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "black_chronoplasm_powder");
    public static final Block BLUE_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "blue_chronoplasm_powder");
    public static final Block BROWN_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "brown_chronoplasm_powder");
    public static final Block CYAN_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "cyan_chronoplasm_powder");
    public static final Block GREEN_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "green_chronoplasm_powder");
    public static final Block LIGHT_BLUE_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "light_blue_chronoplasm_powder");
    public static final Block LIGHT_GRAY_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "light_gray_chronoplasm_powder");
    public static final Block LIME_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "lime_chronoplasm_powder");
    public static final Block MAGENTA_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "magenta_chronoplasm_powder");
    public static final Block ORANGE_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "orange_chronoplasm_powder");
    public static final Block PINK_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "pink_chronoplasm_powder");
    public static final Block RED_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "red_chronoplasm_powder");
    public static final Block WHITE_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "white_chronoplasm_powder");
    public static final Block YELLOW_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "yellow_chronoplasm_powder");
    public static final Block GRAY_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "gray_chronoplasm_powder");
    public static final Block PURPLE_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "purple_chronoplasm_powder");
    public static final Block TEAL_CHRONOPLASM_POWDER = register(Block::new, DWMBlockSettings.CHRONOPLASM_POWDER_SETTINGS, "teal_chronoplasm_powder");

    public static final Block TARDIS_BLOCK = register(TardisBlock::new, DWMBlockSettings.TARDIS_BLOCK, "tardis_block");

    public static final Block TARDIS_INTERIOR_DOOR = register(TardisInteriorDoorBlock::new, DWMBlockSettings.TARDIS_INTERIOR_DOOR, "tardis_interior_door");

    public static final Block FIRST_DOCTOR_CONSOLE = register(FirstDoctorConsoleBlock::new, DWMBlockSettings.FIRST_DOCTOR_CONSOLE, "first_doctor_console");

    public static final Block TARDIS_DOOR_BUTTON = register(TardisButtonBlock::new, DWMBlockSettings.BUTTON_SETTINGS, "tardis_door_button");

    public static final Block GALLIFREY_STONE = register(Block::new, DWMBlockSettings.GALLIFREY_STONE, "gallifrey_stone");
    public static final Block GALLIFREY_STONE_BRICKS = register(Block::new, DWMBlockSettings.GALLIFREY_STONE, "gallifrey_stone_bricks");
    public static final Block CHISELED_GALLIFREY_STONE_BRICKS = register(Block::new, DWMBlockSettings.GALLIFREY_STONE, "chiseled_gallifrey_stone_bricks");
    public static final Block CRACKED_GALLIFREY_STONE_BRICKS = register(Block::new, DWMBlockSettings.GALLIFREY_STONE, "cracked_gallifrey_stone_bricks");
    public static final Block MOSSY_GALLIFREY_STONE_BRICKS = register(Block::new, DWMBlockSettings.GALLIFREY_STONE, "mossy_gallifrey_stone_bricks");
    public static final Block GALLIFREY_COBBLESTONE = register(Block::new, DWMBlockSettings.GALLIFREY_STONE, "gallifrey_cobblestone");
    public static final Block GALLIFREY_MOSSY_COBBLESTONE = register(Block::new, DWMBlockSettings.GALLIFREY_STONE, "gallifrey_mossy_cobblestone");
    public static final Block GALLIFREY_SMOOTH_STONE = register(Block::new, DWMBlockSettings.GALLIFREY_STONE, "gallifrey_smooth_stone");
    public static final Block GALLIFREY_SANDSTONE = register(Block::new, DWMBlockSettings.GALLIFREY_SANDSTONE, "gallifrey_sandstone");
    public static final Block GALLIFREY_CUT_SANDSTONE = register(Block::new, DWMBlockSettings.GALLIFREY_SANDSTONE, "gallifrey_cut_sandstone");
    public static final Block GALLIFREY_CHISELED_SANDSTONE = register(Block::new, DWMBlockSettings.GALLIFREY_SANDSTONE, "gallifrey_chiseled_sandstone");
    public static final Block GALLIFREY_SAND = register(
            settings -> new ColoredFallingBlock(new ColorCode(0xC47A3A), settings),
            DWMBlockSettings.GALLIFREY_SAND,
            "gallifrey_sand"
    );
    public static final Block GALLIFREY_DIRT = register(Block::new, DWMBlockSettings.GALLIFREY_DIRT, "gallifrey_dirt");
    public static final Block GALLIFREY_COARSE_DIRT = register(Block::new, DWMBlockSettings.GALLIFREY_DIRT, "gallifrey_coarse_dirt");

    public static final Block ASH_PLANKS = register(Block::new, DWMBlockSettings.ASH_PLANKS, "ash_planks");
    public static final Block ASH_LOG = register(
            PillarBlock::new,
            DWMBlockSettings.ashLog(DWMBlockSettings.ASH_PLANKS_COLOR, DWMBlockSettings.ASH_BARK_COLOR),
            "ash_log"
    );
    public static final Block ASH_WOOD = register(
            PillarBlock::new,
            DWMBlockSettings.ashLog(DWMBlockSettings.ASH_BARK_COLOR, DWMBlockSettings.ASH_BARK_COLOR),
            "ash_wood"
    );
    public static final Block STRIPPED_ASH_LOG = register(
            PillarBlock::new,
            DWMBlockSettings.ashLog(DWMBlockSettings.ASH_PLANKS_COLOR, DWMBlockSettings.ASH_PLANKS_COLOR),
            "stripped_ash_log"
    );
    public static final Block STRIPPED_ASH_WOOD = register(
            PillarBlock::new,
            DWMBlockSettings.ashLog(DWMBlockSettings.ASH_PLANKS_COLOR, DWMBlockSettings.ASH_PLANKS_COLOR),
            "stripped_ash_wood"
    );
    public static final Block ASH_LEAVES = register(LeavesBlock::new, DWMBlockSettings.ASH_LEAVES, "ash_leaves");
    public static final Block ASH_SAPLING = register(
            settings -> new SaplingBlock(DWMSaplingGenerators.ASH, settings),
            DWMBlockSettings.ASH_SAPLING,
            "ash_sapling"
    );
    public static final Block POTTED_ASH_SAPLING = registerWithoutItem(
            settings -> new FlowerPotBlock(ASH_SAPLING, settings),
            DWMBlockSettings.ASH_FLOWER_POT,
            "potted_ash_sapling"
    );
    public static final Block ASH_STAIRS = register(
            settings -> new StairsBlock(ASH_PLANKS.getDefaultState(), settings),
            AbstractBlock.Settings.copyShallow(ASH_PLANKS),
            "ash_stairs"
    );
    public static final Block ASH_SLAB = register(SlabBlock::new, AbstractBlock.Settings.copyShallow(ASH_PLANKS), "ash_slab");
    public static final Block ASH_FENCE = register(FenceBlock::new, AbstractBlock.Settings.copyShallow(ASH_PLANKS), "ash_fence");
    public static final Block ASH_FENCE_GATE = register(
            settings -> new FenceGateBlock(DWMWoodTypes.ASH, settings),
            AbstractBlock.Settings.copyShallow(ASH_PLANKS),
            "ash_fence_gate"
    );
    public static final Block ASH_BUTTON = register(
            settings -> new ButtonBlock(DWMWoodTypes.ASH_SET, 30, settings),
            DWMBlockSettings.ASH_BUTTON,
            "ash_button"
    );
    public static final Block ASH_PRESSURE_PLATE = register(
            settings -> new PressurePlateBlock(DWMWoodTypes.ASH_SET, settings),
            DWMBlockSettings.ASH_PRESSURE_PLATE,
            "ash_pressure_plate"
    );
    public static final Block ASH_SIGN = registerWithoutItem(
            settings -> new SignBlock(DWMWoodTypes.ASH, settings),
            DWMBlockSettings.ASH_SIGN,
            "ash_sign"
    );
    public static final Block ASH_WALL_SIGN = registerWithoutItem(
            settings -> new WallSignBlock(DWMWoodTypes.ASH, settings),
            AbstractBlock.Settings.create()
                    .mapColor(DWMBlockSettings.ASH_PLANKS_COLOR)
                    .solid()
                    .instrument(NoteBlockInstrument.BASS)
                    .noCollision()
                    .strength(1.0F)
                    .burnable()
                    .lootTable(Optional.of(ASH_SIGN.getLootTableKey().orElseThrow()))
                    .overrideTranslationKey(ASH_SIGN.getTranslationKey()),
            "ash_wall_sign"
    );

    /** Building / stone-like Gallifrey blocks (excludes dirt/sand terrain). */
    public static final List<Block> GALLIFREY_STONE_BUILDING_BLOCKS = List.of(
            GALLIFREY_STONE,
            GALLIFREY_STONE_BRICKS,
            CHISELED_GALLIFREY_STONE_BRICKS,
            CRACKED_GALLIFREY_STONE_BRICKS,
            MOSSY_GALLIFREY_STONE_BRICKS,
            GALLIFREY_COBBLESTONE,
            GALLIFREY_MOSSY_COBBLESTONE,
            GALLIFREY_SMOOTH_STONE,
            GALLIFREY_SANDSTONE,
            GALLIFREY_CUT_SANDSTONE,
            GALLIFREY_CHISELED_SANDSTONE
    );

    /** Full Gallifrey stone-family set including terrain blocks. */
    public static final List<Block> GALLIFREY_STONE_FAMILY = List.of(
            GALLIFREY_STONE,
            GALLIFREY_STONE_BRICKS,
            CHISELED_GALLIFREY_STONE_BRICKS,
            CRACKED_GALLIFREY_STONE_BRICKS,
            MOSSY_GALLIFREY_STONE_BRICKS,
            GALLIFREY_COBBLESTONE,
            GALLIFREY_MOSSY_COBBLESTONE,
            GALLIFREY_SMOOTH_STONE,
            GALLIFREY_SANDSTONE,
            GALLIFREY_CUT_SANDSTONE,
            GALLIFREY_CHISELED_SANDSTONE,
            GALLIFREY_SAND,
            GALLIFREY_DIRT,
            GALLIFREY_COARSE_DIRT
    );

    public static final List<Block> ASH_LOGS = List.of(
            ASH_LOG,
            ASH_WOOD,
            STRIPPED_ASH_LOG,
            STRIPPED_ASH_WOOD
    );

    public static final List<Block> ASH_WOOD_BUILDING_BLOCKS = List.of(
            ASH_PLANKS,
            ASH_LOG,
            ASH_WOOD,
            STRIPPED_ASH_LOG,
            STRIPPED_ASH_WOOD,
            ASH_STAIRS,
            ASH_SLAB,
            ASH_FENCE,
            ASH_FENCE_GATE,
            ASH_BUTTON,
            ASH_PRESSURE_PLATE
    );

    public static final List<Block> ASH_WOOD_FAMILY = List.of(
            ASH_PLANKS,
            ASH_LOG,
            ASH_WOOD,
            STRIPPED_ASH_LOG,
            STRIPPED_ASH_WOOD,
            ASH_LEAVES,
            ASH_SAPLING,
            POTTED_ASH_SAPLING,
            ASH_STAIRS,
            ASH_SLAB,
            ASH_FENCE,
            ASH_FENCE_GATE,
            ASH_BUTTON,
            ASH_PRESSURE_PLATE,
            ASH_SIGN,
            ASH_WALL_SIGN
    );

    public static void initialize() {
        DWMWoodTypes.initialize();

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) ->
                !FirstDoctorConsoleBlock.isPlayerBreakDenied(state));

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (FirstDoctorConsoleBlock.isPlayerBreakDenied(world.getBlockState(pos))) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        StrippableBlockRegistry.register(ASH_LOG, STRIPPED_ASH_LOG);
        StrippableBlockRegistry.register(ASH_WOOD, STRIPPED_ASH_WOOD);

        FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
        flammable.add(ASH_PLANKS, 5, 20);
        flammable.add(ASH_SLAB, 5, 20);
        flammable.add(ASH_FENCE_GATE, 5, 20);
        flammable.add(ASH_FENCE, 5, 20);
        flammable.add(ASH_STAIRS, 5, 20);
        flammable.add(ASH_LOG, 5, 5);
        flammable.add(STRIPPED_ASH_LOG, 5, 5);
        flammable.add(ASH_WOOD, 5, 5);
        flammable.add(STRIPPED_ASH_WOOD, 5, 5);
        flammable.add(ASH_LEAVES, 30, 60);

        BlockEntityType.SIGN.addSupportedBlock(ASH_SIGN);
        BlockEntityType.SIGN.addSupportedBlock(ASH_WALL_SIGN);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
            content.add(BLACK_ROUNDEL_A);
            content.add(BLUE_ROUNDEL_A);
            content.add(BROWN_ROUNDEL_A);
            content.add(CYAN_ROUNDEL_A);
            content.add(GREEN_ROUNDEL_A);
            content.add(LIGHT_BLUE_ROUNDEL_A);
            content.add(LIGHT_GRAY_ROUNDEL_A);
            content.add(LIME_ROUNDEL_A);
            content.add(MAGENTA_ROUNDEL_A);
            content.add(ORANGE_ROUNDEL_A);
            content.add(PINK_ROUNDEL_A);
            content.add(RED_ROUNDEL_A);
            content.add(WHITE_ROUNDEL_A);
            content.add(YELLOW_ROUNDEL_A);
            content.add(GRAY_ROUNDEL_A);
            content.add(PURPLE_ROUNDEL_A);
            content.add(TEAL_ROUNDEL_A);

            content.add(BLACK_ROUNDEL_B);
            content.add(BLUE_ROUNDEL_B);
            content.add(BROWN_ROUNDEL_B);
            content.add(CYAN_ROUNDEL_B);
            content.add(GREEN_ROUNDEL_B);
            content.add(LIGHT_BLUE_ROUNDEL_B);
            content.add(LIGHT_GRAY_ROUNDEL_B);
            content.add(LIME_ROUNDEL_B);
            content.add(MAGENTA_ROUNDEL_B);
            content.add(ORANGE_ROUNDEL_B);
            content.add(PINK_ROUNDEL_B);
            content.add(RED_ROUNDEL_B);
            content.add(WHITE_ROUNDEL_B);
            content.add(YELLOW_ROUNDEL_B);
            content.add(GRAY_ROUNDEL_B);
            content.add(PURPLE_ROUNDEL_B);
            content.add(TEAL_ROUNDEL_B);

            content.add(BLACK_BIG_ROUNDEL_A);
            content.add(BLUE_BIG_ROUNDEL_A);
            content.add(BROWN_BIG_ROUNDEL_A);
            content.add(CYAN_BIG_ROUNDEL_A);
            content.add(GREEN_BIG_ROUNDEL_A);
            content.add(LIGHT_BLUE_BIG_ROUNDEL_A);
            content.add(LIGHT_GRAY_BIG_ROUNDEL_A);
            content.add(LIME_BIG_ROUNDEL_A);
            content.add(MAGENTA_BIG_ROUNDEL_A);
            content.add(ORANGE_BIG_ROUNDEL_A);
            content.add(PINK_BIG_ROUNDEL_A);
            content.add(RED_BIG_ROUNDEL_A);
            content.add(WHITE_BIG_ROUNDEL_A);
            content.add(YELLOW_BIG_ROUNDEL_A);
            content.add(GRAY_BIG_ROUNDEL_A);
            content.add(PURPLE_BIG_ROUNDEL_A);
            content.add(TEAL_BIG_ROUNDEL_A);

            content.add(BLACK_BIG_ROUNDEL_B);
            content.add(BLUE_BIG_ROUNDEL_B);
            content.add(BROWN_BIG_ROUNDEL_B);
            content.add(CYAN_BIG_ROUNDEL_B);
            content.add(GREEN_BIG_ROUNDEL_B);
            content.add(LIGHT_BLUE_BIG_ROUNDEL_B);
            content.add(LIGHT_GRAY_BIG_ROUNDEL_B);
            content.add(LIME_BIG_ROUNDEL_B);
            content.add(MAGENTA_BIG_ROUNDEL_B);
            content.add(ORANGE_BIG_ROUNDEL_B);
            content.add(PINK_BIG_ROUNDEL_B);
            content.add(RED_BIG_ROUNDEL_B);
            content.add(WHITE_BIG_ROUNDEL_B);
            content.add(YELLOW_BIG_ROUNDEL_B);
            content.add(GRAY_BIG_ROUNDEL_B);
            content.add(PURPLE_BIG_ROUNDEL_B);
            content.add(TEAL_BIG_ROUNDEL_B);

            content.add(BLACK_TARDIS_WALL);
            content.add(BLUE_TARDIS_WALL);
            content.add(BROWN_TARDIS_WALL);
            content.add(CYAN_TARDIS_WALL);
            content.add(GREEN_TARDIS_WALL);
            content.add(LIGHT_BLUE_TARDIS_WALL);
            content.add(LIGHT_GRAY_TARDIS_WALL);
            content.add(LIME_TARDIS_WALL);
            content.add(MAGENTA_TARDIS_WALL);
            content.add(ORANGE_TARDIS_WALL);
            content.add(PINK_TARDIS_WALL);
            content.add(RED_TARDIS_WALL);
            content.add(WHITE_TARDIS_WALL);
            content.add(YELLOW_TARDIS_WALL);
            content.add(GRAY_TARDIS_WALL);
            content.add(PURPLE_TARDIS_WALL);
            content.add(TEAL_TARDIS_WALL);

            content.add(BLACK_CHRONOPLASM_POWDER);
            content.add(BLUE_CHRONOPLASM_POWDER);
            content.add(BROWN_CHRONOPLASM_POWDER);
            content.add(CYAN_CHRONOPLASM_POWDER);
            content.add(GREEN_CHRONOPLASM_POWDER);
            content.add(LIGHT_BLUE_CHRONOPLASM_POWDER);
            content.add(LIGHT_GRAY_CHRONOPLASM_POWDER);
            content.add(LIME_CHRONOPLASM_POWDER);
            content.add(MAGENTA_CHRONOPLASM_POWDER);
            content.add(ORANGE_CHRONOPLASM_POWDER);
            content.add(PINK_CHRONOPLASM_POWDER);
            content.add(RED_CHRONOPLASM_POWDER);
            content.add(WHITE_CHRONOPLASM_POWDER);
            content.add(YELLOW_CHRONOPLASM_POWDER);
            content.add(GRAY_CHRONOPLASM_POWDER);
            content.add(PURPLE_CHRONOPLASM_POWDER);
            content.add(TEAL_CHRONOPLASM_POWDER);

            for (Block block : GALLIFREY_STONE_BUILDING_BLOCKS) {
                content.add(block);
            }

            for (Block block : ASH_WOOD_BUILDING_BLOCKS) {
                content.add(block);
            }
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> {
            content.add(GALLIFREY_DIRT);
            content.add(GALLIFREY_COARSE_DIRT);
            content.add(GALLIFREY_SAND);
            content.add(GALLIFREY_COBBLESTONE);
            content.add(GALLIFREY_MOSSY_COBBLESTONE);
            content.add(GALLIFREY_STONE);
            content.add(ASH_LOG);
            content.add(ASH_LEAVES);
            content.add(ASH_SAPLING);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
            content.add(TARDIS_DOOR_BUTTON);
            content.add(ASH_BUTTON);
            content.add(ASH_PRESSURE_PLATE);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            content.add(FIRST_DOCTOR_CONSOLE);
        });
    }

    private static Block register(Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, String id) {
        Identifier blockID = Identifier.of("dwm", id);
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, blockID);
        Block block = factory.apply(settings.registryKey(blockKey));

        registerBlockItem(blockID, block);

        return Registry.register(Registries.BLOCK, blockID, block);
    }

    private static Block registerWithoutItem(Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, String id) {
        Identifier blockID = Identifier.of("dwm", id);
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, blockID);
        Block block = factory.apply(settings.registryKey(blockKey));
        return Registry.register(Registries.BLOCK, blockID, block);
    }

    private static void registerBlockItem(Identifier blockID, Block block) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, blockID);
        BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, blockItem);
    }
}
