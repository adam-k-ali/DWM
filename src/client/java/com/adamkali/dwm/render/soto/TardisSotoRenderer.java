package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.render.boti.BotiStencilSupport;
import com.adamkali.dwm.tardis.data.model.TardisBotiAperture;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisSotoAperture;
import com.adamkali.dwm.tardis.interior.TardisSotoGate;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.RotationPropertyHelper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.Objects;
import java.util.UUID;

/**
 * Stencil-masked interior SOTO: draws a synced exterior footprint through open interior doors.
 */
public final class TardisSotoRenderer {
    private static final int STENCIL_REF = 2;
    private static final int FULLBRIGHT = LightmapTextureManager.pack(15, 15);

    /**
     * Blocks past the TARDIS door face for the SOTO look-out hitch plane.
     * Keeps the preview origin just in front of the shell instead of at the door face / shell body.
     */
    static final double PREVIEW_FORWARD_OFFSET = 1.0;

    /**
     * Exterior door opening center in footprint-relative coords
     * (TARDIS block at {@link SotoExteriorSampler#RELATIVE_TARDIS_POS}).
     * {@link #EXTERIOR_DOOR_PLANE_Z} is the hitch plane just in front of the shell
     * ({@code doorFaceZ - PREVIEW_FORWARD_OFFSET}), not the block center.
     */
    static final double EXTERIOR_DOOR_CENTER_X = SotoExteriorSampler.RELATIVE_TARDIS_POS.getX() + 0.5;
    static final double EXTERIOR_DOOR_CENTER_Y = SotoExteriorSampler.RELATIVE_TARDIS_POS.getY() + 1.0;
    static final double EXTERIOR_DOOR_PLANE_Z =
            SotoExteriorSampler.RELATIVE_TARDIS_POS.getZ() + 0.0 - PREVIEW_FORWARD_OFFSET;

    private final SotoShellModels shellModels;

    public TardisSotoRenderer() {
        this(null);
    }

    public TardisSotoRenderer(SotoShellModels shellModels) {
        this.shellModels = shellModels;
    }

    public static boolean shouldRender(float doorSwing) {
        return DWMConfig.getBoolean(DWMConfig.ENABLE_SOTO)
                && BotiStencilSupport.isAvailable()
                && TardisSotoGate.shouldShow(doorSwing);
    }

