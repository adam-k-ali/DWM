package com.adamkali.dwm.render.soto.portal;

import com.adamkali.dwm.render.soto.SotoExteriorMeshCache;
import com.adamkali.dwm.render.soto.SotoSkyFogRenderer;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.adamkali.dwm.render.soto.ghost.SotoGhostMeshCache;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;

import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;

/**
 * Renders the streamed ghost exterior into the managed full-window portal target.
 */
public final class SotoPortalRenderer {
    private static final int FULLBRIGHT = LightTexture.pack(15, 15);

    private final SotoPortalRenderTarget target;
    private final PortalCamera portalCamera = new PortalCamera();

    public SotoPortalRenderer() {
        this(SotoPortalRenderTarget.getInstance());
    }

    SotoPortalRenderer(SotoPortalRenderTarget target) {
        this.target = target;
    }

    public PortalTexture render(
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
            return PortalTexture.UNAVAILABLE;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.level == null || client.gameRenderer == null) {
            return PortalTexture.UNAVAILABLE;
        }
        Camera mainCamera = client.gameRenderer.getMainCamera();
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
            if (!SotoPortalSupport.isReadyFor(tardisId)) {
                return PortalTexture.UNAVAILABLE;
            }
            if (target.shouldRenderThisFrame(tardisId)) {
                renderScene(client, tardisId, tickDelta, portalView);
            }
            return new PortalTexture(
                    target.colorTextureId(),
                    target.width(),
                    target.height(),
                    portalView,
                    true
            );
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
            target.bindAndClear(
                    ARGB.redFloat(clearRgb),
                    ARGB.greenFloat(clearRgb),
                    ARGB.blueFloat(clearRgb),
                    1.0f
            );
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.depthMask(true);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();

            portalCamera.apply(portalView);
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.set(portalView.viewMatrix());

            MultiBufferSource.BufferSource vertexConsumers =
                    client.renderBuffers().bufferSource();
            PoseStack sceneMatrices = new PoseStack();

            EntityRenderDispatcher entityDispatcher = client.getEntityRenderDispatcher();
            BlockEntityRenderDispatcher blockEntityDispatcher = client.getBlockEntityRenderDispatcher();
            try {
                SotoSkyFogRenderer.renderPortalSky(sceneMatrices, vertexConsumers, atmosphere);
                target.bindForWrite();
                FogParameters previousFog = SotoSkyFogRenderer.applyPortalTerrainFog(atmosphere);
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
                    SotoExteriorMeshCache.renderGhostBlockEntities(
                            sceneMatrices,
                            vertexConsumers,
                            FULLBRIGHT,
                            tickDelta,
                            tardisId,
                            portalCamera
                    );
                    target.bindForWrite();
                    SotoExteriorMeshCache.renderGhostEntities(
                            sceneMatrices,
                            vertexConsumers,
                            FULLBRIGHT,
                            tickDelta,
                            tardisId,
                            portalCamera
                    );
                    target.bindForWrite();
                    vertexConsumers.endBatch();
                    target.bindForWrite();
                } finally {
                    SotoSkyFogRenderer.restoreFog(previousFog);
                }
            } finally {
                if (client.level != null) {
                    entityDispatcher.prepare(client.level, client.gameRenderer.getMainCamera(), client.player);
                    blockEntityDispatcher.prepare(
                            client.level,
                            client.gameRenderer.getMainCamera(),
                            client.hitResult
                    );
                }
            }
        } finally {
            SotoPortalRenderTarget.endPortalPass();
        }
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
