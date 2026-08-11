package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.render.portal.PortalApertureComposite;
import com.adamkali.dwm.render.portal.PortalCameraTransform;
import com.adamkali.dwm.tardis.data.model.PortalAperture;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisBotiRendererAlignmentTest {
    private static final float EPSILON = 1e-4f;
    private static final PortalAperture FIRST_DOCTOR_APERTURE =
            TardisChameleonVariant.FIRST_DOCTOR_BOX.getAperture();

    @Test
    void apertureZ_matchesExteriorDoorPlane() {
        assertEquals(-5.5f / 16.0f, FIRST_DOCTOR_APERTURE.z(), EPSILON);
    }

    @Test
    void everyVariant_hasNonDegenerateApertureForComposite() {
        for (TardisChameleonVariant variant : TardisChameleonVariant.values()) {
            PortalAperture aperture = variant.getAperture();
            assertTrue(aperture.x0() < aperture.x1(), variant + " aperture x range");
            assertTrue(aperture.y0() < aperture.y1(), variant + " aperture y range");
            float width = aperture.x1() - aperture.x0();
            float height = aperture.y1() - aperture.y0();
            assertTrue(width > 0.0f && height > 0.0f, variant + " composite sizing");
            assertTrue(PortalApertureComposite.COMPOSITE_REFERENCE_DEPTH > 0.0f);
        }
    }

    @Test
    void interiorHitch_looksIntoRoomAlongPositiveZ() {
        Vec3 eye = new Vec3(
                TardisBotiRenderer.INTERIOR_DOOR_CENTER_X,
                TardisBotiRenderer.INTERIOR_DOOR_CENTER_Y,
                TardisBotiRenderer.INTERIOR_DOOR_PLANE_Z
        );
        PortalCameraTransform.Result hitch =
                PortalCameraTransform.fromLookDirection(eye, Direction.SOUTH, 0.0f);

        assertEquals(0.0, hitch.lookDirection().x, EPSILON);
        assertEquals(0.0, hitch.lookDirection().y, EPSILON);
        assertEquals(1.0, hitch.lookDirection().z, EPSILON);

        Vector3f deeper = hitch.viewMatrix().transformPosition(
                (float) (eye.x),
                (float) (eye.y),
                (float) (eye.z + 3.0),
                new Vector3f()
        );
        // Looking along +Z: a point further into the room should be in front of the camera (-Z in view space).
        assertTrue(deeper.z < 0.0f, "Room depth should project in front of hitch camera");
    }
}
