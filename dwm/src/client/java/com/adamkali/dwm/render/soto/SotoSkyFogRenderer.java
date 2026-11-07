package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.render.portal.PortalFogRenderer;
import com.adamkali.dwm.render.portal.PortalContentContext;
import com.adamkali.dwm.tardis.portal.PortalSampler;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors;
import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors.EffectsKind;
import com.adamkali.dwm.tardis.soto.SotoCloudState;
import com.adamkali.dwm.tardis.soto.SotoSkyState;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;

/**
 * Mini skybox + fog for the SOTO exterior portal, driven by synced {@link SotoAtmosphere}.
 * <p>
 * Minecraft 26.2 replaced {@code FogParameters}/{@code Tesselator}/{@code CoreShaders} with
 * GpuBufferSlice fog and RenderPass draws. The overworld sky (disc, sun, moon, stars, sunrise glow)
 * is drawn by a portal-owned vanilla {@code SkyRenderer} and clouds by {@link SotoCloudRenderer};
 * other dimensions fill via the target clear colour ({@link #portalBackdropRgb}). Terrain fog apply/restore
 * preserves the previous GpuBufferSlice.
 */
public final class SotoSkyFogRenderer {
    /** Sky fog must never tint the celestials, so push its end far past the sky dome. */
    private static final float SKY_FOG_END = 100_000.0F;

    private static SkyRenderer portalSky;
    private static RenderTarget portalSkyTarget;
    private static SotoCloudRenderer portalClouds;

    private SotoSkyFogRenderer() {
    }

