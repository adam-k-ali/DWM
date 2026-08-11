package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.render.soto.TardisSotoRenderer;
import com.adamkali.dwm.tardis.TardisExteriorFacing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortalCameraTransformTest {
    private static final double EPSILON = 1.0e-5;
    private static final BlockPos FOOTPRINT_ORIGIN = new BlockPos(-400, 64, 700);
    /** Matches {@code SotoExteriorSampler.RELATIVE_TARDIS_POS} without loading that class. */
    private static final BlockPos RELATIVE_TARDIS_POS = new BlockPos(5, 1, 5);

    @Test
    void fromHitch_isInvariantToCallerEyeArgsWhenSameHitch() {
        Vec3 eye = new Vec3(10.5, 70.75, -3.0);
        Vec3 look = new Vec3(0.0, 0.0, 1.0);
        PortalCameraTransform.Result a = PortalCameraTransform.fromHitch(eye, look, 0.0f);
        PortalCameraTransform.Result b = PortalCameraTransform.fromHitch(eye, look, 0.0f);

        assertVec(a.eyeRelative(), b.eyeRelative());
        assertVec(a.lookDirection(), b.lookDirection());
        assertEquals(a.yaw(), b.yaw(), 1.0e-4f);
        assertEquals(a.pitch(), b.pitch(), 1.0e-4f);
    }

    @Test
    void fromLookDirection_buildsInverseViewMatrixAtEye() {
        Vec3 eye = new Vec3(5.5, 2.0, 0.0);
        PortalCameraTransform.Result result =
                PortalCameraTransform.fromLookDirection(eye, Direction.SOUTH, 0.0f);

        Vector3f cameraAtOrigin = result.viewMatrix().transformPosition(
                (float) result.eyeRelative().x,
                (float) result.eyeRelative().y,
                (float) result.eyeRelative().z,
                new Vector3f()
        );
        assertEquals(0.0f, cameraAtOrigin.x, 1.0e-3f);
        assertEquals(0.0f, cameraAtOrigin.y, 1.0e-3f);
        assertEquals(0.0f, cameraAtOrigin.z, 1.0e-3f);
        assertEquals(0.0f, result.pitch(), 1.0e-4f);
    }

    @Test
    void exteriorDoorLookOut_pinsEyeAtHitchForEveryFacing() {
        for (int exteriorRotation : new int[]{0, 4, 8, 12}) {
            Direction exteriorOutwardDirection = TardisExteriorFacing.doorDirection(exteriorRotation);
            Vec3 exteriorOutward = vector(exteriorOutwardDirection);
            PortalCameraTransform.Result result = PortalCameraTransform.exteriorDoorLookOut(
                    RELATIVE_TARDIS_POS,
                    FOOTPRINT_ORIGIN,
                    exteriorOutwardDirection,
                    TardisSotoRenderer.PREVIEW_EYE_HEIGHT,
                    TardisSotoRenderer.PREVIEW_FORWARD_OFFSET
            );

            assertVec(destinationCenter(exteriorOutward), result.eyeRelative());
            assertVec(exteriorOutward, result.lookDirection());
            assertEquals(0.0f, result.pitch(), 1.0e-4f);
            float expectedYaw = yawFor(exteriorOutwardDirection);
            float yawDelta = Math.abs(wrapDegrees(result.yaw() - expectedYaw));
            assertEquals(0.0f, yawDelta, 1.0e-4f);
        }
    }

    @Test
    void exteriorDoorLookOut_reportsWorldPositionFromFootprintOrigin() {
        PortalCameraTransform.Result result = PortalCameraTransform.exteriorDoorLookOut(
                RELATIVE_TARDIS_POS,
                FOOTPRINT_ORIGIN,
                TardisExteriorFacing.doorDirection(12),
                TardisSotoRenderer.PREVIEW_EYE_HEIGHT,
                TardisSotoRenderer.PREVIEW_FORWARD_OFFSET
        );

        assertVec(
                result.eyeRelative().add(
                        FOOTPRINT_ORIGIN.getX(),
                        FOOTPRINT_ORIGIN.getY(),
                        FOOTPRINT_ORIGIN.getZ()
                ),
                result.worldPosition()
        );
    }

    private static Vec3 destinationCenter(Vec3 exteriorOutward) {
        return new Vec3(
                RELATIVE_TARDIS_POS.getX() + 0.5,
                RELATIVE_TARDIS_POS.getY() + TardisSotoRenderer.PREVIEW_EYE_HEIGHT,
                RELATIVE_TARDIS_POS.getZ() + 0.5
        ).add(exteriorOutward.scale(0.5 + TardisSotoRenderer.PREVIEW_FORWARD_OFFSET));
    }

    private static float yawFor(Direction direction) {
        return switch (direction) {
            case SOUTH -> 0.0f;
            case WEST -> 90.0f;
            case NORTH -> 180.0f;
            case EAST -> -90.0f;
            default -> throw new IllegalArgumentException("Horizontal direction required");
        };
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0f;
        if (wrapped >= 180.0f) {
            wrapped -= 360.0f;
        }
        if (wrapped < -180.0f) {
            wrapped += 360.0f;
        }
        return wrapped;
    }

    private static Vec3 vector(Direction direction) {
        return new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    private static void assertVec(Vec3 expected, Vec3 actual) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }
}
