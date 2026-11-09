package com.adamkali.dwm.render.portal;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Pure geometry for the viewer-relative (parallax) portal camera.
 * <p>
 * The real camera is carried through the doorway by the rigid transform that maps the source
 * door frame onto the destination door frame. Rendering the destination scene from that virtual
 * camera with the viewer's own projection makes the doorway behave like a hole in the wall:
 * the portal image lines up 1:1 with the screen pixels behind the door aperture.
 */
public final class PortalViewerCamera {
    /**
     * The oblique clip plane sits this far towards the viewer from the destination door plane so
     * geometry lying exactly on the plane is kept instead of flickering.
     */
    public static final double CLIP_PLANE_OFFSET = 0.02;

    private PortalViewerCamera() {
    }

    /**
     * Maps the viewer camera into the destination frame.
     *
     * @param cameraPos     viewer camera position in source space
     * @param yaw           viewer yaw (degrees, Minecraft convention)
     * @param pitch         viewer pitch (degrees)
     * @param viewRotation  viewer rotation-only view matrix (vanilla {@code viewRotationMatrix})
     * @param source        door frame in source space
     * @param destination   door frame in destination (portal scene) space
     * @param worldOrigin   world position of destination space's origin (footprint origin for SOTO)
     */
    public static PortalCameraTransform.Result map(
            Vec3 cameraPos,
            float yaw,
            float pitch,
            Matrix4fc viewRotation,
            PortalDoorFrame source,
            PortalDoorFrame destination,
            BlockPos worldOrigin
    ) {
        Objects.requireNonNull(cameraPos, "cameraPos");
        Objects.requireNonNull(viewRotation, "viewRotation");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "destination");
        BlockPos origin = worldOrigin == null ? BlockPos.ZERO : worldOrigin;

        double theta = source.yawTo(destination);
        Vec3 eye = rotateY(cameraPos.subtract(source.origin()), theta).add(destination.origin());
        Vec3 look = rotateY(lookVector(yaw, pitch), theta);
        float mappedYaw = wrapDegrees(yaw - (float) Math.toDegrees(theta));

        // V'(q) = viewRot * (M^-1(q) - cameraPos), M^-1(q) = Ry(-theta)(q - dst) + src.
        // Translation is differenced in double so large world coordinates keep precision.
        Vec3 shift = source.origin().subtract(cameraPos);
        Matrix4f view = new Matrix4f(viewRotation)
                .translate((float) shift.x, (float) shift.y, (float) shift.z)
                .rotateY((float) -theta)
                .translate(
                        (float) -destination.origin().x,
                        (float) -destination.origin().y,
                        (float) -destination.origin().z
                );

        Vec3 world = eye.add(origin.getX(), origin.getY(), origin.getZ());
        return new PortalCameraTransform.Result(eye, world, look, mappedYaw, pitch, view);
    }

    /**
     * Destination door plane in the virtual camera's view space, oriented so the visible portal
     * scene (beyond the door) is on the positive side. Feed to {@link PortalObliqueClip}.
     */
    public static Vector4f clipPlaneInView(Matrix4fc viewMatrix, PortalDoorFrame destination) {
        Objects.requireNonNull(viewMatrix, "viewMatrix");
        Objects.requireNonNull(destination, "destination");
        Vec3 n = destination.inward();
        Vec3 point = destination.origin().subtract(n.scale(CLIP_PLANE_OFFSET));
        Vector4f plane = new Vector4f((float) n.x, (float) n.y, (float) n.z, (float) -n.dot(point));
        return new Matrix4f(viewMatrix).invert().transpose().transform(plane);
    }

    /** Minecraft view vector for yaw/pitch in degrees (matches {@code Entity.calculateViewVector}). */
    static Vec3 lookVector(float yaw, float pitch) {
        double yawRad = Math.toRadians(-yaw);
        double pitchRad = Math.toRadians(pitch);
        double cosPitch = Math.cos(pitchRad);
        return new Vec3(Math.sin(yawRad) * cosPitch, -Math.sin(pitchRad), Math.cos(yawRad) * cosPitch);
    }

    /** JOML {@code rotateY} convention. */
    static Vec3 rotateY(Vec3 v, double theta) {
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        return new Vec3(v.x * cos + v.z * sin, v.y, -v.x * sin + v.z * cos);
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
}
