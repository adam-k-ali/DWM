package com.adamkali.dwm.guide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class FieldGuideRecipePanel {
    private FieldGuideRecipePanel() {
    }

    public enum Station {
        CRAFTING("dwm.guide.recipe.crafting"),
        SMELTING("dwm.guide.recipe.smelting"),
        STONECUTTING("dwm.guide.recipe.stonecutting");

        private final String labelKey;

        Station(String labelKey) {
            this.labelKey = labelKey;
        }

        public Component label() {
            return Component.translatable(labelKey);
        }
    }

    public static List<Station> availableStations(FieldGuidePage page) {
        List<Station> stations = new java.util.ArrayList<>(3);
        if (page.craftingRecipe() != null) {
            stations.add(Station.CRAFTING);
        }
        if (page.smeltingRecipe() != null) {
            stations.add(Station.SMELTING);
        }
        if (page.stonecuttingRecipe() != null) {
            stations.add(Station.STONECUTTING);
        }
        return stations;
    }

    public static void render(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int x,
            int y,
            FieldGuidePage page,
            Station station
    ) {
        Identifier recipeId = recipeIdFor(page, station);
        if (recipeId == null) {
            return;
        }

        Optional<RecipeHolder<?>> holder = recipeHolder(client, recipeId);
        if (holder.isEmpty()) {
            graphics.text(client.font, Component.translatable("dwm.guide.recipe.unavailable"), x, y, 0xFF888888, false);
            return;
        }

        Recipe<?> recipe = holder.get().value();
        graphics.text(client.font, station.label(), x, y, 0xFFD4A84B, false);
        int lineY = y + 12;

        if (recipe instanceof CraftingRecipe craftingRecipe) {
            renderCrafting(graphics, client, x, lineY, craftingRecipe);
        } else if (recipe instanceof AbstractCookingRecipe cookingRecipe) {
            renderSingleInput(graphics, client, x, lineY, cookingRecipe.input(), cookingRecipe.assemble(new SingleRecipeInput(ItemStack.EMPTY)));
        } else if (recipe instanceof StonecutterRecipe stonecutterRecipe) {
            renderSingleInput(graphics, client, x, lineY, stonecutterRecipe.input(), stonecutterRecipe.assemble(new SingleRecipeInput(ItemStack.EMPTY)));
        } else if (recipe instanceof SingleItemRecipe singleItemRecipe) {
            renderSingleInput(graphics, client, x, lineY, singleItemRecipe.input(), singleItemRecipe.assemble(new SingleRecipeInput(ItemStack.EMPTY)));
        } else {
            graphics.text(client.font, Component.translatable("dwm.guide.recipe.unavailable"), x, lineY, 0xFF888888, false);
        }

        if (page.patternPage()) {
            graphics.text(
                    client.font,
                    Component.translatable("dwm.guide.pattern.all_colours"),
                    x,
                    lineY + 52,
                    0xFFAAAAAA,
                    false
            );
        }
    }

    private static void renderCrafting(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int x,
            int y,
            CraftingRecipe recipe
    ) {
        List<Ingredient> ingredients = recipe.placementInfo().ingredients();
        int cursorY = y;
        for (Ingredient ingredient : ingredients) {
            ItemStack stack = firstIngredientStack(ingredient);
            if (stack.isEmpty()) {
                continue;
            }
            graphics.text(client.font, stack.getHoverName(), x, cursorY, 0xFFFFFFFF, false);
            cursorY += 10;
            if (cursorY > y + 40) {
                break;
            }
        }
        ItemStack result = recipe.assemble(CraftingInput.EMPTY);
        graphics.text(client.font, Component.literal("→"), x, cursorY + 2, 0xFFCCCCCC, false);
        graphics.text(client.font, result.getHoverName(), x + 12, cursorY + 2, 0xFFFFFFFF, false);
    }

    private static void renderSingleInput(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int x,
            int y,
            Ingredient ingredient,
            ItemStack result
    ) {
        ItemStack input = firstIngredientStack(ingredient);
        graphics.text(client.font, input.getHoverName(), x, y, 0xFFFFFFFF, false);
        graphics.text(client.font, Component.literal("→"), x, y + 12, 0xFFCCCCCC, false);
        graphics.text(client.font, result.getHoverName(), x + 12, y + 12, 0xFFFFFFFF, false);
    }

    private static ItemStack firstIngredientStack(Ingredient ingredient) {
        Optional<Holder<Item>> item = ingredient.items().findFirst();
        return item.map(holder -> new ItemStack(holder.value())).orElse(ItemStack.EMPTY);
    }

    private static Optional<RecipeHolder<?>> recipeHolder(Minecraft client, Identifier recipeId) {
        if (client.getSingleplayerServer() == null) {
            return Optional.empty();
        }
        RecipeManager recipes = client.getSingleplayerServer().getRecipeManager();
        return recipes.byKey(ResourceKey.create(Registries.RECIPE, recipeId));
    }

    private static @Nullable Identifier recipeIdFor(FieldGuidePage page, Station station) {
        return switch (station) {
            case CRAFTING -> page.craftingRecipe();
            case SMELTING -> page.smeltingRecipe();
            case STONECUTTING -> page.stonecuttingRecipe();
        };
    }
}
