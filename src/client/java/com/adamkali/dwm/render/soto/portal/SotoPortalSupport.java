package com.adamkali.dwm.render.soto.portal;

import com.adamkali.dwm.render.boti.BotiStencilSupport;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.adamkali.dwm.render.soto.ghost.SotoGhostMeshCache;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.GraphicsStatus;
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
        WorldRenderEvents.START.register(context -> SotoPortalRenderTarget.beginClientFrame());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> SotoPortalRenderTarget.closeGlobal());
    }

    public static boolean isAvailable() {
        return sessionAvailable && isGraphicsModeSupported() && BotiStencilSupport.isAvailable();
    }

    public static boolean isGraphicsModeSupported() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.options == null) {
            return false;
        }
        GraphicsStatus mode = client.options.graphicsMode().get();
        return mode == GraphicsStatus.FAST || mode == GraphicsStatus.FANCY;
    }

    public static boolean hasGhostMeshes(UUID tardisId) {
        SotoGhostExterior ghost = SotoGhostExterior.get(tardisId);
        return ghost != null && ghost.chunkCount() > 0 && SotoGhostMeshCache.hasMeshes(tardisId);
    }

    public static boolean isReadyFor(UUID tardisId) {
        Minecraft client = Minecraft.getInstance();
        GraphicsStatus graphicsMode = client == null || client.options == null
                ? null
                : client.options.graphicsMode().get();
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
            GraphicsStatus graphicsMode,
            boolean targetReady,
            boolean ghostReady
    ) {
        return session
                && stencil
                && (graphicsMode == GraphicsStatus.FAST || graphicsMode == GraphicsStatus.FANCY)
                && targetReady
                && ghostReady;
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
