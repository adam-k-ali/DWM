package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlockTags;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import java.util.concurrent.CompletableFuture;

public class DWMBlockTagProvider extends FabricTagsProvider.BlockTagProvider {
    public DWMBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        getOrCreateTagBuilder(DWMBlockTags.GALLIFREY_STONE)
                .add(DWMBlocks.GALLIFREY_STONE)
                .add(DWMBlocks.GALLIFREY_STONE_BRICKS)
                .add(DWMBlocks.CHISELED_GALLIFREY_STONE_BRICKS)
                .add(DWMBlocks.CRACKED_GALLIFREY_STONE_BRICKS)
                .add(DWMBlocks.MOSSY_GALLIFREY_STONE_BRICKS)
                .add(DWMBlocks.GALLIFREY_COBBLESTONE)
                .add(DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE)
                .add(DWMBlocks.GALLIFREY_SMOOTH_STONE)
                .add(DWMBlocks.GALLIFREY_SANDSTONE)
                .add(DWMBlocks.GALLIFREY_CUT_SANDSTONE)
                .add(DWMBlocks.GALLIFREY_CHISELED_SANDSTONE)
                .add(DWMBlocks.GALLIFREY_SAND)
                .add(DWMBlocks.GALLIFREY_DIRT)
                .add(DWMBlocks.GALLIFREY_COARSE_DIRT);

        getOrCreateTagBuilder(DWMBlockTags.CITADEL)
                .add(DWMBlocks.CITADEL_WALL)
                .add(DWMBlocks.CITADEL_PANEL)
                .add(DWMBlocks.CITADEL_TILE)
                .add(DWMBlocks.CITADEL_GLASS);

        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(DWMBlocks.GALLIFREY_STONE)
                .add(DWMBlocks.GALLIFREY_STONE_BRICKS)
                .add(DWMBlocks.CHISELED_GALLIFREY_STONE_BRICKS)
                .add(DWMBlocks.CRACKED_GALLIFREY_STONE_BRICKS)
                .add(DWMBlocks.MOSSY_GALLIFREY_STONE_BRICKS)
                .add(DWMBlocks.GALLIFREY_COBBLESTONE)
                .add(DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE)
                .add(DWMBlocks.GALLIFREY_SMOOTH_STONE)
                .add(DWMBlocks.GALLIFREY_SANDSTONE)
                .add(DWMBlocks.GALLIFREY_CUT_SANDSTONE)
                .add(DWMBlocks.GALLIFREY_CHISELED_SANDSTONE)
                .add(DWMBlocks.CITADEL_WALL)
                .add(DWMBlocks.CITADEL_PANEL)
                .add(DWMBlocks.CITADEL_TILE);

        getOrCreateTagBuilder(BlockTags.IMPERMEABLE)
                .add(DWMBlocks.CITADEL_GLASS);

        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(DWMBlocks.GALLIFREY_SAND)
                .add(DWMBlocks.GALLIFREY_DIRT)
                .add(DWMBlocks.GALLIFREY_COARSE_DIRT);

        getOrCreateTagBuilder(BlockTags.SAND)
                .add(DWMBlocks.GALLIFREY_SAND);

        getOrCreateTagBuilder(BlockTags.DIRT)
                .add(DWMBlocks.GALLIFREY_DIRT)
                .add(DWMBlocks.GALLIFREY_COARSE_DIRT);

        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyDatagen.generateBlockTags(new WoodFamilyDatagen.BlockTagSink() {
                @Override
                public void addToTag(TagKey<Block> tag, Block block) {
                    getOrCreateTagBuilder(tag).add(block);
                }

                @Override
                public void addTagToTag(TagKey<Block> tag, TagKey<Block> nested) {
                    getOrCreateTagBuilder(tag).addTag(nested);
                }
            }, family);
        }
    }
}
