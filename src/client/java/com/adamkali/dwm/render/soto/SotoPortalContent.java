package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.render.portal.PortalCameraTransform;
import com.adamkali.dwm.render.portal.PortalContent;
import com.adamkali.dwm.render.portal.PortalContentContext;
import com.adamkali.dwm.render.portal.PortalFeatureFlush;
import com.adamkali.dwm.render.portal.PortalSceneStore;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.adamkali.dwm.render.soto.ghost.SotoGhostMeshCache;
import com.adamkali.dwm.tardis.TardisExteriorFacing;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalShellState;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
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
        PortalSceneStore.requestIfNeeded(PortalStreamKind.SOTO, tardisId);
    }

    @Override
    public boolean isReady(Minecraft client) {
        if (client == null || client.level == null) {
            return false;
        }
        PortalSceneStore.requestIfNeeded(PortalStreamKind.SOTO, tardisId);
        SotoGhostExterior ghost = SotoGhostExterior.get(PortalStreamKind.SOTO, tardisId);
        PortalShellState shell = PortalSceneStore.getShell(PortalStreamKind.SOTO, tardisId);
        return ghost != null
                && shell != null
                && ghost.chunkCount() > 0
                && SotoGhostMeshCache.hasMeshes(PortalStreamKind.SOTO, tardisId);
    }

    @Override
    public int clearRgb(Minecraft client) {
        PortalAtmosphere portal = PortalSceneStore.getAtmosphere(PortalStreamKind.SOTO, tardisId);
        SotoAtmosphere atmosphere = portal != null ? SotoAtmosphere.fromPortal(portal) : SotoAtmosphere.DEFAULT;
        return SotoSkyFogRenderer.portalBackdropRgb(atmosphere);
    }

    @Override
    public PortalCameraTransform.Result hitch(Minecraft client) {
        SotoGhostExterior ghost = SotoGhostExterior.get(PortalStreamKind.SOTO, tardisId);
        PortalShellState shell = PortalSceneStore.getShell(PortalStreamKind.SOTO, tardisId);
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

        PortalAtmosphere portal = PortalSceneStore.getAtmosphere(PortalStreamKind.SOTO, id);
        SotoAtmosphere atmosphere = portal != null ? SotoAtmosphere.fromPortal(portal) : SotoAtmosphere.DEFAULT;

        SotoSkyFogRenderer.renderPortalSky(sceneMatrices, null, atmosphere);
        context.bindTarget();
        GpuBufferSlice previousFog = SotoSkyFogRenderer.applyPortalTerrainFog(atmosphere);
        try {
            SotoGhostMeshCache.drawLayer(
                    PortalStreamKind.SOTO,
                    id,
                    hitch.viewMatrix(),
                    SotoGhostMeshCache.TerrainPass.OPAQUE,
                    hitch
            );
            SotoGhostMeshCache.drawLayer(
                    PortalStreamKind.SOTO,
                    id,
                    hitch.viewMatrix(),
                    SotoGhostMeshCache.TerrainPass.CUTOUT,
                    hitch
            );
            SotoGhostMeshCache.drawLayer(
                    PortalStreamKind.SOTO,
                    id,
                    hitch.viewMatrix(),
                    SotoGhostMeshCache.TerrainPass.TRANSLUCENT,
                    hitch
            );
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
                                PortalStreamKind.SOTO,
                                id,
                                context.portalCamera()
                        );
                        SotoExteriorMeshCache.renderGhostEntities(
                                sceneMatrices,
                                submitStorage,
                                cameraState,
                                tickDelta,
                                PortalStreamKind.SOTO,
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
            }
        } finally {
            SotoSkyFogRenderer.restoreFog(previousFog);
        }
    }
}
