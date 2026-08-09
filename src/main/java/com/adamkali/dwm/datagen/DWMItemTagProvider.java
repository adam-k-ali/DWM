package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;

import java.util.concurrent.CompletableFuture;

public class DWMItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public DWMItemTagProvider(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture,
            FabricTagProvider.BlockTagProvider blockTagProvider
    ) {
        super(output, completableFuture, blockTagProvider);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        copy(com.adamkali.dwm.block.DWMBlockTags.GALLIFREY_STONE, com.adamkali.dwm.item.DWMItemTags.GALLIFREY_STONE);
        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyDatagen.generateItemTags(new WoodFamilyDatagen.ItemTagSink() {
                @Override
                public void copy(TagKey<net.minecraft.block.Block> blockTag, TagKey<Item> itemTag) {
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
