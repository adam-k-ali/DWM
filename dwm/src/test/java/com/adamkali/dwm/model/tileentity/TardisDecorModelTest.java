package com.adamkali.dwm.model.tileentity;

import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TardisDecorModelTest {
    @Test
    void globeModel_bakesWithStandAndGlobeGroups() {
        ModelPart root = assertDoesNotThrow(() -> TardisGlobeModel.getTexturedModelData().bakeRoot());
        assertTrue(root.hasChild("mesh"));
        assertTrue(root.hasChild("Globe"));
        assertTrue(root.hasChild("Globe2"));
        assertTrue(root.hasChild("Globe3"));
        assertTrue(root.hasChild("arrow"));
        assertTrue(root.hasChild("arrow2"));
    }

    @Test
    void compactScannerModel_bakes() {
        ModelPart root = assertDoesNotThrow(() -> TardisCompactScannerModel.getTexturedModelData().bakeRoot());
        assertTrue(root.hasChild("slider") || root.hasChild("scanner") || root.hasChild("mesh"));
    }

    @Test
    void fullScannerModel_bakes() {
        ModelPart root = assertDoesNotThrow(() -> TardisFullScannerModel.getTexturedModelData().bakeRoot());
        assertTrue(root.hasChild("slider") || root.hasChild("scanner") || root.hasChild("mesh"));
    }

    @Test
    void layersAndTextures_areDistinctWhereExpected() {
        assertNotEquals(TardisGlobeModel.LAYER_LOCATION, TardisFullScannerModel.LAYER_LOCATION);
        assertNotEquals(TardisCompactScannerModel.LAYER_LOCATION, TardisFullScannerModel.LAYER_LOCATION);
        assertEquals(TardisCompactScannerModel.TEXTURE_LOCATION, TardisFullScannerModel.TEXTURE_LOCATION);
        assertEquals("tardis_globe", TardisGlobeModel.LAYER_LOCATION.model().getPath());
        assertEquals("tardis_compact_scanner", TardisCompactScannerModel.LAYER_LOCATION.model().getPath());
        assertEquals("tardis_full_scanner", TardisFullScannerModel.LAYER_LOCATION.model().getPath());
    }
}
