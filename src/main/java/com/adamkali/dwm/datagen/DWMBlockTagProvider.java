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

        getOrCreateTagBuilder(DWMBlockTags.ASH_LOGS)
                .add(DWMBlocks.ASH_LOG)
                .add(DWMBlocks.ASH_WOOD)
                .add(DWMBlocks.STRIPPED_ASH_LOG)
                .add(DWMBlocks.STRIPPED_ASH_WOOD);

        getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN)
                .addTag(DWMBlockTags.ASH_LOGS);

        getOrCreateTagBuilder(BlockTags.LOGS)
                .addTag(DWMBlockTags.ASH_LOGS);

        getOrCreateTagBuilder(BlockTags.OVERWORLD_NATURAL_LOGS)
                .add(DWMBlocks.ASH_LOG);

        getOrCreateTagBuilder(BlockTags.PLANKS)
                .add(DWMBlocks.ASH_PLANKS);

        getOrCreateTagBuilder(BlockTags.WOODEN_STAIRS)
                .add(DWMBlocks.ASH_STAIRS);

        getOrCreateTagBuilder(BlockTags.WOODEN_SLABS)
                .add(DWMBlocks.ASH_SLAB);

        getOrCreateTagBuilder(BlockTags.WOODEN_FENCES)
                .add(DWMBlocks.ASH_FENCE);

        getOrCreateTagBuilder(BlockTags.FENCE_GATES)
                .add(DWMBlocks.ASH_FENCE_GATE);

        getOrCreateTagBuilder(BlockTags.WOODEN_BUTTONS)
                .add(DWMBlocks.ASH_BUTTON);

        getOrCreateTagBuilder(BlockTags.WOODEN_PRESSURE_PLATES)
                .add(DWMBlocks.ASH_PRESSURE_PLATE);

        getOrCreateTagBuilder(BlockTags.STANDING_SIGNS)
                .add(DWMBlocks.ASH_SIGN);

        getOrCreateTagBuilder(BlockTags.WALL_SIGNS)
                .add(DWMBlocks.ASH_WALL_SIGN);

        getOrCreateTagBuilder(BlockTags.CEILING_HANGING_SIGNS)
                .add(DWMBlocks.ASH_HANGING_SIGN);

        getOrCreateTagBuilder(BlockTags.WALL_HANGING_SIGNS)
                .add(DWMBlocks.ASH_WALL_HANGING_SIGN);

        getOrCreateTagBuilder(BlockTags.LEAVES)
                .add(DWMBlocks.ASH_LEAVES);

        getOrCreateTagBuilder(BlockTags.SAPLINGS)
                .add(DWMBlocks.ASH_SAPLING);

        getOrCreateTagBuilder(BlockTags.FLOWER_POTS)
                .add(DWMBlocks.POTTED_ASH_SAPLING);

        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(DWMBlocks.ASH_PLANKS)
                .add(DWMBlocks.ASH_LOG)
                .add(DWMBlocks.ASH_WOOD)
                .add(DWMBlocks.STRIPPED_ASH_LOG)
                .add(DWMBlocks.STRIPPED_ASH_WOOD)
                .add(DWMBlocks.ASH_STAIRS)
                .add(DWMBlocks.ASH_SLAB)
                .add(DWMBlocks.ASH_FENCE)
                .add(DWMBlocks.ASH_FENCE_GATE)
                .add(DWMBlocks.ASH_BUTTON)
                .add(DWMBlocks.ASH_PRESSURE_PLATE)
                .add(DWMBlocks.ASH_SIGN)
                .add(DWMBlocks.ASH_WALL_SIGN)
                .add(DWMBlocks.ASH_HANGING_SIGN)
                .add(DWMBlocks.ASH_WALL_HANGING_SIGN);
    }
}
