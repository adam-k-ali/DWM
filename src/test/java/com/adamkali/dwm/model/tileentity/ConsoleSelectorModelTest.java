package com.adamkali.dwm.model.tileentity;

import net.minecraft.client.model.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleSelectorModelTest {
    @Test
    void texturedModelData_createsRootWithSelectorChild() {
        ModelPart root = assertDoesNotThrow(() -> ConsoleSelectorModel.getTexturedModelData().createModel());
        assertNotNull(root.getChild(ConsoleSelectorModel.PART_NAME));
    }

    @Test
    void wrappers_delegateSameMeshAndKeepDistinctLayers() {
        ModelPart biomeRoot = BiomeSelectorModel.getTexturedModelData().createModel();
        ModelPart planetRoot = PlanetLocatorModel.getTexturedModelData().createModel();
        assertNotNull(biomeRoot.getChild(ConsoleSelectorModel.PART_NAME));
        assertNotNull(planetRoot.getChild(ConsoleSelectorModel.PART_NAME));

        assertNotEquals(BiomeSelectorModel.LAYER_LOCATION, PlanetLocatorModel.LAYER_LOCATION);
        assertNotEquals(BiomeSelectorModel.TEXTURE_LOCATION, PlanetLocatorModel.TEXTURE_LOCATION);
        assertEquals("biome_selector", BiomeSelectorModel.LAYER_LOCATION.id().getPath());
        assertEquals("planet_locator", PlanetLocatorModel.LAYER_LOCATION.id().getPath());
    }
}
