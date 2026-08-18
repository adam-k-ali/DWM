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
            assertTrue(PortalApertureComposite.PLACEHOLDER_ARGB != 0);
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

    @Test
    void viewPanU_onAxisIsZero() {
        Vec3 door = new Vec3(0.5, 1.0, 0.5);
        Vec3 player = new Vec3(0.5, 1.62, 0.5 + 3.0);
        assertEquals(0.0f, PortalApertureComposite.viewPanU(player, door, Direction.SOUTH), EPSILON);
    }

    @Test
    void viewPanU_playerOnDoorRight_isPositive() {
        Vec3 door = new Vec3(0.5, 1.0, 0.5);
        Vec3 player = new Vec3(0.5 + 4.0, 1.62, 0.5 + 3.5);
        float pan = PortalApertureComposite.viewPanU(player, door, Direction.SOUTH);
        assertTrue(pan > 1.0f, "Strafing right of a south door should pan U positive");
    }
}
