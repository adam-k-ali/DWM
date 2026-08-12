package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.render.portal.PortalCameraTransform;
import com.adamkali.dwm.render.portal.PortalContent;
import com.adamkali.dwm.render.portal.PortalContentContext;
import com.adamkali.dwm.render.portal.PortalFeatureFlush;
import com.adamkali.dwm.render.portal.PortalSceneStore;
import com.adamkali.dwm.render.soto.SotoExteriorMeshCache;
import com.adamkali.dwm.render.soto.SotoSkyFogRenderer;
import com.adamkali.dwm.render.soto.ghost.SotoGhostMeshCache;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4fStack;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

/**
 * BOTI portal content: hitch-fixed interior look-in with synced ghost terrain (preferred)
 * or blueprint console room fallback from {@link BotiInteriorMeshCache}.
 */
public final class BotiPortalContent implements PortalContent {
    private static final int FULLBRIGHT = LightCoordsUtil.pack(15, 15);
    private static final int DEFAULT_CLEAR_RGB = 0x203040;

    private final UUID tardisId;

    public BotiPortalContent(UUID tardisId) {
        this.tardisId = Objects.requireNonNull(tardisId, "tardisId");
    }

    @Override
    public boolean isReady(Minecraft client) {
        if (client == null || client.level == null || client.gameRenderer == null) {
            return false;
        }
        PortalSceneStore.requestIfNeeded(PortalStreamKind.BOTI, tardisId);
        // Same spirit as SOTO: skip the full-window FBO until ghost meshes exist.
        // Blueprint fallback remains available as a BER placeholder when not ready.
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
        Vec3 eye = new Vec3(
                TardisBotiRenderer.INTERIOR_DOOR_CENTER_X,
                TardisBotiRenderer.INTERIOR_DOOR_CENTER_Y,
                TardisBotiRenderer.INTERIOR_DOOR_PLANE_Z
        );
        return PortalCameraTransform.fromLookDirection(eye, Direction.SOUTH, 0.0f);
    }

    @Override
    public void renderInto(PortalContentContext context) {
        if (SotoGhostMeshCache.hasMeshes(PortalStreamKind.BOTI, tardisId)) {
            renderGhostInto(context);
        } else {
            renderBlueprintFallback(context);
        }
    }

    private void renderGhostInto(PortalContentContext context) {
        UUID id = tardisId;
        float tickDelta = context.tickDelta();
        PortalCameraTransform.Result hitch = context.hitch();
        PoseStack sceneMatrices = context.sceneMatrices();

        context.bindTarget();
        SotoGhostMeshCache.drawLayer(
                PortalStreamKind.BOTI,
                id,
                hitch.viewMatrix(),
                SotoGhostMeshCache.TerrainPass.OPAQUE,
                hitch
        );
        SotoGhostMeshCache.drawLayer(
                PortalStreamKind.BOTI,
                id,
                hitch.viewMatrix(),
                SotoGhostMeshCache.TerrainPass.CUTOUT,
                hitch
        );
        SotoGhostMeshCache.drawLayer(
                PortalStreamKind.BOTI,
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
        }
    }

    private void renderBlueprintFallback(PortalContentContext context) {
        PortalFeatureFlush featureFlush = context.featureFlush();
        if (featureFlush == null) {
            return;
        }
        PoseStack sceneMatrices = context.sceneMatrices();
        SubmitNodeStorage submitStorage = context.submitStorage();
        CameraRenderState cameraState = context.cameraState();
        Matrix4fStack featureModelView = RenderSystem.getModelViewStack();
        featureModelView.pushMatrix();
        try {
            context.portalCamera().getViewRotationMatrix(featureModelView);
            BotiInteriorMeshCache.renderForPortal(
                    sceneMatrices,
                    submitStorage,
                    cameraState,
                    context.portalCamera(),
                    FULLBRIGHT,
                    context.tickDelta(),
                    tardisId
            );
            context.bindTarget();
            featureFlush.renderAllFeatures(submitStorage);
            context.bindTarget();
        } finally {
            featureModelView.popMatrix();
        }
    }
}
