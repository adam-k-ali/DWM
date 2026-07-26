package com.adamkali.dwm.render.boti;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisBotiRendererAlignmentTest {
    private static final float EPSILON = 1e-4f;

    @Test
    void applyInteriorAlignment_mapsDoorCenterToAperture() {
        Vector3f mapped = transform(
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_X,
                (float) TardisBotiRenderer.INTERIOR_DOOR_CENTER_Y,
                (float) TardisBotiRenderer.INTERIOR_DOOR_PLANE_Z
        );

        assertEquals(0.0f, mapped.x, EPSILON);
        assertEquals(TardisBotiRenderer.apertureCenterY(), mapped.y, EPSILON);
        assertEquals(TardisBotiRenderer.APERTURE_Z, mapped.z, EPSILON);
    }

    @Test
    void applyInteriorAlignment_mapsRoomDepthToModelNegativeZ() {
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

        assertTrue(deeper.z < door.z, "Room +Z should map to model -Z (portal view depth)");
        assertEquals(door.z - 3.0f, deeper.z, EPSILON);
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

        // BER rotateX(180) makes model -Y world-up; alignment X-180 sends interior +Y to model -Y.
        assertEquals(door.y - 1.0f, above.y, EPSILON);
        assertTrue(above.y < door.y);
    }

    private static Vector3f transform(float x, float y, float z) {
        MatrixStack matrices = new MatrixStack();
        TardisBotiRenderer.applyInteriorAlignment(matrices);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        return matrix.transformPosition(x, y, z, new Vector3f());
    }
}
