package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors;
import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors.EffectsKind;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * Mini skybox + fog for the SOTO exterior portal, driven by synced {@link SotoAtmosphere}.
 */
public final class SotoSkyFogRenderer {
    /** Vanilla sky dome radius; scale so it sits behind the 11×7×11 footprint. */
    private static final float VANILLA_SKY_RADIUS = 512.0F;
    private static final float SKY_SCALE = 10.0F / VANILLA_SKY_RADIUS;

    static final float PORTAL_FOG_START = 20.0F;
    static final float PORTAL_FOG_END = 30.0F;

    private SotoSkyFogRenderer() {
    }

    /**
     * Draws the Phase 3 sky with fog reaching the edge of the fixed two-chunk ghost stream.
     * <p>
     * Uses only Tessellator/BufferRenderer draws — never the shared entity {@code Immediate},
     * which rebinds the main framebuffer and would paint sky onto the interior.
     * <p>
     * Cull must be off: the backdrop is a cube viewed from the inside.
     */
    public static void renderPortalSky(
            PoseStack matrices,
            MultiBufferSource ignoredVertexConsumers,
            SotoAtmosphere atmosphere
    ) {
        if (atmosphere == null) {
            atmosphere = SotoAtmosphere.DEFAULT;
        }
        EffectsKind kind = SotoAtmosphereColors.effectsKind(atmosphere.dimensionEffectsId());
        float skyAngle = SotoAtmosphereColors.skyAngle(atmosphere.timeOfDay());
        FogParameters skyFog = buildFog(atmosphere, kind, PORTAL_FOG_START, PORTAL_FOG_END);
        int skyRgb = portalBackdropRgb(atmosphere, kind, skyAngle);
        FogParameters previous = RenderSystem.getShaderFog();
        RenderSystem.setShaderFog(skyFog);

        boolean depthWasEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableCull();

        matrices.pushPose();
        try {
            double cx = SotoExteriorSampler.RELATIVE_TARDIS_POS.getX() + 0.5;
            double cy = SotoExteriorSampler.RELATIVE_TARDIS_POS.getY() + 1.0;
            double cz = SotoExteriorSampler.RELATIVE_TARDIS_POS.getZ() + 0.5;
            matrices.translate(cx, cy, cz);
            matrices.scale(SKY_SCALE, SKY_SCALE, SKY_SCALE);
            // Solid colored cube in ghost space (not vanilla SkyRendering VBOs): those expect
            // rotation-only modelview centered on the camera, and celestial Immediate draws
            // rebind the main FBO.
            renderSolidBackdropRgb(matrices, skyRgb);
        } finally {
            matrices.popPose();
            RenderSystem.depthMask(true);
            if (cullWasEnabled) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
            if (!depthWasEnabled) {
                RenderSystem.disableDepthTest();
            }
            RenderSystem.setShaderFog(previous);
        }
    }

    /** RGB used to clear / fill the portal sky when geometry cannot rely on vanilla sky VBOs. */
    public static int portalBackdropRgb(SotoAtmosphere atmosphere) {
        if (atmosphere == null) {
            atmosphere = SotoAtmosphere.DEFAULT;
        }
        EffectsKind kind = SotoAtmosphereColors.effectsKind(atmosphere.dimensionEffectsId());
        float skyAngle = SotoAtmosphereColors.skyAngle(atmosphere.timeOfDay());
        return portalBackdropRgb(atmosphere, kind, skyAngle);
    }

    private static int portalBackdropRgb(SotoAtmosphere atmosphere, EffectsKind kind, float skyAngle) {
        return switch (kind) {
            case OVERWORLD -> SotoAtmosphereColors.skyColor(
                    atmosphere.biomeSkyColor(),
                    skyAngle,
                    atmosphere.rainGradient(),
                    atmosphere.thunderGradient()
            );
            case NETHER, END -> {
                FogParameters fog = buildFog(atmosphere, kind, PORTAL_FOG_START, PORTAL_FOG_END);
                yield ARGB.colorFromFloat(1.0F, fog.red(), fog.green(), fog.blue());
            }
        };
    }

    public static FogParameters applyPortalTerrainFog(SotoAtmosphere atmosphere) {
        if (atmosphere == null) {
            atmosphere = SotoAtmosphere.DEFAULT;
        }
        EffectsKind kind = SotoAtmosphereColors.effectsKind(atmosphere.dimensionEffectsId());
        FogParameters previous = RenderSystem.getShaderFog();
        RenderSystem.setShaderFog(buildFog(atmosphere, kind, PORTAL_FOG_START, PORTAL_FOG_END));
        return previous;
    }

    public static void restoreFog(FogParameters previous) {
        RenderSystem.setShaderFog(previous == null ? FogParameters.NO_FOG : previous);
    }

    static FogParameters buildFog(SotoAtmosphere atmosphere, EffectsKind kind, float start, float end) {
        float skyAngle = SotoAtmosphereColors.skyAngle(atmosphere.timeOfDay());
        Vec3 color = SotoAtmosphereColors.fogColor(
                atmosphere.biomeFogColor(),
                kind,
                skyAngle,
                atmosphere.rainGradient(),
                atmosphere.thunderGradient()
        );
        return new FogParameters(
                start,
                end,
                FogShape.SPHERE,
                (float) color.x,
                (float) color.y,
                (float) color.z,
                1.0F
        );
    }

    private static void renderSolidBackdropRgb(PoseStack matrices, int argb) {
        Matrix4f matrix = matrices.last().pose();
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        float size = VANILLA_SKY_RADIUS;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        // Six faces of a large cube centered at origin (sky-space), wound for outside normals;
        // callers must disable cull so the interior view sees them.
        // -Z
        buffer.addVertex(matrix, -size, -size, -size).setColor(argb);
        buffer.addVertex(matrix, -size, size, -size).setColor(argb);
        buffer.addVertex(matrix, size, size, -size).setColor(argb);
        buffer.addVertex(matrix, size, -size, -size).setColor(argb);
        // +Z
        buffer.addVertex(matrix, size, -size, size).setColor(argb);
        buffer.addVertex(matrix, size, size, size).setColor(argb);
        buffer.addVertex(matrix, -size, size, size).setColor(argb);
        buffer.addVertex(matrix, -size, -size, size).setColor(argb);
        // -X
        buffer.addVertex(matrix, -size, -size, size).setColor(argb);
        buffer.addVertex(matrix, -size, size, size).setColor(argb);
        buffer.addVertex(matrix, -size, size, -size).setColor(argb);
        buffer.addVertex(matrix, -size, -size, -size).setColor(argb);
        // +X
        buffer.addVertex(matrix, size, -size, -size).setColor(argb);
        buffer.addVertex(matrix, size, size, -size).setColor(argb);
        buffer.addVertex(matrix, size, size, size).setColor(argb);
        buffer.addVertex(matrix, size, -size, size).setColor(argb);
        // -Y
        buffer.addVertex(matrix, -size, -size, -size).setColor(argb);
        buffer.addVertex(matrix, size, -size, -size).setColor(argb);
        buffer.addVertex(matrix, size, -size, size).setColor(argb);
        buffer.addVertex(matrix, -size, -size, size).setColor(argb);
        // +Y
        buffer.addVertex(matrix, -size, size, size).setColor(argb);
        buffer.addVertex(matrix, size, size, size).setColor(argb);
        buffer.addVertex(matrix, size, size, -size).setColor(argb);
        buffer.addVertex(matrix, -size, size, -size).setColor(argb);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }
}
