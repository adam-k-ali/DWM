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
     * Hitch-fixed portal camera for this frame.
     * @return hitch, or {@code null} if content is not ready to draw
     */
    PortalCameraTransform.Result hitch(Minecraft client);

    void renderInto(PortalContentContext context);
}
