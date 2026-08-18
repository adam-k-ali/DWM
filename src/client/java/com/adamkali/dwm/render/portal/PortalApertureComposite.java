package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.tardis.data.model.PortalAperture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;

/**
 * Shared aperture compositing: aspect-matched portal UV crop and placeholder quads.
 * <p>
 * Crops the full-window portal FBO to the aperture aspect (letterbox/pillarbox). FOV-at-depth
 * cropping over-zooms small exterior doors (BOTI) relative to the hitch-fixed scene render.
 */
public final class PortalApertureComposite {
    private static final int FULLBRIGHT = LightCoordsUtil.FULL_BRIGHT;

    /** Placeholder ARGB while waiting for the first END_MAIN portal texture. */
    public static final int PLACEHOLDER_ARGB = 0xFF203040;

    private PortalApertureComposite() {
    }

    public static void drawPortalComposite(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            PortalAperture aperture,
            PortalRenderer.PortalTexture portalTexture
    ) {
        PortalSamplingTexture.bindPortalColor(PortalRenderTarget.getInstance());

        float x0 = aperture.x0();
        float x1 = aperture.x1();
        float y0 = aperture.y0();
        float y1 = aperture.y1();
        float z = aperture.z();

        float doorW = Math.max(x1 - x0, 1.0e-4f);
        float doorH = Math.max(y1 - y0, 1.0e-4f);
        float doorAspect = doorW / doorH;
        float fbAspect = portalTexture.height() <= 0
                ? doorAspect
                : (float) portalTexture.width() / (float) portalTexture.height();

        float cropU;
        float cropV;
        if (fbAspect > doorAspect) {
            cropV = 1.0f;
            cropU = doorAspect / fbAspect;
        } else {
            cropU = 1.0f;
            cropV = fbAspect / doorAspect;
        }

        float uMin = 0.5f - cropU * 0.5f;
        float uMax = 0.5f + cropU * 0.5f;
        float vMin = 0.5f - cropV * 0.5f;
        float vMax = 0.5f + cropV * 0.5f;
        // y0/bottom → vMax; BER X-180 already flips the quad in model space.
        float[] us = {uMin, uMin, uMax, uMax};
        float[] vs = {vMax, vMin, vMin, vMax};
        float[] xs = {x0, x0, x1, x1};
        float[] ys = {y0, y1, y1, y0};

        submitNodeCollector.submitCustomGeometry(
                matrices,
                PortalDoorRenderer.portalCompositeRenderType(),
                (PoseStack.Pose pose, VertexConsumer consumer) -> {
                    Matrix4f matrix = pose.pose();
                    for (int i = 0; i < 4; i++) {
                        consumer.addVertex(matrix, xs[i], ys[i], z)
                                .setColor(0xFFFFFFFF)
                                .setUv(us[i], vs[i])
                                .setOverlay(OverlayTexture.NO_OVERLAY)
                                .setLight(FULLBRIGHT)
                                .setNormal(0.0f, 0.0f, -1.0f);
                    }
                }
        );
    }

    public static void drawApertureQuad(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            PortalAperture aperture,
            int argb
    ) {
        float x0 = aperture.x0();
        float x1 = aperture.x1();
        float y0 = aperture.y0();
        float y1 = aperture.y1();
        float z = aperture.z();
        submitNodeCollector.submitCustomGeometry(
                matrices,
                RenderTypes.debugQuads(),
                (PoseStack.Pose pose, VertexConsumer consumer) -> {
                    Matrix4f matrix = pose.pose();
                    consumer.addVertex(matrix, x0, y0, z).setColor(argb);
                    consumer.addVertex(matrix, x0, y1, z).setColor(argb);
                    consumer.addVertex(matrix, x1, y1, z).setColor(argb);
                    consumer.addVertex(matrix, x1, y0, z).setColor(argb);
                }
        );
    }

    public static void drawPlaceholder(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            PortalAperture aperture
    ) {
        drawApertureQuad(matrices, submitNodeCollector, aperture, PLACEHOLDER_ARGB);
    }
}
