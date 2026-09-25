package com.adamkali.dwm.render.portal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;

/**
 * Owns the full-window color/depth target used by the portal pass.
 * <p>
 * Minecraft 26.3 routes GPU draws through an explicit {@link RenderPass}; there is no
 * global output-texture override. Portal terrain and feature flushes open a pass against
 * {@link #colorTextureView()} / {@link #depthTextureView()} via {@link #openRenderPass}.
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
    }

    /**
     * Re-applies the portal viewport when vanilla code would otherwise bind the main target.
     * Kept for mixin/call-site compatibility; 26.3 draws go through {@link #openRenderPass}.
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
                        GpuFormat.RGBA8_UNORM,
                        GpuFormat.D32_FLOAT
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
     * Clears the portal target to {@code rgba} then leaves the GL viewport pointing at it.
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
     * {@code clearColorAndDepthTextures}). Leaves the portal viewport set for subsequent passes.
     */
    public void clearViaRenderPass(float r, float g, float b, float a) {
        if (!isReady()) {
            throw new IllegalStateException("portal framebuffer is not ready");
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (var ignored = encoder.createRenderPass(
                () -> "dwm_portal_clear",
                framebuffer.getColorTextureView(),
                Optional.of(new Vector4f(r, g, b, a)),
                framebuffer.getDepthTextureView(),
                OptionalDouble.of(RenderSystem.DEFAULT_DEPTH_CLEAR_VALUE)
        )) {
            // Load-op clear only.
        }
        bindForWrite();
        GL11.glViewport(0, 0, width, height);
    }

    /**
     * Opens a draw pass targeting this portal color/depth. Caller must close the pass.
     */
    public RenderPass openRenderPass(String label) {
        if (!isReady()) {
            throw new IllegalStateException("portal framebuffer is not ready");
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        RenderPass pass = encoder.createRenderPass(
                () -> label,
                framebuffer.getColorTextureView(),
                Optional.empty(),
                framebuffer.getDepthTextureView(),
                OptionalDouble.empty()
        );
        RenderSystem.bindDefaultUniforms(pass);
        return pass;
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
     * Sets the GL viewport to the portal size. 26.3 draws must still open a
     * {@link #openRenderPass} against this target.
     */
    public void bindForWrite() {
        if (!isReady()) {
            throw new IllegalStateException("portal framebuffer is not ready");
        }
        GL11.glViewport(0, 0, width, height);
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

    /**
     * Captures mutable render and GL state around the offscreen pass.
     * <p>
     * 26.3 has no global output-texture override; this guard restores fog and GL enable bits.
     */
    public static final class RenderStateGuard implements AutoCloseable {
        private final GpuBufferSlice previousFog;
        private final boolean depthEnabled;
        private final boolean cullEnabled;
        private final boolean blendEnabled;
        private final boolean stencilEnabled;
        private final boolean depthMask;
        private final int depthFunc;
        private boolean closed;

        private RenderStateGuard() {
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
