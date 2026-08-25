package com.adamkali.dwm.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstDoctorConsoleBlockEntityRendererHologramTest {
    private static final float EPSILON = 1e-4f;

    @Test
    void hologramYawDegrees_scalesLinearlyWithTime() {
        assertEquals(0.0f, FirstDoctorConsoleBlockEntityRenderer.hologramYawDegrees(0.0f), EPSILON);
        assertEquals(
                FirstDoctorConsoleBlockEntityRenderer.HOLOGRAM_DEGREES_PER_TICK * 80.0f,
                FirstDoctorConsoleBlockEntityRenderer.hologramYawDegrees(80.0f),
                EPSILON
        );
        assertEquals(
                360.0f,
                FirstDoctorConsoleBlockEntityRenderer.hologramYawDegrees(160.0f),
                EPSILON
        );
    }

    @Test
    void hologramBobOffset_zeroAtOriginAndBoundedByAmplitude() {
        assertEquals(0.0f, FirstDoctorConsoleBlockEntityRenderer.hologramBobOffset(0.0f), EPSILON);

        float quarter = (float) (Math.PI / (2.0 * FirstDoctorConsoleBlockEntityRenderer.HOLOGRAM_BOB_SPEED));
        assertEquals(
                FirstDoctorConsoleBlockEntityRenderer.HOLOGRAM_BOB_AMPLITUDE,
                FirstDoctorConsoleBlockEntityRenderer.hologramBobOffset(quarter),
                EPSILON
        );

        for (int i = 0; i <= 40; i++) {
            float t = i * 1.0f;
            float offset = FirstDoctorConsoleBlockEntityRenderer.hologramBobOffset(t);
            assertTrue(
                    Math.abs(offset) <= FirstDoctorConsoleBlockEntityRenderer.HOLOGRAM_BOB_AMPLITUDE + EPSILON,
                    "bob out of range: t=" + t + " offset=" + offset
            );
        }
    }
}
