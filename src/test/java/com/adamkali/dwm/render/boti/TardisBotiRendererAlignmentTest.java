package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.tardis.data.model.TardisBotiAperture;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisBotiRendererAlignmentTest {
    private static final float EPSILON = 1e-4f;
    private static final TardisBotiAperture FIRST_DOCTOR_APERTURE =
            TardisChameleonVariant.FIRST_DOCTOR_BOX.getAperture();

    @Test
    void apertureZ_matchesExteriorDoorPlane() {
        assertEquals(-5.5f / 16.0f, FIRST_DOCTOR_APERTURE.z(), EPSILON);
    }

    @Test
    void applyInteriorAlignment_mapsDoorCenterToAperture() {
        Vector3f mapped = transform(
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_X,
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_Y,
                (float) TardisBotiRenderer.INTERIOR_DOOR_PLANE_Z
        );

        assertEquals(0.0f, mapped.x, EPSILON);
        assertEquals(FIRST_DOCTOR_APERTURE.centerY(), mapped.y, EPSILON);
        assertEquals(FIRST_DOCTOR_APERTURE.z(), mapped.z, EPSILON);
    }

    @Test
    void applyInteriorAlignment_mapsRoomDepthToModelPositiveZ() {
        Vector3f door = transform(
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_X,
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_Y,
                (float) TardisBotiRenderer.INTERIOR_DOOR_PLANE_Z
        );
        Vector3f deeper = transform(
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_X,
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_Y,
                (float) TardisBotiRenderer.INTERIOR_DOOR_PLANE_Z + 3.0f
        );

        // Front doors face -Z; player looks in along +Z — room must extend +Z behind the door.
        assertTrue(deeper.z > door.z, "Room +Z should map to model +Z (behind front door)");
        assertEquals(door.z + 3.0f, deeper.z, EPSILON);
    }

    @Test
    void applyInteriorAlignment_mapsInteriorUpToModelNegativeY() {
        Vector3f door = transform(
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_X,
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_Y,
                (float) TardisBotiRenderer.INTERIOR_DOOR_PLANE_Z
        );
        Vector3f above = transform(
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_X,
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_Y + 1.0f,
                (float) TardisBotiRenderer.INTERIOR_DOOR_PLANE_Z
        );

        // BER rotateX(180) makes model -Y world-up; alignment Z-180 sends interior +Y to model -Y.
        assertEquals(door.y - 1.0f, above.y, EPSILON);
        assertTrue(above.y < door.y);
    }

    @Test
    void applyInteriorAlignment_mapsRoomPositiveXToModelNegativeX() {
        Vector3f door = transform(
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_X,
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_Y,
                (float) TardisBotiRenderer.INTERIOR_DOOR_PLANE_Z
        );
        Vector3f right = transform(
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_X + 1.0f,
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_Y,
                (float) TardisBotiRenderer.INTERIOR_DOOR_PLANE_Z
        );

        assertEquals(door.x - 1.0f, right.x, EPSILON);
        assertTrue(right.x < door.x);
    }

    private static Vector3f transform(float x, float y, float z) {
        PoseStack matrices = new PoseStack();
        TardisBotiRenderer.applyInteriorAlignment(matrices, FIRST_DOCTOR_APERTURE);
        Matrix4f matrix = matrices.last().pose();
        return matrix.transformPosition(x, y, z, new Vector3f());
    }
}
