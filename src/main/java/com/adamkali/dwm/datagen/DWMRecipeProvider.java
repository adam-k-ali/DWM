package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DWMRecipeProvider extends FabricRecipeProvider {
    public DWMRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter) {
        return new RecipeGenerator(registryLookup, exporter) {
            @Override
            public void generate() {
                offer2x2CompactingRecipe(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.GALLIFREY_STONE_BRICKS, DWMBlocks.GALLIFREY_STONE);
                offer2x2CompactingRecipe(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.GALLIFREY_SANDSTONE, DWMBlocks.GALLIFREY_SAND);
                offer2x2CompactingRecipe(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.GALLIFREY_CUT_SANDSTONE, DWMBlocks.GALLIFREY_SANDSTONE);

                createShaped(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CHISELED_GALLIFREY_STONE_BRICKS)
                        .input('#', DWMBlocks.GALLIFREY_STONE_BRICKS)
                        .pattern("#")
                        .pattern("#")
                        .criterion(hasItem(DWMBlocks.GALLIFREY_STONE_BRICKS), conditionsFromItem(DWMBlocks.GALLIFREY_STONE_BRICKS))
                        .offerTo(exporter);

                createShaped(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.GALLIFREY_CHISELED_SANDSTONE)
                        .input('#', DWMBlocks.GALLIFREY_SANDSTONE)
                        .pattern("#")
                        .pattern("#")
                        .criterion(hasItem(DWMBlocks.GALLIFREY_SANDSTONE), conditionsFromItem(DWMBlocks.GALLIFREY_SANDSTONE))
                        .offerTo(exporter);

                createShapeless(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.MOSSY_GALLIFREY_STONE_BRICKS)
                        .input(DWMBlocks.GALLIFREY_STONE_BRICKS)
                        .input(Items.VINE)
                        .criterion(hasItem(DWMBlocks.GALLIFREY_STONE_BRICKS), conditionsFromItem(DWMBlocks.GALLIFREY_STONE_BRICKS))
                        .offerTo(exporter);

                createShapeless(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE)
                        .input(DWMBlocks.GALLIFREY_COBBLESTONE)
                        .input(Items.VINE)
                        .criterion(hasItem(DWMBlocks.GALLIFREY_COBBLESTONE), conditionsFromItem(DWMBlocks.GALLIFREY_COBBLESTONE))
                        .offerTo(exporter);

                createShapeless(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.GALLIFREY_COARSE_DIRT, 4)
                        .input(DWMBlocks.GALLIFREY_DIRT)
                        .input(DWMBlocks.GALLIFREY_DIRT)
                        .input(Items.GRAVEL)
                        .input(Items.GRAVEL)
                        .criterion(hasItem(DWMBlocks.GALLIFREY_DIRT), conditionsFromItem(DWMBlocks.GALLIFREY_DIRT))
                        .offerTo(exporter);

                offerSmelting(
                        List.of(DWMBlocks.GALLIFREY_COBBLESTONE),
                        RecipeCategory.BUILDING_BLOCKS,
                        DWMBlocks.GALLIFREY_STONE,
                        0.1F,
                        200,
                        "gallifrey_stone"
                );
                offerSmelting(
                        List.of(DWMBlocks.GALLIFREY_STONE),
                        RecipeCategory.BUILDING_BLOCKS,
                        DWMBlocks.GALLIFREY_SMOOTH_STONE,
                        0.1F,
                        200,
                        "gallifrey_smooth_stone"
                );
                offerSmelting(
                        List.of(DWMBlocks.GALLIFREY_STONE_BRICKS),
                        RecipeCategory.BUILDING_BLOCKS,
                        DWMBlocks.CRACKED_GALLIFREY_STONE_BRICKS,
                        0.1F,
                        200,
                        "cracked_gallifrey_stone_bricks"
                );

                offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_PANEL, DWMBlocks.CITADEL_WALL);
                offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_TILE, DWMBlocks.CITADEL_WALL);
                offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_WALL, DWMBlocks.CITADEL_PANEL);
                offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_TILE, DWMBlocks.CITADEL_PANEL);
                offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_WALL, DWMBlocks.CITADEL_TILE);
                offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_PANEL, DWMBlocks.CITADEL_TILE);

                createShaped(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.CITADEL_GLASS, 8)
                        .input('#', DWMBlocks.CITADEL_WALL)
                        .input('G', Items.GLASS)
                        .pattern("###")
                        .pattern("#G#")
                        .pattern("###")
                        .criterion(hasItem(DWMBlocks.CITADEL_WALL), conditionsFromItem(DWMBlocks.CITADEL_WALL))
                        .offerTo(exporter);

                for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
                    WoodFamilyDatagen.generateRecipes(this, exporter, family);
                }
            }
        };
    }

    @Override
    public String getName() {
        return "DWM Recipes";
    }
}
