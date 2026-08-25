package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors;
import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors.EffectsKind;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * Mini skybox + fog for the SOTO exterior portal, driven by synced {@link SotoAtmosphere}.
 * <p>
 * Minecraft 26.2 replaced {@code FogParameters}/{@code Tesselator}/{@code CoreShaders} with
 * GpuBufferSlice fog and RenderPass draws. Portal sky currently fills via target clear color
 * ({@link #portalBackdropRgb}); solid backdrop mesh redraw is deferred until a POSITION_COLOR
 * RenderPass helper lands. Terrain fog apply/restore preserves the previous GpuBufferSlice.
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
     * The shared entity submit collector must not be used for sky fills — it can rebind the main
     * framebuffer. Backdrop RGB is applied by the portal clear; optional mesh fill is a no-op
     * until RenderPass POSITION_COLOR drawing is ported.
     */
    public static void renderPortalSky(
            PoseStack matrices,
            SubmitNodeCollector ignoredSubmitNodeCollector,
            SotoAtmosphere atmosphere
    ) {
        if (atmosphere == null) {
            atmosphere = SotoAtmosphere.DEFAULT;
        }
        EffectsKind kind = SotoAtmosphereColors.effectsKind(atmosphere.dimensionEffectsId());
        float skyAngle = SotoAtmosphereColors.skyAngle(atmosphere.timeOfDay());
        int skyRgb = portalBackdropRgb(atmosphere, kind, skyAngle);
        GpuBufferSlice previous = RenderSystem.getShaderFog();

        boolean depthWasEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        SotoGl.enableDepthTest();
        SotoGl.depthMask(false);
        SotoGl.depthFunc(GL11.GL_LEQUAL);
        SotoGl.disableCull();

        matrices.pushPose();
        try {
            double cx = SotoExteriorSampler.RELATIVE_TARDIS_POS.getX() + 0.5;
            double cy = SotoExteriorSampler.RELATIVE_TARDIS_POS.getY() + 1.0;
            double cz = SotoExteriorSampler.RELATIVE_TARDIS_POS.getZ() + 0.5;
            matrices.translate(cx, cy, cz);
            matrices.scale(SKY_SCALE, SKY_SCALE, SKY_SCALE);
            // Portal clear already painted skyRgb; mesh backdrop deferred (no Tesselator in 26.2).
            renderSolidBackdropRgb(matrices, skyRgb);
        } finally {
            matrices.popPose();
            SotoGl.depthMask(true);
            if (cullWasEnabled) {
                SotoGl.enableCull();
            } else {
                SotoGl.disableCull();
            }
            if (!depthWasEnabled) {
                SotoGl.disableDepthTest();
            }
            if (previous != null) {
                RenderSystem.setShaderFog(previous);
            }
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
                FogColor fog = buildFogColor(atmosphere, kind);
                yield ARGB.colorFromFloat(1.0F, fog.red(), fog.green(), fog.blue());
            }
        };
    }

    /**
     * Remembers the previous shader fog slice. Minecraft 26.2 fog is a UBO slice; we cannot
     * construct a custom portal fog buffer without FogRenderer internals, so terrain draws keep
     * the active fog. Restore still swaps the captured slice back.
     */
    public static GpuBufferSlice applyPortalTerrainFog(SotoAtmosphere atmosphere) {
        // atmosphere retained for API stability / future FogData upload.
        return RenderSystem.getShaderFog();
    }

    public static void restoreFog(GpuBufferSlice previous) {
        if (previous != null) {
            RenderSystem.setShaderFog(previous);
        }
    }

    static FogColor buildFogColor(SotoAtmosphere atmosphere, EffectsKind kind) {
        float skyAngle = SotoAtmosphereColors.skyAngle(atmosphere.timeOfDay());
        Vec3 color = SotoAtmosphereColors.fogColor(
                atmosphere.biomeFogColor(),
                kind,
                skyAngle,
                atmosphere.rainGradient(),
                atmosphere.thunderGradient()
        );
        return new FogColor(
                PORTAL_FOG_START,
                PORTAL_FOG_END,
                (float) color.x,
                (float) color.y,
                (float) color.z
        );
    }

    private static void renderSolidBackdropRgb(PoseStack matrices, int argb) {
        // Intentionally empty on 26.2 until POSITION_COLOR RenderPass draw helper exists.
        // Portal bindAndClear already fills with portalBackdropRgb.
        Matrix4f ignored = matrices.last().pose();
        if (ignored == null || argb == 0 && VANILLA_SKY_RADIUS <= 0) {
            return;
        }
    }

    /** Local fog color description (replaces removed FogParameters for color math). */
    record FogColor(float start, float end, float red, float green, float blue) {
    }
}
