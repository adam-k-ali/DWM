package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.tardis.data.model.PortalAperture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalViewerCameraTest {
    private static final float EPSILON = 1.0e-3f;

    /** Exterior door in the overworld: doorway faces west, so stepping in heads east. */
    private static final PortalDoorFrame EXTERIOR = new PortalDoorFrame(
            new Vec3(-12_345.5, 70.0, 98_765.25), new Vec3(1.0, 0.0, 0.0));
    /** Interior door bank in plot-local coords: stepping in heads south (+Z) into the room. */
    private static final PortalDoorFrame INTERIOR = new PortalDoorFrame(
            new Vec3(5.5, 1.0, 2.125), new Vec3(0.0, 0.0, 1.0));

    @Test
    void lookVector_matchesVanillaCameraForwards() {
        float yaw = 37.0f;
        float pitch = -21.0f;
        Matrix4f viewRotation = viewRotation(yaw, pitch);
        Vector3f forward = new Matrix4f(viewRotation).invert().transformDirection(new Vector3f(0, 0, -1));
        Vec3 look = PortalViewerCamera.lookVector(yaw, pitch);
        assertEquals(forward.x, look.x, EPSILON);
        assertEquals(forward.y, look.y, EPSILON);
        assertEquals(forward.z, look.z, EPSILON);
    }

    @Test
    void sourcePointAndItsMappedPoint_landOnTheSameViewCoordinates() {
        Vec3 cameraPos = EXTERIOR.origin().add(-4.2, 1.62, 1.3);
        float yaw = -70.0f;
        float pitch = 12.0f;
        Matrix4f viewRotation = viewRotation(yaw, pitch);
        PortalCameraTransform.Result mapped = PortalViewerCamera.map(
                cameraPos, yaw, pitch, viewRotation, EXTERIOR, INTERIOR, BlockPos.ZERO);

        // A point just inside the exterior doorway, expressed in source space…
        Vec3 sourcePoint = EXTERIOR.origin().add(0.6, 1.1, -0.3);
        Vector3f expected = viewRotation.transformPosition(
                (float) (sourcePoint.x - cameraPos.x),
                (float) (sourcePoint.y - cameraPos.y),
                (float) (sourcePoint.z - cameraPos.z),
                new Vector3f());
        // …and the same point carried through the door into destination space.
        Vec3 offset = sourcePoint.subtract(EXTERIOR.origin());
        Vec3 destPoint = PortalViewerCamera.rotateY(offset, EXTERIOR.yawTo(INTERIOR)).add(INTERIOR.origin());
        Vector3f actual = mapped.viewMatrix().transformPosition(
                (float) destPoint.x, (float) destPoint.y, (float) destPoint.z, new Vector3f());

        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }

    @Test
    void mappedEye_isTheViewMatrixOrigin_andYawAgreesWithLook() {
        Vec3 cameraPos = EXTERIOR.origin().add(-3.0, 1.62, 0.5);
        float yaw = -90.0f;
        float pitch = 5.0f;
        PortalCameraTransform.Result mapped = PortalViewerCamera.map(
                cameraPos, yaw, pitch, viewRotation(yaw, pitch), EXTERIOR, INTERIOR, new BlockPos(1000, 0, -2000));

        Vector3f atEye = mapped.viewMatrix().transformPosition(
                (float) mapped.eyeRelative().x, (float) mapped.eyeRelative().y, (float) mapped.eyeRelative().z,
                new Vector3f());
        assertEquals(0.0f, atEye.length(), EPSILON);

        // 3 blocks in front of the door outside → 3 blocks behind the interior door plane.
        assertEquals(INTERIOR.origin().z - 3.0, mapped.eyeRelative().z, EPSILON);
        assertEquals(INTERIOR.origin().x - 0.5, mapped.eyeRelative().x, EPSILON);
        assertEquals(1000.0 + mapped.eyeRelative().x, mapped.worldPosition().x, EPSILON);

        Vec3 fromYaw = PortalViewerCamera.lookVector(mapped.yaw(), mapped.pitch());
        assertEquals(mapped.lookDirection().x, fromYaw.x, EPSILON);
        assertEquals(mapped.lookDirection().z, fromYaw.z, EPSILON);
        // Facing the exterior door (east) means facing into the room (south).
        assertEquals(1.0, mapped.lookDirection().z, 0.01);
    }

    @Test
    void clipPlane_keepsTheRoomAndRejectsTheViewerSide() {
        Vec3 cameraPos = EXTERIOR.origin().add(-2.0, 1.62, 0.0);
        float yaw = -90.0f;
        PortalCameraTransform.Result mapped = PortalViewerCamera.map(
                cameraPos, yaw, 0.0f, viewRotation(yaw, 0.0f), EXTERIOR, INTERIOR, BlockPos.ZERO);
        Vector4f plane = PortalViewerCamera.clipPlaneInView(mapped.viewMatrix(), INTERIOR);

        assertTrue(plane.w < 0.0f, "camera must be behind the door plane");
        assertEquals(-(2.0 - PortalViewerCamera.CLIP_PLANE_OFFSET), plane.w, EPSILON);

        Vector4f roomPoint = viewPoint(mapped, INTERIOR.origin().add(0.0, 1.0, 3.0));
        Vector4f wallBehindDoor = viewPoint(mapped, INTERIOR.origin().add(0.0, 1.0, -1.0));
        assertTrue(plane.dot(roomPoint) > 0.0f);
        assertTrue(plane.dot(wallBehindDoor) < 0.0f);
    }

    @Test
    void doorFrameFromAperture_usesLowerEdgeAndHintSide() {
        // X-180 flip: model +Y points down, so aperture y0 is the top edge in world space.
        Matrix4f pose = new Matrix4f().translate(10.0f, 3.0f, 20.0f).rotateX((float) Math.PI);
        PortalAperture aperture = new PortalAperture(-0.5f, 0.5f, 0.0f, 2.0f, 0.25f);
        PortalDoorFrame frame = PortalDoorFrame.fromAperture(pose, aperture, new Vec3(100, 0, 0), new Vec3(0, 0, 1));

        assertEquals(110.0, frame.origin().x, EPSILON);
        assertEquals(1.0, frame.origin().y, EPSILON);
        assertEquals(19.75, frame.origin().z, EPSILON);
        assertEquals(1.0, frame.inward().z, EPSILON);
    }

    private static Vector4f viewPoint(PortalCameraTransform.Result mapped, Vec3 p) {
        return mapped.viewMatrix().transform(new Vector4f((float) p.x, (float) p.y, (float) p.z, 1.0f));
    }

    /** Vanilla {@code Camera.getViewRotationMatrix} for the given yaw/pitch. */
    private static Matrix4f viewRotation(float yaw, float pitch) {
        Quaternionf rotation = new Quaternionf().rotationYXZ(
                (float) Math.PI - (float) Math.toRadians(yaw), (float) -Math.toRadians(pitch), 0.0f);
        return new Matrix4f().rotation(rotation.conjugate(new Quaternionf()));
    }
}
