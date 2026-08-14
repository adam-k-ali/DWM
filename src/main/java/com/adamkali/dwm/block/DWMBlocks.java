package com.adamkali.dwm.block;

import com.adamkali.dwm.item.DWMCreativeTabs;

import com.adamkali.dwm.block.plant.SaccharineCaneBlock;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyDefinition;
import com.adamkali.dwm.block.wood.WoodFamilyFeature;
import com.adamkali.dwm.block.wood.WoodFamilyRegistrar;
import com.adamkali.dwm.item.DWMItemTags;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.registry.CompostableRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
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

    public static final Block BLACK_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "black_roundel_b");
    public static final Block BLUE_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "blue_roundel_b");
    public static final Block BROWN_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "brown_roundel_b");
    public static final Block CYAN_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "cyan_roundel_b");
    public static final Block GREEN_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "green_roundel_b");
    public static final Block LIGHT_BLUE_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "light_blue_roundel_b");
    public static final Block LIGHT_GRAY_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "light_gray_roundel_b");
    public static final Block LIME_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "lime_roundel_b");
    public static final Block MAGENTA_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "magenta_roundel_b");
    public static final Block ORANGE_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "orange_roundel_b");
    public static final Block PINK_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "pink_roundel_b");
    public static final Block RED_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "red_roundel_b");
    public static final Block WHITE_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "white_roundel_b");
    public static final Block YELLOW_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "yellow_roundel_b");
    public static final Block GRAY_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "gray_roundel_b");
    public static final Block PURPLE_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "purple_roundel_b");
    public static final Block TEAL_ROUNDEL_B = register(Block::new, DWMBlockSettings.TARDIS_WALL_SETTINGS.noOcclusion(), "teal_roundel_b");

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

    public static final Block TARDIS_CHAIR_SMALL = register(
            settings -> new TardisChairBlock(settings, TardisDecorShapes.SMALL_CHAIR, TardisDecorShapes.SMALL_CHAIR_SEAT_Y),
            DWMBlockSettings.TARDIS_DECOR_SETTINGS,
            "tardis_chair_small");
    public static final Block TARDIS_CHAIR_LARGE = register(
            settings -> new TardisChairBlock(settings, TardisDecorShapes.LARGE_CHAIR, TardisDecorShapes.LARGE_CHAIR_SEAT_Y),
            DWMBlockSettings.TARDIS_DECOR_SETTINGS,
            "tardis_chair_large");
    public static final Block DECORATIONAL_COLUMN = register(
            settings -> new TardisDecorBlock(settings, TardisDecorShapes.COLUMN),
            DWMBlockSettings.TARDIS_DECOR_SETTINGS,
            "decorational_column");
    public static final Block TARDIS_CEILING_VENT = register(
            settings -> new TardisDecorBlock(settings, TardisDecorShapes.CEILING_VENT),
            DWMBlockSettings.TARDIS_DECOR_SETTINGS,
            "tardis_ceiling_vent");
    public static final Block TARDIS_GLOBE = register(
            settings -> new TardisDecorEntityBlock(settings, TardisDecorShapes.GLOBE),
            DWMBlockSettings.TARDIS_DECOR_SETTINGS,
            "tardis_globe");
    public static final Block TARDIS_COMPACT_SCANNER = register(
            settings -> new TardisDecorEntityBlock(settings, TardisDecorShapes.COMPACT_SCANNER),
            DWMBlockSettings.TARDIS_DECOR_SETTINGS,
            "tardis_compact_scanner");
    public static final Block TARDIS_FULL_SCANNER = register(
            settings -> new TardisDecorEntityBlock(settings, TardisDecorShapes.FULL_SCANNER_NORTH),
            DWMBlockSettings.TARDIS_DECOR_SETTINGS,
            "tardis_full_scanner");

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
            settings -> new ColoredFallingBlock(new ColorRGBA(0xC47A3A), settings),
            DWMBlockSettings.GALLIFREY_SAND,
            "gallifrey_sand"
    );
    public static final Block GALLIFREY_DIRT = register(Block::new, DWMBlockSettings.GALLIFREY_DIRT, "gallifrey_dirt");
    public static final Block GALLIFREY_COARSE_DIRT = register(Block::new, DWMBlockSettings.GALLIFREY_DIRT, "gallifrey_coarse_dirt");
    public static final Block GALLIFREY_GRASS_BLOCK = register(Block::new, DWMBlockSettings.GALLIFREY_GRASS, "gallifrey_grass_block");

    public static final Block ORANGE_SAND = register(
            settings -> new ColoredFallingBlock(new ColorRGBA(0xF4B583), settings),
            DWMBlockSettings.ORANGE_SAND,
            "orange_sand"
    );
    public static final Block ORANGE_SANDSTONE = register(Block::new, DWMBlockSettings.ORANGE_SANDSTONE, "orange_sandstone");
    public static final Block ORANGE_SANDSTONE_STAIRS = register(
            settings -> new StairBlock(ORANGE_SANDSTONE.defaultBlockState(), settings),
            BlockBehaviour.Properties.ofLegacyCopy(ORANGE_SANDSTONE),
            "orange_sandstone_stairs"
    );
    public static final Block ORANGE_SANDSTONE_SLAB = register(
            SlabBlock::new,
            BlockBehaviour.Properties.ofLegacyCopy(ORANGE_SANDSTONE),
            "orange_sandstone_slab"
    );
    public static final Block ORANGE_SANDSTONE_WALL = register(
            WallBlock::new,
            BlockBehaviour.Properties.ofLegacyCopy(ORANGE_SANDSTONE),
            "orange_sandstone_wall"
    );
    public static final Block CUT_ORANGE_SANDSTONE = register(Block::new, DWMBlockSettings.ORANGE_SANDSTONE, "cut_orange_sandstone");
    public static final Block CUT_ORANGE_SANDSTONE_SLAB = register(
            SlabBlock::new,
            BlockBehaviour.Properties.ofLegacyCopy(CUT_ORANGE_SANDSTONE),
            "cut_orange_sandstone_slab"
    );
    public static final Block CHISELED_ORANGE_SANDSTONE = register(Block::new, DWMBlockSettings.ORANGE_SANDSTONE, "chiseled_orange_sandstone");
    public static final Block SMOOTH_ORANGE_SANDSTONE = register(Block::new, DWMBlockSettings.ORANGE_SANDSTONE, "smooth_orange_sandstone");
    public static final Block SMOOTH_ORANGE_SANDSTONE_STAIRS = register(
            settings -> new StairBlock(SMOOTH_ORANGE_SANDSTONE.defaultBlockState(), settings),
            BlockBehaviour.Properties.ofLegacyCopy(SMOOTH_ORANGE_SANDSTONE),
            "smooth_orange_sandstone_stairs"
    );
    public static final Block SMOOTH_ORANGE_SANDSTONE_SLAB = register(
            SlabBlock::new,
            BlockBehaviour.Properties.ofLegacyCopy(SMOOTH_ORANGE_SANDSTONE),
            "smooth_orange_sandstone_slab"
    );

    public static final Block CITADEL_WALL = register(Block::new, DWMBlockSettings.CITADEL, "citadel_wall");
    public static final Block CITADEL_PANEL = register(Block::new, DWMBlockSettings.CITADEL, "citadel_panel");
    public static final Block CITADEL_TILE = register(Block::new, DWMBlockSettings.CITADEL, "citadel_tile");
    public static final Block CITADEL_GLASS = register(TransparentBlock::new, DWMBlockSettings.CITADEL_GLASS, "citadel_glass");

    public static final Block AZBANTIUM_ORE = register(Block::new, DWMBlockSettings.azbantium(), "azbantium_ore");
    public static final Block AZBANTIUM_BLOCK = register(Block::new, DWMBlockSettings.azbantium(), "azbantium_block");

    public static final Block GALLIFREY_COAL_ORE = register(Block::new, DWMBlockSettings.gallifreyVanillaOre(), "gallifrey_coal_ore");
    public static final Block GALLIFREY_IRON_ORE = register(Block::new, DWMBlockSettings.gallifreyVanillaOre(), "gallifrey_iron_ore");
    public static final Block GALLIFREY_GOLD_ORE = register(Block::new, DWMBlockSettings.gallifreyVanillaOre(), "gallifrey_gold_ore");
    public static final Block GALLIFREY_DIAMOND_ORE = register(Block::new, DWMBlockSettings.gallifreyVanillaOre(), "gallifrey_diamond_ore");

    public static final Block FLOWER_OF_REMEMBRANCE = register(
            props -> new FlowerBlock(MobEffects.SATURATION, 0.35F, props),
            DWMBlockSettings.gallifreyCrossPlant(),
            "flower_of_remembrance"
    );
    public static final Block POTTED_FLOWER_OF_REMEMBRANCE = registerWithoutItem(
            props -> new FlowerPotBlock(FLOWER_OF_REMEMBRANCE, props),
            DWMBlockSettings.gallifreyPottedPlant(),
            "potted_flower_of_remembrance"
    );
    public static final Block MOONLIGHT_BLOOM = register(
            props -> new FlowerBlock(MobEffects.NIGHT_VISION, 5.0F, props),
            DWMBlockSettings.gallifreyCrossPlant(),
            "moonlight_bloom"
    );
    public static final Block POTTED_MOONLIGHT_BLOOM = registerWithoutItem(
            props -> new FlowerPotBlock(MOONLIGHT_BLOOM, props),
            DWMBlockSettings.gallifreyPottedPlant(),
            "potted_moonlight_bloom"
    );
    public static final Block SACCHARINE_CANE = register(
            SaccharineCaneBlock::new,
            DWMBlockSettings.saccharineCane(),
            "saccharine_cane"
    );

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
                    EnumSet.of(
                            WoodFamilyFeature.DOOR,
                            WoodFamilyFeature.CUSTOM_DOOR_MODEL,
                            WoodFamilyFeature.TRAPDOOR,
                            WoodFamilyFeature.CUSTOM_TRAPDOOR_MODEL
                    )
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
                    EnumSet.of(
                            WoodFamilyFeature.DOOR,
                            WoodFamilyFeature.CUSTOM_DOOR_MODEL,
                            WoodFamilyFeature.TRAPDOOR,
                            WoodFamilyFeature.CUSTOM_TRAPDOOR_MODEL
                    )
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

    public static final RegisteredWoodFamily CARDINAL = WoodFamilyRegistrar.registerBlocks(
            new WoodFamilyDefinition(
                    "cardinal",
                    "Cardinal",
                    MapColor.TERRACOTTA_RED,
                    MapColor.TERRACOTTA_GRAY,
                    DWMWoodTypes.CARDINAL,
                    DWMWoodTypes.CARDINAL_SET,
                    DWMSaplingGenerators.CARDINAL,
                    DWMBlockTags.CARDINAL_LOGS,
                    DWMItemTags.CARDINAL_LOGS,
                    EnumSet.of(
                            WoodFamilyFeature.TALL_DOOR,
                            WoodFamilyFeature.CUSTOM_DOOR_MODEL,
                            WoodFamilyFeature.TRAPDOOR,
                            WoodFamilyFeature.CUSTOM_TRAPDOOR_MODEL
                    )
            )
    );
    public static final Block CARDINAL_PLANKS = CARDINAL.blocks().planks();
    public static final Block CARDINAL_LOG = CARDINAL.blocks().log();
    public static final Block CARDINAL_WOOD = CARDINAL.blocks().wood();
    public static final Block STRIPPED_CARDINAL_LOG = CARDINAL.blocks().strippedLog();
    public static final Block STRIPPED_CARDINAL_WOOD = CARDINAL.blocks().strippedWood();
    public static final Block CARDINAL_LEAVES = CARDINAL.blocks().leaves();
    public static final Block CARDINAL_SAPLING = CARDINAL.blocks().sapling();
    public static final Block POTTED_CARDINAL_SAPLING = CARDINAL.blocks().pottedSapling();
    public static final Block CARDINAL_STAIRS = CARDINAL.blocks().stairs();
    public static final Block CARDINAL_SLAB = CARDINAL.blocks().slab();
    public static final Block CARDINAL_FENCE = CARDINAL.blocks().fence();
    public static final Block CARDINAL_FENCE_GATE = CARDINAL.blocks().fenceGate();
    public static final Block CARDINAL_BUTTON = CARDINAL.blocks().button();
    public static final Block CARDINAL_PRESSURE_PLATE = CARDINAL.blocks().pressurePlate();
    public static final Block CARDINAL_DOOR = CARDINAL.requireDoor();
    public static final Block CARDINAL_TRAPDOOR = CARDINAL.requireTrapdoor();
    public static final Block CARDINAL_SIGN = CARDINAL.blocks().sign();
    public static final Block CARDINAL_WALL_SIGN = CARDINAL.blocks().wallSign();
    public static final Block CARDINAL_HANGING_SIGN = CARDINAL.blocks().hangingSign();
    public static final Block CARDINAL_WALL_HANGING_SIGN = CARDINAL.blocks().wallHangingSign();

    public static final List<RegisteredWoodFamily> WOOD_FAMILIES = List.of(ASH, DARK_ASH, CARDINAL);

    public static final List<Block> ASH_LOGS = ASH.logs();
    public static final List<Block> ASH_WOOD_BUILDING_BLOCKS = ASH.buildingBlocks();
    public static final List<Block> ASH_WOOD_FAMILY = ASH.familyBlocks();
    public static final List<Block> DARK_ASH_LOGS = DARK_ASH.logs();
    public static final List<Block> DARK_ASH_WOOD_BUILDING_BLOCKS = DARK_ASH.buildingBlocks();
    public static final List<Block> DARK_ASH_WOOD_FAMILY = DARK_ASH.familyBlocks();
    public static final List<Block> CARDINAL_LOGS = CARDINAL.logs();
    public static final List<Block> CARDINAL_WOOD_BUILDING_BLOCKS = CARDINAL.buildingBlocks();
    public static final List<Block> CARDINAL_WOOD_FAMILY = CARDINAL.familyBlocks();

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
            GALLIFREY_COARSE_DIRT,
            GALLIFREY_GRASS_BLOCK
    );

    /** Orange sandstone building set (excludes falling sand). */
    public static final List<Block> ORANGE_SAND_BUILDING_BLOCKS = List.of(
            ORANGE_SANDSTONE,
            ORANGE_SANDSTONE_STAIRS,
            ORANGE_SANDSTONE_SLAB,
            ORANGE_SANDSTONE_WALL,
            CUT_ORANGE_SANDSTONE,
            CUT_ORANGE_SANDSTONE_SLAB,
            CHISELED_ORANGE_SANDSTONE,
            SMOOTH_ORANGE_SANDSTONE,
            SMOOTH_ORANGE_SANDSTONE_STAIRS,
            SMOOTH_ORANGE_SANDSTONE_SLAB
    );

    /** Full orange sand family including terrain sand. */
    public static final List<Block> ORANGE_SAND_FAMILY = List.of(
            ORANGE_SAND,
            ORANGE_SANDSTONE,
            ORANGE_SANDSTONE_STAIRS,
            ORANGE_SANDSTONE_SLAB,
            ORANGE_SANDSTONE_WALL,
            CUT_ORANGE_SANDSTONE,
            CUT_ORANGE_SANDSTONE_SLAB,
            CHISELED_ORANGE_SANDSTONE,
            SMOOTH_ORANGE_SANDSTONE,
            SMOOTH_ORANGE_SANDSTONE_STAIRS,
            SMOOTH_ORANGE_SANDSTONE_SLAB
    );

    /** Citadel decorative solids (excludes glass). */
    public static final List<Block> CITADEL_BUILDING_BLOCKS = List.of(
            CITADEL_WALL,
            CITADEL_PANEL,
            CITADEL_TILE
    );

    /** Full citadel set including glass. */
    public static final List<Block> CITADEL_FAMILY = List.of(
            CITADEL_WALL,
            CITADEL_PANEL,
            CITADEL_TILE,
            CITADEL_GLASS
    );

    public static final List<Block> AZBANTIUM_BLOCKS = List.of(
            AZBANTIUM_ORE,
            AZBANTIUM_BLOCK
    );

    /** Gallifrey-stone-textured ores that drop vanilla coal / raw iron / raw gold / diamond. */
    public static final List<Block> GALLIFREY_VANILLA_ORES = List.of(
            GALLIFREY_COAL_ORE,
            GALLIFREY_IRON_ORE,
            GALLIFREY_GOLD_ORE,
            GALLIFREY_DIAMOND_ORE
    );

    /** Placeable Gallifrey decorative plants (items). */
    public static final List<Block> GALLIFREY_PLANTS = List.of(
            FLOWER_OF_REMEMBRANCE,
            MOONLIGHT_BLOOM,
            SACCHARINE_CANE
    );

    /** Cross flowers that have potted variants. */
    public static final List<Block> GALLIFREY_CROSS_PLANTS = List.of(
            FLOWER_OF_REMEMBRANCE,
            MOONLIGHT_BLOOM
    );

    /** Potted Gallifrey flowers (no BlockItem). */
    public static final List<Block> GALLIFREY_POTTED_PLANTS = List.of(
            POTTED_FLOWER_OF_REMEMBRANCE,
            POTTED_MOONLIGHT_BLOOM
    );

    public static void initialize() {
        DWMWoodTypes.initialize();

        CompostableRegistry.INSTANCE.add(FLOWER_OF_REMEMBRANCE.asItem(), 0.65F);
        CompostableRegistry.INSTANCE.add(MOONLIGHT_BLOOM.asItem(), 0.65F);
        CompostableRegistry.INSTANCE.add(SACCHARINE_CANE.asItem(), 0.50F);

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) ->
                !FirstDoctorConsoleBlock.isPlayerBreakDenied(state));

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (FirstDoctorConsoleBlock.isPlayerBreakDenied(world.getBlockState(pos))) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        for (RegisteredWoodFamily family : WOOD_FAMILIES) {
            WoodFamilyRegistrar.wireRuntime(family);
        }

        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.BUILDING_BLOCKS).register(content -> {
            content.accept(BLACK_ROUNDEL_A);
            content.accept(BLUE_ROUNDEL_A);
            content.accept(BROWN_ROUNDEL_A);
            content.accept(CYAN_ROUNDEL_A);
            content.accept(GREEN_ROUNDEL_A);
            content.accept(LIGHT_BLUE_ROUNDEL_A);
            content.accept(LIGHT_GRAY_ROUNDEL_A);
            content.accept(LIME_ROUNDEL_A);
            content.accept(MAGENTA_ROUNDEL_A);
            content.accept(ORANGE_ROUNDEL_A);
            content.accept(PINK_ROUNDEL_A);
            content.accept(RED_ROUNDEL_A);
            content.accept(WHITE_ROUNDEL_A);
            content.accept(YELLOW_ROUNDEL_A);
            content.accept(GRAY_ROUNDEL_A);
            content.accept(PURPLE_ROUNDEL_A);
            content.accept(TEAL_ROUNDEL_A);

            content.accept(BLACK_ROUNDEL_B);
            content.accept(BLUE_ROUNDEL_B);
            content.accept(BROWN_ROUNDEL_B);
            content.accept(CYAN_ROUNDEL_B);
            content.accept(GREEN_ROUNDEL_B);
            content.accept(LIGHT_BLUE_ROUNDEL_B);
            content.accept(LIGHT_GRAY_ROUNDEL_B);
            content.accept(LIME_ROUNDEL_B);
            content.accept(MAGENTA_ROUNDEL_B);
            content.accept(ORANGE_ROUNDEL_B);
            content.accept(PINK_ROUNDEL_B);
            content.accept(RED_ROUNDEL_B);
            content.accept(WHITE_ROUNDEL_B);
            content.accept(YELLOW_ROUNDEL_B);
            content.accept(GRAY_ROUNDEL_B);
            content.accept(PURPLE_ROUNDEL_B);
            content.accept(TEAL_ROUNDEL_B);

            content.accept(BLACK_BIG_ROUNDEL_A);
            content.accept(BLUE_BIG_ROUNDEL_A);
            content.accept(BROWN_BIG_ROUNDEL_A);
            content.accept(CYAN_BIG_ROUNDEL_A);
            content.accept(GREEN_BIG_ROUNDEL_A);
            content.accept(LIGHT_BLUE_BIG_ROUNDEL_A);
            content.accept(LIGHT_GRAY_BIG_ROUNDEL_A);
            content.accept(LIME_BIG_ROUNDEL_A);
            content.accept(MAGENTA_BIG_ROUNDEL_A);
            content.accept(ORANGE_BIG_ROUNDEL_A);
            content.accept(PINK_BIG_ROUNDEL_A);
            content.accept(RED_BIG_ROUNDEL_A);
            content.accept(WHITE_BIG_ROUNDEL_A);
            content.accept(YELLOW_BIG_ROUNDEL_A);
            content.accept(GRAY_BIG_ROUNDEL_A);
            content.accept(PURPLE_BIG_ROUNDEL_A);
            content.accept(TEAL_BIG_ROUNDEL_A);

            content.accept(BLACK_BIG_ROUNDEL_B);
            content.accept(BLUE_BIG_ROUNDEL_B);
            content.accept(BROWN_BIG_ROUNDEL_B);
            content.accept(CYAN_BIG_ROUNDEL_B);
            content.accept(GREEN_BIG_ROUNDEL_B);
            content.accept(LIGHT_BLUE_BIG_ROUNDEL_B);
            content.accept(LIGHT_GRAY_BIG_ROUNDEL_B);
            content.accept(LIME_BIG_ROUNDEL_B);
            content.accept(MAGENTA_BIG_ROUNDEL_B);
            content.accept(ORANGE_BIG_ROUNDEL_B);
            content.accept(PINK_BIG_ROUNDEL_B);
            content.accept(RED_BIG_ROUNDEL_B);
            content.accept(WHITE_BIG_ROUNDEL_B);
            content.accept(YELLOW_BIG_ROUNDEL_B);
            content.accept(GRAY_BIG_ROUNDEL_B);
            content.accept(PURPLE_BIG_ROUNDEL_B);
            content.accept(TEAL_BIG_ROUNDEL_B);

            content.accept(BLACK_TARDIS_WALL);
            content.accept(BLUE_TARDIS_WALL);
            content.accept(BROWN_TARDIS_WALL);
            content.accept(CYAN_TARDIS_WALL);
            content.accept(GREEN_TARDIS_WALL);
            content.accept(LIGHT_BLUE_TARDIS_WALL);
            content.accept(LIGHT_GRAY_TARDIS_WALL);
            content.accept(LIME_TARDIS_WALL);
            content.accept(MAGENTA_TARDIS_WALL);
            content.accept(ORANGE_TARDIS_WALL);
            content.accept(PINK_TARDIS_WALL);
            content.accept(RED_TARDIS_WALL);
            content.accept(WHITE_TARDIS_WALL);
            content.accept(YELLOW_TARDIS_WALL);
            content.accept(GRAY_TARDIS_WALL);
            content.accept(PURPLE_TARDIS_WALL);
            content.accept(TEAL_TARDIS_WALL);

            content.accept(BLACK_CHRONOPLASM_POWDER);
            content.accept(BLUE_CHRONOPLASM_POWDER);
            content.accept(BROWN_CHRONOPLASM_POWDER);
            content.accept(CYAN_CHRONOPLASM_POWDER);
            content.accept(GREEN_CHRONOPLASM_POWDER);
            content.accept(LIGHT_BLUE_CHRONOPLASM_POWDER);
            content.accept(LIGHT_GRAY_CHRONOPLASM_POWDER);
            content.accept(LIME_CHRONOPLASM_POWDER);
            content.accept(MAGENTA_CHRONOPLASM_POWDER);
            content.accept(ORANGE_CHRONOPLASM_POWDER);
            content.accept(PINK_CHRONOPLASM_POWDER);
            content.accept(RED_CHRONOPLASM_POWDER);
            content.accept(WHITE_CHRONOPLASM_POWDER);
            content.accept(YELLOW_CHRONOPLASM_POWDER);
            content.accept(GRAY_CHRONOPLASM_POWDER);
            content.accept(PURPLE_CHRONOPLASM_POWDER);
            content.accept(TEAL_CHRONOPLASM_POWDER);

            content.accept(TARDIS_CHAIR_SMALL);
            content.accept(TARDIS_CHAIR_LARGE);
            content.accept(DECORATIONAL_COLUMN);
            content.accept(TARDIS_CEILING_VENT);
            content.accept(TARDIS_GLOBE);
            content.accept(TARDIS_COMPACT_SCANNER);
            content.accept(TARDIS_FULL_SCANNER);

            for (Block block : GALLIFREY_STONE_BUILDING_BLOCKS) {
                content.accept(block);
            }

            for (Block block : ORANGE_SAND_BUILDING_BLOCKS) {
                content.accept(block);
            }

            for (Block block : CITADEL_FAMILY) {
                content.accept(block);
            }

            content.accept(AZBANTIUM_BLOCK);

            for (RegisteredWoodFamily family : WOOD_FAMILIES) {
                for (Block block : family.buildingBlocks()) {
                    content.accept(block);
                }
            }
        });

        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.NATURAL_BLOCKS).register(content -> {
            content.accept(GALLIFREY_GRASS_BLOCK);
            content.accept(GALLIFREY_DIRT);
            content.accept(GALLIFREY_COARSE_DIRT);
            content.accept(GALLIFREY_SAND);
            content.accept(ORANGE_SAND);
            content.accept(GALLIFREY_COBBLESTONE);
            content.accept(GALLIFREY_MOSSY_COBBLESTONE);
            content.accept(GALLIFREY_STONE);
            content.accept(AZBANTIUM_ORE);
            for (Block ore : GALLIFREY_VANILLA_ORES) {
                content.accept(ore);
            }
            for (RegisteredWoodFamily family : WOOD_FAMILIES) {
                content.accept(family.blocks().log());
                content.accept(family.blocks().leaves());
                content.accept(family.blocks().sapling());
            }
            for (Block plant : GALLIFREY_PLANTS) {
                content.accept(plant);
            }
        });

        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.REDSTONE_BLOCKS).register(content -> {
            content.accept(TARDIS_DOOR_BUTTON);
            for (RegisteredWoodFamily family : WOOD_FAMILIES) {
                content.accept(family.blocks().button());
                content.accept(family.blocks().pressurePlate());
                if (family.trapdoorOrNull() != null) {
                    content.accept(family.requireTrapdoor());
                }
                if (family.doorOrNull() != null) {
                    content.accept(family.requireDoor());
                }
            }
        });

        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.FUNCTIONAL_BLOCKS).register(content -> {
            content.accept(FIRST_DOCTOR_CONSOLE);
        });
    }

    public static Block registerBlock(Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, String id) {
        return register(factory, settings, id);
    }

    public static Block registerBlockWithoutItem(Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, String id) {
        return registerWithoutItem(factory, settings, id);
    }

    private static Block register(Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, String id) {
        Identifier blockID = Identifier.fromNamespaceAndPath("dwm", id);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, blockID);
        Block block = factory.apply(settings.setId(blockKey));

        registerBlockItem(blockID, block);

        return Registry.register(BuiltInRegistries.BLOCK, blockID, block);
    }

    private static Block registerWithoutItem(Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, String id) {
        Identifier blockID = Identifier.fromNamespaceAndPath("dwm", id);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, blockID);
        Block block = factory.apply(settings.setId(blockKey));
        return Registry.register(BuiltInRegistries.BLOCK, blockID, block);
    }

    private static void registerBlockItem(Identifier blockID, Block block) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, blockID);
        BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
    }
}
