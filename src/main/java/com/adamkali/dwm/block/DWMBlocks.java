package com.adamkali.dwm.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

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

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
            content.add(BLACK_ROUNDEL_A.asItem());
            content.add(BLUE_ROUNDEL_A.asItem());
            content.add(BROWN_ROUNDEL_A.asItem());
            content.add(CYAN_ROUNDEL_A.asItem());
            content.add(GREEN_ROUNDEL_A.asItem());
            content.add(LIGHT_BLUE_ROUNDEL_A.asItem());
            content.add(LIGHT_GRAY_ROUNDEL_A.asItem());
            content.add(LIME_ROUNDEL_A.asItem());
            content.add(MAGENTA_ROUNDEL_A.asItem());
            content.add(ORANGE_ROUNDEL_A.asItem());
            content.add(PINK_ROUNDEL_A.asItem());
            content.add(RED_ROUNDEL_A.asItem());
            content.add(WHITE_ROUNDEL_A.asItem());
            content.add(YELLOW_ROUNDEL_A.asItem());
            content.add(GRAY_ROUNDEL_A.asItem());
            content.add(PURPLE_ROUNDEL_A.asItem());
            content.add(TEAL_ROUNDEL_A.asItem());

            content.add(BLACK_ROUNDEL_B.asItem());
            content.add(BLUE_ROUNDEL_B.asItem());
            content.add(BROWN_ROUNDEL_B.asItem());
            content.add(CYAN_ROUNDEL_B.asItem());
            content.add(GREEN_ROUNDEL_B.asItem());
            content.add(LIGHT_BLUE_ROUNDEL_B.asItem());
            content.add(LIGHT_GRAY_ROUNDEL_B.asItem());
            content.add(LIME_ROUNDEL_B.asItem());
            content.add(MAGENTA_ROUNDEL_B.asItem());
            content.add(ORANGE_ROUNDEL_B.asItem());
            content.add(PINK_ROUNDEL_B.asItem());
            content.add(RED_ROUNDEL_B.asItem());
            content.add(WHITE_ROUNDEL_B.asItem());
            content.add(YELLOW_ROUNDEL_B.asItem());
            content.add(GRAY_ROUNDEL_B.asItem());
            content.add(PURPLE_ROUNDEL_B.asItem());
            content.add(TEAL_ROUNDEL_B.asItem());

            content.add(BLACK_BIG_ROUNDEL_A.asItem());
            content.add(BLUE_BIG_ROUNDEL_A.asItem());
            content.add(BROWN_BIG_ROUNDEL_A.asItem());
            content.add(CYAN_BIG_ROUNDEL_A.asItem());
            content.add(GREEN_BIG_ROUNDEL_A.asItem());
            content.add(LIGHT_BLUE_BIG_ROUNDEL_A.asItem());
            content.add(LIGHT_GRAY_BIG_ROUNDEL_A.asItem());
            content.add(LIME_BIG_ROUNDEL_A.asItem());
            content.add(MAGENTA_BIG_ROUNDEL_A.asItem());
            content.add(ORANGE_BIG_ROUNDEL_A.asItem());
            content.add(PINK_BIG_ROUNDEL_A.asItem());
            content.add(RED_BIG_ROUNDEL_A.asItem());
            content.add(WHITE_BIG_ROUNDEL_A.asItem());
            content.add(YELLOW_BIG_ROUNDEL_A.asItem());
            content.add(GRAY_BIG_ROUNDEL_A.asItem());
            content.add(PURPLE_BIG_ROUNDEL_A.asItem());
            content.add(TEAL_BIG_ROUNDEL_A.asItem());

            content.add(BLACK_BIG_ROUNDEL_B.asItem());
            content.add(BLUE_BIG_ROUNDEL_B.asItem());
            content.add(BROWN_BIG_ROUNDEL_B.asItem());
            content.add(CYAN_BIG_ROUNDEL_B.asItem());
            content.add(GREEN_BIG_ROUNDEL_B.asItem());
            content.add(LIGHT_BLUE_BIG_ROUNDEL_B.asItem());
            content.add(LIGHT_GRAY_BIG_ROUNDEL_B.asItem());
            content.add(LIME_BIG_ROUNDEL_B.asItem());
            content.add(MAGENTA_BIG_ROUNDEL_B.asItem());
            content.add(ORANGE_BIG_ROUNDEL_B.asItem());
            content.add(PINK_BIG_ROUNDEL_B.asItem());
            content.add(RED_BIG_ROUNDEL_B.asItem());
            content.add(WHITE_BIG_ROUNDEL_B.asItem());
            content.add(YELLOW_BIG_ROUNDEL_B.asItem());
            content.add(GRAY_BIG_ROUNDEL_B.asItem());
            content.add(PURPLE_BIG_ROUNDEL_B.asItem());
            content.add(TEAL_BIG_ROUNDEL_B.asItem());

            content.add(BLACK_TARDIS_WALL.asItem());
            content.add(BLUE_TARDIS_WALL.asItem());
            content.add(BROWN_TARDIS_WALL.asItem());
            content.add(CYAN_TARDIS_WALL.asItem());
            content.add(GREEN_TARDIS_WALL.asItem());
            content.add(LIGHT_BLUE_TARDIS_WALL.asItem());
            content.add(LIGHT_GRAY_TARDIS_WALL.asItem());
            content.add(LIME_TARDIS_WALL.asItem());
            content.add(MAGENTA_TARDIS_WALL.asItem());
            content.add(ORANGE_TARDIS_WALL.asItem());
            content.add(PINK_TARDIS_WALL.asItem());
            content.add(RED_TARDIS_WALL.asItem());
            content.add(WHITE_TARDIS_WALL.asItem());
            content.add(YELLOW_TARDIS_WALL.asItem());
            content.add(GRAY_TARDIS_WALL.asItem());
            content.add(PURPLE_TARDIS_WALL.asItem());
            content.add(TEAL_TARDIS_WALL.asItem());

            content.add(BLACK_CHRONOPLASM_POWDER.asItem());
            content.add(BLUE_CHRONOPLASM_POWDER.asItem());
            content.add(BROWN_CHRONOPLASM_POWDER.asItem());
            content.add(CYAN_CHRONOPLASM_POWDER.asItem());
            content.add(GREEN_CHRONOPLASM_POWDER.asItem());
            content.add(LIGHT_BLUE_CHRONOPLASM_POWDER.asItem());
            content.add(LIGHT_GRAY_CHRONOPLASM_POWDER.asItem());
            content.add(LIME_CHRONOPLASM_POWDER.asItem());
            content.add(MAGENTA_CHRONOPLASM_POWDER.asItem());
            content.add(ORANGE_CHRONOPLASM_POWDER.asItem());
            content.add(PINK_CHRONOPLASM_POWDER.asItem());
            content.add(RED_CHRONOPLASM_POWDER.asItem());
            content.add(WHITE_CHRONOPLASM_POWDER.asItem());
            content.add(YELLOW_CHRONOPLASM_POWDER.asItem());
            content.add(GRAY_CHRONOPLASM_POWDER.asItem());
            content.add(PURPLE_CHRONOPLASM_POWDER.asItem());
            content.add(TEAL_CHRONOPLASM_POWDER.asItem());
        });
    }

    private static Block register(Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, String id) {
        Identifier blockID = Identifier.of("dwm", id);
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, blockID);
        Block block = factory.apply(settings.registryKey(blockKey));

        registerBlockItem(blockID, block);

        return Registry.register(Registries.BLOCK, blockID, block);
    }

    private static void registerBlockItem(Identifier blockID, Block block) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, blockID);
        BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, blockItem);
    }
}
