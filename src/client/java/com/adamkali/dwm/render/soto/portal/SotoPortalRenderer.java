package com.adamkali.dwm.render.soto.portal;

import com.adamkali.dwm.render.soto.SotoExteriorMeshCache;
import com.adamkali.dwm.render.soto.SotoSkyFogRenderer;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.adamkali.dwm.render.soto.ghost.SotoGhostMeshCache;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;

/**
 * Renders the streamed ghost exterior into the managed full-window portal target.
 * <p>
 * Mid-BER GPU work blacks out the world on 26.2. Call {@link #renderOffMainPass} from
 * {@code LevelRenderEvents.END_MAIN} only; BER uses {@link #peekLastRendered}.
 */
public final class SotoPortalRenderer {
    private static final int FULLBRIGHT = LightCoordsUtil.FULL_BRIGHT;

    private final SotoPortalRenderTarget target;
    private final PortalCamera portalCamera = new PortalCamera();
    private final Set<UUID> renderedReady = new HashSet<>();

    public SotoPortalRenderer() {
        this(SotoPortalRenderTarget.getInstance());
    }

    SotoPortalRenderer(SotoPortalRenderTarget target) {
        this.target = target;
    }

    /**
     * Safe for BER: returns last END_MAIN-rendered portal texture without touching GPU state.
     */
    public PortalTexture peekLastRendered(UUID tardisId) {
        if (tardisId == null || !renderedReady.contains(tardisId) || !target.isReady()) {
            return PortalTexture.UNAVAILABLE;
        }
        int colorTex = target.colorTextureId();
        if (colorTex < 0) {
            return PortalTexture.UNAVAILABLE;
        }
        return new PortalTexture(colorTex, target.width(), target.height(), null, true);
    }

    /**
     * Full portal scene render — call only after the main world pass (END_MAIN).
     */
    public PortalTexture renderOffMainPass(
            UUID tardisId,
            BlockPos interiorDoorPos,
            Direction interiorDoorFacing,
            float tickDelta
    ) {
        if (tardisId == null || interiorDoorPos == null || interiorDoorFacing == null) {
            return PortalTexture.UNAVAILABLE;
        }
        if (!SotoPortalSupport.isAvailable()) {
            return PortalTexture.UNAVAILABLE;
        }
        SotoGhostExterior.requestIfNeeded(tardisId);
        SotoGhostExterior ghost = SotoGhostExterior.get(tardisId);
        SotoExteriorMeshCache.ShellState shell = SotoExteriorMeshCache.getShellState(tardisId);
        if (ghost == null || shell == null || ghost.chunkCount() == 0 || !SotoGhostMeshCache.hasMeshes(tardisId)) {
            renderedReady.remove(tardisId);
            return PortalTexture.UNAVAILABLE;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.level == null || client.gameRenderer == null) {
            return PortalTexture.UNAVAILABLE;
        }
        Camera mainCamera = client.gameRenderer.mainCamera();
        SotoPortalCameraTransform.Result portalView = SotoPortalCameraTransform.map(
                mainCamera,
                interiorDoorPos,
                interiorDoorFacing,
                ghost.footprintOrigin(),
                shell.exteriorRotation()
        );

        try (SotoPortalRenderTarget.RenderStateGuard ignored =
                     SotoPortalRenderTarget.RenderStateGuard.capture()) {
            if (!target.ensureReady(client)) {
                SotoPortalSupport.disableForSession("Portal framebuffer is incomplete", null);
                return PortalTexture.UNAVAILABLE;
            }
            if (!target.shouldRenderThisFrame(tardisId)) {
                return peekLastRendered(tardisId);
            }
            renderScene(client, tardisId, tickDelta, portalView);
            renderedReady.add(tardisId);
            int colorTex = target.colorTextureId();
            return new PortalTexture(colorTex, target.width(), target.height(), portalView, true);
        }
    }

