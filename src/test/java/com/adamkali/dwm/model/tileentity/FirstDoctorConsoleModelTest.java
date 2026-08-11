package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FirstDoctorConsoleModelTest {
    private static final float EPSILON = 1e-4f;

    @Test
    void texturedModelData_hasTimeRotorHierarchy() {
        ModelPart root = FirstDoctorConsoleModel.getTexturedModelData().bakeRoot();
        assertTrue(root.hasChild("time_rotor"));
        ModelPart rotor = root.getChild("time_rotor");
        assertTrue(rotor.hasChild("Time_middle"));
        assertTrue(rotor.hasChild("rotor"));
        assertTrue(rotor.hasChild("rotor6"));
    }

    @Test
    void rotorBobOffset_zeroWhenInactive() {
        assertEquals(0.0f, FirstDoctorConsoleModel.rotorBobOffset(12.5f, false), EPSILON);
        assertEquals(0.0f, FirstDoctorConsoleModel.rotorBobOffset(0.0f, false), EPSILON);
    }

    @Test
    void rotorBobOffset_dipsDownFromRestNeverAbove() {
        assertEquals(0.0f, FirstDoctorConsoleModel.rotorBobOffset(0.0f, true), EPSILON);

        float trough = FirstDoctorConsoleModel.rotorBobOffset(
                (float) (Math.PI / FirstDoctorConsoleModel.ROTOR_BOB_SPEED),
                true
        );
        assertEquals(-FirstDoctorConsoleModel.ROTOR_BOB_AMPLITUDE, trough, EPSILON);

        float midDescent = FirstDoctorConsoleModel.rotorBobOffset(
                (float) (Math.PI / (2.0 * FirstDoctorConsoleModel.ROTOR_BOB_SPEED)),
                true
        );
        assertEquals(-FirstDoctorConsoleModel.ROTOR_BOB_AMPLITUDE * 0.5f, midDescent, EPSILON);

        // Sample a full cycle — offset must stay in [-amplitude, 0]
        for (int i = 0; i <= 40; i++) {
            float t = i * 1.0f;
            float offset = FirstDoctorConsoleModel.rotorBobOffset(t, true);
            assertTrue(offset <= EPSILON, "must not rise above rest: t=" + t + " offset=" + offset);
            assertTrue(
                    offset >= -FirstDoctorConsoleModel.ROTOR_BOB_AMPLITUDE - EPSILON,
                    "must not dip past amplitude: t=" + t + " offset=" + offset
            );
        }
    }

    @Test
    void setAngles_movesTimeRotorPivotY() {
        ModelPart root = FirstDoctorConsoleModel.getTexturedModelData().bakeRoot();
        FirstDoctorConsoleModel model = new FirstDoctorConsoleModel(root);
        ModelPart timeRotor = root.getChild("time_rotor");

        TardisRenderState state = new TardisRenderState();
        state.setRotorBobOffset(-2.5f);
        model.setupAnim(state);
        assertEquals(-2.5f, timeRotor.y, EPSILON);

        state.setRotorBobOffset(0.0f);
        model.setupAnim(state);
        assertEquals(0.0f, timeRotor.y, EPSILON);
    }
}
