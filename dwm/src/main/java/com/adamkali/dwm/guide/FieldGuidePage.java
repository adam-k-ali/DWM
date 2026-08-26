package com.adamkali.dwm.guide;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One Field Guide page: localized title/body plus optional recipe ids per station.
 */
public record FieldGuidePage(
        String id,
        String titleKey,
        String bodyKey,
        List<Identifier> craftingRecipes,
        @Nullable Identifier smeltingRecipe,
        @Nullable Identifier stonecuttingRecipe,
        boolean patternPage
) {
    public FieldGuidePage {
        craftingRecipes = List.copyOf(craftingRecipes);
    }

    public @Nullable Identifier craftingRecipe() {
        return craftingRecipes.isEmpty() ? null : craftingRecipes.getFirst();
    }

    public static FieldGuidePage text(String id, String titleKey, String bodyKey) {
        return new FieldGuidePage(id, titleKey, bodyKey, List.of(), null, null, false);
    }

    public static FieldGuidePage crafting(String id, String titleKey, String bodyKey, Identifier recipe, boolean patternPage) {
        return crafting(id, titleKey, bodyKey, List.of(recipe), patternPage);
    }

    public static FieldGuidePage crafting(String id, String titleKey, String bodyKey, List<Identifier> recipes, boolean patternPage) {
        return new FieldGuidePage(id, titleKey, bodyKey, recipes, null, null, patternPage);
    }

    public static FieldGuidePage smelting(String id, String titleKey, String bodyKey, Identifier recipe, boolean patternPage) {
        return new FieldGuidePage(id, titleKey, bodyKey, List.of(), recipe, null, patternPage);
    }
}
