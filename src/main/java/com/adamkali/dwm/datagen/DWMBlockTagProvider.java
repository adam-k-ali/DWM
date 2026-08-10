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

public class DWMBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public DWMBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(DWMBlockTags.GALLIFREY_STONE)
                .add(key(DWMBlocks.GALLIFREY_STONE))
                .add(key(DWMBlocks.GALLIFREY_STONE_BRICKS))
                .add(key(DWMBlocks.CHISELED_GALLIFREY_STONE_BRICKS))
                .add(key(DWMBlocks.CRACKED_GALLIFREY_STONE_BRICKS))
                .add(key(DWMBlocks.MOSSY_GALLIFREY_STONE_BRICKS))
                .add(key(DWMBlocks.GALLIFREY_COBBLESTONE))
                .add(key(DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE))
                .add(key(DWMBlocks.GALLIFREY_SMOOTH_STONE))
                .add(key(DWMBlocks.GALLIFREY_SANDSTONE))
                .add(key(DWMBlocks.GALLIFREY_CUT_SANDSTONE))
                .add(key(DWMBlocks.GALLIFREY_CHISELED_SANDSTONE))
                .add(key(DWMBlocks.GALLIFREY_SAND))
                .add(key(DWMBlocks.GALLIFREY_DIRT))
                .add(key(DWMBlocks.GALLIFREY_COARSE_DIRT));

        builder(DWMBlockTags.CITADEL)
                .add(key(DWMBlocks.CITADEL_WALL))
                .add(key(DWMBlocks.CITADEL_PANEL))
                .add(key(DWMBlocks.CITADEL_TILE))
                .add(key(DWMBlocks.CITADEL_GLASS));

        builder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(key(DWMBlocks.GALLIFREY_STONE))
                .add(key(DWMBlocks.GALLIFREY_STONE_BRICKS))
                .add(key(DWMBlocks.CHISELED_GALLIFREY_STONE_BRICKS))
                .add(key(DWMBlocks.CRACKED_GALLIFREY_STONE_BRICKS))
                .add(key(DWMBlocks.MOSSY_GALLIFREY_STONE_BRICKS))
                .add(key(DWMBlocks.GALLIFREY_COBBLESTONE))
                .add(key(DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE))
                .add(key(DWMBlocks.GALLIFREY_SMOOTH_STONE))
                .add(key(DWMBlocks.GALLIFREY_SANDSTONE))
                .add(key(DWMBlocks.GALLIFREY_CUT_SANDSTONE))
                .add(key(DWMBlocks.GALLIFREY_CHISELED_SANDSTONE))
                .add(key(DWMBlocks.CITADEL_WALL))
                .add(key(DWMBlocks.CITADEL_PANEL))
                .add(key(DWMBlocks.CITADEL_TILE));

        builder(BlockTags.IMPERMEABLE)
                .add(key(DWMBlocks.CITADEL_GLASS));

        builder(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(key(DWMBlocks.GALLIFREY_SAND))
                .add(key(DWMBlocks.GALLIFREY_DIRT))
                .add(key(DWMBlocks.GALLIFREY_COARSE_DIRT));

        builder(BlockTags.SAND)
                .add(key(DWMBlocks.GALLIFREY_SAND));

        builder(BlockTags.DIRT)
                .add(key(DWMBlocks.GALLIFREY_DIRT))
                .add(key(DWMBlocks.GALLIFREY_COARSE_DIRT));

        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyDatagen.generateBlockTags(new WoodFamilyDatagen.BlockTagSink() {
                @Override
                public void addToTag(TagKey<Block> tag, Block block) {
                    builder(tag).add(key(block));
                }

                @Override
                public void addTagToTag(TagKey<Block> tag, TagKey<Block> nested) {
                    builder(tag).addTag(nested);
                }
            }, family);
        }
    }

    private static net.minecraft.resources.ResourceKey<Block> key(Block block) {
        return block.builtInRegistryHolder().key();
    }
}
