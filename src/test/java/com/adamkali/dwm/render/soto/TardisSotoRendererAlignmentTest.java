package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TardisSotoRendererAlignmentTest {
    private static final float EPSILON = 1e-4f;

    @Test
    void exteriorDoorPlane_isOffsetForwardOfDoorFace() {
        double doorFaceZ = SotoExteriorSampler.RELATIVE_TARDIS_POS.getZ() + 0.0;
        assertEquals(doorFaceZ - TardisSotoRenderer.PREVIEW_FORWARD_OFFSET,
                TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z, EPSILON);
    }

    @Test
    void exteriorDoorCenter_matchesRelativeTardisAndEyeHeight() {
        assertEquals(
                SotoExteriorSampler.RELATIVE_TARDIS_POS.getX() + 0.5,
                TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                EPSILON
        );
        assertEquals(
                SotoExteriorSampler.RELATIVE_TARDIS_POS.getY() + TardisSotoRenderer.PREVIEW_EYE_HEIGHT,
                TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                EPSILON
        );
    }
}
