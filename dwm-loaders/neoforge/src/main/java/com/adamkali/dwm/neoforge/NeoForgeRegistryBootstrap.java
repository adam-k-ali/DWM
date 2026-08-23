package com.adamkali.dwm.neoforge;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.GameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Opens NeoForge/vanilla registries so Fabric-style {@code Registry.register} calls in
 * {@link com.adamkali.dwm.DwmCommon} can run during mod construction, then repairs the
 * NeoForge block→item map used by {@link Block#asItem()} and block-state shape caches.
 *
 * <p>NeoForge freezes mapped registries before mod constructors and populates
 * {@link GameData#getBlockItemMap()} via item-registry add callbacks. Direct vanilla
 * registration after {@link GameData#unfreezeData()} can leave that map incomplete,
 * so creative tabs see {@code minecraft:air} for DWM blocks.
 *
 * <p>NeoForge also runs {@link BlockState#initCache()} only for blocks observed by its
 * registry bake callbacks. Blocks registered during the early unlock window never get
 * {@code onAdd}, so neighbor occlusion during chunk meshing NPEs on
 * {@code occlusionShapesByFace}. Call {@link #initBlockStateCachesAfterRegistration()}
 * after {@link com.adamkali.dwm.DwmCommon#init()}.
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

    static void initBlockStateCachesAfterRegistration() {
        int states = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null || !DWMReference.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                state.initCache();
                states++;
            }
        }
        LOGGER.debug("Initialized occlusion caches for {} DWM block states", states);
    }
}
