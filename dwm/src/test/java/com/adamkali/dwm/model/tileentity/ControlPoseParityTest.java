package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.model.json.TestModelLoading;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Lever / switch / needle / rotor poses now come from JSON bindings; these pin the old Java values. */
class ControlPoseParityTest {
    private static final float EPS = 1.0e-5F;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void doorLockLeverPitch() {
        ModelPart root = TestModelLoading.bake(DoorLockModel.LAYER_LOCATION);
        DoorLockModel model = new DoorLockModel(root);
        TardisRenderState state = new TardisRenderState();
        state.setDoorsLocked(false);
        model.setupAnim(state);
        assertEquals(0.0F, root.getChild("lever").xRot, EPS);
        state.setDoorsLocked(true);
        model.setupAnim(state);
        assertEquals(0.45F, root.getChild("lever").xRot, EPS);
    }

    @Test
    void cloakLeverPitch() {
        ModelPart root = TestModelLoading.bake(CloakLeverModel.LAYER_LOCATION);
        CloakLeverModel model = new CloakLeverModel(root);
        ModelPart lever = root.getChild("lever").getChild("lever_control2");
        TardisRenderState state = new TardisRenderState();
        state.setCloaked(false);
        model.setupAnim(state);
        assertEquals(-1.134464F, lever.xRot, EPS);
        state.setCloaked(true);
        model.setupAnim(state);
        assertEquals(-0.261799F, lever.xRot, EPS);
    }

    @Test
    void coordinateLockSwitches() {
        ModelPart root = TestModelLoading.bake(CoordinateLockModel.LAYER_LOCATION);
        CoordinateLockModel model = new CoordinateLockModel(root);
        ModelPart x = root.getChild("coord_button_x").getChild("switch");
        ModelPart y = root.getChild("coord_button_y").getChild("switch3");
        ModelPart z = root.getChild("coord_button_z").getChild("switch2");
        TardisRenderState state = new TardisRenderState();
        model.setupAnim(state);
        assertEquals(0.0F, x.xRot, EPS);
        assertEquals(0.0F, y.xRot, EPS);
        assertEquals(0.0F, z.xRot, EPS);
        state.setLockX(true);
        state.setLockZ(true);
        model.setupAnim(state);
        assertEquals(0.6F, x.xRot, EPS);
        assertEquals(0.0F, y.xRot, EPS);
        assertEquals(0.6F, z.xRot, EPS);
        state.setLockY(true);
        model.setupAnim(state);
        assertEquals(0.6F, y.xRot, EPS);
    }

    @Test
    void stabilisersLeverPitch() {
        ModelPart root = TestModelLoading.bake(StabilisersModel.LAYER_LOCATION);
        StabilisersModel model = new StabilisersModel(root);
        ModelPart lever = root.getChild("stable_adjust").getChild("lever");
        TardisRenderState state = new TardisRenderState();
        state.setStabilisersEnabled(true);
        model.setupAnim(state);
        assertEquals(1.047198F, lever.xRot, EPS);
        state.setStabilisersEnabled(false);
        model.setupAnim(state);
        assertEquals(0.174533F, lever.xRot, EPS);
    }

    @Test
    void readerNeedlesSweepAroundCentre() {
        ModelPart reader = TestModelLoading.bake(ReaderModel.LAYER_LOCATION);
        ReaderModel readerModel = new ReaderModel(reader);
        ModelPart radiation = TestModelLoading.bake(RadiationReaderModel.LAYER_LOCATION);
        RadiationReaderModel radiationModel = new RadiationReaderModel(radiation);
        TardisRenderState state = new TardisRenderState();
        for (float needle : new float[] {0.0F, 0.5F, 0.8F, 1.0F}) {
            state.setNeedle(needle);
            readerModel.setupAnim(state);
            radiationModel.setupAnim(state);
            assertEquals((needle - 0.5F) * 2.4F, reader.getChild("needle").yRot, EPS);
            assertEquals((needle - 0.5F) * 1.6F, radiation.getChild("needle").xRot, EPS);
        }
    }
}
