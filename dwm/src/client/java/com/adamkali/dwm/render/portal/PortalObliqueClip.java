package com.adamkali.dwm.render.portal;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

/**
 * Oblique near-plane clipping (Lengyel) for vanilla's reverse-Z projection.
 * <p>
 * Replaces the near plane with the door plane, so geometry between the virtual portal camera and
 * the destination doorway (walls around the door, the shell body) is clipped without needing
 * clip distances in vanilla shaders. Reverse-Z: near maps to NDC depth 1, far to 0 (zero-to-one)
 * or -1 (GL default range).
 */
public final class PortalObliqueClip {
    /**
     * When the camera is closer than this to the clip plane the oblique frustum degenerates;
     * the unmodified projection is returned instead.
     */
    public static final float MIN_PLANE_DISTANCE = 0.05f;

    private PortalObliqueClip() {
    }

    /**
     * @param projection reverse-Z perspective projection
     * @param planeView  clip plane in view space; the kept side is {@code dot(plane, v) >= 0} and
     *                   the camera (origin) must be on the negative side
     * @param zZeroToOne whether NDC depth is [0, 1] (otherwise [-1, 1])
     * @return a new matrix; a plain copy of {@code projection} when the plane is unusable
     */
    public static Matrix4f apply(Matrix4fc projection, Vector4fc planeView, boolean zZeroToOne) {
        Matrix4f result = new Matrix4f(projection);
        if (planeView == null) {
            return result;
        }
        float normalLength = (float) Math.sqrt(
                planeView.x() * planeView.x() + planeView.y() * planeView.y() + planeView.z() * planeView.z());
        if (normalLength < 1.0e-6f) {
            return result;
        }
        Vector4f plane = new Vector4f(planeView).div(normalLength);
        // plane.w is the signed distance of the camera from the plane; it must sit clearly behind it.
        if (plane.w > -MIN_PLANE_DISTANCE) {
            return result;
        }

        float farNdc = zZeroToOne ? 0.0f : -1.0f;
        Vector4f farCorner = new Matrix4f(projection).invert().transform(
                new Vector4f(Math.signum(plane.x), Math.signum(plane.y), farNdc, 1.0f));
        float planeDotCorner = plane.dot(farCorner);
        if (Math.abs(planeDotCorner) < 1.0e-12f) {
            return result;
        }
        Vector4f row3 = result.getRow(3, new Vector4f());
        float k = (1.0f - farNdc) * row3.dot(farCorner) / planeDotCorner;
        // Near (NDC 1): z_c = w_c  ⇔  k * dot(plane, v) = 0, i.e. the door plane.
        Vector4f row2 = new Vector4f(row3).sub(new Vector4f(plane).mul(k));
        result.setRow(2, row2);
        return result;
    }
}