    /**
     * Client render distance used for portal fog / stream sizing, falling back to
     * {@link PortalSampler#DEFAULT_STREAM_RADIUS_CHUNKS}.
     */
    public static int clientStreamRadiusChunks() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.options == null) {
            return PortalSampler.DEFAULT_STREAM_RADIUS_CHUNKS;
        }
        int renderDistance = client.options.getEffectiveRenderDistance();
        return renderDistance > 0 ? renderDistance : PortalSampler.DEFAULT_STREAM_RADIUS_CHUNKS;
    }

    /**
     * Draws the overworld sun, moon, stars and sunrise/sunset glow into the portal target using a
     * portal-owned vanilla {@link SkyRenderer}, then clouds via {@link SotoCloudRenderer} (vanilla's
     * cloud renderer hard-binds the main target). Nether/End keep the flat fog-colour clear backdrop.
     * <p>
     * The shared entity submit collector must not be used for sky fills — it can rebind the main
     * framebuffer.
     */
    public static void renderPortalSky(PortalContentContext context, SotoAtmosphere atmosphere, BlockPos footprintOrigin) {
        SotoSkyState state = SotoSkyState.from(atmosphere);
        if (!state.hasCelestials()) {
            return;
        }
        SkyRenderer renderer = portalSkyRenderer(context.target().renderTarget());
        if (renderer == null) {
            return;
        }
        GpuBufferSlice previousFog = PortalFogRenderer.apply(
                0.0F, SKY_FOG_END, ARGB.redFloat(state.skyColor()),
                ARGB.greenFloat(state.skyColor()), ARGB.blueFloat(state.skyColor()));

        boolean depthWasEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        SotoGl.enableDepthTest();
        SotoGl.depthMask(false);
        SotoGl.depthFunc(GL11.GL_LEQUAL);
        SotoGl.disableCull();

        // Sky meshes are camera-centred: the model-view must carry rotation only.
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        try {
            modelView.identity();
            context.portalCamera().getViewRotationMatrix(modelView);
            PoseStack poseStack = new PoseStack();
            renderer.renderSkyDisc(state.skyColor());
            renderer.renderSunriseAndSunset(poseStack, state.sunAngle(), state.sunriseSunsetColor());
            renderer.renderSunMoonAndStars(
                    poseStack,
                    state.sunAngle(),
                    state.moonAngle(),
                    state.starAngle(),
                    state.moonPhase(),
                    state.rainBrightness(),
                    state.starBrightness());
            renderPortalClouds(context, atmosphere, footprintOrigin);
        } finally {
            modelView.popMatrix();
            SotoGl.depthMask(true);
            if (cullWasEnabled) {
                SotoGl.enableCull();
            } else {
                SotoGl.disableCull();
            }
            if (!depthWasEnabled) {
                SotoGl.disableDepthTest();
            }
            PortalFogRenderer.restore(previousFog);
        }
    }

    /**
     * Draws clouds with the portal-owned {@link SotoCloudRenderer}. The portal camera lives in
     * footprint-local space; {@link SotoCloudState#cloudCamera} adds the synced footprint origin so the
     * clouds are placed from the exterior's real world position.
     */
    private static void renderPortalClouds(
            PortalContentContext context, SotoAtmosphere atmosphere, BlockPos footprintOrigin) {
        Minecraft client = context.client();
        if (client.level == null || client.options == null) {
            return;
        }
        SotoCloudState clouds = SotoCloudState.from(atmosphere);
        CloudStatus status = client.options.cloudStatus().get();
        if (!clouds.hasClouds() || status == CloudStatus.OFF) {
            return;
        }
        if (portalClouds == null) {
            portalClouds = new SotoCloudRenderer();
        }
        Vec3 camera = context.portalCamera().position();
        portalClouds.render(
                context.target().colorTextureView(),
                context.target().depthTextureView(),
                clouds.color(),
                status,
                clouds.height(),
                client.options.cloudRange().get(),
                SotoCloudState.cloudCamera(footprintOrigin, camera),
                client.level.getGameTime(),
                context.tickDelta());
    }

    private static SkyRenderer portalSkyRenderer(RenderTarget target) {
        if (target == null) {
            return null;
        }
        if (portalSky == null || portalSkyTarget != target) {
            closePortalSky();
            Minecraft client = Minecraft.getInstance();
            portalSky = new SkyRenderer(client.getTextureManager(), client.getAtlasManager(), target);
            portalSkyTarget = target;
        }
        return portalSky;
    }

    /** Releases the portal sky buffers; called when the portal target is destroyed. */
    public static void closePortalSky() {
        if (portalClouds != null) {
            portalClouds.close();
            portalClouds = null;
        }
        if (portalSky != null) {
            portalSky.close();
        }
        portalSky = null;
        portalSkyTarget = null;
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

    public static GpuBufferSlice applyPortalTerrainFog(SotoAtmosphere atmosphere) {
        int radius = clientStreamRadiusChunks();
        return applyPortalTerrainFog(
                atmosphere, PortalSampler.fogStartBlocks(radius), PortalSampler.fogEndBlocks(radius));
    }

    public static GpuBufferSlice applyPortalTerrainFog(SotoAtmosphere atmosphere, float start, float end) {
        SotoAtmosphere resolved = atmosphere == null ? SotoAtmosphere.DEFAULT : atmosphere;
        EffectsKind kind = SotoAtmosphereColors.effectsKind(resolved.dimensionEffectsId());
        FogColor fog = buildFogColor(resolved, kind, start, end);
        return PortalFogRenderer.apply(fog.start(), fog.end(), fog.red(), fog.green(), fog.blue());
    }

    public static void restoreFog(GpuBufferSlice previous) {
        PortalFogRenderer.restore(previous);
    }

    static FogColor buildFogColor(SotoAtmosphere atmosphere, EffectsKind kind) {
        int radius = clientStreamRadiusChunks();
        return buildFogColor(
                atmosphere, kind, PortalSampler.fogStartBlocks(radius), PortalSampler.fogEndBlocks(radius));
    }

    static FogColor buildFogColor(SotoAtmosphere atmosphere, EffectsKind kind, float start, float end) {
        float skyAngle = SotoAtmosphereColors.skyAngle(atmosphere.timeOfDay());
        Vec3 color = SotoAtmosphereColors.fogColor(
                atmosphere.biomeFogColor(),
                kind,
                skyAngle,
                atmosphere.rainGradient(),
                atmosphere.thunderGradient()
        );
        return new FogColor(
                start,
                end,
                (float) color.x,
                (float) color.y,
                (float) color.z
        );
    }

    /** Local fog color description (replaces removed FogParameters for color math). */
    record FogColor(float start, float end, float red, float green, float blue) {
    }
}
