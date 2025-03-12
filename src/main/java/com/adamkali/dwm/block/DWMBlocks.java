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
    public static final Block WHITE_ROUNDEL_A = register(Block::new, DWMBlockSettings.ROUNDEL_BLOCK_SETTINGS, "white_roundel_a");
    public static final Block WHITE_ROUNDEL_B = register(Block::new, DWMBlockSettings.ROUNDEL_BLOCK_SETTINGS.nonOpaque(), "white_roundel_b");
    public static final Block WHITE_BIG_ROUNDEL_A = register(Block::new, DWMBlockSettings.ROUNDEL_BLOCK_SETTINGS, "white_big_roundel_a");
    public static final Block WHITE_BIG_ROUNDEL_B = register(Block::new, DWMBlockSettings.ROUNDEL_BLOCK_SETTINGS.nonOpaque(), "white_big_roundel_b");
    public static final Block WHITE_EXTRUDED_ROUNDEL_A = register(DWMHorizontalFacingBlock::new, DWMBlockSettings.ROUNDEL_BLOCK_SETTINGS, "white_extruded_roundel_a");
    public static final Block WHITE_EXTRUDED_ROUNDEL_B = register(DWMHorizontalFacingBlock::new, DWMBlockSettings.ROUNDEL_BLOCK_SETTINGS.nonOpaque(), "white_extruded_roundel_b");

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
            content.add(WHITE_ROUNDEL_A.asItem());
            content.add(WHITE_ROUNDEL_B.asItem());
            content.add(WHITE_BIG_ROUNDEL_A.asItem());
            content.add(WHITE_BIG_ROUNDEL_B.asItem());
            content.add(WHITE_EXTRUDED_ROUNDEL_A.asItem());
            content.add(WHITE_EXTRUDED_ROUNDEL_B.asItem());
        });
    }

    private static Block register(Function<AbstractBlock.Settings, Block> block, String id) {
        return register(block, AbstractBlock.Settings.create(), id);
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
