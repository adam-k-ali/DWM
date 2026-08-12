package com.adamkali.dwm.render.portal;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;

/**
 * Owns the full-window color/depth target used by the portal pass.
 * <p>
 * Minecraft 26.2 removed {@code RenderTarget.bindWrite}/{@code clear}/{@code getColorTextureId}.
 * Portal writes are routed by setting {@link RenderSystem#outputColorTextureOverride} and
 * {@link RenderSystem#outputDepthTextureOverride} for the duration of the offscreen pass.
 * Shared single FBO: last END_MAIN writer wins when multiple portal keys render in one frame.
 */
public final class PortalRenderTarget implements AutoCloseable {
    private static final PortalRenderTarget INSTANCE = new PortalRenderTarget();
    private static final String LABEL = "dwm_portal";

    private static boolean portalPassActive;
    private static boolean redirectingMainWrite;

    private final Map<PortalKey, Long> renderedFrameByKey = new HashMap<>();
    private TextureTarget framebuffer;
    private int width;
    private int height;
    private long clientFrame;

    private PortalRenderTarget() {
    }

    public static PortalRenderTarget getInstance() {
        return INSTANCE;
    }

    public static boolean isPortalPassActive() {
        return portalPassActive;
    }

    public static void beginPortalPass() {
        portalPassActive = true;
    }

    public static void endPortalPass() {
        portalPassActive = false;
        redirectingMainWrite = false;
        clearOutputOverrides();
    }

    /**
     * Re-applies portal output overrides when vanilla code would otherwise bind the main target.
     * Kept for mixin/call-site compatibility; 26.2 no longer has {@code bindWrite}.
     */
    public static void redirectMainBeginWrite(boolean setViewport) {
        if (!INSTANCE.isReady() || INSTANCE.framebuffer == null) {
            return;
        }
        redirectingMainWrite = true;
        try {
            INSTANCE.bindForWrite();
            if (setViewport) {
                GL11.glViewport(0, 0, INSTANCE.width, INSTANCE.height);
            }
        } finally {
            redirectingMainWrite = false;
        }
    }

    public static boolean isRedirectingMainWrite() {
        return redirectingMainWrite;
    }

    public static void beginClientFrame() {
        INSTANCE.clientFrame++;
        if (INSTANCE.renderedFrameByKey.size() > 64) {
            INSTANCE.renderedFrameByKey.entrySet().removeIf(entry -> entry.getValue() != INSTANCE.clientFrame);
        }
    }

    public static void closeGlobal() {
        INSTANCE.close();
    }

    public boolean ensureReady(Minecraft client) {
        if (client == null || !RenderSystem.isOnRenderThread()) {
            return false;
        }
        RenderTarget main = client.gameRenderer.mainRenderTarget();
        if (main == null || main.width <= 0 || main.height <= 0) {
            return false;
        }
        int requiredWidth = main.width;
        int requiredHeight = main.height;
        try {
            if (framebuffer == null) {
                framebuffer = new TextureTarget(
                        LABEL,
                        requiredWidth,
                        requiredHeight,
                        true,
                        GpuFormat.RGBA8_UNORM
                );
                width = requiredWidth;
                height = requiredHeight;
            } else if (width != requiredWidth || height != requiredHeight) {
                framebuffer.resize(requiredWidth, requiredHeight);
                width = requiredWidth;
                height = requiredHeight;
                renderedFrameByKey.clear();
                PortalFrameCache.invalidateForResize();
            }
            return isReady();
        } catch (Throwable failure) {
            close();
            return false;
        }
    }

    public boolean isReady() {
        return framebuffer != null
                && framebuffer.getColorTexture() != null
                && framebuffer.getDepthTexture() != null
                && !framebuffer.getColorTexture().isClosed()
                && !framebuffer.getDepthTexture().isClosed();
    }

    public boolean shouldRenderThisFrame(PortalKey key) {
        if (key == null) {
            return false;
        }
        Long renderedFrame = renderedFrameByKey.get(key);
        if (renderedFrame != null && renderedFrame == clientFrame) {
            return false;
        }
        renderedFrameByKey.put(key, clientFrame);
        return true;
    }

