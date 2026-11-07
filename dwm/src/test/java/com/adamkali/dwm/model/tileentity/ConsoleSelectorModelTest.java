package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.model.json.TestModelLoading;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.client.model.geom.ModelPart;

class ConsoleSelectorModelTest {
    @Test
    void texturedModelData_createsRootWithSelectorChild() {
        ModelPart root = assertDoesNotThrow(() -> TestModelLoading.bake(BiomeSelectorModel.LAYER_LOCATION));
        assertNotNull(root.getChild(ConsoleSelectorModel.PART_NAME));
    }

    @Test
    void wrappers_delegateSameMeshAndKeepDistinctLayers() {
        ModelPart biomeRoot = TestModelLoading.bake(BiomeSelectorModel.LAYER_LOCATION);
        ModelPart planetRoot = TestModelLoading.bake(PlanetLocatorModel.LAYER_LOCATION);
        ModelPart waypointRoot = TestModelLoading.bake(WaypointSelectorModel.LAYER_LOCATION);
        ModelPart playerRoot = TestModelLoading.bake(PlayerLocatorModel.LAYER_LOCATION);
        ModelPart chameleonRoot = TestModelLoading.bake(ChameleonCircuitModel.LAYER_LOCATION);
        assertNotNull(biomeRoot.getChild(ConsoleSelectorModel.PART_NAME));
        assertNotNull(planetRoot.getChild(ConsoleSelectorModel.PART_NAME));
        assertNotNull(waypointRoot.getChild(ConsoleSelectorModel.PART_NAME));
        assertNotNull(playerRoot.getChild(ConsoleSelectorModel.PART_NAME));
        assertNotNull(chameleonRoot.getChild(ConsoleSelectorModel.PART_NAME));

        assertNotEquals(BiomeSelectorModel.LAYER_LOCATION, PlanetLocatorModel.LAYER_LOCATION);
        assertNotEquals(BiomeSelectorModel.LAYER_LOCATION, WaypointSelectorModel.LAYER_LOCATION);
        assertNotEquals(BiomeSelectorModel.LAYER_LOCATION, PlayerLocatorModel.LAYER_LOCATION);
        assertNotEquals(BiomeSelectorModel.LAYER_LOCATION, ChameleonCircuitModel.LAYER_LOCATION);
        assertNotEquals(TestModelLoading.texture(BiomeSelectorModel.LAYER_LOCATION), TestModelLoading.texture(PlanetLocatorModel.LAYER_LOCATION));
        assertEquals("biome_selector", BiomeSelectorModel.LAYER_LOCATION.model().getPath());
        assertEquals("planet_locator", PlanetLocatorModel.LAYER_LOCATION.model().getPath());
        assertEquals("waypoint_selector", WaypointSelectorModel.LAYER_LOCATION.model().getPath());
        assertEquals("player_locator", PlayerLocatorModel.LAYER_LOCATION.model().getPath());
        assertEquals("chameleon_circuit", ChameleonCircuitModel.LAYER_LOCATION.model().getPath());
        assertTrue(TestModelLoading.texture(ChameleonCircuitModel.LAYER_LOCATION).getPath().contains("chameleion_circuit_on"));
    }
}