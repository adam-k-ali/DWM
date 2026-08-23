package com.adamkali.dwm.neoforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.GameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Opens NeoForge/vanilla registries so Fabric-style {@code Registry.register} calls in
 * {@link com.adamkali.dwm.DwmCommon} can run during mod construction, then repairs the
 * NeoForge block→item map used by {@link Block#asItem()}.
 *
 * <p>NeoForge freezes mapped registries before mod constructors and populates
 * {@link GameData#getBlockItemMap()} via item-registry add callbacks. Direct vanilla
 * registration after {@link GameData#unfreezeData()} can leave that map incomplete,
 * so creative tabs see {@code minecraft:air} for DWM blocks.
 */
final class NeoForgeRegistryBootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger("dwm");

    private NeoForgeRegistryBootstrap() {
    }

    static void unlockForFabricStyleRegistration() {
        // Unfreezes every MappedRegistry in BuiltInRegistries (NeoForge API).
        GameData.unfreezeData();
    }

    static void syncBlockItemMapAfterRegistration() {
        Map<Block, Item> blockToItem = GameData.getBlockItemMap();
        int synced = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BlockItem blockItem) {
                blockItem.registerBlocks(blockToItem, item);
                synced++;
            }
        }
        LOGGER.debug("Synced {} BlockItem entries into NeoForge block→item map", synced);
    }
}
