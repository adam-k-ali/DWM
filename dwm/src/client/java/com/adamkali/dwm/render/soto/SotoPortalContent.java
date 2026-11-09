package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.render.portal.PortalCameraTransform;
import com.adamkali.dwm.render.portal.PortalContent;
import com.adamkali.dwm.render.portal.PortalContentContext;
import com.adamkali.dwm.render.portal.PortalFeatureFlush;
import com.adamkali.dwm.render.portal.PortalFrameCache;
import com.adamkali.dwm.render.portal.PortalLightmap;
import com.adamkali.dwm.render.portal.PortalPerfStats;
import com.adamkali.dwm.render.portal.PortalDoorFrame;
import com.adamkali.dwm.render.portal.PortalSceneStore;
import com.adamkali.dwm.render.portal.PortalViewerCamera;
import com.adamkali.dwm.render.portal.PortalViewerState;
import com.adamkali.dwm.render.TardisBlockEntityRenderer;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.adamkali.dwm.render.soto.ghost.SotoGhostMeshCache;
import com.adamkali.dwm.tardis.TardisExteriorFacing;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalShellState;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.adamkali.dwm.tardis.soto.SotoCloudState;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;

/**
 * SOTO portal content: hitch-fixed exterior look-out with ghost terrain + entities.
 */
public final class SotoPortalContent implements PortalContent {
    private final UUID tardisId;
    /** Parallax: interior doorway in world space; {@code null} keeps the fixed door hitch. */
    private final PortalDoorFrame interiorDoor;
    private long lastCloudRefreshTick = Long.MIN_VALUE;

    public SotoPortalContent(UUID tardisId) {
        this(tardisId, null);
    }

    public SotoPortalContent(UUID tardisId, PortalDoorFrame interiorDoor) {
        this.tardisId = Objects.requireNonNull(tardisId, "tardisId");
        this.interiorDoor = interiorDoor;
    }

