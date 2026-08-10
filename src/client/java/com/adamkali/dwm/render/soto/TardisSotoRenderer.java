package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.render.soto.portal.SotoPortalRenderer;
import com.adamkali.dwm.render.soto.portal.SotoPortalSupport;
import com.adamkali.dwm.tardis.data.model.TardisSotoAperture;
import com.adamkali.dwm.tardis.interior.TardisSotoGate;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.UUID;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Stencil-masked interior SOTO: composites a portal exterior view through open interior doors.
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
     * door {@code applyTransforms}). Flushes {@code vertexConsumers} when Immediate.
     */
    public void render(
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            float tickDelta,
            UUID tardisId,
            BlockPos interiorDoorPos,
            Direction interiorDoorFacing
    ) {
        if (tardisId == null || interiorDoorPos == null || interiorDoorFacing == null) {
            return;
        }
        TardisSotoAperture aperture = TardisSotoAperture.CLASSIC_INTERIOR_DOORS;
        flush(vertexConsumers);

        matrices.pushPose();
        try {
            writeStencilMask(matrices, aperture);
            clearDepthInMaskToFar(matrices, aperture);

            GL11.glEnable(GL11.GL_STENCIL_TEST);
            RenderSystem.stencilFunc(GL11.GL_EQUAL, STENCIL_REF, 0xFF);
            RenderSystem.stencilMask(0x00);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.depthMask(true);
            drawExterior(
                    matrices,
                    tickDelta,
                    tardisId,
                    interiorDoorPos,
                    interiorDoorFacing,
                    aperture
            );
            flush(vertexConsumers);
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
     * The portal FBO is rendered from a fixed exterior hitch. Screen-space UVs would slide that
     * image as the player moves (and sample outside 0..1 near the door edges). Mapping a fixed,
     * aspect-correct center crop onto the aperture keeps the exterior view stable and avoids
     * edge wrap/smudge. Geometry still clips the draw to the doorway.
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
        // Same winding as {@link #drawApertureQuad}.
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
        // Minecraft framebuffer color textures sample with V=0 at the top of the image
        // (opposite raw GL). Door bottom (y0) must sample high V so ground stays at the
        // threshold; y1 samples low V for sky.
        float vMin = 0.5f - cropV * 0.5f;
        float vMax = 0.5f + cropV * 0.5f;
        float[] us = {uMin, uMin, uMax, uMax};
        // y0,y1,y1,y0 → vMax,vMin,vMin,vMax (V flipped vs OpenGL bottom-origin)
        float[] vs = {vMax, vMin, vMin, vMax};

        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean stencilWasEnabled = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.colorMask(true, true, true, true);
        // Geometry is the clip; do not use stencil (can invert when STENCIL_BITS reports 0).
        GL11.glDisable(GL11.GL_STENCIL_TEST);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderTexture(0, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        BufferBuilder buffer =
                Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (int i = 0; i < 4; i++) {
            buffer.addVertex(model, xs[i], ys[i], z).setUv(us[i], vs[i]);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        if (stencilWasEnabled) {
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            RenderSystem.stencilFunc(GL11.GL_EQUAL, STENCIL_REF, 0xFF);
            RenderSystem.stencilMask(0x00);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        }
        if (cullWasEnabled) {
            RenderSystem.enableCull();
        } else {
            RenderSystem.disableCull();
        }
        RenderSystem.depthMask(true);
    }

    private static void writeStencilMask(PoseStack matrices, TardisSotoAperture aperture) {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        RenderSystem.stencilFunc(GL11.GL_ALWAYS, STENCIL_REF, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);

        RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 0.0f);
        drawApertureQuad(matrices, aperture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.depthMask(true);
        RenderSystem.stencilMask(0x00);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, STENCIL_REF, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    private static void clearDepthInMaskToFar(PoseStack matrices, TardisSotoAperture aperture) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(false, false, false, false);
        GL11.glDepthRange(1.0, 1.0);
        drawApertureQuad(matrices, aperture);
        GL11.glDepthRange(0.0, 1.0);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    private static void sealApertureDepth(PoseStack matrices, TardisSotoAperture aperture) {
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        if (cullWasEnabled) {
            RenderSystem.disableCull();
        }
        drawApertureQuad(matrices, aperture);
        if (cullWasEnabled) {
            RenderSystem.enableCull();
        }
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.colorMask(true, true, true, true);
    }

    private static void restoreGlState() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
    }

    private static void drawApertureQuad(PoseStack matrices, TardisSotoAperture aperture) {
        Matrix4f matrix = matrices.last().pose();
        RenderSystem.setShader(CoreShaders.POSITION);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        // Same winding as BOTI: local −Z normal. After BER X-180 that becomes world +Z
        // (toward the console room), so the front face is visible to a player looking out.
        buffer.addVertex(matrix, aperture.x0(), aperture.y0(), aperture.z());
        buffer.addVertex(matrix, aperture.x0(), aperture.y1(), aperture.z());
        buffer.addVertex(matrix, aperture.x1(), aperture.y1(), aperture.z());
        buffer.addVertex(matrix, aperture.x1(), aperture.y0(), aperture.z());
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void flush(MultiBufferSource vertexConsumers) {
        if (vertexConsumers instanceof MultiBufferSource.BufferSource immediate) {
            immediate.endBatch();
        }
    }
}
