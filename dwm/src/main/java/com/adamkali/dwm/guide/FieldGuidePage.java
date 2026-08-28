package com.adamkali.dwm.guide;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One Field Guide page: localized title/body plus optional recipe ids per station,
 * derived from datapack {@link FieldGuideContent} blocks.
 */
public record FieldGuidePage(Identifier id, List<FieldGuideContent> content) {
    public FieldGuidePage {
        content = List.copyOf(content);
    }

    public String titleKey() {
        return text().titleKey();
    }

    public String bodyKey() {
        return text().bodyKey();
    }

    public List<Identifier> craftingRecipes() {
        return content.stream()
                .filter(FieldGuideContent.Crafting.class::isInstance)
                .map(FieldGuideContent.Crafting.class::cast)
                .flatMap(block -> block.recipes().stream())
                .toList();
    }

    public @Nullable Identifier craftingRecipe() {
        List<Identifier> recipes = craftingRecipes();
        return recipes.isEmpty() ? null : recipes.getFirst();
    }

    public @Nullable Identifier smeltingRecipe() {
        return content.stream()
                .filter(FieldGuideContent.Smelting.class::isInstance)
                .map(FieldGuideContent.Smelting.class::cast)
                .map(FieldGuideContent.Smelting::recipe)
                .findFirst()
                .orElse(null);
    }

    public @Nullable Identifier stonecuttingRecipe() {
        return content.stream()
                .filter(FieldGuideContent.Stonecutting.class::isInstance)
                .map(FieldGuideContent.Stonecutting.class::cast)
                .map(FieldGuideContent.Stonecutting::recipe)
                .findFirst()
                .orElse(null);
    }

    public boolean patternPage() {
        for (FieldGuideContent block : content) {
            if (block instanceof FieldGuideContent.Crafting crafting && crafting.pattern()) {
                return true;
            }
            if (block instanceof FieldGuideContent.Smelting smelting && smelting.pattern()) {
                return true;
            }
            if (block instanceof FieldGuideContent.Stonecutting stonecutting && stonecutting.pattern()) {
                return true;
            }
        }
        return false;
    }

    private FieldGuideContent.Text text() {
        for (FieldGuideContent block : content) {
            if (block instanceof FieldGuideContent.Text text) {
                return text;
            }
        }
        throw new IllegalStateException("Page has no text content: " + id);
    }
}
