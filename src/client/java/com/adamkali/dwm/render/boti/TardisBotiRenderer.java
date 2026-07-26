package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisBotiGate;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * Stencil-masked exterior BOTI: draws a baked First Doctor console room through the open door.
 */
public final class TardisBotiRenderer {
    private static final int STENCIL_REF = 1;
    private static final int FULLBRIGHT = LightmapTextureManager.pack(15, 15);

    /**
     * Door aperture in model-local space after the same BER transforms as the exterior shell
     * (Blockbench units / 16). Tuned for police-box / capsule door openings.
     */
    private static final float APERTURE_X0 = -5.0f / 16.0f;
    private static final float APERTURE_X1 = 5.0f / 16.0f;
    private static final float APERTURE_Y0 = -22.0f / 16.0f;
    private static final float APERTURE_Y1 = -1.0f / 16.0f;
    private static final float APERTURE_Z = 0.55f / 16.0f;

    /** Interior door opening center (local structure coords). */
    private static final double INTERIOR_DOOR_CENTER_X = 5.5;
    private static final double INTERIOR_DOOR_CENTER_Y = 1.5;
    private static final double INTERIOR_DOOR_PLANE_Z = 0.0;

    private TardisBotiRenderer() {
    }

    public static boolean shouldRender(TardisDoorState doorState) {
        return DWMConfig.getBoolean(DWMConfig.ENABLE_BOTI)
                && BotiStencilSupport.isAvailable()
                && TardisBotiGate.shouldShow(doorState);
    }

    /**
     * Renders BOTI into the current BER matrix stack (caller must already have applied exterior
     * model transforms). Flushes {@code vertexConsumers} when it is an Immediate provider.
     */
    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        flush(vertexConsumers);

        matrices.push();
        try {
            writeStencilMask(matrices);
            clearDepthInMask(matrices);
            drawInterior(matrices, vertexConsumers);
            flush(vertexConsumers);
            sealApertureDepth(matrices);
        } catch (Throwable t) {
            BotiStencilSupport.disableForSession("BOTI render failed", t);
        } finally {
            restoreGlState();
            matrices.pop();
        }
    }

    private static void writeStencilMask(MatrixStack matrices) {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clearStencil(0);
        // Clear only stencil; depth/color preserved. Drivers vary on stencil-only clear with mask.
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        RenderSystem.stencilFunc(GL11.GL_ALWAYS, STENCIL_REF, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);

        drawApertureQuad(matrices);

        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.stencilMask(0x00);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, STENCIL_REF, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    private static void clearDepthInMask(MatrixStack matrices) {
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(false, false, false, false);
        drawApertureQuad(matrices);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    private static void drawInterior(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        matrices.push();
        // Aperture is roughly at model origin on Z; map interior door plane onto it and look +Z into the room.
        double apertureCenterY = (APERTURE_Y0 + APERTURE_Y1) * 0.5;
        matrices.translate(0.0, apertureCenterY, APERTURE_Z);
        // Interior faces south at z=0; looking into the room is +Z. Flip so exterior view looks inward.
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        matrices.translate(-INTERIOR_DOOR_CENTER_X, -INTERIOR_DOOR_CENTER_Y, -INTERIOR_DOOR_PLANE_Z);
        BotiInteriorMeshCache.render(matrices, vertexConsumers, FULLBRIGHT);
        matrices.pop();
    }

    private static void sealApertureDepth(MatrixStack matrices) {
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        drawApertureQuad(matrices);
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

    private static void drawApertureQuad(MatrixStack matrices) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(ShaderProgramKeys.POSITION);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(matrix, APERTURE_X0, APERTURE_Y0, APERTURE_Z);
        buffer.vertex(matrix, APERTURE_X1, APERTURE_Y0, APERTURE_Z);
        buffer.vertex(matrix, APERTURE_X1, APERTURE_Y1, APERTURE_Z);
        buffer.vertex(matrix, APERTURE_X0, APERTURE_Y1, APERTURE_Z);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private static void flush(VertexConsumerProvider vertexConsumers) {
        if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }

    /** Expose layout size for tests / debug. */
    public static int interiorSizeX() {
        return FirstDoctorConsoleRoomLayout.SIZE_X;
    }
}
