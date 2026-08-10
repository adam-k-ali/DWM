package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GallifreyDimensionsTest {
    @Test
    void dimensionIdAndWorldKey_matchModNamespace() {
        assertEquals(Identifier.of(DWMReference.MOD_ID, "gallifrey"), GallifreyDimensions.DIMENSION_ID);
        assertEquals(RegistryKeys.WORLD, GallifreyDimensions.GALLIFREY_WORLD_KEY.getRegistryRef());
        assertEquals(GallifreyDimensions.DIMENSION_ID, GallifreyDimensions.GALLIFREY_WORLD_KEY.getValue());
        assertTrue(GallifreyDimensions.isGallifreyWorld(GallifreyDimensions.GALLIFREY_WORLD_KEY));
        assertFalse(GallifreyDimensions.isGallifreyWorld((net.minecraft.world.World) null));
    }

    @Test
    void isGallifreyBiomeTag_usesExpectedId() {
        assertEquals(Identifier.of(DWMReference.MOD_ID, "is_gallifrey"), DWMBiomeTags.IS_GALLIFREY.id());
    }
}