    /**
     * Exterior shell aperture in footprint-relative coords, built with the exterior BER's own
     * transforms for the synced chameleon variant and rotation.
     */
    static PortalDoorFrame exteriorDoorFrame(PortalShellState shell) {
        float rotationDegrees = RotationSegment.convertToDegrees(shell.exteriorRotation());
        Direction outward = TardisExteriorFacing.doorDirection(shell.exteriorRotation());
        PoseStack pose = new PoseStack();
        BlockPos tardis = SotoExteriorSampler.RELATIVE_TARDIS_POS;
        pose.translate(tardis.getX(), tardis.getY(), tardis.getZ());
        TardisBlockEntityRenderer.applyExteriorTransforms(pose, rotationDegrees);
        return PortalDoorFrame.fromAperture(
                pose.last().pose(),
                shell.variant().getAperture(),
                Vec3.ZERO,
                new Vec3(outward.getStepX(), 0.0, outward.getStepZ())
        );
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

    /** Clouds drift every tick but the portal frame is cached, so redraw periodically while they are visible. */
    @Override
    public void onFrameRequested(Minecraft client) {
        if (client.level == null || client.options == null
                || client.options.cloudStatus().get() == CloudStatus.OFF) {
            return;
        }
        PortalAtmosphere portal = PortalSceneStore.getAtmosphere(PortalStreamKind.SOTO, tardisId);
        if (!SotoCloudState.from(portal != null ? SotoAtmosphere.fromPortal(portal) : SotoAtmosphere.DEFAULT)
                .hasClouds()) {
            return;
        }
        long now = client.level.getGameTime();
        if (SotoCloudState.refreshDue(now, lastCloudRefreshTick)) {
            lastCloudRefreshTick = now;
            PortalFrameCache.markDirty(PortalStreamKind.SOTO, tardisId);
        }
    }

    @Override
    public int clearRgb(Minecraft client) {
        PortalAtmosphere portal = PortalSceneStore.getAtmosphere(PortalStreamKind.SOTO, tardisId);
        SotoAtmosphere atmosphere = portal != null ? SotoAtmosphere.fromPortal(portal) : SotoAtmosphere.DEFAULT;
        return SotoSkyFogRenderer.portalBackdropRgb(atmosphere);
    }

    @Override
    public PortalCameraTransform.Result hitch(Minecraft client) {
        PortalCameraTransform.Result fixed = fixedHitch();
        if (interiorDoor == null || fixed == null) {
            return fixed;
        }
        PortalViewerState.Viewer viewer = PortalViewerState.current(client);
        PortalShellState shell = PortalSceneStore.getShell(PortalStreamKind.SOTO, tardisId);
        SotoGhostExterior ghost = SotoGhostExterior.get(PortalStreamKind.SOTO, tardisId);
        if (viewer == null || shell == null || ghost == null) {
            return fixed;
        }
        return PortalViewerCamera.map(
                viewer.position(), viewer.yaw(), viewer.pitch(), viewer.viewRotation(),
                interiorDoor, exteriorDoorFrame(shell), ghost.footprintOrigin());
    }

    @Override
    public Vector4f clipPlaneView(PortalCameraTransform.Result camera) {
        PortalShellState shell = PortalSceneStore.getShell(PortalStreamKind.SOTO, tardisId);
        if (interiorDoor == null || camera == null || shell == null) {
            return null;
        }
        return PortalViewerCamera.clipPlaneInView(camera.viewMatrix(), exteriorDoorFrame(shell));
    }

    /** Door hitch: the whole camera in hitch mode; the stable cull/batch camera in parallax mode. */
    private PortalCameraTransform.Result fixedHitch() {
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
        // Batches are rebuilt whenever the cull camera changes; keep it fixed so parallax only moves the view.
        PortalCameraTransform.Result fixed = fixedHitch();
        PortalCameraTransform.Result cull = fixed != null ? fixed : hitch;
        PoseStack sceneMatrices = context.sceneMatrices();

        PortalAtmosphere portal = PortalSceneStore.getAtmosphere(PortalStreamKind.SOTO, id);
        SotoAtmosphere atmosphere = portal != null ? SotoAtmosphere.fromPortal(portal) : SotoAtmosphere.DEFAULT;

        long skyStart = PortalPerfStats.begin();
        SotoGhostExterior skyGhost = SotoGhostExterior.get(PortalStreamKind.SOTO, id);
        SotoSkyFogRenderer.renderPortalSky(
                context, atmosphere, skyGhost != null ? skyGhost.footprintOrigin() : null);
        context.bindTarget();
        GpuBufferSlice previousFog = SotoSkyFogRenderer.applyPortalTerrainFog(atmosphere);
        PortalPerfStats.end(PortalPerfStats.Stage.SKY_FOG, skyStart);
        PortalLightmap portalLightmap = PortalLightmap.apply(Minecraft.getInstance(), atmosphere);
        try {
            long opaqueStart = PortalPerfStats.begin();
            SotoGhostMeshCache.drawLayer(
                    PortalStreamKind.SOTO,
                    id,
                    hitch.viewMatrix(),
                    SotoGhostMeshCache.TerrainPass.OPAQUE,
                    cull
            );
            PortalPerfStats.end(PortalPerfStats.Stage.TERRAIN_OPAQUE, opaqueStart);

            long cutoutStart = PortalPerfStats.begin();
            SotoGhostMeshCache.drawLayer(
                    PortalStreamKind.SOTO,
                    id,
                    hitch.viewMatrix(),
                    SotoGhostMeshCache.TerrainPass.CUTOUT,
                    cull
            );
            PortalPerfStats.end(PortalPerfStats.Stage.TERRAIN_CUTOUT, cutoutStart);

            long translucentStart = PortalPerfStats.begin();
            SotoGhostMeshCache.drawLayer(
                    PortalStreamKind.SOTO,
                    id,
                    hitch.viewMatrix(),
                    SotoGhostMeshCache.TerrainPass.TRANSLUCENT,
                    cull
            );
            PortalPerfStats.end(PortalPerfStats.Stage.TERRAIN_TRANSLUCENT, translucentStart);

            long featuresStart = PortalPerfStats.begin();
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
                                -1,
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
            } finally {
                PortalPerfStats.end(PortalPerfStats.Stage.GHOST_FEATURES, featuresStart);
            }
        } finally {
            SotoSkyFogRenderer.restoreFog(previousFog);
            if (portalLightmap != null) {
                portalLightmap.close();
            }
        }
    }
}
