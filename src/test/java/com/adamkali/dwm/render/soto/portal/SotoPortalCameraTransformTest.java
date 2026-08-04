package com.adamkali.dwm.render.soto.portal;

import com.adamkali.dwm.tardis.TardisExteriorFacing;
import com.adamkali.dwm.tardis.data.model.TardisSotoAperture;
import com.adamkali.dwm.tardis.interior.TardisInteriorDoorShapes;
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
    void mapsLookingThroughEveryInteriorFacingToExteriorOutward() {
        for (Direction interiorFacing : Direction.Type.HORIZONTAL) {
            Vec3d sourceCenter = sourceCenter(interiorFacing);
            Vec3d camera = sourceCenter.add(vector(interiorFacing).multiply(2.0));
            float sourceYaw = yawFor(interiorFacing.getOpposite());

            for (int exteriorRotation : new int[]{0, 4, 8, 12}) {
                SotoPortalCameraTransform.Result result = SotoPortalCameraTransform.map(
                        camera,
                        sourceYaw,
                        0.0f,
                        DOOR_POS,
                        interiorFacing,
                        FOOTPRINT_ORIGIN,
                        SotoExteriorSampler.RELATIVE_TARDIS_POS,
                        exteriorRotation
                );

                assertVec(vector(TardisExteriorFacing.doorDirection(exteriorRotation)), result.lookDirection());
                assertEquals(0.0f, result.pitch(), 1.0e-4f);
            }
        }
    }

    @Test
    void mapsRoomDistanceBehindExteriorDoor() {
        Direction interiorFacing = Direction.SOUTH;
        Vec3d sourceCenter = sourceCenter(interiorFacing);
        Vec3d camera = sourceCenter.add(vector(interiorFacing).multiply(3.0));

        SotoPortalCameraTransform.Result result = SotoPortalCameraTransform.map(
                camera,
                180.0f,
                0.0f,
                DOOR_POS,
                interiorFacing,
                FOOTPRINT_ORIGIN,
                SotoExteriorSampler.RELATIVE_TARDIS_POS,
                0
        );

        Vec3d exteriorOutward = vector(Direction.NORTH);
        Vec3d destinationCenter = destinationCenter(exteriorOutward);
        assertVec(destinationCenter.add(exteriorOutward.multiply(-3.0)), result.ghostRelativePosition());
    }

    @Test
    void mapsStrafeWithoutMirroringThePortalView() {
        Direction interiorFacing = Direction.SOUTH;
        Vec3d sourceRight = vector(interiorFacing.rotateYCounterclockwise());
        Vec3d camera = sourceCenter(interiorFacing)
                .add(vector(interiorFacing).multiply(2.0))
                .add(sourceRight.multiply(1.25));

        SotoPortalCameraTransform.Result result = SotoPortalCameraTransform.map(
                camera,
                180.0f,
                0.0f,
                DOOR_POS,
                interiorFacing,
                FOOTPRINT_ORIGIN,
                SotoExteriorSampler.RELATIVE_TARDIS_POS,
                4
        );

        Direction exteriorOutwardDirection = TardisExteriorFacing.doorDirection(4);
        Vec3d exteriorOutward = vector(exteriorOutwardDirection);
        Vec3d exteriorRight = vector(exteriorOutwardDirection.rotateYCounterclockwise());
        Vec3d expected = destinationCenter(exteriorOutward)
                .add(exteriorOutward.multiply(-2.0))
                .add(exteriorRight.multiply(-1.25));
        assertVec(expected, result.ghostRelativePosition());
    }

    @Test
    void preservesPitchAndBuildsInverseViewMatrix() {
        Direction interiorFacing = Direction.WEST;
        Vec3d camera = sourceCenter(interiorFacing).add(vector(interiorFacing).multiply(2.0));

        SotoPortalCameraTransform.Result result = SotoPortalCameraTransform.map(
                camera,
                -45.0f,
                -25.0f,
                DOOR_POS,
                interiorFacing,
                FOOTPRINT_ORIGIN,
                SotoExteriorSampler.RELATIVE_TARDIS_POS,
                8
        );

        assertEquals(-25.0f, result.pitch(), 1.0e-4f);
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
        Vec3d camera = sourceCenter(Direction.NORTH).add(0.0, 0.75, -2.0);
        SotoPortalCameraTransform.Result result = SotoPortalCameraTransform.map(
                camera,
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

    private static Vec3d sourceCenter(Direction facing) {
        Vec3d roomNormal = vector(facing);
        Vec3d right = vector(facing.rotateYCounterclockwise());
        return new Vec3d(
                DOOR_POS.getX() + 0.5,
                DOOR_POS.getY() + TardisInteriorDoorShapes.MODEL_HEIGHT_BLOCKS
                        - TardisSotoAperture.CLASSIC_INTERIOR_DOORS.centerY(),
                DOOR_POS.getZ() + 0.5
        ).add(right.multiply(TardisInteriorDoorShapes.BANK_CENTER_OFFSET_BLOCKS))
                .add(roomNormal.multiply(-TardisSotoAperture.CLASSIC_INTERIOR_DOORS.z()));
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

    private static Vec3d vector(Direction direction) {
        return new Vec3d(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
    }

    private static void assertVec(Vec3d expected, Vec3d actual) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }
}
