package com.adamkali.dwm.render.soto.portal;

import com.adamkali.dwm.tardis.TardisExteriorFacing;
import com.adamkali.dwm.tardis.data.model.TardisSotoAperture;
import com.adamkali.dwm.tardis.interior.TardisInteriorDoorShapes;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Objects;

/**
 * Pure geometry for mapping the interior camera through the SOTO doorway.
 *
 * <p>The source door's room-facing side maps to the back of the exterior door. This is the
 * horizontal 180-degree turn expected from a linked portal: looking out of the interior points
 * out of the exterior, while strafing left/right produces matching parallax without mirroring.
 */
public final class SotoPortalCameraTransform {
    private static final double EXTERIOR_HITCH_OFFSET = 1.0;

    private SotoPortalCameraTransform() {
    }

    public static Result map(
            Camera camera,
            BlockPos interiorDoorPos,
            Direction interiorDoorFacing,
            BlockPos ghostFootprintOrigin,
            int exteriorRotation
    ) {
        Objects.requireNonNull(camera, "camera");
        return map(
                camera.getPos(),
                camera.getYaw(),
                camera.getPitch(),
                interiorDoorPos,
                interiorDoorFacing,
                ghostFootprintOrigin,
                SotoExteriorSampler.RELATIVE_TARDIS_POS,
                exteriorRotation
        );
    }

    static Result map(
            Vec3d cameraPosition,
            float cameraYaw,
            float cameraPitch,
            BlockPos interiorDoorPos,
            Direction interiorDoorFacing,
            BlockPos ghostFootprintOrigin,
            BlockPos relativeTardisPos,
            int exteriorRotation
    ) {
        Objects.requireNonNull(cameraPosition, "cameraPosition");
        Objects.requireNonNull(interiorDoorPos, "interiorDoorPos");
        Objects.requireNonNull(interiorDoorFacing, "interiorDoorFacing");
        Objects.requireNonNull(ghostFootprintOrigin, "ghostFootprintOrigin");
        Objects.requireNonNull(relativeTardisPos, "relativeTardisPos");
        if (!interiorDoorFacing.getAxis().isHorizontal()) {
            throw new IllegalArgumentException("Interior door facing must be horizontal");
        }

        Vec3d sourceRoomNormal = vector(interiorDoorFacing);
        Vec3d sourceRight = vector(interiorDoorFacing.rotateYCounterclockwise());
        Vec3d sourceCenter = sourceDoorCenter(interiorDoorPos, sourceRoomNormal, sourceRight);

        Direction exteriorOutwardDirection = TardisExteriorFacing.doorDirection(exteriorRotation);
        Vec3d exteriorOutward = vector(exteriorOutwardDirection);
        Vec3d exteriorRight = vector(exteriorOutwardDirection.rotateYCounterclockwise());
        Vec3d destinationCenter = destinationDoorCenter(relativeTardisPos, exteriorOutward);

        Vec3d sourceOffset = cameraPosition.subtract(sourceCenter);
        Vec3d mappedOffset = mapVector(sourceOffset, sourceRight, sourceRoomNormal, exteriorRight, exteriorOutward);
        Vec3d ghostRelativePosition = destinationCenter.add(mappedOffset);

        Vec3d sourceLook = directionFromYawPitch(cameraYaw, cameraPitch);
        Vec3d mappedLook = mapVector(sourceLook, sourceRight, sourceRoomNormal, exteriorRight, exteriorOutward)
                .normalize();
        float mappedYaw = yawFromDirection(mappedLook);
        float mappedPitch = pitchFromDirection(mappedLook);
        Matrix4f viewMatrix = createViewMatrix(ghostRelativePosition, mappedLook, mappedYaw);

        Vec3d exteriorWorldPosition = ghostRelativePosition.add(
                ghostFootprintOrigin.getX(),
                ghostFootprintOrigin.getY(),
                ghostFootprintOrigin.getZ()
        );
        return new Result(
                ghostRelativePosition,
                exteriorWorldPosition,
                mappedLook,
                mappedYaw,
                mappedPitch,
                viewMatrix
        );
    }

    private static Vec3d sourceDoorCenter(BlockPos doorPos, Vec3d roomNormal, Vec3d sourceRight) {
        TardisSotoAperture aperture = TardisSotoAperture.CLASSIC_INTERIOR_DOORS;
        double roomPlaneOffset = -aperture.z();
        return new Vec3d(
                doorPos.getX() + 0.5,
                doorPos.getY() + TardisInteriorDoorShapes.MODEL_HEIGHT_BLOCKS - aperture.centerY(),
                doorPos.getZ() + 0.5
        ).add(sourceRight.multiply(TardisInteriorDoorShapes.BANK_CENTER_OFFSET_BLOCKS))
                .add(roomNormal.multiply(roomPlaneOffset));
    }

    private static Vec3d destinationDoorCenter(BlockPos relativeTardisPos, Vec3d exteriorOutward) {
        return new Vec3d(
                relativeTardisPos.getX() + 0.5,
                relativeTardisPos.getY() + 1.0,
                relativeTardisPos.getZ() + 0.5
        ).add(exteriorOutward.multiply(0.5 + EXTERIOR_HITCH_OFFSET));
    }

    private static Vec3d mapVector(
            Vec3d source,
            Vec3d sourceRight,
            Vec3d sourceRoomNormal,
            Vec3d exteriorRight,
            Vec3d exteriorOutward
    ) {
        double right = source.dotProduct(sourceRight);
        double up = source.y;
        double roomward = source.dotProduct(sourceRoomNormal);
        return exteriorRight.multiply(-right)
                .add(0.0, up, 0.0)
                .add(exteriorOutward.multiply(-roomward));
    }

    private static Vec3d directionFromYawPitch(float yaw, float pitch) {
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);
        double horizontal = Math.cos(pitchRadians);
        return new Vec3d(
                -Math.sin(yawRadians) * horizontal,
                -Math.sin(pitchRadians),
                Math.cos(yawRadians) * horizontal
        );
    }

    private static float yawFromDirection(Vec3d direction) {
        return wrapDegrees((float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
    }

    private static float pitchFromDirection(Vec3d direction) {
        double clampedY = Math.max(-1.0, Math.min(1.0, direction.y));
        return (float) Math.toDegrees(-Math.asin(clampedY));
    }

    private static Matrix4f createViewMatrix(Vec3d position, Vec3d look, float yaw) {
        double yawRadians = Math.toRadians(yaw);
        Vec3d right = new Vec3d(Math.cos(yawRadians), 0.0, Math.sin(yawRadians));
        Vec3d up = look.crossProduct(right).normalize();
        Vec3d center = position.add(look);
        return new Matrix4f().lookAt(
                (float) position.x,
                (float) position.y,
                (float) position.z,
                (float) center.x,
                (float) center.y,
                (float) center.z,
                (float) up.x,
                (float) up.y,
                (float) up.z
        );
    }

    private static Vec3d vector(Direction direction) {
        return new Vec3d(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
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

    public record Result(
            Vec3d ghostRelativePosition,
            Vec3d exteriorWorldPosition,
            Vec3d lookDirection,
            float yaw,
            float pitch,
            Matrix4f viewMatrix
    ) {
        public Result {
            viewMatrix = new Matrix4f(viewMatrix);
        }

        @Override
        public Matrix4f viewMatrix() {
            return new Matrix4f(viewMatrix);
        }
    }
}
