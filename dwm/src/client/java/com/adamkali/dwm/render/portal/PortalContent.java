package com.adamkali.dwm.render.portal;

import net.minecraft.client.Minecraft;

/**
 * Pluggable portal scene body: readiness, hitch, clear color, and offscreen draw.
 */
public interface PortalContent {
    boolean isReady(Minecraft client);

    /** Backdrop clear color as 0xRRGGBB (alpha ignored). */
    int clearRgb(Minecraft client);

    /**
     * Portal camera for this frame: the fixed door hitch, or the viewer-relative camera in parallax mode.
     * @return hitch, or {@code null} if content is not ready to draw
     */
    PortalCameraTransform.Result hitch(Minecraft client);

    /**
     * Parallax only: the destination door plane in the view space of {@code camera}, kept side
     * positive. Non-null switches the pass to the viewer's projection, oblique-clipped at this plane.
     */
    default org.joml.Vector4f clipPlaneView(PortalCameraTransform.Result camera) {
        return null;
    }

    /** Called every time the scene is requested, before the frame-cache check (e.g. to mark time-driven content dirty). */
    default void onFrameRequested(Minecraft client) {
    }

    void renderInto(PortalContentContext context);
}
