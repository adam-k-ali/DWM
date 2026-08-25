package com.adamkali.dwm.guide;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * One Field Guide page: localized title/body plus optional recipe ids per station.
 */
public record FieldGuidePage(
        String id,
        String titleKey,
        String bodyKey,
        @Nullable Identifier craftingRecipe,
        @Nullable Identifier smeltingRecipe,
        @Nullable Identifier stonecuttingRecipe,
        boolean patternPage
) {
    public static FieldGuidePage text(String id, String titleKey, String bodyKey) {
        return new FieldGuidePage(id, titleKey, bodyKey, null, null, null, false);
    }

    public static FieldGuidePage crafting(String id, String titleKey, String bodyKey, Identifier recipe, boolean patternPage) {
        return new FieldGuidePage(id, titleKey, bodyKey, recipe, null, null, patternPage);
    }

    public static FieldGuidePage smelting(String id, String titleKey, String bodyKey, Identifier recipe, boolean patternPage) {
        return new FieldGuidePage(id, titleKey, bodyKey, null, recipe, null, patternPage);
    }
}
