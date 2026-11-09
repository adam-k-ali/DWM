package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.tardis.data.model.PortalAperture;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;

/**
 * A doorway's placement in some coordinate space: the bottom-centre of the aperture on the door
 * plane, and the horizontal unit direction a viewer travels when stepping through it.
 * <p>
 * Pairing a source frame (where the viewer stands) with a destination frame (where the portal
 * scene lives) defines the rigid transform used by {@link PortalViewerCamera}.
 */
public record PortalDoorFrame(Vec3 origin, Vec3 inward) {
    public PortalDoorFrame {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(inward, "inward");
        Vec3 flat = new Vec3(inward.x, 0.0, inward.z);
        if (flat.lengthSqr() < 1.0e-12) {
            throw new IllegalArgumentException("Door frame inward direction must be horizontal");
        }
        inward = flat.normalize();
    }

    /**
     * Builds a frame from an aperture rendered through {@code pose}.
     *
     * @param pose       model-local → target-space matrix (e.g. a BER pose, which is camera-relative)
     * @param offset     added to every transformed point (e.g. camera position to reach world space)
     * @param inwardHint any vector on the "through the door" side; picks the sign of the plane normal
     */
    public static PortalDoorFrame fromAperture(
            Matrix4fc pose,
            PortalAperture aperture,
            Vec3 offset,
            Vec3 inwardHint
    ) {
        Objects.requireNonNull(pose, "pose");
        Objects.requireNonNull(aperture, "aperture");
        Objects.requireNonNull(inwardHint, "inwardHint");
        Vec3 base = offset == null ? Vec3.ZERO : offset;
        float z = aperture.z();
        Vec3 left0 = transform(pose, aperture.x0(), aperture.y0(), z, base);
        Vec3 right0 = transform(pose, aperture.x1(), aperture.y0(), z, base);
        Vec3 left1 = transform(pose, aperture.x0(), aperture.y1(), z, base);
        Vec3 right1 = transform(pose, aperture.x1(), aperture.y1(), z, base);
        Vec3 mid0 = left0.add(right0).scale(0.5);
        Vec3 mid1 = left1.add(right1).scale(0.5);
        // BER flips (X-180) can put model y0 at the top; the frame origin is always the lower edge.
        Vec3 origin = mid0.y <= mid1.y ? mid0 : mid1;

        Vec3 across = right0.subtract(left0);
        Vec3 normal = new Vec3(across.z, 0.0, -across.x);
        if (normal.lengthSqr() < 1.0e-12) {
            normal = new Vec3(inwardHint.x, 0.0, inwardHint.z);
        }
        if (normal.dot(inwardHint) < 0.0) {
            normal = normal.scale(-1.0);
        }
        return new PortalDoorFrame(origin, normal);
    }

    /** Rotation about +Y (radians, JOML {@code rotateY} convention) taking this inward onto {@code other}'s. */
    public double yawTo(PortalDoorFrame other) {
        return heading(other.inward) - heading(inward);
    }

    /** Heading of a horizontal vector such that {@code rotateY(a)} adds {@code a} to it. */
    static double heading(Vec3 v) {
        return Math.atan2(v.x, v.z);
    }

    private static Vec3 transform(Matrix4fc pose, float x, float y, float z, Vec3 offset) {
        Vector3f out = pose.transformPosition(x, y, z, new Vector3f());
        return new Vec3(out.x + offset.x, out.y + offset.y, out.z + offset.z);
    }
}
