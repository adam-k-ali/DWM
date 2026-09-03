package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import static org.junit.jupiter.api.Assertions.*;

class SkaroDimensionsTest {
    @Test
    void dimensionIdAndWorldKey_matchModNamespace() {
        assertEquals(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "skaro"), SkaroDimensions.DIMENSION_ID);
        assertEquals(Registries.DIMENSION, SkaroDimensions.SKARO_WORLD_KEY.registryKey());
        assertEquals(SkaroDimensions.DIMENSION_ID, SkaroDimensions.SKARO_WORLD_KEY.identifier());
        assertTrue(SkaroDimensions.isSkaroWorld(SkaroDimensions.SKARO_WORLD_KEY));
        assertFalse(SkaroDimensions.isSkaroWorld((net.minecraft.world.level.Level) null));
    }

    @Test
    void isSkaroBiomeTag_usesExpectedId() {
        assertEquals(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "is_skaro"), DWMBiomeTags.IS_SKARO.location());
    }

    @Test
    void skaroBiomeKeys_matchExactFiveIdContract() {
        List<ResourceKey<Biome>> keys = List.of(
                DWMBiomeKeys.SKARO_IRRADIATED_WASTES,
                DWMBiomeKeys.SKARO_PETRIFIED_JUNGLE,
                DWMBiomeKeys.SKARO_DRAMMANKIN_MIRE,
                DWMBiomeKeys.SKARO_DRAMMANKIN_MOUNTAINS,
                DWMBiomeKeys.SKARO_THAL_PLATEAU
        );
        assertEquals(5, keys.size());
        assertEquals(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "skaro_irradiated_wastes"), keys.get(0).identifier());
        assertEquals(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "skaro_petrified_jungle"), keys.get(1).identifier());
        assertEquals(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "skaro_drammankin_mire"), keys.get(2).identifier());
        assertEquals(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "skaro_drammankin_mountains"), keys.get(3).identifier());
        assertEquals(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "skaro_thal_plateau"), keys.get(4).identifier());
        for (ResourceKey<Biome> key : keys) {
            assertEquals(Registries.BIOME, key.registryKey());
        }
    }
}
