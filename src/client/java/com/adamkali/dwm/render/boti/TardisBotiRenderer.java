package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.tardis.data.model.TardisBotiAperture;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisBotiGate;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.UUID;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;

/**
 * Stencil-masked exterior BOTI: draws a synced (or blueprint-fallback) console room through the open door.
 * <p>
 * <b>Public BER API (Minecraft 26.2):</b>
 * {@code TardisBotiRenderer.render(PoseStack, SubmitNodeCollector, CameraRenderState, float, UUID, TardisChameleonVariant)}
 * <p>
 * Immediate RenderSystem stencil/depth helpers are gone; depth/cull/color use
 * {@link GlStateManager}, stencil uses LWJGL {@link GL11}. Aperture quads and interior geometry
 * are submitted through {@link SubmitNodeCollector} ({@code submitCustomGeometry} /
 * moving-block / BE / entity submit). Visual stencil ordering against deferred feature phases
 * is deferred to MCP verification.
 */
public final class TardisBotiRenderer {
    private static final int STENCIL_REF = 1;
    private static final int FULLBRIGHT = LightCoordsUtil.pack(15, 15);
    /** RGBA write enable mask for {@code GlStateManager._colorMask(int)}. */
    private static final int COLOR_MASK_ALL = 0xF;
    private static final int COLOR_MASK_NONE = 0x0;

    /** Interior door opening center (local structure coords). */
    static final double INTERIOR_DOOR_CENTER_X = 5.5;
    /** Door blocks at y=1..2 span [1, 3); geometric center. */
    static final double INTERIOR_DOOR_CENTER_Y = 2.0;
    static final double INTERIOR_DOOR_PLANE_Z = 0.0;

    private TardisBotiRenderer() {
    }

    public static boolean shouldRender(TardisDoorState doorState) {
        return DWMConfig.getBoolean(DWMConfig.ENABLE_BOTI)
                && BotiStencilSupport.isAvailable()
                && TardisBotiGate.shouldShow(doorState);
    }

    /**
     * Renders BOTI into the current BER matrix stack (caller must already have applied exterior
     * model transforms). Submits aperture + interior into {@code submitNodeCollector}.
     */
    public static void render(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraState,
            float tickDelta,
            UUID tardisId,
            TardisChameleonVariant variant
    ) {
        TardisBotiAperture aperture = variant.getAperture();

        matrices.pushPose();
        try {
            writeStencilMask(matrices, submitNodeCollector, aperture);
            // Far depth in the mask so block-layer LEQUAL draws accept interior behind the door.
            clearDepthInMaskToFar(matrices, submitNodeCollector, aperture);

            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glStencilFunc(GL11.GL_EQUAL, STENCIL_REF, 0xFF);
            GL11.glStencilMask(0x00);
            GlStateManager._enableDepthTest();
            GlStateManager._depthFunc(GL11.GL_LEQUAL);
            GlStateManager._depthMask(true);
            drawInterior(matrices, submitNodeCollector, cameraState, tickDelta, tardisId, aperture);
            sealApertureDepth(matrices, submitNodeCollector, aperture);
        } catch (Throwable t) {
            BotiStencilSupport.disableForSession("BOTI render failed", t);
        } finally {
            restoreGlState();
            matrices.popPose();
        }
    }

