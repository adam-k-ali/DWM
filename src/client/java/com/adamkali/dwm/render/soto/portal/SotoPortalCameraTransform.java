package com.adamkali.dwm.render.soto.portal;

import com.adamkali.dwm.render.soto.TardisSotoRenderer;
import com.adamkali.dwm.tardis.TardisExteriorFacing;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import org.joml.Matrix4f;

import java.util.Objects;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Pure geometry for the SOTO portal exterior camera.
 *
 * <p>The portal eye stays fixed at the exterior door hitch looking outward. Interior player
 * movement and head rotation do not dolly or pan the exterior view; the aperture composite only
 * reveals that fixed render.
 */
public final class SotoPortalCameraTransform {

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
                camera.getPosition(),
                camera.getYRot(),
                camera.getXRot(),
                interiorDoorPos,
                interiorDoorFacing,
                ghostFootprintOrigin,
                SotoExteriorSampler.RELATIVE_TARDIS_POS,
                exteriorRotation
        );
    }

    static Result map(
            Vec3 cameraPosition,
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

        Direction exteriorOutwardDirection = TardisExteriorFacing.doorDirection(exteriorRotation);
        Vec3 exteriorOutward = vector(exteriorOutwardDirection);
        Vec3 ghostRelativePosition = destinationDoorCenter(relativeTardisPos, exteriorOutward);
        float mappedYaw = yawFromDirection(exteriorOutward);
        float mappedPitch = 0.0f;
        Matrix4f viewMatrix = createViewMatrix(ghostRelativePosition, exteriorOutward, mappedYaw);

        Vec3 exteriorWorldPosition = ghostRelativePosition.add(
                ghostFootprintOrigin.getX(),
                ghostFootprintOrigin.getY(),
                ghostFootprintOrigin.getZ()
        );
        return new Result(
                ghostRelativePosition,
                exteriorWorldPosition,
                exteriorOutward,
                mappedYaw,
                mappedPitch,
                viewMatrix
        );
    }

    private static Vec3 destinationDoorCenter(BlockPos relativeTardisPos, Vec3 exteriorOutward) {
        return new Vec3(
                relativeTardisPos.getX() + 0.5,
                relativeTardisPos.getY() + TardisSotoRenderer.PREVIEW_EYE_HEIGHT,
                relativeTardisPos.getZ() + 0.5
        ).add(exteriorOutward.scale(0.5 + TardisSotoRenderer.PREVIEW_FORWARD_OFFSET));
    }

    private static float yawFromDirection(Vec3 direction) {
        return wrapDegrees((float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
    }

    private static Matrix4f createViewMatrix(Vec3 position, Vec3 look, float yaw) {
        double yawRadians = Math.toRadians(yaw);
        Vec3 right = new Vec3(Math.cos(yawRadians), 0.0, Math.sin(yawRadians));
        Vec3 up = look.cross(right).normalize();
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
            Vec3 ghostRelativePosition,
            Vec3 exteriorWorldPosition,
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
    }
}
