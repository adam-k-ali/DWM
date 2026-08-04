package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlockTags;
import com.adamkali.dwm.block.DWMBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class DWMBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public DWMBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
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

        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
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
                .add(DWMBlocks.GALLIFREY_CHISELED_SANDSTONE);

        getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE)
                .add(DWMBlocks.GALLIFREY_SAND)
                .add(DWMBlocks.GALLIFREY_DIRT)
                .add(DWMBlocks.GALLIFREY_COARSE_DIRT);

        getOrCreateTagBuilder(BlockTags.SAND)
                .add(DWMBlocks.GALLIFREY_SAND);

        getOrCreateTagBuilder(BlockTags.DIRT)
                .add(DWMBlocks.GALLIFREY_DIRT)
                .add(DWMBlocks.GALLIFREY_COARSE_DIRT);
    }
}