    private static void writeStencilMask(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            TardisBotiAperture aperture
    ) {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilMask(0xFF);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        GL11.glStencilFunc(GL11.GL_ALWAYS, STENCIL_REF, 0xFF);
        // Only stamp stencil where the aperture passes the existing depth buffer (shell/world).
        // REPLACE on depth-fail would x-ray through the exterior from behind.
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        // Keep color writes on: some drivers skip stencil updates when color+depth writes are off.
        GlStateManager._colorMask(COLOR_MASK_ALL);
        GlStateManager._depthMask(false);
        GlStateManager._enableDepthTest();
        GlStateManager._depthFunc(GL11.GL_LEQUAL);
        // Keep cull on so the back-facing aperture (rear / inside-looking-out) does not stamp.

        drawApertureQuad(matrices, submitNodeCollector, aperture, 0x00000000);

        GlStateManager._depthMask(true);
        GL11.glStencilMask(0x00);
        GL11.glStencilFunc(GL11.GL_EQUAL, STENCIL_REF, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    /**
     * Clears depth in the stencil mask to the far plane so subsequent LEQUAL draws (block layers)
     * can render interior geometry behind the door aperture.
     */
    private static void clearDepthInMaskToFar(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            TardisBotiAperture aperture
    ) {
        GlStateManager._enableDepthTest();
        GlStateManager._depthFunc(GL11.GL_ALWAYS);
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(COLOR_MASK_NONE);
        // Force written depth to 1.0 (far) regardless of aperture clip-space Z.
        // Restricted by stencil EQUAL from writeStencilMask — cull stays on.
        GL11.glDepthRange(1.0, 1.0);
        drawApertureQuad(matrices, submitNodeCollector, aperture, 0x00000000);
        GL11.glDepthRange(0.0, 1.0);
        GlStateManager._colorMask(COLOR_MASK_ALL);
        GlStateManager._depthFunc(GL11.GL_LEQUAL);
    }

    private static void sealApertureDepth(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            TardisBotiAperture aperture
    ) {
        GlStateManager._colorMask(COLOR_MASK_NONE);
        GlStateManager._depthMask(true);
        GlStateManager._depthFunc(GL11.GL_ALWAYS);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        if (cullWasEnabled) {
            GlStateManager._disableCull();
        }
        drawApertureQuad(matrices, submitNodeCollector, aperture, 0x00000000);
        if (cullWasEnabled) {
            GlStateManager._enableCull();
        }
        GlStateManager._depthFunc(GL11.GL_LEQUAL);
        GlStateManager._colorMask(COLOR_MASK_ALL);
    }

    private static void drawInterior(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraState,
            float tickDelta,
            UUID tardisId,
            TardisBotiAperture aperture
    ) {
        matrices.pushPose();
        applyInteriorAlignment(matrices, aperture);
        BotiInteriorMeshCache.render(matrices, submitNodeCollector, cameraState, FULLBRIGHT, tickDelta, tardisId);
        matrices.popPose();
    }

    /**
     * Maps interior structure coords onto the exterior door aperture in BER model space.
     * <p>
     * BER already applied {@code rotateX(180)} (Blockbench), so model +Y is world-down. Exterior
     * doors are on model −Z; the aperture must sit on that plane. Z-180 keeps upright Y and room
     * depth in model +Z (into the shell). Depth in the aperture is cleared to far before interior
     * draw so block-layer {@code GL_LEQUAL} accepts into-shell fragments.
     * Net relative to the door center: {@code (x, y, z) → (-x, -y, z)}.
     */
    static void applyInteriorAlignment(PoseStack matrices, TardisBotiAperture aperture) {
        matrices.translate(0.0, aperture.centerY(), aperture.z());
        matrices.mulPose(Axis.ZP.rotationDegrees(180.0f));
        matrices.translate(-INTERIOR_DOOR_CENTER_X, -INTERIOR_DOOR_CENTER_Y, -INTERIOR_DOOR_PLANE_Z);
    }

    private static void restoreGlState() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glStencilMask(0xFF);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GlStateManager._depthFunc(GL11.GL_LEQUAL);
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(COLOR_MASK_ALL);
    }

    private static void drawApertureQuad(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            TardisBotiAperture aperture,
            int argb
    ) {
        submitNodeCollector.submitCustomGeometry(
                matrices,
                RenderTypes.debugQuads(),
                (PoseStack.Pose pose, VertexConsumer consumer) -> {
                    Matrix4f matrix = pose.pose();
                    // Winding so the front face normals toward model -Z (player outside the doors).
                    consumer.addVertex(matrix, aperture.x0(), aperture.y0(), aperture.z()).setColor(argb);
                    consumer.addVertex(matrix, aperture.x0(), aperture.y1(), aperture.z()).setColor(argb);
                    consumer.addVertex(matrix, aperture.x1(), aperture.y1(), aperture.z()).setColor(argb);
                    consumer.addVertex(matrix, aperture.x1(), aperture.y0(), aperture.z()).setColor(argb);
                }
        );
    }

    /** Expose layout size for tests / debug. */
    public static int interiorSizeX() {
        return FirstDoctorConsoleRoomLayout.SIZE_X;
    }
}
