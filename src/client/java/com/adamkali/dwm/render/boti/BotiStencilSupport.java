package com.adamkali.dwm.render.boti;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Session-wide stencil availability for exterior BOTI. The framebuffer mixin requests a
 * depth+stencil attachment; if that fails we disable BOTI for the rest of the session.
 */
public final class BotiStencilSupport {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean depthStencilTextureReady;
    private static boolean stencilAvailable = true;
    private static boolean failureLogged;

    private BotiStencilSupport() {
    }

    public static boolean isAvailable() {
        return stencilAvailable;
    }

    public static void markDepthStencilTextureReady() {
        depthStencilTextureReady = true;
    }

    public static boolean isDepthStencilTextureReady() {
        return depthStencilTextureReady;
    }

    public static void clearDepthStencilTextureReady() {
        depthStencilTextureReady = false;
    }

    public static void disableForSession(String reason, Throwable error) {
        depthStencilTextureReady = false;
        if (!stencilAvailable) {
            return;
        }
        stencilAvailable = false;
        if (!failureLogged) {
            failureLogged = true;
            if (error != null) {
                LOGGER.warn("Disabling TARDIS BOTI for this session: {}", reason, error);
            } else {
                LOGGER.warn("Disabling TARDIS BOTI for this session: {}", reason);
            }
        }
    }

    /** Package-visible for tests. */
    static void resetForTests() {
        depthStencilTextureReady = false;
        stencilAvailable = true;
        failureLogged = false;
    }
}
