package com.adamkali.dwm.render.portal;

import org.joml.Matrix4f;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Pure geometry for hitch-fixed portal cameras.
 * <p>
 * BER facades supply eye + look; interior player motion does not dolly or pan the hitch.
 */
public final class PortalCameraTransform {

    private PortalCameraTransform() {
    }

    public static Result fromHitch(Vec3 eyeRelative, Vec3 lookDirection, float pitch) {
        return fromHitch(eyeRelative, lookDirection, pitch, BlockPos.ZERO);
    }

    public static Result fromHitch(
            Vec3 eyeRelative,
            Vec3 lookDirection,
            float pitch,
            BlockPos worldOrigin
    ) {
        Objects.requireNonNull(eyeRelative, "eyeRelative");
        Objects.requireNonNull(lookDirection, "lookDirection");
        Objects.requireNonNull(worldOrigin, "worldOrigin");
        Vec3 look = lookDirection.normalize();
        float yaw = yawFromDirection(look);
        Matrix4f viewMatrix = createViewMatrix(eyeRelative, look, yaw);
        Vec3 worldPosition = eyeRelative.add(
                worldOrigin.getX(),
                worldOrigin.getY(),
                worldOrigin.getZ()
        );
        return new Result(eyeRelative, worldPosition, look, yaw, pitch, viewMatrix);
    }

    public static Result fromLookDirection(Vec3 eyeRelative, Direction look, float pitch) {
        Objects.requireNonNull(look, "look");
        return fromHitch(eyeRelative, vector(look), pitch);
    }

    /**
     * Hitch-fixed exterior door look-out (SOTO): eye just outside the shell facing outward.
     */
    public static Result exteriorDoorLookOut(
            BlockPos relativeTardisPos,
            BlockPos footprintOrigin,
            Direction exteriorOutward,
            double eyeHeight,
            double forwardOffset
    ) {
        Objects.requireNonNull(relativeTardisPos, "relativeTardisPos");
        Objects.requireNonNull(footprintOrigin, "footprintOrigin");
        Objects.requireNonNull(exteriorOutward, "exteriorOutward");
        Vec3 look = vector(exteriorOutward);
        Vec3 eye = new Vec3(
                relativeTardisPos.getX() + 0.5,
                relativeTardisPos.getY() + eyeHeight,
                relativeTardisPos.getZ() + 0.5
        ).add(look.scale(0.5 + forwardOffset));
        return fromHitch(eye, look, 0.0f, footprintOrigin);
    }

    private static float yawFromDirection(Vec3 direction) {
        return wrapDegrees((float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
    }

    private static Matrix4f createViewMatrix(Vec3 position, Vec3 look, float yaw) {
        double yawRadians = Math.toRadians(yaw);
        Vec3 right = new Vec3(Math.cos(yawRadians), 0.0, Math.sin(yawRadians));
        Vec3 up = look.cross(right).normalize();
        if (up.lengthSqr() < 1.0e-8) {
            up = new Vec3(0.0, 1.0, 0.0);
        }
        Vec3 center = position.add(look);
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

    private static Vec3 vector(Direction direction) {
        return new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
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
            Vec3 eyeRelative,
            Vec3 worldPosition,
            Vec3 lookDirection,
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

        /** Alias used by SOTO exterior mesh code for footprint-relative eye. */
        public Vec3 ghostRelativePosition() {
            return eyeRelative;
        }

        public Vec3 exteriorWorldPosition() {
            return worldPosition;
        }
    }
}
