package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.tardis.data.model.TardisBotiAperture;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisSotoAperture;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
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
    void exteriorDoorPlane_isOffsetForwardOfDoorFace() {
        double doorFaceZ = SotoExteriorSampler.RELATIVE_TARDIS_POS.getZ() + 0.0;
        assertEquals(doorFaceZ - TardisSotoRenderer.PREVIEW_FORWARD_OFFSET,
                TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z, EPSILON);
    }

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
    void rotation0_mapsVisualNorthDoorThroughAperture() {
        // Rotation 0: hitch is PREVIEW_FORWARD_OFFSET north of the door face; outward further −Z.
        Vector3f doorFront = transformWithFacing(
                0,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z
        );
        Vector3f outward = transformWithFacing(
                0,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z - 3.0f
        );

        assertEquals(SOTO_APERTURE.centerX(), doorFront.x, EPSILON);
        assertEquals(SOTO_APERTURE.centerY(), doorFront.y, EPSILON);
        assertEquals(SOTO_APERTURE.z(), doorFront.z, EPSILON);
        assertTrue(outward.z > doorFront.z);
    }

    @Test
    void eastFacingCorrection_mapsOutsideThroughAperture() {
        // Rotation 12 = east (270° raw). Shell door faces west (−X); hitch is 1 block west of west face.
        float hitchX = SotoExteriorSampler.RELATIVE_TARDIS_POS.getX() + 0.0f
                - (float) TardisSotoRenderer.PREVIEW_FORWARD_OFFSET;
        Vector3f doorFront = transformWithFacing(
                12, hitchX, (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y, 5.5f);
        Vector3f outward = transformWithFacing(
                12, hitchX - 3.0f, (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y, 5.5f);
        assertEquals(SOTO_APERTURE.z(), doorFront.z, 0.05f);
        assertTrue(outward.z > doorFront.z);
    }

    @Test
    void lookoutStableView_usesFixedViewDepthRegardlessOfStrafe() {
        MatrixStack centered = new MatrixStack();
        TardisSotoRenderer.applyExteriorAlignment(centered, SOTO_APERTURE, FIRST_DOCTOR_APERTURE);
        TardisSotoRenderer.applyDoorFacingCorrection(centered, 0);
        float depthCentered = hitchZAfterStableView(copyStack(centered));

        Matrix4f bm = centered.peek().getPositionMatrix();
        Vector3f hitch0 = bm.transformPosition(
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z,
                new Vector3f()
        );
        Vector3f outward0 = bm.transformPosition(
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z - 1.0f,
                new Vector3f()
        );
        Vector3f outwardDir = new Vector3f(outward0).sub(hitch0).normalize();
        Vector3f strafe = new Vector3f(0.0f, 1.0f, 0.0f).cross(outwardDir);
        if (strafe.lengthSquared() < 1e-6f) {
            strafe.set(1.0f, 0.0f, 0.0f).cross(outwardDir);
        }
        strafe.normalize().mul(6.0f);

        MatrixStack strafed = copyStack(centered);
        strafed.peek().getPositionMatrix().mulLocal(new Matrix4f().translation(
                strafe.x - outwardDir.x * 4.0f,
                strafe.y - outwardDir.y * 4.0f,
                strafe.z - outwardDir.z * 4.0f
        ));
        float depthStrafed = hitchZAfterStableView(strafed);

        assertEquals(-TardisSotoRenderer.LOOKOUT_VIEW_DEPTH, depthCentered, 1e-3f);
        assertEquals(-TardisSotoRenderer.LOOKOUT_VIEW_DEPTH, depthStrafed, 1e-3f);
    }

    @Test
    void lookoutStableView_centersOutwardAlongNegativeZ() {
        MatrixStack matrices = new MatrixStack();
        TardisSotoRenderer.applyExteriorAlignment(matrices, SOTO_APERTURE, FIRST_DOCTOR_APERTURE);
        TardisSotoRenderer.applyDoorFacingCorrection(matrices, 0);
        matrices.peek().getPositionMatrix().mulLocal(new Matrix4f().translation(2.0f, 0.5f, -3.0f));
        TardisSotoRenderer.applyLookoutStableView(matrices);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Vector3f hitch = matrix.transformPosition(
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z,
                new Vector3f()
        );
        Vector3f outward = matrix.transformPosition(
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z - 3.0f,
                new Vector3f()
        );
        assertEquals(0.0f, hitch.x, 1e-3f);
        assertEquals(0.0f, hitch.y, 1e-3f);
        assertEquals(-TardisSotoRenderer.LOOKOUT_VIEW_DEPTH, hitch.z, 1e-3f);
        assertTrue(outward.z < hitch.z);
        assertEquals(-1.0f, new Vector3f(outward).sub(hitch).normalize().z, 1e-3f);
    }

    private static MatrixStack copyStack(MatrixStack source) {
        MatrixStack copy = new MatrixStack();
        copy.peek().getPositionMatrix().set(source.peek().getPositionMatrix());
        copy.peek().getNormalMatrix().set(source.peek().getNormalMatrix());
        return copy;
    }

    private static float hitchZAfterStableView(MatrixStack matrices) {
        TardisSotoRenderer.applyLookoutStableView(matrices);
        Vector3f hitch = matrices.peek().getPositionMatrix().transformPosition(
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_X,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_CENTER_Y,
                (float) TardisSotoRenderer.EXTERIOR_DOOR_PLANE_Z,
                new Vector3f()
        );
        return hitch.z;
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
