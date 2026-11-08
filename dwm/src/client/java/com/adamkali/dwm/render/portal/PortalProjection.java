package com.adamkali.dwm.render.portal;

import org.joml.Matrix4f;

/**
 * Fixed perspective projection for the portal pass.
 * <p>
 * The portal view is hitch-fixed, so it must not inherit the player's FOV, view-bob, hurt-tilt or
 * nausea from the main level projection. The aspect ratio is the portal FBO's, which keeps the crop
 * in {@link PortalApertureComposite} valid.
 */
public final class PortalProjection {
    /** Vanilla's default vertical FOV, so the default look is unchanged. */
    public static final float FOV_DEGREES = 70.0f;
    public static final float Z_NEAR = 0.05f;

    private PortalProjection() {
    }

    /**
     * Writes the portal projection for an FBO of the given size into {@code dest}.
     * <p>
     * Vanilla's {@code Projection} builds a reverse-Z matrix (near and far swapped) that the level
     * pipelines' depth state expects; a standard-depth matrix drops distant geometry.
     */
    public static Matrix4f matrix(int fboWidth, int fboHeight, float zFar, boolean zZeroToOne, Matrix4f dest) {
        float aspect = fboWidth / (float) Math.max(1, fboHeight);
        return dest.setPerspective((float) Math.toRadians(FOV_DEGREES), aspect, zFar, Z_NEAR, zZeroToOne);
    }

    /** Far plane matching vanilla's level camera. */
    public static float farPlane(int effectiveRenderDistanceChunks, int cloudRange) {
        return Math.max(effectiveRenderDistanceChunks * 16 * 4.0f, cloudRange * 16);
    }
}
