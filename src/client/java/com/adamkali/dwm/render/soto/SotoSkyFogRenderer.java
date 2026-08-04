package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors;
import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors.EffectsKind;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * Mini skybox + fog for the SOTO exterior portal, driven by synced {@link SotoAtmosphere}.
 */
public final class SotoSkyFogRenderer {
    /** Vanilla sky dome radius; scale so it sits behind the 11×7×11 footprint. */
    private static final float VANILLA_SKY_RADIUS = 512.0F;
    private static final float SKY_SCALE = 10.0F / VANILLA_SKY_RADIUS;

    static final float TERRAIN_FOG_START = 4.0F;
    static final float TERRAIN_FOG_END = 14.0F;
    static final float PORTAL_FOG_START = 20.0F;
    static final float PORTAL_FOG_END = 30.0F;

    private static SkyRendering skyRendering;

    private SotoSkyFogRenderer() {
    }

    private static SkyRendering sky() {
        if (skyRendering == null) {
            skyRendering = new SkyRendering();
        }
        return skyRendering;
    }

    /**
     * Draws sky (or nether backdrop) in the already-aligned exterior footprint space.
     * Caller must have stencil test active. Depth writes are disabled for this pass.
     */
    public static void renderSky(MatrixStack matrices, VertexConsumerProvider vertexConsumers, SotoAtmosphere atmosphere) {
        renderSky(matrices, vertexConsumers, atmosphere, TERRAIN_FOG_START, TERRAIN_FOG_END);
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
            MatrixStack matrices,
            VertexConsumerProvider ignoredVertexConsumers,
            SotoAtmosphere atmosphere
    ) {
        if (atmosphere == null) {
            atmosphere = SotoAtmosphere.DEFAULT;
        }
        EffectsKind kind = SotoAtmosphereColors.effectsKind(atmosphere.dimensionEffectsId());
        float skyAngle = SotoAtmosphereColors.skyAngle(atmosphere.timeOfDay());
        Fog skyFog = buildFog(atmosphere, kind, PORTAL_FOG_START, PORTAL_FOG_END);
        int skyRgb = portalBackdropRgb(atmosphere, kind, skyAngle);
        Fog previous = RenderSystem.getShaderFog();
        RenderSystem.setShaderFog(skyFog);

        boolean depthWasEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableCull();

        matrices.push();
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
            matrices.pop();
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
                Fog fog = buildFog(atmosphere, kind, PORTAL_FOG_START, PORTAL_FOG_END);
                yield ColorHelper.fromFloats(1.0F, fog.red(), fog.green(), fog.blue());
            }
        };
    }

    private static void renderSky(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            SotoAtmosphere atmosphere,
            float fogStart,
            float fogEnd
    ) {
        if (atmosphere == null) {
            atmosphere = SotoAtmosphere.DEFAULT;
        }
        EffectsKind kind = SotoAtmosphereColors.effectsKind(atmosphere.dimensionEffectsId());
        float skyAngle = SotoAtmosphereColors.skyAngle(atmosphere.timeOfDay());
        Fog skyFog = buildFog(atmosphere, kind, fogStart, fogEnd);
        Fog previous = RenderSystem.getShaderFog();
        RenderSystem.setShaderFog(skyFog);

        boolean depthWasEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);

        matrices.push();
        try {
            double cx = SotoExteriorSampler.RELATIVE_TARDIS_POS.getX() + 0.5;
            double cy = SotoExteriorSampler.RELATIVE_TARDIS_POS.getY() + 1.0;
            double cz = SotoExteriorSampler.RELATIVE_TARDIS_POS.getZ() + 0.5;
            matrices.translate(cx, cy, cz);
            matrices.scale(SKY_SCALE, SKY_SCALE, SKY_SCALE);

            switch (kind) {
                case END -> sky().renderEndSky();
                case NETHER -> renderSolidBackdrop(matrices, skyFog);
                case OVERWORLD -> renderOverworldSky(matrices, vertexConsumers, atmosphere, skyAngle, skyFog);
            }
        } finally {
            matrices.pop();
            RenderSystem.depthMask(true);
            if (!depthWasEnabled) {
                RenderSystem.disableDepthTest();
            }
            RenderSystem.setShaderFog(previous);
        }
    }

    /**
     * Applies short-range terrain fog for the exterior mesh, matching synced fog color.
     * Returns the previous fog so the caller can restore it.
     */
    public static Fog applyTerrainFog(SotoAtmosphere atmosphere) {
        return applyTerrainFog(atmosphere, TERRAIN_FOG_START, TERRAIN_FOG_END);
    }

    public static Fog applyPortalTerrainFog(SotoAtmosphere atmosphere) {
        return applyTerrainFog(atmosphere, PORTAL_FOG_START, PORTAL_FOG_END);
    }

    private static Fog applyTerrainFog(SotoAtmosphere atmosphere, float start, float end) {
        if (atmosphere == null) {
            atmosphere = SotoAtmosphere.DEFAULT;
        }
        EffectsKind kind = SotoAtmosphereColors.effectsKind(atmosphere.dimensionEffectsId());
        Fog previous = RenderSystem.getShaderFog();
        RenderSystem.setShaderFog(buildFog(atmosphere, kind, start, end));
        return previous;
    }

    public static void restoreFog(Fog previous) {
        RenderSystem.setShaderFog(previous == null ? Fog.DUMMY : previous);
    }

    static Fog buildFog(SotoAtmosphere atmosphere, EffectsKind kind, float start, float end) {
        float skyAngle = SotoAtmosphereColors.skyAngle(atmosphere.timeOfDay());
        Vec3d color = SotoAtmosphereColors.fogColor(
                atmosphere.biomeFogColor(),
                kind,
                skyAngle,
                atmosphere.rainGradient(),
                atmosphere.thunderGradient()
        );
        return new Fog(
                start,
                end,
                FogShape.SPHERE,
                (float) color.x,
                (float) color.y,
                (float) color.z,
                1.0F
        );
    }

    private static void renderOverworldSky(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            SotoAtmosphere atmosphere,
            float skyAngle,
            Fog skyFog
    ) {
        int skyRgb = SotoAtmosphereColors.skyColor(
                atmosphere.biomeSkyColor(),
                skyAngle,
                atmosphere.rainGradient(),
                atmosphere.thunderGradient()
        );
        float r = ColorHelper.getRedFloat(skyRgb);
        float g = ColorHelper.getGreenFloat(skyRgb);
        float b = ColorHelper.getBlueFloat(skyRgb);
        sky().renderSky(r, g, b);

        VertexConsumerProvider.Immediate immediate = resolveImmediate(vertexConsumers);
        float rainAlpha = 1.0F - atmosphere.rainGradient();
        float stars = SotoAtmosphereColors.starBrightness(skyAngle) * rainAlpha;

        if (SotoAtmosphereColors.isSunRisingOrSetting(skyAngle)) {
            int sunrise = SotoAtmosphereColors.sunriseSunsetColor(skyAngle);
            sky().renderGlowingSky(matrices, immediate, SotoAtmosphereColors.skyAngleRadians(atmosphere.timeOfDay()), sunrise);
            immediate.draw();
        }

        sky().renderCelestialBodies(
                matrices,
                immediate,
                skyAngle,
                SotoAtmosphereColors.moonPhase(atmosphere.timeOfDay()),
                rainAlpha,
                stars,
                skyFog
        );
        immediate.draw();
    }

    private static void renderSolidBackdrop(MatrixStack matrices, Fog fog) {
        renderSolidBackdropRgb(
                matrices,
                ColorHelper.fromFloats(1.0F, fog.red(), fog.green(), fog.blue())
        );
    }

    private static void renderSolidBackdropRgb(MatrixStack matrices, int argb) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float size = VANILLA_SKY_RADIUS;
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        // Six faces of a large cube centered at origin (sky-space), wound for outside normals;
        // callers must disable cull so the interior view sees them.
        // -Z
        buffer.vertex(matrix, -size, -size, -size).color(argb);
        buffer.vertex(matrix, -size, size, -size).color(argb);
        buffer.vertex(matrix, size, size, -size).color(argb);
        buffer.vertex(matrix, size, -size, -size).color(argb);
        // +Z
        buffer.vertex(matrix, size, -size, size).color(argb);
        buffer.vertex(matrix, size, size, size).color(argb);
        buffer.vertex(matrix, -size, size, size).color(argb);
        buffer.vertex(matrix, -size, -size, size).color(argb);
        // -X
        buffer.vertex(matrix, -size, -size, size).color(argb);
        buffer.vertex(matrix, -size, size, size).color(argb);
        buffer.vertex(matrix, -size, size, -size).color(argb);
        buffer.vertex(matrix, -size, -size, -size).color(argb);
        // +X
        buffer.vertex(matrix, size, -size, -size).color(argb);
        buffer.vertex(matrix, size, size, -size).color(argb);
        buffer.vertex(matrix, size, size, size).color(argb);
        buffer.vertex(matrix, size, -size, size).color(argb);
        // -Y
        buffer.vertex(matrix, -size, -size, -size).color(argb);
        buffer.vertex(matrix, size, -size, -size).color(argb);
        buffer.vertex(matrix, size, -size, size).color(argb);
        buffer.vertex(matrix, -size, -size, size).color(argb);
        // +Y
        buffer.vertex(matrix, -size, size, size).color(argb);
        buffer.vertex(matrix, size, size, size).color(argb);
        buffer.vertex(matrix, size, size, -size).color(argb);
        buffer.vertex(matrix, -size, size, -size).color(argb);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private static VertexConsumerProvider.Immediate resolveImmediate(VertexConsumerProvider vertexConsumers) {
        if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
            return immediate;
        }
        return MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
    }
}
