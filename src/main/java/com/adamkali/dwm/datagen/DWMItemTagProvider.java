package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import java.util.concurrent.CompletableFuture;

public class DWMItemTagProvider extends FabricTagsProvider.ItemTagProvider {
    public DWMItemTagProvider(
            FabricPackOutput output,
            CompletableFuture<HolderLookup.Provider> completableFuture,
            FabricTagsProvider.BlockTagProvider blockTagProvider
    ) {
        super(output, completableFuture, blockTagProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        copy(com.adamkali.dwm.block.DWMBlockTags.GALLIFREY_STONE, com.adamkali.dwm.item.DWMItemTags.GALLIFREY_STONE);
        copy(com.adamkali.dwm.block.DWMBlockTags.CITADEL, com.adamkali.dwm.item.DWMItemTags.CITADEL);
        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyDatagen.generateItemTags(new WoodFamilyDatagen.ItemTagSink() {
                @Override
                public void copy(TagKey<net.minecraft.world.level.block.Block> blockTag, TagKey<Item> itemTag) {
                    DWMItemTagProvider.this.copy(blockTag, itemTag);
                }

                @Override
                public void addToTag(TagKey<Item> tag, Item item) {
                    getOrCreateTagBuilder(tag).add(item);
                }
            }, family);
        }
        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.LOGS, ItemTags.LOGS);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
        copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
        copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        copy(BlockTags.DOORS, ItemTags.DOORS);
        copy(BlockTags.TRAPDOORS, ItemTags.TRAPDOORS);
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
    }
}
