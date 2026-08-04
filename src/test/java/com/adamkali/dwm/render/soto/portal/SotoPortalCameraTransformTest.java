package com.adamkali.dwm.render.soto.portal;

import com.adamkali.dwm.tardis.TardisExteriorFacing;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SotoPortalCameraTransformTest {
    private static final double EPSILON = 1.0e-5;
    private static final BlockPos DOOR_POS = new BlockPos(100, 20, -30);
    private static final BlockPos FOOTPRINT_ORIGIN = new BlockPos(-400, 64, 700);

    @Test
    void pinsEyeAtExteriorHitchLookingOutwardForEveryFacing() {
        for (Direction interiorFacing : Direction.Type.HORIZONTAL) {
            Vec3d camera = new Vec3d(
                    DOOR_POS.getX() + 2.5,
                    DOOR_POS.getY() + 1.6,
                    DOOR_POS.getZ() - 3.0
            );

            for (int exteriorRotation : new int[]{0, 4, 8, 12}) {
                Direction exteriorOutwardDirection = TardisExteriorFacing.doorDirection(exteriorRotation);
                Vec3d exteriorOutward = vector(exteriorOutwardDirection);
                SotoPortalCameraTransform.Result result = SotoPortalCameraTransform.map(
                        camera,
                        45.0f,
                        -12.0f,
                        DOOR_POS,
                        interiorFacing,
                        FOOTPRINT_ORIGIN,
                        SotoExteriorSampler.RELATIVE_TARDIS_POS,
                        exteriorRotation
                );

                assertVec(destinationCenter(exteriorOutward), result.ghostRelativePosition());
                assertVec(exteriorOutward, result.lookDirection());
                assertEquals(0.0f, result.pitch(), 1.0e-4f);
                float expectedYaw = yawFor(exteriorOutwardDirection);
                float yawDelta = Math.abs(wrapDegrees(result.yaw() - expectedYaw));
                assertEquals(0.0f, yawDelta, 1.0e-4f);
            }
        }
    }

    @Test
    void ignoresInteriorCameraTranslation() {
        Direction interiorFacing = Direction.SOUTH;
        Vec3d nearDoor = new Vec3d(DOOR_POS.getX() + 0.5, DOOR_POS.getY() + 1.5, DOOR_POS.getZ() + 1.0);
        Vec3d farLeft = nearDoor.add(4.0, 0.5, 3.0);

        SotoPortalCameraTransform.Result near = SotoPortalCameraTransform.map(
                nearDoor,
                180.0f,
                0.0f,
                DOOR_POS,
                interiorFacing,
                FOOTPRINT_ORIGIN,
                SotoExteriorSampler.RELATIVE_TARDIS_POS,
                0
        );
        SotoPortalCameraTransform.Result far = SotoPortalCameraTransform.map(
                farLeft,
                90.0f,
                20.0f,
                DOOR_POS,
                interiorFacing,
                FOOTPRINT_ORIGIN,
                SotoExteriorSampler.RELATIVE_TARDIS_POS,
                0
        );

        assertVec(near.ghostRelativePosition(), far.ghostRelativePosition());
        assertVec(near.lookDirection(), far.lookDirection());
        assertEquals(near.yaw(), far.yaw(), 1.0e-4f);
        assertEquals(near.pitch(), far.pitch(), 1.0e-4f);
    }

    @Test
    void buildsInverseViewMatrixAtHitch() {
        SotoPortalCameraTransform.Result result = SotoPortalCameraTransform.map(
                new Vec3d(0.0, 0.0, 0.0),
                -45.0f,
                -25.0f,
                DOOR_POS,
                Direction.WEST,
                FOOTPRINT_ORIGIN,
                SotoExteriorSampler.RELATIVE_TARDIS_POS,
                8
        );

        Vector3f cameraAtOrigin = result.viewMatrix().transformPosition(
                (float) result.ghostRelativePosition().x,
                (float) result.ghostRelativePosition().y,
                (float) result.ghostRelativePosition().z,
                new Vector3f()
        );
        assertEquals(0.0f, cameraAtOrigin.x, 1.0e-3f);
        assertEquals(0.0f, cameraAtOrigin.y, 1.0e-3f);
        assertEquals(0.0f, cameraAtOrigin.z, 1.0e-3f);
    }

    @Test
    void reportsExteriorWorldPositionFromFootprintOrigin() {
        SotoPortalCameraTransform.Result result = SotoPortalCameraTransform.map(
                new Vec3d(DOOR_POS.getX(), DOOR_POS.getY() + 1.0, DOOR_POS.getZ()),
                0.0f,
                12.0f,
                DOOR_POS,
                Direction.NORTH,
                FOOTPRINT_ORIGIN,
                SotoExteriorSampler.RELATIVE_TARDIS_POS,
                12
        );

        assertVec(
                result.ghostRelativePosition().add(
                        FOOTPRINT_ORIGIN.getX(),
                        FOOTPRINT_ORIGIN.getY(),
                        FOOTPRINT_ORIGIN.getZ()
                ),
                result.exteriorWorldPosition()
        );
    }

    private static Vec3d destinationCenter(Vec3d exteriorOutward) {
        BlockPos relative = SotoExteriorSampler.RELATIVE_TARDIS_POS;
        return new Vec3d(relative.getX() + 0.5, relative.getY() + 1.0, relative.getZ() + 0.5)
                .add(exteriorOutward.multiply(1.5));
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

    private static Vec3d vector(Direction direction) {
        return new Vec3d(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
    }

    private static void assertVec(Vec3d expected, Vec3d actual) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }
}
