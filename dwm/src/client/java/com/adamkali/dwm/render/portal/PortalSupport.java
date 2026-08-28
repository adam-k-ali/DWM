package com.adamkali.dwm.render.portal;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

/**
 * Capability and lifecycle gate for deferred door-portal FBO rendering (Fast/Fancy/Custom).
 * Does not depend on stencil framebuffer support.
 */
public final class PortalSupport {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean sessionAvailable = true;
    private static boolean failureLogged;
    private static boolean initialized;

    private PortalSupport() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        LevelRenderEvents.START_MAIN.register(context -> PortalRenderTarget.beginClientFrame());
        // Portal FBO clear/mesh draws must not run mid-BER (blacks out world/items on 26.2).
        LevelRenderEvents.END_MAIN.register(context -> {
            try {
                PortalScheduler.flushEndMain();
            } finally {
                PortalFogRenderer.endFrame();
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            PortalRenderTarget.closeGlobal();
            PortalFeatureFlush.closeGlobal();
            PortalFogRenderer.closeGlobal();
        });
    }

    public static boolean isAvailable() {
        return sessionAvailable && isGraphicsModeSupported();
    }

    /**
     * Portal compositing is incompatible with Fabulous-style order-independent transparency.
     * In 26.2+, that lives in {@code improvedTransparency}; presets also gained {@code CUSTOM}
     * for mixed video settings (common after upgrades). Allow Fast/Fancy/Custom when OIT is off.
     */
    public static boolean isGraphicsModeSupported() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.options == null) {
            return false;
        }
        if (!isSupportedGraphicsPreset(client.options.graphicsPreset().get())) {
            return false;
        }
        return !Boolean.TRUE.equals(client.options.improvedTransparency().get());
    }

    static boolean isReady(boolean session, GraphicsPreset graphicsMode, boolean targetReady) {
        return session && isSupportedGraphicsPreset(graphicsMode) && targetReady;
    }

    static boolean isSupportedGraphicsPreset(GraphicsPreset graphicsMode) {
        return graphicsMode == GraphicsPreset.FAST
                || graphicsMode == GraphicsPreset.FANCY
                || graphicsMode == GraphicsPreset.CUSTOM;
    }

    public static void disableForSession(String reason, Throwable error) {
        if (!sessionAvailable) {
            return;
        }
        sessionAvailable = false;
        PortalRenderTarget.closeGlobal();
        PortalFogRenderer.closeGlobal();
        if (!failureLogged) {
            failureLogged = true;
            if (error == null) {
                LOGGER.warn("Disabling TARDIS door portal rendering for this session: {}", reason);
            } else {
                LOGGER.warn("Disabling TARDIS door portal rendering for this session: {}", reason, error);
            }
        }
    }

    static void resetForTests() {
        sessionAvailable = true;
        failureLogged = false;
        PortalRenderTarget.closeGlobal();
        PortalFogRenderer.closeGlobal();
    }
}
