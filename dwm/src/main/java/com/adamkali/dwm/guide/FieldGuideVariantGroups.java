package com.adamkali.dwm.guide;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Groups crafting recipe ids that share a result item so the Field Guide can show
 * one icon per item and a Vanilla/Zeiton path toggle when alternatives exist.
 */
public final class FieldGuideVariantGroups {
    private FieldGuideVariantGroups() {
    }

    public record Group(Identifier resultId, List<Identifier> recipes) {
        public Group {
            recipes = List.copyOf(recipes);
        }
    }

    public static List<Group> group(List<Identifier> recipes, Function<Identifier, Identifier> resultId) {
        Map<Identifier, List<Identifier>> byResult = new LinkedHashMap<>();
        for (Identifier recipe : recipes) {
            Identifier result = resultId.apply(recipe);
            Identifier key = result != null ? result : recipe;
            byResult.computeIfAbsent(key, ignored -> new ArrayList<>()).add(recipe);
        }
        List<Group> groups = new ArrayList<>();
        byResult.forEach((result, ids) -> groups.add(new Group(result, ids)));
        return List.copyOf(groups);
    }

    public static boolean isZeitonPath(Identifier recipeId) {
        return recipeId.getPath().endsWith("_from_zeiton");
    }

    public static boolean hasPathToggle(List<Group> groups) {
        return groups.stream().anyMatch(group -> group.recipes().size() > 1);
    }

    public static Identifier recipeFor(Group group, boolean zeitonPreferred) {
        if (zeitonPreferred) {
            return group.recipes().stream()
                    .filter(FieldGuideVariantGroups::isZeitonPath)
                    .findFirst()
                    .orElse(group.recipes().getFirst());
        }
        return group.recipes().stream()
                .filter(recipe -> !isZeitonPath(recipe))
                .findFirst()
                .orElse(group.recipes().getFirst());
    }
}
