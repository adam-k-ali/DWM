package com.adamkali.dwm.render.portal;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Context supplied to {@link PortalContent#renderInto} after the portal FBO is bound and cleared.
 */
public final class PortalContentContext {
    private final Minecraft client;
    private final float tickDelta;
    private final PortalCameraTransform.Result hitch;
    private final Camera portalCamera;
    private final PoseStack sceneMatrices;
    private final SubmitNodeStorage submitStorage;
    private final CameraRenderState cameraState;
    private final PortalRenderTarget target;
    private final PortalFeatureFlush featureFlush;

    public PortalContentContext(
            Minecraft client,
            float tickDelta,
            PortalCameraTransform.Result hitch,
            Camera portalCamera,
            PoseStack sceneMatrices,
            SubmitNodeStorage submitStorage,
            CameraRenderState cameraState,
            PortalRenderTarget target,
            PortalFeatureFlush featureFlush
    ) {
        this.client = client;
        this.tickDelta = tickDelta;
        this.hitch = hitch;
        this.portalCamera = portalCamera;
        this.sceneMatrices = sceneMatrices;
        this.submitStorage = submitStorage;
        this.cameraState = cameraState;
        this.target = target;
        this.featureFlush = featureFlush;
    }

    public Minecraft client() {
        return client;
    }

    public float tickDelta() {
        return tickDelta;
    }

    public PortalCameraTransform.Result hitch() {
        return hitch;
    }

    public Camera portalCamera() {
        return portalCamera;
    }

    public PoseStack sceneMatrices() {
        return sceneMatrices;
    }

    public SubmitNodeStorage submitStorage() {
        return submitStorage;
    }

    public CameraRenderState cameraState() {
        return cameraState;
    }

    public PortalRenderTarget target() {
        return target;
    }

    public PortalFeatureFlush featureFlush() {
        return featureFlush;
    }

    public void bindTarget() {
        target.bindForWrite();
    }
}
