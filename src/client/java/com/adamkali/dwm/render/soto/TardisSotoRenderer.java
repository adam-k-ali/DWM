package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.render.soto.portal.SotoPortalRenderTarget;
import com.adamkali.dwm.render.soto.portal.SotoPortalRenderer;
import com.adamkali.dwm.render.soto.portal.SotoPortalSamplingTexture;
import com.adamkali.dwm.render.soto.portal.SotoPortalScheduler;
import com.adamkali.dwm.render.soto.portal.SotoPortalSupport;
import com.adamkali.dwm.tardis.data.model.TardisSotoAperture;
import com.adamkali.dwm.tardis.interior.TardisSotoGate;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;

/**
 * Interior SOTO: composites a portal exterior view through open interior doors.
 * <p>
 * Public API for the interior-door BER sibling:
 * {@link #shouldRender(float)} and
 * {@link #render(PoseStack, SubmitNodeCollector, float, UUID, BlockPos, Direction)}.
 * <p>
 * Minecraft 26.2: BER only schedules portal work and submits deferred aperture/composite
 * geometry. Portal FBO clear/mesh draws run on {@code LevelRenderEvents.END_MAIN} via
 * {@link SotoPortalScheduler} — mid-BER GPU/GL poisons the main world pass.
 */
public final class TardisSotoRenderer {
    private static final int FULLBRIGHT = LightCoordsUtil.FULL_BRIGHT;

    /**
     * Blocks past the TARDIS door face for the SOTO look-out hitch plane.
     * Keeps the preview origin just in front of the shell instead of at the door face / shell body.
     */
    public static final double PREVIEW_FORWARD_OFFSET = 0.5;

    /**
     * Eye height above the TARDIS block base for the exterior hitch.
     * Matches the classic chameleon BOTI aperture mid-height (~0.75 would be exact center);
     * kept slightly lower so the look-out sits nearer the threshold than mid-door.
     */
    public static final double PREVIEW_EYE_HEIGHT = 0.75;

    /**
     * Fixed camera↔door depth used when cropping the full-window portal FBO onto the aperture.
     * Player-relative depth would change the UV crop while walking (dolly), but the exterior pass
     * is hitch-fixed — crop must stay constant. ~2.75 matches a typical stand-back in the
     * console room so vertical FOV through the 2-block opening stays aligned with player view.
     */
    public static final float COMPOSITE_REFERENCE_DEPTH = 2.75f;

    /**
     * Exterior door opening center in footprint-relative coords
     * (TARDIS block at {@link SotoExteriorSampler#RELATIVE_TARDIS_POS}).
     * {@link #EXTERIOR_DOOR_PLANE_Z} is the hitch plane just in front of the shell
     * ({@code doorFaceZ - PREVIEW_FORWARD_OFFSET}), not the block center.
     */
    static final double EXTERIOR_DOOR_CENTER_X = SotoExteriorSampler.RELATIVE_TARDIS_POS.getX() + 0.5;
    static final double EXTERIOR_DOOR_CENTER_Y =
            SotoExteriorSampler.RELATIVE_TARDIS_POS.getY() + PREVIEW_EYE_HEIGHT;
    static final double EXTERIOR_DOOR_PLANE_Z =
            SotoExteriorSampler.RELATIVE_TARDIS_POS.getZ() + 0.0 - PREVIEW_FORWARD_OFFSET;

    public TardisSotoRenderer() {
    }

    public static boolean shouldRender(float doorSwing) {
        return DWMConfig.getBoolean(DWMConfig.ENABLE_SOTO)
                && SotoPortalSupport.isAvailable()
                && TardisSotoGate.shouldShow(doorSwing);
    }

    /**
     * Schedules portal work for END_MAIN and submits deferred doorway composite geometry.
     * Must not touch GL state or open RenderPasses (mid-BER poison on 26.2).
     */
    public void render(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            float tickDelta,
            UUID tardisId,
            BlockPos interiorDoorPos,
            Direction interiorDoorFacing
    ) {
        if (tardisId == null || interiorDoorPos == null || interiorDoorFacing == null) {
            return;
        }
        TardisSotoAperture aperture = TardisSotoAperture.CLASSIC_INTERIOR_DOORS;
        SotoPortalScheduler.schedule(tardisId, interiorDoorPos, interiorDoorFacing, tickDelta);
        SotoPortalRenderer.PortalTexture portalTexture =
                SotoPortalScheduler.peekCompositeTexture(tardisId);
        matrices.pushPose();
        try {
            if (portalTexture.available()) {
                drawPortalComposite(matrices, submitNodeCollector, aperture, portalTexture);
            } else {
                // First frame(s) before END_MAIN produces a texture — keep aperture occupied.
                drawApertureQuad(matrices, submitNodeCollector, aperture, 0xFF203040);
            }
        } finally {
            matrices.popPose();
        }
    }

    /**
     * Composites the portal color texture onto the door aperture with an FOV-matched center crop.
     * <p>
     * The portal FBO is full-window at the player's projection. Stretching a near-full aspect crop
     * onto the aperture made the exterior look too wide. Crop the center of the FBO to the angular
     * size of the aperture at a fixed reference depth ({@link #COMPOSITE_REFERENCE_DEPTH}) so FOV
     * stays correct without dollying when the player walks (the exterior pass is hitch-fixed).
     */
    private static void drawPortalComposite(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            TardisSotoAperture aperture,
            SotoPortalRenderer.PortalTexture portalTexture
    ) {
        SotoPortalSamplingTexture.bindPortalColor(SotoPortalRenderTarget.getInstance());

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
        Minecraft client = Minecraft.getInstance();
        Camera camera = client != null && client.gameRenderer != null
                ? client.gameRenderer.mainCamera()
                : null;
        if (camera != null) {
            float depth = COMPOSITE_REFERENCE_DEPTH;
            float halfAngH = (float) Math.atan((doorH * 0.5f) / depth);
            float halfAngW = (float) Math.atan((doorW * 0.5f) / depth);
            float halfVFov = (float) Math.toRadians(Math.max(camera.getFov(), 1.0e-3f) * 0.5f);
            float halfHFov = (float) Math.atan(Math.tan(halfVFov) * fbAspect);
            cropV = Math.min(1.0f, (float) (Math.tan(halfAngH) / Math.tan(halfVFov)));
            cropU = Math.min(1.0f, (float) (Math.tan(halfAngW) / Math.tan(halfHFov)));
        } else if (fbAspect > doorAspect) {
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
                RenderTypes.entityCutout(SotoPortalSamplingTexture.ID),
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

    private static void drawApertureQuad(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            TardisSotoAperture aperture,
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
}
