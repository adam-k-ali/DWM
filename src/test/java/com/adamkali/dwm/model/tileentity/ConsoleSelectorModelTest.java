package com.adamkali.dwm.model.tileentity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.client.model.geom.ModelPart;

class ConsoleSelectorModelTest {
    @Test
    void texturedModelData_createsRootWithSelectorChild() {
        ModelPart root = assertDoesNotThrow(() -> ConsoleSelectorModel.getTexturedModelData().bakeRoot());
        assertNotNull(root.getChild(ConsoleSelectorModel.PART_NAME));
    }

    @Test
    void wrappers_delegateSameMeshAndKeepDistinctLayers() {
        ModelPart biomeRoot = BiomeSelectorModel.getTexturedModelData().bakeRoot();
        ModelPart planetRoot = PlanetLocatorModel.getTexturedModelData().bakeRoot();
        ModelPart waypointRoot = WaypointSelectorModel.getTexturedModelData().bakeRoot();
        ModelPart playerRoot = PlayerLocatorModel.getTexturedModelData().bakeRoot();
        assertNotNull(biomeRoot.getChild(ConsoleSelectorModel.PART_NAME));
        assertNotNull(planetRoot.getChild(ConsoleSelectorModel.PART_NAME));
        assertNotNull(waypointRoot.getChild(ConsoleSelectorModel.PART_NAME));
        assertNotNull(playerRoot.getChild(ConsoleSelectorModel.PART_NAME));

        assertNotEquals(BiomeSelectorModel.LAYER_LOCATION, PlanetLocatorModel.LAYER_LOCATION);
        assertNotEquals(BiomeSelectorModel.LAYER_LOCATION, WaypointSelectorModel.LAYER_LOCATION);
        assertNotEquals(BiomeSelectorModel.LAYER_LOCATION, PlayerLocatorModel.LAYER_LOCATION);
        assertNotEquals(BiomeSelectorModel.TEXTURE_LOCATION, PlanetLocatorModel.TEXTURE_LOCATION);
        assertEquals("biome_selector", BiomeSelectorModel.LAYER_LOCATION.model().getPath());
        assertEquals("planet_locator", PlanetLocatorModel.LAYER_LOCATION.model().getPath());
        assertEquals("waypoint_selector", WaypointSelectorModel.LAYER_LOCATION.model().getPath());
        assertEquals("player_locator", PlayerLocatorModel.LAYER_LOCATION.model().getPath());
    }
}
