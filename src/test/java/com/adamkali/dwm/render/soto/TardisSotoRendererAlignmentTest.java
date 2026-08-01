package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.tardis.data.model.TardisBotiAperture;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisSotoAperture;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisSotoRendererAlignmentTest {
    private static final float EPSILON = 1e-4f;
    private static final TardisSotoAperture SOTO_APERTURE = TardisSotoAperture.CLASSIC_INTERIOR_DOORS;
    private static final TardisBotiAperture FIRST_DOCTOR_APERTURE =
            TardisChameleonVariant.FIRST_DOCTOR_BOX.getAperture();

    @Test
    void applyExteriorAlignment_mapsExteriorDoorCenterToSotoAperture() {
        Vector3f mapped = transformAligned(
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z
        );

        assertEquals(SOTO_APERTURE.centerX(), mapped.x, EPSILON);
        assertEquals(SOTO_APERTURE.centerY(), mapped.y, EPSILON);
        assertEquals(SOTO_APERTURE.z(), mapped.z, EPSILON);
    }

    @Test
    void applyExteriorAlignment_mapsOutwardDepthAwayFromAperture() {
        Vector3f door = transformAligned(
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z
        );
        Vector3f outward = transformAligned(
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z - 3.0f
        );

        // Y-180 flips Z: outward (−Z from door plane) goes through the aperture (look-out).
        assertEquals(door.z + 3.0f, outward.z, EPSILON);
        assertTrue(outward.z > door.z);
    }

    @Test
    void applyExteriorAlignment_mapsUpToNegativeY() {
        Vector3f door = transformAligned(
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z
        );
        Vector3f above = transformAligned(
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y + 1.0f,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z
        );

        assertEquals(door.y - 1.0f, above.y, EPSILON);
        assertTrue(above.y < door.y);
    }

    @Test
    void southFacingCorrection_mapsFrontOfDoorThroughAperture() {
        // Rotation 0 = south (+Z door). +Z face of block at origin is z=1; outward further at z=3.
        Vector3f doorFront = transformWithFacing(
                0,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                1.0f
        );
        Vector3f outward = transformWithFacing(
                0,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                3.0f
        );

        assertEquals(SOTO_APERTURE.centerX(), doorFront.x, EPSILON);
        assertEquals(SOTO_APERTURE.centerY(), doorFront.y, EPSILON);
        assertEquals(SOTO_APERTURE.z(), doorFront.z, EPSILON);
        // With Y-180, outward is through the aperture (higher mapped Z = look-out).
        assertTrue(outward.z > doorFront.z);
    }

    @Test
    void eastFacingCorrection_mapsOutsideThroughAperture() {
        // Rotation 12 = east (−90°). Door on +X face; outside further +X.
        Vector3f doorFront = transformWithFacing(12, 1.0f, (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y, 0.5f);
        Vector3f outward = transformWithFacing(12, 3.0f, (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y, 0.5f);
        assertEquals(SOTO_APERTURE.z(), doorFront.z, 0.05f);
        assertTrue(outward.z > doorFront.z);
    }

    private static Vector3f transformAligned(float x, float y, float z) {
        MatrixStack matrices = new MatrixStack();
        TardisSotoRenderer.applyExteriorAlignment(matrices, SOTO_APERTURE, FIRST_DOCTOR_APERTURE);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        return matrix.transformPosition(x, y, z, new Vector3f());
    }

    private static Vector3f transformWithFacing(int exteriorRotation, float x, float y, float z) {
        MatrixStack matrices = new MatrixStack();
        TardisSotoRenderer.applyExteriorAlignment(matrices, SOTO_APERTURE, FIRST_DOCTOR_APERTURE);
        TardisSotoRenderer.applyDoorFacingCorrection(matrices, exteriorRotation);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        return matrix.transformPosition(x, y, z, new Vector3f());
    }
}
