package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.render.portal.PortalCameraTransform;
import com.adamkali.dwm.render.portal.PortalContent;
import com.adamkali.dwm.render.portal.PortalContentContext;
import com.adamkali.dwm.render.portal.PortalFeatureFlush;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.adamkali.dwm.render.soto.ghost.SotoGhostMeshCache;
import com.adamkali.dwm.tardis.TardisExteriorFacing;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4fStack;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;

/**
 * SOTO portal content: hitch-fixed exterior look-out with ghost terrain + entities.
 */
public final class SotoPortalContent implements PortalContent {
    private static final int FULLBRIGHT = LightCoordsUtil.FULL_BRIGHT;

    private final UUID tardisId;

    public SotoPortalContent(UUID tardisId) {
        this.tardisId = Objects.requireNonNull(tardisId, "tardisId");
    }

    public void requestSync() {
        SotoGhostExterior.requestIfNeeded(tardisId);
    }

    @Override
    public boolean isReady(Minecraft client) {
        if (client == null || client.level == null) {
            return false;
        }
        SotoGhostExterior.requestIfNeeded(tardisId);
        SotoGhostExterior ghost = SotoGhostExterior.get(tardisId);
        SotoExteriorMeshCache.ShellState shell = SotoExteriorMeshCache.getShellState(tardisId);
        return ghost != null
                && shell != null
                && ghost.chunkCount() > 0
                && SotoGhostMeshCache.hasMeshes(tardisId);
    }

    @Override
    public int clearRgb(Minecraft client) {
        SotoAtmosphere atmosphere = SotoExteriorMeshCache.getAtmosphere(tardisId);
        if (atmosphere == null) {
            atmosphere = SotoAtmosphere.DEFAULT;
        }
        return SotoSkyFogRenderer.portalBackdropRgb(atmosphere);
    }

    @Override
    public PortalCameraTransform.Result hitch(Minecraft client) {
        SotoGhostExterior ghost = SotoGhostExterior.get(tardisId);
        SotoExteriorMeshCache.ShellState shell = SotoExteriorMeshCache.getShellState(tardisId);
        if (ghost == null || shell == null) {
            return null;
        }
        return PortalCameraTransform.exteriorDoorLookOut(
                SotoExteriorSampler.RELATIVE_TARDIS_POS,
                ghost.footprintOrigin(),
                TardisExteriorFacing.doorDirection(shell.exteriorRotation()),
                TardisSotoRenderer.PREVIEW_EYE_HEIGHT,
                TardisSotoRenderer.PREVIEW_FORWARD_OFFSET
        );
    }

    @Override
    public void renderInto(PortalContentContext context) {
        UUID id = tardisId;
        float tickDelta = context.tickDelta();
        PortalCameraTransform.Result hitch = context.hitch();
        PoseStack sceneMatrices = context.sceneMatrices();

        SotoAtmosphere atmosphere = SotoExteriorMeshCache.getAtmosphere(id);
        if (atmosphere == null) {
            atmosphere = SotoAtmosphere.DEFAULT;
        }

        SotoSkyFogRenderer.renderPortalSky(sceneMatrices, null, atmosphere);
        context.bindTarget();
        GpuBufferSlice previousFog = SotoSkyFogRenderer.applyPortalTerrainFog(atmosphere);
        try {
            context.bindTarget();
            SotoGhostMeshCache.drawLayer(
                    id,
                    hitch.viewMatrix(),
                    SotoGhostMeshCache.TerrainPass.OPAQUE
            );
            context.bindTarget();
            SotoGhostMeshCache.drawLayer(
                    id,
                    hitch.viewMatrix(),
                    SotoGhostMeshCache.TerrainPass.CUTOUT
            );
            context.bindTarget();
            SotoGhostMeshCache.drawLayer(
                    id,
                    hitch.viewMatrix(),
                    SotoGhostMeshCache.TerrainPass.TRANSLUCENT
            );
            context.bindTarget();
            try {
                PortalFeatureFlush featureFlush = context.featureFlush();
                if (featureFlush != null) {
                    SubmitNodeStorage submitStorage = context.submitStorage();
                    CameraRenderState cameraState = context.cameraState();
                    Matrix4fStack featureModelView = RenderSystem.getModelViewStack();
                    featureModelView.pushMatrix();
                    try {
                        context.portalCamera().getViewRotationMatrix(featureModelView);
                        SotoExteriorMeshCache.renderGhostBlockEntities(
                                sceneMatrices,
                                submitStorage,
                                cameraState,
                                FULLBRIGHT,
                                tickDelta,
                                id,
                                context.portalCamera()
                        );
                        SotoExteriorMeshCache.renderGhostEntities(
                                sceneMatrices,
                                submitStorage,
                                cameraState,
                                tickDelta,
                                id,
                                context.portalCamera()
                        );
                        context.bindTarget();
                        featureFlush.renderAllFeatures(submitStorage);
                        context.bindTarget();
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
    }
}
