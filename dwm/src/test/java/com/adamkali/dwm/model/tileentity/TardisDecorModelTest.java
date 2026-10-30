package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.model.json.TestModelLoading;
import com.adamkali.dwm.render.state.TardisGlobeRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TardisDecorModelTest {
    @Test
    void globeModel_bakesArmillaryParts() {
        ModelPart root = assertDoesNotThrow(() -> TestModelLoading.bake(TardisGlobeModel.LAYER_LOCATION));
        assertTrue(root.hasChild("base"));
        ModelPart frame = root.getChild("frame");
        assertTrue(frame.hasChild("ring"));
        assertTrue(frame.hasChild("finial"));
        ModelPart sphere = root.getChild("sphere");
        for (String ring : new String[]{"equator", "meridian_a", "meridian_b"}) {
            ModelPart part = sphere.getChild(ring);
            assertTrue(part.hasChild("ring_a") && part.hasChild("ring_b"), ring);
        }
        assertTrue(sphere.hasChild("core"));
        assertTrue(sphere.hasChild("moon_orbit"));
    }

    @Test
    void globeModel_restPoseIsTiltedAndStill() {
        ModelPart root = TestModelLoading.bake(TardisGlobeModel.LAYER_LOCATION);
        TardisGlobeModel model = new TardisGlobeModel(root);
        model.setupAnim(new TardisGlobeRenderState());

        ModelPart sphere = root.getChild("sphere");
        assertEquals(23.4F * Mth.DEG_TO_RAD, sphere.zRot, 1.0e-5F);
        assertEquals(0.0F, sphere.yRot, 1.0e-5F);
        assertEquals(0.0F, sphere.getChild("meridian_a").xRot, 1.0e-5F);
        assertEquals(0.0F, root.getChild("frame").zRot, 1.0e-5F);
    }

    @Test
    void globeModel_ambientTurnsSphere() {
        ModelPart root = TestModelLoading.bake(TardisGlobeModel.LAYER_LOCATION);
        TardisGlobeModel model = new TardisGlobeModel(root);
        TardisGlobeRenderState state = new TardisGlobeRenderState();
        state.ageInTicks = 50.0F;
        model.setupAnim(state);

        ModelPart sphere = root.getChild("sphere");
        assertEquals(50.0F * 1.2F * Mth.DEG_TO_RAD, sphere.yRot, 1.0e-4F);
        // No interaction: the gimbal rings stay closed.
        assertEquals(0.0F, sphere.getChild("meridian_a").xRot, 1.0e-5F);
        assertEquals(0.0F, sphere.getChild("meridian_b").zRot, 1.0e-5F);
    }

    @Test
    void globeModel_spinAddsTurnAndOpensRings() {
        ModelPart root = TestModelLoading.bake(TardisGlobeModel.LAYER_LOCATION);
        TardisGlobeModel model = new TardisGlobeModel(root);
        TardisGlobeRenderState state = new TardisGlobeRenderState();
        state.spinDegrees = 90.0F;
        state.spinEnergy = 1.0F;
        model.setupAnim(state);

        ModelPart sphere = root.getChild("sphere");
        assertEquals(90.0F * Mth.DEG_TO_RAD, sphere.yRot, 1.0e-4F);
        assertEquals(25.0F * Mth.DEG_TO_RAD, sphere.getChild("meridian_a").xRot, 1.0e-4F);
        assertEquals(-35.0F * Mth.DEG_TO_RAD, sphere.getChild("meridian_b").zRot, 1.0e-4F);
        assertEquals(1.0F, sphere.getChild("core").y, 1.0e-5F);
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
