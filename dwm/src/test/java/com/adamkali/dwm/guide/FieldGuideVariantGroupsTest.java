package com.adamkali.dwm.guide;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldGuideVariantGroupsTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void groupsPairedZeitonRecipesByResult() {
        List<FieldGuideVariantGroups.Group> groups = FieldGuideVariantGroups.group(
                List.of(
                        id("circuit_telepathic"),
                        id("circuit_telepathic_from_zeiton"),
                        id("circuit_cloak"),
                        id("circuit_cloak_from_zeiton")
                ),
                recipe -> Map.of(
                        id("circuit_telepathic"), id("circuit_telepathic"),
                        id("circuit_telepathic_from_zeiton"), id("circuit_telepathic"),
                        id("circuit_cloak"), id("circuit_cloak"),
                        id("circuit_cloak_from_zeiton"), id("circuit_cloak")
                ).get(recipe)
        );

        assertEquals(2, groups.size());
        assertEquals(id("circuit_telepathic"), groups.get(0).resultId());
        assertEquals(2, groups.get(0).recipes().size());
        assertTrue(FieldGuideVariantGroups.hasPathToggle(groups));
        assertEquals(id("circuit_cloak"), FieldGuideVariantGroups.recipeFor(groups.get(1), false));
        assertEquals(id("circuit_cloak_from_zeiton"), FieldGuideVariantGroups.recipeFor(groups.get(1), true));
    }

    @Test
    void uniqueResultsStaySeparateWithoutPathToggle() {
        List<FieldGuideVariantGroups.Group> groups = FieldGuideVariantGroups.group(
                List.of(id("circuit_stabilisers"), id("circuit_waypoints"), id("circuit_fast_return")),
                recipe -> recipe
        );

        assertEquals(3, groups.size());
        assertFalse(FieldGuideVariantGroups.hasPathToggle(groups));
        assertEquals(id("circuit_waypoints"), FieldGuideVariantGroups.recipeFor(groups.get(1), true));
    }

    @Test
    void wrapMathFitsEightIconsOnOneRowAndNinthOnASecond() {
        assertEquals(8, FieldGuideBookLayout.variantColumns());
        assertEquals(1, FieldGuideBookLayout.variantRowCount(5));
        assertEquals(1, FieldGuideBookLayout.variantRowCount(8));
        assertEquals(2, FieldGuideBookLayout.variantRowCount(9));
        assertEquals(2, FieldGuideBookLayout.variantRowCount(10));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path);
    }
}
