package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.item.DWMItemTags;
import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
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

                offerPlanksRecipe(DWMBlocks.ASH_PLANKS, DWMItemTags.ASH_LOGS, 4);
                offerBarkBlockRecipe(DWMBlocks.ASH_WOOD, DWMBlocks.ASH_LOG);
                offerBarkBlockRecipe(DWMBlocks.STRIPPED_ASH_WOOD, DWMBlocks.STRIPPED_ASH_LOG);
                createStairsRecipe(DWMBlocks.ASH_STAIRS, Ingredient.ofItem(DWMBlocks.ASH_PLANKS))
                        .criterion(hasItem(DWMBlocks.ASH_PLANKS), conditionsFromItem(DWMBlocks.ASH_PLANKS))
                        .offerTo(exporter);
                offerSlabRecipe(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.ASH_SLAB, DWMBlocks.ASH_PLANKS);
                createFenceRecipe(DWMBlocks.ASH_FENCE, Ingredient.ofItem(DWMBlocks.ASH_PLANKS))
                        .criterion(hasItem(DWMBlocks.ASH_PLANKS), conditionsFromItem(DWMBlocks.ASH_PLANKS))
                        .offerTo(exporter);
                createFenceGateRecipe(DWMBlocks.ASH_FENCE_GATE, Ingredient.ofItem(DWMBlocks.ASH_PLANKS))
                        .criterion(hasItem(DWMBlocks.ASH_PLANKS), conditionsFromItem(DWMBlocks.ASH_PLANKS))
                        .offerTo(exporter);
                offerPressurePlateRecipe(DWMBlocks.ASH_PRESSURE_PLATE, DWMBlocks.ASH_PLANKS);
                createButtonRecipe(DWMBlocks.ASH_BUTTON, Ingredient.ofItem(DWMBlocks.ASH_PLANKS))
                        .criterion(hasItem(DWMBlocks.ASH_PLANKS), conditionsFromItem(DWMBlocks.ASH_PLANKS))
                        .offerTo(exporter);
                createSignRecipe(DWMItems.ASH_SIGN, Ingredient.ofItem(DWMBlocks.ASH_PLANKS))
                        .criterion(hasItem(DWMBlocks.ASH_PLANKS), conditionsFromItem(DWMBlocks.ASH_PLANKS))
                        .offerTo(exporter);
                offerHangingSignRecipe(DWMItems.ASH_HANGING_SIGN, DWMBlocks.STRIPPED_ASH_LOG);
                offerBoatRecipe(DWMItems.ASH_BOAT, DWMBlocks.ASH_PLANKS);

                offerPlanksRecipe(DWMBlocks.DARK_ASH_PLANKS, DWMItemTags.DARK_ASH_LOGS, 4);
                offerBarkBlockRecipe(DWMBlocks.DARK_ASH_WOOD, DWMBlocks.DARK_ASH_LOG);
                offerBarkBlockRecipe(DWMBlocks.STRIPPED_DARK_ASH_WOOD, DWMBlocks.STRIPPED_DARK_ASH_LOG);
                createStairsRecipe(DWMBlocks.DARK_ASH_STAIRS, Ingredient.ofItem(DWMBlocks.DARK_ASH_PLANKS))
                        .criterion(hasItem(DWMBlocks.DARK_ASH_PLANKS), conditionsFromItem(DWMBlocks.DARK_ASH_PLANKS))
                        .offerTo(exporter);
                offerSlabRecipe(RecipeCategory.BUILDING_BLOCKS, DWMBlocks.DARK_ASH_SLAB, DWMBlocks.DARK_ASH_PLANKS);
                createFenceRecipe(DWMBlocks.DARK_ASH_FENCE, Ingredient.ofItem(DWMBlocks.DARK_ASH_PLANKS))
                        .criterion(hasItem(DWMBlocks.DARK_ASH_PLANKS), conditionsFromItem(DWMBlocks.DARK_ASH_PLANKS))
                        .offerTo(exporter);
                createFenceGateRecipe(DWMBlocks.DARK_ASH_FENCE_GATE, Ingredient.ofItem(DWMBlocks.DARK_ASH_PLANKS))
                        .criterion(hasItem(DWMBlocks.DARK_ASH_PLANKS), conditionsFromItem(DWMBlocks.DARK_ASH_PLANKS))
                        .offerTo(exporter);
                offerPressurePlateRecipe(DWMBlocks.DARK_ASH_PRESSURE_PLATE, DWMBlocks.DARK_ASH_PLANKS);
                createButtonRecipe(DWMBlocks.DARK_ASH_BUTTON, Ingredient.ofItem(DWMBlocks.DARK_ASH_PLANKS))
                        .criterion(hasItem(DWMBlocks.DARK_ASH_PLANKS), conditionsFromItem(DWMBlocks.DARK_ASH_PLANKS))
                        .offerTo(exporter);
                createTrapdoorRecipe(DWMBlocks.DARK_ASH_TRAPDOOR, Ingredient.ofItem(DWMBlocks.DARK_ASH_PLANKS))
                        .criterion(hasItem(DWMBlocks.DARK_ASH_PLANKS), conditionsFromItem(DWMBlocks.DARK_ASH_PLANKS))
                        .offerTo(exporter);
                createSignRecipe(DWMItems.DARK_ASH_SIGN, Ingredient.ofItem(DWMBlocks.DARK_ASH_PLANKS))
                        .criterion(hasItem(DWMBlocks.DARK_ASH_PLANKS), conditionsFromItem(DWMBlocks.DARK_ASH_PLANKS))
                        .offerTo(exporter);
                offerHangingSignRecipe(DWMItems.DARK_ASH_HANGING_SIGN, DWMBlocks.STRIPPED_DARK_ASH_LOG);
                offerBoatRecipe(DWMItems.DARK_ASH_BOAT, DWMBlocks.DARK_ASH_PLANKS);
            }
        };
    }

    @Override
    public String getName() {
        return "DWM Recipes";
    }
}
