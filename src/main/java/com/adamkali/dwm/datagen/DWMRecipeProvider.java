package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DWMRecipeProvider extends FabricRecipeProvider {
    public DWMRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {
                twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.GALLIFREY_STONE_BRICKS, DWMBlocks.GALLIFREY_STONE);
                twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.GALLIFREY_SANDSTONE, DWMBlocks.GALLIFREY_SAND);
                twoByTwoPacker(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.GALLIFREY_CUT_SANDSTONE, DWMBlocks.GALLIFREY_SANDSTONE);

                shaped(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CHISELED_GALLIFREY_STONE_BRICKS)
                        .define('#', DWMBlocks.GALLIFREY_STONE_BRICKS)
                        .pattern("#")
                        .pattern("#")
                        .unlockedBy(getHasName(DWMBlocks.GALLIFREY_STONE_BRICKS), has(DWMBlocks.GALLIFREY_STONE_BRICKS))
                        .save(output);

                shaped(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.GALLIFREY_CHISELED_SANDSTONE)
                        .define('#', DWMBlocks.GALLIFREY_SANDSTONE)
                        .pattern("#")
                        .pattern("#")
                        .unlockedBy(getHasName(DWMBlocks.GALLIFREY_SANDSTONE), has(DWMBlocks.GALLIFREY_SANDSTONE))
                        .save(output);

                shapeless(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.MOSSY_GALLIFREY_STONE_BRICKS)
                        .requires(DWMBlocks.GALLIFREY_STONE_BRICKS)
                        .requires(Items.VINE)
                        .unlockedBy(getHasName(DWMBlocks.GALLIFREY_STONE_BRICKS), has(DWMBlocks.GALLIFREY_STONE_BRICKS))
                        .save(output);

                shapeless(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE)
                        .requires(DWMBlocks.GALLIFREY_COBBLESTONE)
                        .requires(Items.VINE)
                        .unlockedBy(getHasName(DWMBlocks.GALLIFREY_COBBLESTONE), has(DWMBlocks.GALLIFREY_COBBLESTONE))
                        .save(output);

                shapeless(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.GALLIFREY_COARSE_DIRT, 4)
                        .requires(DWMBlocks.GALLIFREY_DIRT)
                        .requires(DWMBlocks.GALLIFREY_DIRT)
                        .requires(Items.GRAVEL)
                        .requires(Items.GRAVEL)
                        .unlockedBy(getHasName(DWMBlocks.GALLIFREY_DIRT), has(DWMBlocks.GALLIFREY_DIRT))
                        .save(output);

                oreSmelting(
                        List.of(DWMBlocks.GALLIFREY_COBBLESTONE),
                        RecipeCategory.BUILDING_BLOCKS,
                        DWMBlocks.GALLIFREY_STONE,
                        0.1F,
                        200,
                        "gallifrey_stone"
                );
                oreSmelting(
                        List.of(DWMBlocks.GALLIFREY_STONE),
                        RecipeCategory.BUILDING_BLOCKS,
                        DWMBlocks.GALLIFREY_SMOOTH_STONE,
                        0.1F,
                        200,
                        "gallifrey_smooth_stone"
                );
                oreSmelting(
                        List.of(DWMBlocks.GALLIFREY_STONE_BRICKS),
                        RecipeCategory.BUILDING_BLOCKS,
                        DWMBlocks.CRACKED_GALLIFREY_STONE_BRICKS,
                        0.1F,
                        200,
                        "cracked_gallifrey_stone_bricks"
                );

                stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_PANEL, DWMBlocks.CITADEL_WALL);
                stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_TILE, DWMBlocks.CITADEL_WALL);
                stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_WALL, DWMBlocks.CITADEL_PANEL);
                stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_TILE, DWMBlocks.CITADEL_PANEL);
                stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_WALL, DWMBlocks.CITADEL_TILE);
                stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_PANEL, DWMBlocks.CITADEL_TILE);

                shaped(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_GLASS, 8)
                        .define('#', DWMBlocks.CITADEL_WALL)
                        .define('G', Items.GLASS)
                        .pattern("###")
                        .pattern("#G#")
                        .pattern("###")
                        .unlockedBy(getHasName(DWMBlocks.CITADEL_WALL), has(DWMBlocks.CITADEL_WALL))
                        .save(output);

                for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
                    WoodFamilyDatagen.generateRecipes(this, output, family);
                }
            }
        };
    }

    @Override
    public String getName() {
        return "DWM Recipes";
    }
}
