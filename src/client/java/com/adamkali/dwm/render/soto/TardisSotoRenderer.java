package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.render.soto.portal.SotoPortalRenderer;
import com.adamkali.dwm.render.soto.portal.SotoPortalSupport;
import com.adamkali.dwm.tardis.data.model.TardisSotoAperture;
import com.adamkali.dwm.tardis.interior.TardisSotoGate;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.UUID;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Stencil-masked interior SOTO: composites a portal exterior view through open interior doors.
 * <p>
 * Public API for the interior-door BER sibling:
 * {@link #shouldRender(float)} and
 * {@link #render(PoseStack, SubmitNodeCollector, float, UUID, BlockPos, Direction)}.
 * <p>
 * Minecraft 26.2 removed {@code MultiBufferSource}/{@code Tesselator}/{@code CoreShaders} and
 * {@code RenderSystem} stencil helpers. Flush is a no-op (submit collection is deferred). Aperture
 * stencil/depth draws and portal textured composite still need a RenderPass POSITION(_TEX) helper;
 * GL state setup is preserved so the OpenGL path remains structurally intact.
 */
public final class TardisSotoRenderer {
    private static final int STENCIL_REF = 2;

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

    private final SotoPortalRenderer portalRenderer;

    public TardisSotoRenderer() {
        this.portalRenderer = new SotoPortalRenderer();
    }

    public static boolean shouldRender(float doorSwing) {
        return DWMConfig.getBoolean(DWMConfig.ENABLE_SOTO)
                && SotoPortalSupport.isAvailable()
                && TardisSotoGate.shouldShow(doorSwing);
    }

    /**
     * Renders SOTO into the current BER matrix stack (caller must already have applied interior
     * door {@code applyTransforms}). {@code submitNodeCollector} is accepted for API parity with
     * 26.2 BER submit paths; mid-frame flush is unnecessary under deferred submit.
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
        flush(submitNodeCollector);

        matrices.pushPose();
        try {
            writeStencilMask(matrices, aperture);
            clearDepthInMaskToFar(matrices, aperture);

            GL11.glEnable(GL11.GL_STENCIL_TEST);
            SotoGl.stencilFunc(GL11.GL_EQUAL, STENCIL_REF, 0xFF);
            SotoGl.stencilMask(0x00);
            SotoGl.enableDepthTest();
            SotoGl.depthFunc(GL11.GL_LEQUAL);
            SotoGl.depthMask(true);
            drawExterior(
                    matrices,
                    tickDelta,
                    tardisId,
                    interiorDoorPos,
                    interiorDoorFacing,
                    aperture
            );
            flush(submitNodeCollector);
            sealApertureDepth(matrices, aperture);
        } catch (Throwable t) {
            SotoPortalSupport.disableForSession("Portal render failed", t);
        } finally {
            restoreGlState();
            matrices.popPose();
        }
    }

    private void drawExterior(
            PoseStack matrices,
            float tickDelta,
            UUID tardisId,
            BlockPos interiorDoorPos,
            Direction interiorDoorFacing,
            TardisSotoAperture aperture
    ) {
        SotoPortalRenderer.PortalTexture portalTexture =
                portalRenderer.render(tardisId, interiorDoorPos, interiorDoorFacing, tickDelta);
        if (portalTexture.available()) {
            drawPortalComposite(matrices, aperture, portalTexture);
        }
    }

    /**
     * Composites the portal color texture as a door-aperture quad with aperture-local UVs.
     * <p>
     * Textured draw is deferred until a POSITION_TEX RenderPass helper exists; GL sampler wrap
     * state is still applied when a legacy GL texture id is available (OpenGL backend).
     */
    private static void drawPortalComposite(
            PoseStack matrices,
            TardisSotoAperture aperture,
            SotoPortalRenderer.PortalTexture portalTexture
    ) {
        int textureId = portalTexture.textureId();
        if (textureId <= 0) {
            return;
        }
        Matrix4f model = matrices.last().pose();

        float x0 = aperture.x0();
        float x1 = aperture.x1();
        float y0 = aperture.y0();
        float y1 = aperture.y1();
        float z = aperture.z();
        float[] xs = {x0, x0, x1, x1};
        float[] ys = {y0, y1, y1, y0};

        float doorAspect = Math.max(x1 - x0, 1.0e-4f) / Math.max(y1 - y0, 1.0e-4f);
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
        float[] us = {uMin, uMin, uMax, uMax};
        float[] vs = {vMax, vMin, vMin, vMax};

        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean stencilWasEnabled = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
        SotoGl.enableDepthTest();
        SotoGl.depthFunc(GL11.GL_LEQUAL);
        SotoGl.depthMask(false);
        SotoGl.disableCull();
        SotoGl.disableBlend();
        SotoGl.colorMask(true, true, true, true);
        GL11.glDisable(GL11.GL_STENCIL_TEST);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        // TODO(soto-draw): upload POSITION_TEX quads via RenderPass (BufferUploader/CoreShaders gone).
        // Keep UV math warm so the helper can drop in without re-deriving crop.
        touchCompositeMath(model, xs, ys, z, us, vs);

        if (stencilWasEnabled) {
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            SotoGl.stencilFunc(GL11.GL_EQUAL, STENCIL_REF, 0xFF);
            SotoGl.stencilMask(0x00);
            SotoGl.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        }
        if (cullWasEnabled) {
            SotoGl.enableCull();
        } else {
            SotoGl.disableCull();
        }
        SotoGl.depthMask(true);
    }

    private static void writeStencilMask(PoseStack matrices, TardisSotoAperture aperture) {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        SotoGl.stencilMask(0xFF);
        SotoGl.clearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        SotoGl.stencilFunc(GL11.GL_ALWAYS, STENCIL_REF, 0xFF);
        SotoGl.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        SotoGl.colorMask(true, true, true, true);
        SotoGl.depthMask(false);
        SotoGl.enableDepthTest();
        SotoGl.depthFunc(GL11.GL_LEQUAL);

        SotoGl.colorMask(false, false, false, false);
        drawApertureQuad(matrices, aperture);
        SotoGl.colorMask(true, true, true, true);

        SotoGl.depthMask(true);
        SotoGl.stencilMask(0x00);
        SotoGl.stencilFunc(GL11.GL_EQUAL, STENCIL_REF, 0xFF);
        SotoGl.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    private static void clearDepthInMaskToFar(PoseStack matrices, TardisSotoAperture aperture) {
        SotoGl.enableDepthTest();
        SotoGl.depthFunc(GL11.GL_ALWAYS);
        SotoGl.depthMask(true);
        SotoGl.colorMask(false, false, false, false);
        GL11.glDepthRange(1.0, 1.0);
        drawApertureQuad(matrices, aperture);
        GL11.glDepthRange(0.0, 1.0);
        SotoGl.colorMask(true, true, true, true);
        SotoGl.depthFunc(GL11.GL_LEQUAL);
    }

    private static void sealApertureDepth(PoseStack matrices, TardisSotoAperture aperture) {
        SotoGl.colorMask(false, false, false, false);
        SotoGl.depthMask(true);
        SotoGl.depthFunc(GL11.GL_ALWAYS);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        if (cullWasEnabled) {
            SotoGl.disableCull();
        }
        drawApertureQuad(matrices, aperture);
        if (cullWasEnabled) {
            SotoGl.enableCull();
        }
        SotoGl.depthFunc(GL11.GL_LEQUAL);
        SotoGl.colorMask(true, true, true, true);
    }

    private static void restoreGlState() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        SotoGl.stencilMask(0xFF);
        SotoGl.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        SotoGl.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        SotoGl.depthFunc(GL11.GL_LEQUAL);
        SotoGl.depthMask(true);
        SotoGl.colorMask(true, true, true, true);
    }

    private static void drawApertureQuad(PoseStack matrices, TardisSotoAperture aperture) {
        // TODO(soto-draw): POSITION quad via RenderPass (Tesselator/BufferUploader/CoreShaders gone).
        Matrix4f matrix = matrices.last().pose();
        touchAperture(matrix, aperture);
    }

    private static void touchAperture(Matrix4f matrix, TardisSotoAperture aperture) {
        // Keep coordinates referenced so the deferred draw helper can reuse the same winding.
        if (matrix == null || aperture == null) {
            return;
        }
    }

    private static void touchCompositeMath(
            Matrix4f model,
            float[] xs,
            float[] ys,
            float z,
            float[] us,
            float[] vs
    ) {
        if (model == null || xs == null || ys == null || us == null || vs == null) {
            return;
        }
        // z retained for aperture plane.
        if (z != z) {
            return;
        }
    }

    private static void flush(SubmitNodeCollector submitNodeCollector) {
        // Deferred submit has no MultiBufferSource.BufferSource#endBatch equivalent mid-pass.
        if (submitNodeCollector == null) {
            return;
        }
    }
}
