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

import java.util.UUID;

/**
 * Stencil-masked exterior BOTI: draws a synced (or blueprint-fallback) console room through the open door.
 */
public final class TardisBotiRenderer {
    private static final int STENCIL_REF = 1;
    private static final int FULLBRIGHT = LightmapTextureManager.pack(15, 15);

    /**
     * Door aperture in model-local space after the same BER transforms as the exterior shell
     * (Blockbench units / 16). Must use the same +Y range as ModelPart door meshes (TT Capsule
     * door ~1..23px; First Doctor doors similar) — negative Y places the portal above the shell.
     */
    private static final float APERTURE_X0 = -5.0f / 16.0f;
    private static final float APERTURE_X1 = 5.0f / 16.0f;
    private static final float APERTURE_Y0 = 1.0f / 16.0f;
    private static final float APERTURE_Y1 = 23.0f / 16.0f;
    /** Police-box door plane (First Doctor LeftDoor pivot z=-5.5). Must match exterior door. */
    static final float APERTURE_Z = -5.5f / 16.0f;

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
     * model transforms). Flushes {@code vertexConsumers} when it is an Immediate provider.
     */
    public static void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            float tickDelta,
            UUID tardisId
    ) {
        flush(vertexConsumers);

        matrices.push();
        try {
            writeStencilMask(matrices);
            // Far depth in the mask so block-layer LEQUAL draws accept interior behind the door.
            clearDepthInMaskToFar(matrices);

            GL11.glEnable(GL11.GL_STENCIL_TEST);
            RenderSystem.stencilFunc(GL11.GL_EQUAL, STENCIL_REF, 0xFF);
            RenderSystem.stencilMask(0x00);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.depthMask(true);
            drawInterior(matrices, vertexConsumers, tickDelta, tardisId);
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
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        RenderSystem.stencilFunc(GL11.GL_ALWAYS, STENCIL_REF, 0xFF);
        // Only stamp stencil where the aperture passes the existing depth buffer (shell/world).
        // REPLACE on depth-fail would x-ray through the exterior from behind.
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        // Keep colorMask true: some drivers skip stencil updates when color+depth writes are off.
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        // Keep cull on so the back-facing aperture (rear / inside-looking-out) does not stamp.

        RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 0.0f);
        drawApertureQuad(matrices);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.depthMask(true);
        RenderSystem.stencilMask(0x00);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, STENCIL_REF, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    /**
     * Clears depth in the stencil mask to the far plane so subsequent LEQUAL draws (block layers)
     * can render interior geometry behind the door aperture.
     */
    private static void clearDepthInMaskToFar(MatrixStack matrices) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(false, false, false, false);
        // Force written depth to 1.0 (far) regardless of aperture clip-space Z.
        // Restricted by stencil EQUAL from writeStencilMask — cull stays on.
        GL11.glDepthRange(1.0, 1.0);
        drawApertureQuad(matrices);
        GL11.glDepthRange(0.0, 1.0);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    private static void sealApertureDepth(MatrixStack matrices) {
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        if (cullWasEnabled) {
            RenderSystem.disableCull();
        }
        drawApertureQuad(matrices);
        if (cullWasEnabled) {
            RenderSystem.enableCull();
        }
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.colorMask(true, true, true, true);
    }

    private static void drawInterior(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            float tickDelta,
            UUID tardisId
    ) {
        matrices.push();
        applyInteriorAlignment(matrices);
        BotiInteriorMeshCache.render(matrices, vertexConsumers, FULLBRIGHT, tickDelta, tardisId);
        matrices.pop();
    }

    /**
     * Maps interior structure coords onto the exterior door aperture in BER model space.
     * <p>
     * BER already applied {@code rotateX(180)} (Blockbench), so model +Y is world-down. Exterior
     * doors are on model −Z (LeftDoor pivot −5.5px); the aperture must sit on that plane. Z-180
     * keeps upright Y and room depth in model +Z (into the shell). Depth in the aperture is cleared
     * to far before interior draw so block-layer {@code GL_LEQUAL} accepts into-shell fragments.
     * Net relative to the door center: {@code (x, y, z) → (-x, -y, z)}.
     */
    static void applyInteriorAlignment(MatrixStack matrices) {
        double apertureCenterY = (APERTURE_Y0 + APERTURE_Y1) * 0.5;
        matrices.translate(0.0, apertureCenterY, APERTURE_Z);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        matrices.translate(-INTERIOR_DOOR_CENTER_X, -INTERIOR_DOOR_CENTER_Y, -INTERIOR_DOOR_PLANE_Z);
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
        // Winding so the front face normals toward model -Z (player outside the doors).
        buffer.vertex(matrix, APERTURE_X0, APERTURE_Y0, APERTURE_Z);
        buffer.vertex(matrix, APERTURE_X0, APERTURE_Y1, APERTURE_Z);
        buffer.vertex(matrix, APERTURE_X1, APERTURE_Y1, APERTURE_Z);
        buffer.vertex(matrix, APERTURE_X1, APERTURE_Y0, APERTURE_Z);
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

    /** Aperture vertical center in BER model space (for tests). */
    static float apertureCenterY() {
        return (APERTURE_Y0 + APERTURE_Y1) * 0.5f;
    }
}