    private void renderScene(
            Minecraft client,
            UUID tardisId,
            float tickDelta,
            SotoPortalCameraTransform.Result portalView
    ) {
        SotoAtmosphere atmosphere = SotoExteriorMeshCache.getAtmosphere(tardisId);
        if (atmosphere == null) {
            atmosphere = SotoAtmosphere.DEFAULT;
        }
        int clearRgb = SotoSkyFogRenderer.portalBackdropRgb(atmosphere);
        SotoPortalRenderTarget.beginPortalPass();
        try {
            target.clearViaRenderPass(
                    ARGB.redFloat(clearRgb),
                    ARGB.greenFloat(clearRgb),
                    ARGB.blueFloat(clearRgb),
                    1.0f
            );
            GlStateManager._disableScissorTest();
            GlStateManager._enableDepthTest();
            GlStateManager._depthFunc(GL11.GL_LEQUAL);
            GlStateManager._depthMask(true);
            GlStateManager._colorMask(0xF);
            GlStateManager._enableCull();
            GlStateManager._disableBlend(0);
            GL11.glDisable(GL11.GL_STENCIL_TEST);

            portalCamera.apply(portalView);
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.set(portalView.viewMatrix());

            PoseStack sceneMatrices = new PoseStack();
            EntityRenderDispatcher entityDispatcher = client.getEntityRenderDispatcher();
            BlockEntityRenderDispatcher blockEntityDispatcher = client.getBlockEntityRenderDispatcher();
            try {
                SotoSkyFogRenderer.renderPortalSky(sceneMatrices, null, atmosphere);
                target.bindForWrite();
                GpuBufferSlice previousFog = SotoSkyFogRenderer.applyPortalTerrainFog(atmosphere);
                try {
                    target.bindForWrite();
                    SotoGhostMeshCache.drawLayer(
                            tardisId,
                            portalView.viewMatrix(),
                            SotoGhostMeshCache.TerrainPass.OPAQUE
                    );
                    target.bindForWrite();
                    SotoGhostMeshCache.drawLayer(
                            tardisId,
                            portalView.viewMatrix(),
                            SotoGhostMeshCache.TerrainPass.CUTOUT
                    );
                    target.bindForWrite();
                    SotoGhostMeshCache.drawLayer(
                            tardisId,
                            portalView.viewMatrix(),
                            SotoGhostMeshCache.TerrainPass.TRANSLUCENT
                    );
                    target.bindForWrite();
                    try {
                        SotoPortalFeatureFlush featureFlush = SotoPortalFeatureFlush.get(client);
                        if (featureFlush != null) {
                            SubmitNodeStorage submitStorage = new SubmitNodeStorage();
                            CameraRenderState cameraState = createPortalCameraState(portalView);
                            // Vanilla features bake pose verts then multiply by viewRotation only;
                            // submit positions must be camera-relative.
                            Matrix4fStack featureModelView = RenderSystem.getModelViewStack();
                            featureModelView.pushMatrix();
                            try {
                                portalCamera.getViewRotationMatrix(featureModelView);
                                SotoExteriorMeshCache.renderGhostBlockEntities(
                                        sceneMatrices,
                                        submitStorage,
                                        cameraState,
                                        FULLBRIGHT,
                                        tickDelta,
                                        tardisId,
                                        portalCamera
                                );
                                SotoExteriorMeshCache.renderGhostEntities(
                                        sceneMatrices,
                                        submitStorage,
                                        cameraState,
                                        tickDelta,
                                        tardisId,
                                        portalCamera
                                );
                                target.bindForWrite();
                                featureFlush.renderAllFeatures(submitStorage);
                                target.bindForWrite();
                            } finally {
                                featureModelView.popMatrix();
                            }
                        }
                    } catch (Throwable ignored) {
                        // Keep terrain portal alive; entity/BE features are best-effort.
                    }
                } finally {
                    SotoSkyFogRenderer.restoreFog(previousFog);
                }
            } finally {
                if (client.level != null) {
                    entityDispatcher.prepare(client.gameRenderer.mainCamera(), client.player);
                    blockEntityDispatcher.prepare(client.gameRenderer.mainCamera().position());
                }
                modelViewStack.popMatrix();
            }
        } finally {
            SotoPortalRenderTarget.endPortalPass();
        }
    }

    private CameraRenderState createPortalCameraState(SotoPortalCameraTransform.Result portalView) {
        CameraRenderState cameraState = new CameraRenderState();
        cameraState.initialized = true;
        cameraState.pos = portalView.ghostRelativePosition();
        cameraState.blockPos = BlockPos.containing(cameraState.pos);
        cameraState.yRot = portalView.yaw();
        cameraState.xRot = portalView.pitch();
        cameraState.orientation.set(portalCamera.rotation());
        return cameraState;
    }

    public record PortalTexture(
            int textureId,
            int width,
            int height,
            SotoPortalCameraTransform.Result camera,
            boolean available
    ) {
        static final PortalTexture UNAVAILABLE = new PortalTexture(-1, 0, 0, null, false);
    }

    private static final class PortalCamera extends Camera {
        void apply(SotoPortalCameraTransform.Result result) {
            setPosition(result.ghostRelativePosition());
            setRotation(result.yaw(), result.pitch());
        }
    }
}
