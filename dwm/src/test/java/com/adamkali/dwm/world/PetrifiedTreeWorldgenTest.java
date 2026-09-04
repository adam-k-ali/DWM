package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PetrifiedTreeWorldgenTest {
    @Test
    void configuredFeatureKeys_matchExpectedIds() {
        assertEquals(id("petrified_tree"), DWMConfiguredFeatures.PETRIFIED_TREE.identifier());
        assertEquals(id("petrified_snag"), DWMConfiguredFeatures.PETRIFIED_SNAG.identifier());
        assertEquals(id("fallen_petrified_tree"), DWMConfiguredFeatures.FALLEN_PETRIFIED_TREE.identifier());
    }

    @Test
    void placedFeatureKeys_matchExpectedIds() {
        assertEquals(id("petrified_jungle_trees"), DWMPlacedFeatures.PETRIFIED_JUNGLE_TREES.identifier());
        assertEquals(id("petrified_jungle_snags"), DWMPlacedFeatures.PETRIFIED_JUNGLE_SNAGS.identifier());
        assertEquals(id("fallen_petrified_jungle_trees"), DWMPlacedFeatures.FALLEN_PETRIFIED_JUNGLE_TREES.identifier());
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path);
    }
}