    /**
     * Renders SOTO into the current BER matrix stack (caller must already have applied interior
     * door {@code applyTransforms}). Flushes {@code vertexConsumers} when Immediate.
     */
    public void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            float tickDelta,
            UUID tardisId
    ) {
        if (tardisId == null) {
            return;
        }
        TardisSotoAperture aperture = TardisSotoAperture.CLASSIC_INTERIOR_DOORS;
        flush(vertexConsumers);

        matrices.push();
        try {
            writeStencilMask(matrices, aperture);
            clearDepthInMaskToFar(matrices, aperture);

            GL11.glEnable(GL11.GL_STENCIL_TEST);
            RenderSystem.stencilFunc(GL11.GL_EQUAL, STENCIL_REF, 0xFF);
            RenderSystem.stencilMask(0x00);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.depthMask(true);
            drawExterior(matrices, vertexConsumers, tickDelta, tardisId, aperture);
            flush(vertexConsumers);
            sealApertureDepth(matrices, aperture);
        } catch (Throwable t) {
            BotiStencilSupport.disableForSession("SOTO render failed", t);
        } finally {
            restoreGlState();
            matrices.pop();
        }
    }

    private void drawExterior(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            float tickDelta,
            UUID tardisId,
            TardisSotoAperture aperture
    ) {
        SotoExteriorMeshCache.ShellState shell = SotoExteriorMeshCache.getShellState(tardisId);
        TardisChameleonVariant variant =
                shell == null ? TardisChameleonVariant.TT_CAPSULE : shell.variant();
        TardisBotiAperture exteriorAperture = variant.getAperture();
        int exteriorRotation = shell == null ? 0 : shell.exteriorRotation();
        SotoAtmosphere atmosphere = SotoExteriorMeshCache.getAtmosphere(tardisId);
        if (atmosphere == null) {
            atmosphere = SotoAtmosphere.DEFAULT;
        }

        matrices.push();
        applyExteriorAlignment(matrices, aperture, exteriorAperture);
        applyDoorFacingCorrection(matrices, exteriorRotation);
        applyLookoutStableView(matrices);
        SotoSkyFogRenderer.renderSky(matrices, vertexConsumers, atmosphere);
        Fog previousFog = SotoSkyFogRenderer.applyTerrainFog(atmosphere);
        try {
            SotoExteriorMeshCache.renderWorld(matrices, vertexConsumers, FULLBRIGHT, tickDelta, tardisId);
            flush(vertexConsumers);
            // Shell is omitted on the SOTO path: stable-view reprojects the hitch to aperture
            // depth, which would otherwise pull the shell body between the eye and the lookout.
        } finally {
            SotoSkyFogRenderer.restoreFog(previousFog);
        }
        matrices.pop();
    }

    /**
     * Maps exterior footprint coords onto the interior door aperture in BER model space.
     * <p>
     * Aperture translate, Z-180 (Y correction for BER X-180), Y-180 (put footprint outward in the
     * look-out direction), then offset so the −Z door plane lands on the SOTO aperture.
     * Net relative to the door center: {@code (x, y, z) → (x, -y, -z)}.
     * <p>
     * Call {@link #applyDoorFacingCorrection} after this so the live door facing matches the −Z plane.
     */
    static void applyExteriorAlignment(
            MatrixStack matrices,
            TardisSotoAperture sotoAperture,
            TardisBotiAperture exteriorAperture
    ) {
        Objects.requireNonNull(exteriorAperture, "exteriorAperture");
        matrices.translate(sotoAperture.centerX(), sotoAperture.centerY(), sotoAperture.z());
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        matrices.translate(-EXTERIOR_DOOR_CENTER_X, -EXTERIOR_DOOR_CENTER_Y, -EXTERIOR_DOOR_PLANE_Z);
    }

    /**
     * Rotates the footprint about the TARDIS column so the chameleon door faces −Z
     * ({@link #EXTERIOR_DOOR_PLANE_Z}).
     * <p>
     * {@code exteriorRotation} is {@code TardisBlock.FACING_ROTATION}. Raw rotation 0 is the
     * skull/banner south convention, but shell BER transforms leave the doors facing the opposite
     * way (see {@link com.adamkali.dwm.tardis.TardisExteriorFacing}). Corrective yaw is therefore
     * {@code toDegrees(rotation)} (not {@code yaw - 180}), so rotation 0 looks out the visual
     * north / −Z door axis through {@link #EXTERIOR_DOOR_PLANE_Z} (hitch just in front of the shell).
     */
    static void applyDoorFacingCorrection(MatrixStack matrices, int exteriorRotation) {
        float yaw = RotationPropertyHelper.toDegrees(exteriorRotation);
        float corrective = yaw;
        double cx = SotoExteriorSampler.RELATIVE_TARDIS_POS.getX() + 0.5;
        double cz = SotoExteriorSampler.RELATIVE_TARDIS_POS.getZ() + 0.5;
        matrices.translate(cx, 0.0, cz);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(corrective));
        matrices.translate(-cx, 0.0, -cz);
    }

    /**
     * Fixed eye distance in front of the hitch after stable-view reprojection.
     * Must not depend on player↔door distance (that dollys when strafing / glancing).
     */
    static final float LOOKOUT_VIEW_DEPTH = 1.0f;

    /**
     * Freezes the exterior lookout at the hitch with a fixed view depth.
     * <p>
     * Player-relative depths ({@code |hitch|} or door-plane distance) dolly when strafing or
     * looking from an oblique angle. Instead: view from the hitch looking outward, then place
     * the hitch at a constant distance along camera forward so FOV stays stable.
     */
    static void applyLookoutStableView(MatrixStack matrices) {
        Matrix4f m = matrices.peek().getPositionMatrix();
        Vector3f hitch = m.transformPosition(
                (float) EXTERIOR_DOOR_CENTER_X,
                (float) EXTERIOR_DOOR_CENTER_Y,
                (float) EXTERIOR_DOOR_PLANE_Z,
                new Vector3f()
        );
        Vector3f outwardPoint = m.transformPosition(
                (float) EXTERIOR_DOOR_CENTER_X,
                (float) EXTERIOR_DOOR_CENTER_Y,
                (float) (EXTERIOR_DOOR_PLANE_Z - 1.0),
                new Vector3f()
        );
        Vector3f outward = outwardPoint.sub(hitch, new Vector3f());
        if (outward.lengthSquared() < 1e-8f) {
            return;
        }
        outward.normalize();

        Matrix4f mv = matrices.peek().getPositionMatrix();
        // 1) Eye at hitch.
        mv.mulLocal(new Matrix4f().translation(-hitch.x, -hitch.y, -hitch.z));
        // 2) Look straight out the doors (camera forward is −Z).
        Quaternionf turn = new Quaternionf().rotationTo(outward, new Vector3f(0.0f, 0.0f, -1.0f));
        mv.mulLocal(new Matrix4f().rotation(turn));
        // 3) Fixed FOV: hitch at constant depth (not player-relative).
        mv.mulLocal(new Matrix4f().translation(0.0f, 0.0f, -LOOKOUT_VIEW_DEPTH));
    }

    private static void writeStencilMask(MatrixStack matrices, TardisSotoAperture aperture) {
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

    private static void clearDepthInMaskToFar(MatrixStack matrices, TardisSotoAperture aperture) {
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

    private static void sealApertureDepth(MatrixStack matrices, TardisSotoAperture aperture) {
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

    private static void drawApertureQuad(MatrixStack matrices, TardisSotoAperture aperture) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(ShaderProgramKeys.POSITION);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        // Same winding as BOTI: local −Z normal. After BER X-180 that becomes world +Z
        // (toward the console room), so the front face is visible to a player looking out.
        buffer.vertex(matrix, aperture.x0(), aperture.y0(), aperture.z());
        buffer.vertex(matrix, aperture.x0(), aperture.y1(), aperture.z());
        buffer.vertex(matrix, aperture.x1(), aperture.y1(), aperture.z());
        buffer.vertex(matrix, aperture.x1(), aperture.y0(), aperture.z());
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private static void flush(VertexConsumerProvider vertexConsumers) {
        if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }
}
