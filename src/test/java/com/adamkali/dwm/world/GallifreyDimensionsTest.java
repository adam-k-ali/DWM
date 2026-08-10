package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GallifreyDimensionsTest {
    @Test
    void dimensionIdAndWorldKey_matchModNamespace() {
        assertEquals(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "gallifrey"), GallifreyDimensions.DIMENSION_ID);
        assertEquals(Registries.DIMENSION, GallifreyDimensions.GALLIFREY_WORLD_KEY.registryKey());
        assertEquals(GallifreyDimensions.DIMENSION_ID, );
        assertTrue(GallifreyDimensions.isGallifreyWorld(GallifreyDimensions.GALLIFREY_WORLD_KEY));
        assertFalse(GallifreyDimensions.isGallifreyWorld((net.minecraft.world.level.Level) null));
    }

    @Test
    void isGallifreyBiomeTag_usesExpectedId() {
        assertEquals(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "is_gallifrey"), );
    }
}
