package com.adamkali.dwm.render.soto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TardisSotoRendererAlignmentTest {
    private static final float EPSILON = 1e-4f;
    private static final double RELATIVE_TARDIS_X = 5.0;
    private static final double RELATIVE_TARDIS_Y = 1.0;
    private static final double RELATIVE_TARDIS_Z = 5.0;

    @Test
    void exteriorDoorPlane_isOffsetForwardOfDoorFace() {
        double doorFaceZ = RELATIVE_TARDIS_Z;
        assertEquals(doorFaceZ - TardisSotoRenderer.PREVIEW_FORWARD_OFFSET,
                TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z, EPSILON);
    }

    @Test
    void exteriorDoorCenter_matchesRelativeTardisAndEyeHeight() {
        assertEquals(
                RELATIVE_TARDIS_X + 0.5,
                TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                EPSILON
        );
        assertEquals(
                RELATIVE_TARDIS_Y + TardisSotoRenderer.PREVIEW_EYE_HEIGHT,
                TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                EPSILON
        );
    }
}
