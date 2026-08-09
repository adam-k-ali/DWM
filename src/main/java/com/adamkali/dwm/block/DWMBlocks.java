package com.adamkali.dwm.block;

import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyDefinition;
import com.adamkali.dwm.block.wood.WoodFamilyFeature;
import com.adamkali.dwm.block.wood.WoodFamilyRegistrar;
import com.adamkali.dwm.item.DWMItemTags;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ColoredFallingBlock;
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

import java.util.EnumSet;
import java.util.List;
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

    public static final RegisteredWoodFamily ASH = WoodFamilyRegistrar.registerBlocks(
            new WoodFamilyDefinition(
                    "ash",
                    "Ash",
                    DWMBlockSettings.ASH_PLANKS_COLOR,
                    DWMBlockSettings.ASH_BARK_COLOR,
                    DWMWoodTypes.ASH,
                    DWMWoodTypes.ASH_SET,
                    DWMSaplingGenerators.ASH,
                    DWMBlockTags.ASH_LOGS,
                    DWMItemTags.ASH_LOGS,
                    EnumSet.of(WoodFamilyFeature.DOOR, WoodFamilyFeature.CUSTOM_DOOR_MODEL, WoodFamilyFeature.TRAPDOOR)
            )
    );
    public static final Block ASH_PLANKS = ASH.blocks().planks();
    public static final Block ASH_LOG = ASH.blocks().log();
    public static final Block ASH_WOOD = ASH.blocks().wood();
    public static final Block STRIPPED_ASH_LOG = ASH.blocks().strippedLog();
    public static final Block STRIPPED_ASH_WOOD = ASH.blocks().strippedWood();
    public static final Block ASH_LEAVES = ASH.blocks().leaves();
    public static final Block ASH_SAPLING = ASH.blocks().sapling();
    public static final Block POTTED_ASH_SAPLING = ASH.blocks().pottedSapling();
    public static final Block ASH_STAIRS = ASH.blocks().stairs();
    public static final Block ASH_SLAB = ASH.blocks().slab();
    public static final Block ASH_FENCE = ASH.blocks().fence();
    public static final Block ASH_FENCE_GATE = ASH.blocks().fenceGate();
    public static final Block ASH_BUTTON = ASH.blocks().button();
    public static final Block ASH_PRESSURE_PLATE = ASH.blocks().pressurePlate();
    public static final Block ASH_DOOR = ASH.requireDoor();
    public static final Block ASH_TRAPDOOR = ASH.requireTrapdoor();
    public static final Block ASH_SIGN = ASH.blocks().sign();
    public static final Block ASH_WALL_SIGN = ASH.blocks().wallSign();
    public static final Block ASH_HANGING_SIGN = ASH.blocks().hangingSign();
    public static final Block ASH_WALL_HANGING_SIGN = ASH.blocks().wallHangingSign();

    public static final RegisteredWoodFamily DARK_ASH = WoodFamilyRegistrar.registerBlocks(
            new WoodFamilyDefinition(
                    "dark_ash",
                    "Dark Ash",
                    DWMBlockSettings.DARK_ASH_PLANKS_COLOR,
                    DWMBlockSettings.DARK_ASH_BARK_COLOR,
                    DWMWoodTypes.DARK_ASH,
                    DWMWoodTypes.DARK_ASH_SET,
                    DWMSaplingGenerators.DARK_ASH,
                    DWMBlockTags.DARK_ASH_LOGS,
                    DWMItemTags.DARK_ASH_LOGS,
                    EnumSet.of(WoodFamilyFeature.DOOR, WoodFamilyFeature.CUSTOM_DOOR_MODEL, WoodFamilyFeature.TRAPDOOR)
            )
    );
    public static final Block DARK_ASH_PLANKS = DARK_ASH.blocks().planks();
    public static final Block DARK_ASH_LOG = DARK_ASH.blocks().log();
    public static final Block DARK_ASH_WOOD = DARK_ASH.blocks().wood();
    public static final Block STRIPPED_DARK_ASH_LOG = DARK_ASH.blocks().strippedLog();
    public static final Block STRIPPED_DARK_ASH_WOOD = DARK_ASH.blocks().strippedWood();
    public static final Block DARK_ASH_LEAVES = DARK_ASH.blocks().leaves();
    public static final Block DARK_ASH_SAPLING = DARK_ASH.blocks().sapling();
    public static final Block POTTED_DARK_ASH_SAPLING = DARK_ASH.blocks().pottedSapling();
    public static final Block DARK_ASH_STAIRS = DARK_ASH.blocks().stairs();
    public static final Block DARK_ASH_SLAB = DARK_ASH.blocks().slab();
    public static final Block DARK_ASH_FENCE = DARK_ASH.blocks().fence();
    public static final Block DARK_ASH_FENCE_GATE = DARK_ASH.blocks().fenceGate();
    public static final Block DARK_ASH_BUTTON = DARK_ASH.blocks().button();
    public static final Block DARK_ASH_PRESSURE_PLATE = DARK_ASH.blocks().pressurePlate();
    public static final Block DARK_ASH_DOOR = DARK_ASH.requireDoor();
    public static final Block DARK_ASH_TRAPDOOR = DARK_ASH.requireTrapdoor();
    public static final Block DARK_ASH_SIGN = DARK_ASH.blocks().sign();
    public static final Block DARK_ASH_WALL_SIGN = DARK_ASH.blocks().wallSign();
    public static final Block DARK_ASH_HANGING_SIGN = DARK_ASH.blocks().hangingSign();
    public static final Block DARK_ASH_WALL_HANGING_SIGN = DARK_ASH.blocks().wallHangingSign();

    public static final List<RegisteredWoodFamily> WOOD_FAMILIES = List.of(ASH, DARK_ASH);

    public static final List<Block> ASH_LOGS = ASH.logs();
    public static final List<Block> ASH_WOOD_BUILDING_BLOCKS = ASH.buildingBlocks();
    public static final List<Block> ASH_WOOD_FAMILY = ASH.familyBlocks();
    public static final List<Block> DARK_ASH_LOGS = DARK_ASH.logs();
    public static final List<Block> DARK_ASH_WOOD_BUILDING_BLOCKS = DARK_ASH.buildingBlocks();
    public static final List<Block> DARK_ASH_WOOD_FAMILY = DARK_ASH.familyBlocks();

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

        for (RegisteredWoodFamily family : WOOD_FAMILIES) {
            WoodFamilyRegistrar.wireRuntime(family);
        }

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

            for (RegisteredWoodFamily family : WOOD_FAMILIES) {
                for (Block block : family.buildingBlocks()) {
                    content.add(block);
                }
            }
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> {
            content.add(GALLIFREY_DIRT);
            content.add(GALLIFREY_COARSE_DIRT);
            content.add(GALLIFREY_SAND);
            content.add(GALLIFREY_COBBLESTONE);
            content.add(GALLIFREY_MOSSY_COBBLESTONE);
            content.add(GALLIFREY_STONE);
            for (RegisteredWoodFamily family : WOOD_FAMILIES) {
                content.add(family.blocks().log());
                content.add(family.blocks().leaves());
                content.add(family.blocks().sapling());
            }
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
            content.add(TARDIS_DOOR_BUTTON);
            for (RegisteredWoodFamily family : WOOD_FAMILIES) {
                content.add(family.blocks().button());
                content.add(family.blocks().pressurePlate());
                if (family.trapdoorOrNull() != null) {
                    content.add(family.requireTrapdoor());
                }
                if (family.doorOrNull() != null) {
                    content.add(family.requireDoor());
                }
            }
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            content.add(FIRST_DOCTOR_CONSOLE);
        });
    }

    public static Block registerBlock(Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, String id) {
        return register(factory, settings, id);
    }

    public static Block registerBlockWithoutItem(Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, String id) {
        return registerWithoutItem(factory, settings, id);
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
