package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.config.DWMConfig;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

/**
 * The main camera as the parallax portal pass needs it: read at portal render time (same frame,
 * after camera extraction) rather than copied in the BER.
 */
public final class PortalViewerState {
    private static final Matrix4f LEVEL_PROJECTION = new Matrix4f();
    private static boolean levelProjectionCaptured;

    private PortalViewerState() {
    }

    public static boolean isParallaxEnabled() {
        return DWMConfig.getBoolean(DWMConfig.PORTAL_PARALLAX);
    }

    /** Called from {@code GameRendererPortalMixin} with the final level projection for this frame. */
    public static void captureLevelProjection(Matrix4fc projection) {
        if (projection == null) {
            return;
        }
        LEVEL_PROJECTION.set(projection);
        levelProjectionCaptured = true;
    }

    /** Viewer camera for the frame being rendered, or {@code null} before the first level frame. */
    public static Viewer current(Minecraft client) {
        if (client == null || client.gameRenderer == null) {
            return null;
        }
        CameraRenderState camera = client.gameRenderer.gameRenderState.levelRenderState.cameraRenderState;
        if (camera == null || !camera.initialized) {
            return null;
        }
        Matrix4f projection = levelProjectionCaptured
                ? new Matrix4f(LEVEL_PROJECTION)
                : new Matrix4f(camera.projectionMatrix);
        return new Viewer(camera.pos, camera.yRot, camera.xRot, new Matrix4f(camera.viewRotationMatrix), projection);
    }

    public record Viewer(Vec3 position, float yaw, float pitch, Matrix4fc viewRotation, Matrix4fc projection) {
    }
}
