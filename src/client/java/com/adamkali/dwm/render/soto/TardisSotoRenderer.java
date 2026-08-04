package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.render.soto.portal.SotoPortalRenderer;
import com.adamkali.dwm.render.soto.portal.SotoPortalSupport;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.RotationPropertyHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

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
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
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
            drawExterior(
                    matrices,
                    vertexConsumers,
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
            matrices.pop();
        }
    }

    private void drawExterior(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
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
            return;
        }
        if (!SotoPortalSupport.isAvailable()) {
            return;
        }
        // macOS often reports STENCIL_BITS=0; the 3D lookout relies on EQUAL masking and can
        // paint the exterior outside the aperture. Wait for the portal texture instead.
        if (GL11.glGetInteger(GL11.GL_STENCIL_BITS) == 0) {
            return;
        }

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
     * Composites the portal color texture as a door-aperture quad with aperture-local UVs.
     * <p>
     * The portal FBO is rendered from a fixed exterior hitch. Screen-space UVs would slide that
     * image as the player moves (and sample outside 0..1 near the door edges). Mapping a fixed,
     * aspect-correct center crop onto the aperture keeps the exterior view stable and avoids
     * edge wrap/smudge. Geometry still clips the draw to the doorway.
     */
    private static void drawPortalComposite(
            MatrixStack matrices,
            TardisSotoAperture aperture,
            SotoPortalRenderer.PortalTexture portalTexture
    ) {
        int textureId = portalTexture.textureId();
        if (textureId <= 0) {
            return;
        }
        Matrix4f model = matrices.peek().getPositionMatrix();

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
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        RenderSystem.setShaderTexture(0, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        BufferBuilder buffer =
                Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        for (int i = 0; i < 4; i++) {
            buffer.vertex(model, xs[i], ys[i], z).texture(us[i], vs[i]);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

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
     * Kept near {@link #PREVIEW_FORWARD_OFFSET} so the fallback eye stays at the door face
     * rather than pulling back into the shell body.
     */
    static final float LOOKOUT_VIEW_DEPTH = 0.25f;

    /**
     * Freezes the exterior lookout at the hitch with a fixed view depth.
     * <p>
     * Player-relative depths ({@code |hitch|} or door-plane distance) dolly when strafing or
     * looking from an oblique angle. Instead: view from the hitch looking outward, then place
     * the hitch at a constant distance along camera forward so FOV stays stable.
     * <p>
     * Must use an explicit up vector (lookAt), not {@code rotationTo(outward, -Z)} alone:
     * {@link #applyExteriorAlignment} includes a Y flip for BER X-180, and a pure
     * direction-to-direction rotation preserves that flip as an inverted roll.
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
        Vector3f upPoint = m.transformPosition(
                (float) EXTERIOR_DOOR_CENTER_X,
                (float) EXTERIOR_DOOR_CENTER_Y + 1.0f,
                (float) EXTERIOR_DOOR_PLANE_Z,
                new Vector3f()
        );
        Vector3f outward = outwardPoint.sub(hitch, new Vector3f());
        if (outward.lengthSquared() < 1e-8f) {
            return;
        }
        outward.normalize();
        Vector3f up = upPoint.sub(hitch, new Vector3f());
        if (up.lengthSquared() < 1e-8f) {
            up.set(0.0f, 1.0f, 0.0f);
        }

        Matrix4f look = new Matrix4f().lookAt(
                hitch.x,
                hitch.y,
                hitch.z,
                hitch.x + outward.x,
                hitch.y + outward.y,
                hitch.z + outward.z,
                up.x,
                up.y,
                up.z
        );
        Matrix4f mv = matrices.peek().getPositionMatrix();
        mv.set(new Matrix4f()
                .translation(0.0f, 0.0f, -LOOKOUT_VIEW_DEPTH)
                .mul(look)
                .mul(m));
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
