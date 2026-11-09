package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.render.portal.PortalCameraTransform;
import com.adamkali.dwm.render.portal.PortalContent;
import com.adamkali.dwm.render.portal.PortalContentContext;
import com.adamkali.dwm.render.portal.PortalFeatureFlush;
import com.adamkali.dwm.render.portal.PortalPerfStats;
import com.adamkali.dwm.render.portal.PortalDoorFrame;
import com.adamkali.dwm.render.portal.PortalSceneStore;
import com.adamkali.dwm.render.portal.PortalViewerCamera;
import com.adamkali.dwm.render.portal.PortalViewerState;
import com.adamkali.dwm.render.TardisInteriorDoorBlockEntityRenderer;
import com.adamkali.dwm.tardis.data.model.PortalAperture;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.render.soto.SotoExteriorMeshCache;
import com.adamkali.dwm.render.soto.SotoSkyFogRenderer;
import com.adamkali.dwm.render.soto.ghost.SotoGhostMeshCache;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalSampler;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * BOTI portal content: hitch-fixed interior look-in from synced ghost terrain
 * ({@link SotoGhostMeshCache}). Ready only once real streamed meshes exist — approach preload
 * warms the stream before the door opens.
 */
public final class BotiPortalContent implements PortalContent {
    private static final int DEFAULT_CLEAR_RGB = 0x203040;

    private static PortalDoorFrame interiorDoorFrame;

    private final UUID tardisId;
    /** Parallax: exterior doorway in world space; {@code null} keeps the fixed door hitch. */
    private final PortalDoorFrame exteriorDoor;

    public BotiPortalContent(UUID tardisId) {
        this(tardisId, null);
    }

    public BotiPortalContent(UUID tardisId, PortalDoorFrame exteriorDoor) {
        this.tardisId = Objects.requireNonNull(tardisId, "tardisId");
        this.exteriorDoor = exteriorDoor;
    }

    /**
     * Room-facing aperture of the shipped interior door bank in plot-local coords, built with the
     * interior door BER's own transforms so the parallax mapping lines up with the real doorway.
     */
    static PortalDoorFrame interiorDoorFrame() {
        if (interiorDoorFrame == null) {
            Direction intoRoom = Direction.SOUTH;
            PoseStack pose = new PoseStack();
            BlockPos origin = FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN;
            pose.translate(origin.getX(), origin.getY(), origin.getZ());
            TardisInteriorDoorBlockEntityRenderer.applyTransforms(pose, intoRoom);
            interiorDoorFrame = PortalDoorFrame.fromAperture(
                    pose.last().pose(),
                    PortalAperture.CLASSIC_INTERIOR_DOORS,
                    Vec3.ZERO,
                    new Vec3(intoRoom.getStepX(), 0.0, intoRoom.getStepZ())
            );
        }
        return interiorDoorFrame;
    }

    @Override
    public boolean isReady(Minecraft client) {
        if (client == null || client.level == null || client.gameRenderer == null) {
            return false;
        }
        PortalSceneStore.requestIfNeeded(PortalStreamKind.BOTI, tardisId);
        return SotoGhostMeshCache.hasMeshes(PortalStreamKind.BOTI, tardisId);
    }

    @Override
    public int clearRgb(Minecraft client) {
        PortalAtmosphere atmosphere = PortalSceneStore.getAtmosphere(PortalStreamKind.BOTI, tardisId);
        if (atmosphere != null) {
            SotoAtmosphere soto = SotoAtmosphere.fromPortal(atmosphere);
            return SotoSkyFogRenderer.portalBackdropRgb(soto);
        }
        return DEFAULT_CLEAR_RGB;
    }

    @Override
    public PortalCameraTransform.Result hitch(Minecraft client) {
        if (exteriorDoor != null) {
            PortalViewerState.Viewer viewer = PortalViewerState.current(client);
            if (viewer != null) {
                return PortalViewerCamera.map(
                        viewer.position(), viewer.yaw(), viewer.pitch(), viewer.viewRotation(),
                        exteriorDoor, interiorDoorFrame(), BlockPos.ZERO);
            }
        }
        return fixedHitch();
    }

    @Override
    public Vector4f clipPlaneView(PortalCameraTransform.Result camera) {
        if (exteriorDoor == null || camera == null) {
            return null;
        }
        return PortalViewerCamera.clipPlaneInView(camera.viewMatrix(), interiorDoorFrame());
    }

    /** Door hitch: the whole camera in hitch mode; the stable cull/batch camera in parallax mode. */
    private static PortalCameraTransform.Result fixedHitch() {
        Vec3 eye = new Vec3(
                TardisBotiRenderer.INTERIOR_DOOR_CENTER_X,
                TardisBotiRenderer.INTERIOR_DOOR_CENTER_Y,
                TardisBotiRenderer.INTERIOR_DOOR_PLANE_Z
        );
        return PortalCameraTransform.fromLookDirection(eye, Direction.SOUTH, 0.0f);
    }

    @Override
    public void renderInto(PortalContentContext context) {
        PortalAtmosphere portal = PortalSceneStore.getAtmosphere(PortalStreamKind.BOTI, tardisId);
        SotoAtmosphere atmosphere = portal != null ? SotoAtmosphere.fromPortal(portal) : SotoAtmosphere.DEFAULT;
        context.bindTarget();
        long fogStart = PortalPerfStats.begin();
        int radius = BotiInteriorSampler.clipStreamRadiusChunks(SotoSkyFogRenderer.clientStreamRadiusChunks());
        GpuBufferSlice previousFog = SotoSkyFogRenderer.applyPortalTerrainFog(
                atmosphere, PortalSampler.fogStartBlocks(radius), PortalSampler.fogEndBlocks(radius)
        );
        PortalPerfStats.end(PortalPerfStats.Stage.SKY_FOG, fogStart);
        try {
            renderLitContent(context);
        } finally {
            SotoSkyFogRenderer.restoreFog(previousFog);
        }
    }

    private void renderLitContent(PortalContentContext context) {
        UUID id = tardisId;
        float tickDelta = context.tickDelta();
        PortalCameraTransform.Result hitch = context.hitch();
        // Batches are rebuilt whenever the cull camera changes; keep it fixed so parallax only moves the view.
        PortalCameraTransform.Result cull = fixedHitch();
        PoseStack sceneMatrices = context.sceneMatrices();

        long opaqueStart = PortalPerfStats.begin();
        SotoGhostMeshCache.drawLayer(
                PortalStreamKind.BOTI,
                id,
                hitch.viewMatrix(),
                SotoGhostMeshCache.TerrainPass.OPAQUE,
                cull
        );
        PortalPerfStats.end(PortalPerfStats.Stage.TERRAIN_OPAQUE, opaqueStart);

        long cutoutStart = PortalPerfStats.begin();
        SotoGhostMeshCache.drawLayer(
                PortalStreamKind.BOTI,
                id,
                hitch.viewMatrix(),
                SotoGhostMeshCache.TerrainPass.CUTOUT,
                cull
        );
        PortalPerfStats.end(PortalPerfStats.Stage.TERRAIN_CUTOUT, cutoutStart);

        long translucentStart = PortalPerfStats.begin();
        SotoGhostMeshCache.drawLayer(
                PortalStreamKind.BOTI,
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
                            PortalStreamKind.BOTI,
                            id,
                            context.portalCamera()
                    );
                    SotoExteriorMeshCache.renderGhostEntities(
                            sceneMatrices,
                            submitStorage,
                            cameraState,
                            tickDelta,
                            PortalStreamKind.BOTI,
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
    }
}
