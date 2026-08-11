package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.render.portal.PortalCameraTransform;
import com.adamkali.dwm.render.portal.PortalContent;
import com.adamkali.dwm.render.portal.PortalContentContext;
import com.adamkali.dwm.render.portal.PortalFeatureFlush;
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
 * BOTI portal content: hitch-fixed interior look-in with synced/blueprint console room.
 */
public final class BotiPortalContent implements PortalContent {
    private static final int FULLBRIGHT = LightCoordsUtil.pack(15, 15);
    /** Solid dark backdrop (no exterior sky/fog). */
    private static final int CLEAR_RGB = 0x203040;

    private final UUID tardisId;

    public BotiPortalContent(UUID tardisId) {
        this.tardisId = Objects.requireNonNull(tardisId, "tardisId");
    }

    @Override
    public boolean isReady(Minecraft client) {
        // Blueprint fallback always supplies geometry; synced snapshot preferred when present.
        return client != null && client.level != null && client.gameRenderer != null;
    }

    @Override
    public int clearRgb(Minecraft client) {
        return CLEAR_RGB;
    }

    @Override
    public PortalCameraTransform.Result hitch(Minecraft client) {
        Vec3 eye = new Vec3(
                TardisBotiRenderer.INTERIOR_DOOR_CENTER_X,
                TardisBotiRenderer.INTERIOR_DOOR_CENTER_Y,
                TardisBotiRenderer.INTERIOR_DOOR_PLANE_Z
        );
        // Look into the console room along +Z (door plane at z=0).
        return PortalCameraTransform.fromLookDirection(eye, Direction.SOUTH, 0.0f);
    }

    @Override
    public void renderInto(PortalContentContext context) {
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
