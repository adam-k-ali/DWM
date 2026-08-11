package com.adamkali.dwm.render.portal;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;

/**
 * Renders a {@link PortalScene} into the managed full-window portal target.
 * <p>
 * Mid-BER GPU work blacks out the world on 26.2. Call {@link #renderOffMainPass} from
 * {@code LevelRenderEvents.END_MAIN} only; BER uses {@link #peekLastRendered}.
 */
public final class PortalRenderer {
    private final PortalRenderTarget target;
    private final PortalCamera portalCamera = new PortalCamera();
    private final Set<PortalKey> renderedReady = new HashSet<>();

    public PortalRenderer() {
        this(PortalRenderTarget.getInstance());
    }

    PortalRenderer(PortalRenderTarget target) {
        this.target = target;
    }

    /**
     * Safe for BER: returns last END_MAIN-rendered portal texture without touching GPU state.
     */
    public PortalTexture peekLastRendered(PortalKey key) {
        if (key == null || !renderedReady.contains(key) || !target.isReady()) {
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
    public PortalTexture renderOffMainPass(PortalScene scene) {
        if (scene == null) {
            return PortalTexture.UNAVAILABLE;
        }
        if (!PortalSupport.isAvailable()) {
            return PortalTexture.UNAVAILABLE;
        }
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.level == null || client.gameRenderer == null) {
            return PortalTexture.UNAVAILABLE;
        }
        PortalContent content = scene.content();
        if (!content.isReady(client)) {
            renderedReady.remove(scene.key());
            return PortalTexture.UNAVAILABLE;
        }
        PortalCameraTransform.Result hitch = content.hitch(client);
        if (hitch == null) {
            renderedReady.remove(scene.key());
            return PortalTexture.UNAVAILABLE;
        }

        try (PortalRenderTarget.RenderStateGuard ignored =
                     PortalRenderTarget.RenderStateGuard.capture()) {
            if (!target.ensureReady(client)) {
                PortalSupport.disableForSession("Portal framebuffer is incomplete", null);
                return PortalTexture.UNAVAILABLE;
            }
            if (!target.shouldRenderThisFrame(scene.key())) {
                return peekLastRendered(scene.key());
            }
            renderScene(client, scene, hitch, content.clearRgb(client));
            renderedReady.add(scene.key());
            int colorTex = target.colorTextureId();
            return new PortalTexture(colorTex, target.width(), target.height(), hitch, true);
        }
    }

    private void renderScene(
            Minecraft client,
            PortalScene scene,
            PortalCameraTransform.Result hitch,
            int clearRgb
    ) {
        PortalRenderTarget.beginPortalPass();
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

            portalCamera.apply(hitch);
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.set(hitch.viewMatrix());

            PoseStack sceneMatrices = new PoseStack();
            EntityRenderDispatcher entityDispatcher = client.getEntityRenderDispatcher();
            BlockEntityRenderDispatcher blockEntityDispatcher = client.getBlockEntityRenderDispatcher();
            try {
                target.bindForWrite();
                PortalFeatureFlush featureFlush = PortalFeatureFlush.get(client);
                SubmitNodeStorage submitStorage = new SubmitNodeStorage();
                CameraRenderState cameraState = createPortalCameraState(hitch);
                PortalContentContext context = new PortalContentContext(
                        client,
                        scene.tickDelta(),
                        hitch,
                        portalCamera,
                        sceneMatrices,
                        submitStorage,
                        cameraState,
                        target,
                        featureFlush
                );
                scene.content().renderInto(context);
            } finally {
                if (client.level != null) {
                    entityDispatcher.prepare(client.gameRenderer.mainCamera(), client.player);
                    blockEntityDispatcher.prepare(client.gameRenderer.mainCamera().position());
                }
                modelViewStack.popMatrix();
            }
        } finally {
            PortalRenderTarget.endPortalPass();
        }
    }

    private CameraRenderState createPortalCameraState(PortalCameraTransform.Result hitch) {
        CameraRenderState cameraState = new CameraRenderState();
        cameraState.initialized = true;
        cameraState.pos = hitch.eyeRelative();
        cameraState.blockPos = BlockPos.containing(cameraState.pos);
        cameraState.yRot = hitch.yaw();
        cameraState.xRot = hitch.pitch();
        cameraState.orientation.set(portalCamera.rotation());
        return cameraState;
    }

    public record PortalTexture(
            int textureId,
            int width,
            int height,
            PortalCameraTransform.Result camera,
            boolean available
    ) {
        static final PortalTexture UNAVAILABLE = new PortalTexture(-1, 0, 0, null, false);
    }

    private static final class PortalCamera extends Camera {
        void apply(PortalCameraTransform.Result result) {
            setPosition(result.eyeRelative());
            setRotation(result.yaw(), result.pitch());
        }
    }
}
