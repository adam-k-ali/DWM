package com.adamkali.dwm.render.soto.portal;

import com.adamkali.dwm.render.boti.BotiStencilSupport;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.adamkali.dwm.render.soto.ghost.SotoGhostMeshCache;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * Capability and lifecycle gate for the vanilla Fast/Fancy Phase 3 renderer.
 */
public final class SotoPortalSupport {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean sessionAvailable = true;
    private static boolean failureLogged;
    private static boolean initialized;

    private SotoPortalSupport() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        LevelRenderEvents.START_MAIN.register(context -> SotoPortalRenderTarget.beginClientFrame());
        // Portal FBO clear/mesh draws must not run mid-BER (blacks out world/items on 26.2).
        LevelRenderEvents.END_MAIN.register(context -> SotoPortalScheduler.flushEndMain());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            SotoPortalRenderTarget.closeGlobal();
            SotoPortalFeatureFlush.closeGlobal();
        });
        // Portal sampling texture registers lazily on first composite (TextureManager is null here).
    }

    public static boolean isAvailable() {
        return sessionAvailable && isGraphicsModeSupported() && BotiStencilSupport.isAvailable();
    }

    /**
     * SOTO portal compositing is incompatible with Fabulous-style order-independent transparency.
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

    public static boolean hasGhostMeshes(UUID tardisId) {
        SotoGhostExterior ghost = SotoGhostExterior.get(tardisId);
        return ghost != null && ghost.chunkCount() > 0 && SotoGhostMeshCache.hasMeshes(tardisId);
    }

    public static boolean isReadyFor(UUID tardisId) {
        Minecraft client = Minecraft.getInstance();
        GraphicsPreset graphicsMode = client == null || client.options == null
                ? null
                : client.options.graphicsPreset().get();
        return isReady(
                sessionAvailable,
                BotiStencilSupport.isAvailable(),
                graphicsMode,
                SotoPortalRenderTarget.getInstance().isReady(),
                hasGhostMeshes(tardisId)
        );
    }

    static boolean isReady(
            boolean session,
            boolean stencil,
            GraphicsPreset graphicsMode,
            boolean targetReady,
            boolean ghostReady
    ) {
        return session
                && stencil
                && isSupportedGraphicsPreset(graphicsMode)
                && targetReady
                && ghostReady;
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
        SotoPortalRenderTarget.closeGlobal();
        if (!failureLogged) {
            failureLogged = true;
            if (error == null) {
                LOGGER.warn("Disabling TARDIS SOTO portal rendering for this session: {}", reason);
            } else {
                LOGGER.warn("Disabling TARDIS SOTO portal rendering for this session: {}", reason, error);
            }
        }
    }

    static void resetForTests() {
        sessionAvailable = true;
        failureLogged = false;
        SotoPortalRenderTarget.closeGlobal();
    }
}