    public void bindAndClear() {
        bindAndClear(0.0f, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Clears the portal target to {@code rgba} then leaves output overrides pointing at it.
     * <p>
     * Prefer {@link #clearViaRenderPass} at END_MAIN — vanilla
     * {@link CommandEncoder#clearColorAndDepthTextures} ends by binding GL framebuffer 0.
     */
    public void bindAndClear(float r, float g, float b, float a) {
        clearOnly(r, g, b, a);
        bindForWrite();
        GL11.glViewport(0, 0, width, height);
    }

    /**
     * Clears portal color/depth via a dedicated RenderPass load-op (no FBO-0 rebind from
     * {@code clearColorAndDepthTextures}). Leaves output overrides pointing at the portal.
     */
    public void clearViaRenderPass(float r, float g, float b, float a) {
        if (!isReady()) {
            throw new IllegalStateException("portal framebuffer is not ready");
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (var ignored = encoder.createRenderPass(
                () -> "dwm_portal_clear",
                framebuffer.getColorTextureView(),
                java.util.Optional.of(new Vector4f(r, g, b, a)),
                framebuffer.getDepthTextureView(),
                java.util.OptionalDouble.of(RenderSystem.DEFAULT_DEPTH_CLEAR_VALUE)
        )) {
            // Load-op clear only.
        }
        bindForWrite();
        GL11.glViewport(0, 0, width, height);
    }

    /**
     * Clears portal color/depth without changing output overrides. Still leaves GL FBO at 0
     * (vanilla clear behavior) — avoid mid-frame; prefer {@link #clearViaRenderPass}.
     */
    public void clearOnly(float r, float g, float b, float a) {
        if (!isReady()) {
            throw new IllegalStateException("portal framebuffer is not ready");
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.clearColorAndDepthTextures(
                framebuffer.getColorTexture(),
                new Vector4f(r, g, b, a),
                framebuffer.getDepthTexture(),
                RenderSystem.DEFAULT_DEPTH_CLEAR_VALUE
        );
    }

    /**
     * Point subsequent GPU draws at the portal color/depth attachments.
     */
    public void bindForWrite() {
        if (!isReady()) {
            throw new IllegalStateException("portal framebuffer is not ready");
        }
        RenderSystem.outputColorTextureOverride = framebuffer.getColorTextureView();
        RenderSystem.outputDepthTextureOverride = framebuffer.getDepthTextureView();
    }

    public int colorTextureId() {
        if (!isReady()) {
            return -1;
        }
        GpuTexture color = framebuffer.getColorTexture();
        if (color instanceof GlTexture glTexture) {
            return glTexture.glId();
        }
        // Vulkan / non-GL backends: no legacy GL texture id for composite sampling.
        return -1;
    }

    public GpuTexture colorTexture() {
        return isReady() ? framebuffer.getColorTexture() : null;
    }

    public GpuTextureView colorTextureView() {
        return isReady() ? framebuffer.getColorTextureView() : null;
    }

    public GpuTextureView depthTextureView() {
        return isReady() ? framebuffer.getDepthTextureView() : null;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    @Override
    public void close() {
        renderedFrameByKey.clear();
        clearOutputOverrides();
        if (framebuffer != null) {
            framebuffer.destroyBuffers();
            framebuffer = null;
        }
        width = 0;
        height = 0;
        PortalFrameCache.invalidateForResize();
    }

    long clientFrameForTest() {
        return clientFrame;
    }

    private static void clearOutputOverrides() {
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
    }

    /**
     * Captures mutable render and GL state around the offscreen pass.
     * <p>
     * Slimmed for 26.2: projection/modelview/shader fog are GpuBufferSlice-based and no longer
     * expose the old matrix/fog parameter APIs used by the 1.21.4 guard.
     */
    public static final class RenderStateGuard implements AutoCloseable {
        private final GpuTextureView previousColorOverride;
        private final GpuTextureView previousDepthOverride;
        private final GpuBufferSlice previousFog;
        private final boolean depthEnabled;
        private final boolean cullEnabled;
        private final boolean blendEnabled;
        private final boolean stencilEnabled;
        private final boolean depthMask;
        private final int depthFunc;
        private boolean closed;

        private RenderStateGuard() {
            previousColorOverride = RenderSystem.outputColorTextureOverride;
            previousDepthOverride = RenderSystem.outputDepthTextureOverride;
            previousFog = RenderSystem.getShaderFog();
            depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
            blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
            stencilEnabled = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
            depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
            depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        }

        public static RenderStateGuard capture() {
            RenderSystem.assertOnRenderThread();
            return new RenderStateGuard();
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            RenderSystem.outputColorTextureOverride = previousColorOverride;
            RenderSystem.outputDepthTextureOverride = previousDepthOverride;
            if (previousFog != null) {
                RenderSystem.setShaderFog(previousFog);
            }
            setEnabled(GL11.GL_DEPTH_TEST, depthEnabled);
            setEnabled(GL11.GL_CULL_FACE, cullEnabled);
            setEnabled(GL11.GL_BLEND, blendEnabled);
            setEnabled(GL11.GL_STENCIL_TEST, stencilEnabled);
            GL11.glDepthMask(depthMask);
            GL11.glDepthFunc(depthFunc);
        }

        private static void setEnabled(int capability, boolean enabled) {
            if (enabled) {
                GL11.glEnable(capability);
            } else {
                GL11.glDisable(capability);
            }
        }
    }
}
