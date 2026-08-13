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
        var gallifreyStoneTag = builder(DWMBlockTags.GALLIFREY_STONE);
        for (Block block : DWMBlocks.GALLIFREY_STONE_FAMILY) {
            gallifreyStoneTag.add(key(block));
        }

        builder(DWMBlockTags.CITADEL)
                .add(key(DWMBlocks.CITADEL_WALL))
                .add(key(DWMBlocks.CITADEL_PANEL))
                .add(key(DWMBlocks.CITADEL_TILE))
                .add(key(DWMBlocks.CITADEL_GLASS));

        var orangeSandTag = builder(DWMBlockTags.ORANGE_SAND);
        for (Block block : DWMBlocks.ORANGE_SAND_FAMILY) {
            orangeSandTag.add(key(block));
        }

        var gallifreyPlantsTag = builder(DWMBlockTags.GALLIFREY_PLANTS);
        for (Block plant : DWMBlocks.GALLIFREY_PLANTS) {
            gallifreyPlantsTag.add(key(plant));
        }

        builder(DWMBlockTags.GALLIFREY_ORE_REPLACEABLES)
                .add(key(DWMBlocks.GALLIFREY_STONE));

        builder(DWMBlockTags.AZBANTIUM_ORES)
                .add(key(DWMBlocks.AZBANTIUM_ORE));

        var smallFlowers = builder(BlockTags.SMALL_FLOWERS);
        for (Block flower : DWMBlocks.GALLIFREY_CROSS_PLANTS) {
            smallFlowers.add(key(flower));
        }

        var flowers = builder(BlockTags.FLOWERS);
        for (Block flower : DWMBlocks.GALLIFREY_CROSS_PLANTS) {
            flowers.add(key(flower));
        }

        var flowerPots = builder(BlockTags.FLOWER_POTS);
        for (Block potted : DWMBlocks.GALLIFREY_POTTED_PLANTS) {
            flowerPots.add(key(potted));
        }

        var pickaxe = builder(BlockTags.MINEABLE_WITH_PICKAXE);
        for (Block block : DWMBlocks.GALLIFREY_STONE_BUILDING_BLOCKS) {
            pickaxe.add(key(block));
        }
        pickaxe
                .add(key(DWMBlocks.ORANGE_SANDSTONE))
                .add(key(DWMBlocks.ORANGE_SANDSTONE_STAIRS))
                .add(key(DWMBlocks.ORANGE_SANDSTONE_SLAB))
                .add(key(DWMBlocks.ORANGE_SANDSTONE_WALL))
                .add(key(DWMBlocks.CUT_ORANGE_SANDSTONE))
                .add(key(DWMBlocks.CUT_ORANGE_SANDSTONE_SLAB))
                .add(key(DWMBlocks.CHISELED_ORANGE_SANDSTONE))
                .add(key(DWMBlocks.SMOOTH_ORANGE_SANDSTONE))
                .add(key(DWMBlocks.SMOOTH_ORANGE_SANDSTONE_STAIRS))
                .add(key(DWMBlocks.SMOOTH_ORANGE_SANDSTONE_SLAB))
                .add(key(DWMBlocks.CITADEL_WALL))
                .add(key(DWMBlocks.CITADEL_PANEL))
                .add(key(DWMBlocks.CITADEL_TILE))
                .add(key(DWMBlocks.AZBANTIUM_ORE))
                .add(key(DWMBlocks.AZBANTIUM_BLOCK))
                .add(key(DWMBlocks.GALLIFREY_COAL_ORE))
                .add(key(DWMBlocks.GALLIFREY_IRON_ORE))
                .add(key(DWMBlocks.GALLIFREY_GOLD_ORE))
                .add(key(DWMBlocks.GALLIFREY_DIAMOND_ORE));

        builder(BlockTags.NEEDS_STONE_TOOL)
                .add(key(DWMBlocks.GALLIFREY_IRON_ORE));

        builder(BlockTags.NEEDS_IRON_TOOL)
                .add(key(DWMBlocks.GALLIFREY_GOLD_ORE))
                .add(key(DWMBlocks.GALLIFREY_DIAMOND_ORE));

        builder(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(key(DWMBlocks.AZBANTIUM_ORE))
                .add(key(DWMBlocks.AZBANTIUM_BLOCK));

        builder(DWMBlockTags.COAL_ORES)
                .add(key(DWMBlocks.GALLIFREY_COAL_ORE));
        builder(BlockTags.IRON_ORES)
                .add(key(DWMBlocks.GALLIFREY_IRON_ORE));
        builder(BlockTags.GOLD_ORES)
                .add(key(DWMBlocks.GALLIFREY_GOLD_ORE));
        builder(DWMBlockTags.DIAMOND_ORES)
                .add(key(DWMBlocks.GALLIFREY_DIAMOND_ORE));

        builder(BlockTags.IMPERMEABLE)
                .add(key(DWMBlocks.CITADEL_GLASS));

        builder(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(key(DWMBlocks.GALLIFREY_SAND))
                .add(key(DWMBlocks.ORANGE_SAND))
                .add(key(DWMBlocks.GALLIFREY_DIRT))
                .add(key(DWMBlocks.GALLIFREY_COARSE_DIRT))
                .add(key(DWMBlocks.GALLIFREY_GRASS_BLOCK));

        builder(BlockTags.SAND)
                .add(key(DWMBlocks.GALLIFREY_SAND))
                .add(key(DWMBlocks.ORANGE_SAND));

        var stairs = builder(BlockTags.STAIRS)
                .add(key(DWMBlocks.ORANGE_SANDSTONE_STAIRS))
                .add(key(DWMBlocks.SMOOTH_ORANGE_SANDSTONE_STAIRS));
        stairs.add(key(DWMBlocks.GALLIFREY_COBBLESTONE_STAIRS));
        stairs.add(key(DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE_STAIRS));
        stairs.add(key(DWMBlocks.GALLIFREY_STONE_BRICK_STAIRS));
        stairs.add(key(DWMBlocks.MOSSY_GALLIFREY_STONE_BRICK_STAIRS));

        var slabs = builder(BlockTags.SLABS)
                .add(key(DWMBlocks.ORANGE_SANDSTONE_SLAB))
                .add(key(DWMBlocks.CUT_ORANGE_SANDSTONE_SLAB))
                .add(key(DWMBlocks.SMOOTH_ORANGE_SANDSTONE_SLAB));
        for (Block slab : DWMBlocks.GALLIFREY_STONE_SLABS) {
            slabs.add(key(slab));
        }

        var walls = builder(BlockTags.WALLS)
                .add(key(DWMBlocks.ORANGE_SANDSTONE_WALL));
        walls.add(key(DWMBlocks.GALLIFREY_COBBLESTONE_WALL));
        walls.add(key(DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE_WALL));
        walls.add(key(DWMBlocks.GALLIFREY_STONE_BRICK_WALL));
        walls.add(key(DWMBlocks.MOSSY_GALLIFREY_STONE_BRICK_WALL));

        builder(BlockTags.DIRT)
                .add(key(DWMBlocks.GALLIFREY_DIRT))
                .add(key(DWMBlocks.GALLIFREY_COARSE_DIRT))
                .add(key(DWMBlocks.GALLIFREY_GRASS_BLOCK));

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
