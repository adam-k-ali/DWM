package com.adamkali.dwm.render.soto.portal;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogParameters;

/**
 * Owns the full-window color/depth target used by the SOTO portal pass.
 */
public final class SotoPortalRenderTarget implements AutoCloseable {
    private static final SotoPortalRenderTarget INSTANCE = new SotoPortalRenderTarget();

    /**
     * While true, {@link com.adamkali.dwm.mixin.client.FramebufferMixin} redirects the client
     * main framebuffer's {@code bindWrite} to this portal target. RenderLayer MAIN_TARGET
     * otherwise steals the draw buffer mid-pass.
     */
    private static boolean portalPassActive;
    private static boolean redirectingMainWrite;

    private final Map<UUID, Long> renderedFrameByTardis = new HashMap<>();
    private TextureTarget framebuffer;
    private int width;
    private int height;
    private long clientFrame;

    private SotoPortalRenderTarget() {
    }

    public static SotoPortalRenderTarget getInstance() {
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
     * Called when vanilla code (e.g. {@code RenderPhase.MAIN_TARGET}) tries to bind the main
     * framebuffer during the portal pass. Binds the portal target instead.
     */
    public static void redirectMainBeginWrite(boolean setViewport) {
        if (!INSTANCE.isReady() || INSTANCE.framebuffer == null) {
            return;
        }
        redirectingMainWrite = true;
        try {
            INSTANCE.framebuffer.bindWrite(setViewport);
        } finally {
            redirectingMainWrite = false;
        }
    }

    public static boolean isRedirectingMainWrite() {
        return redirectingMainWrite;
    }

    /**
     * Called once at the beginning of each world frame.
     */
    public static void beginClientFrame() {
        INSTANCE.clientFrame++;
        if (INSTANCE.renderedFrameByTardis.size() > 64) {
            INSTANCE.renderedFrameByTardis.entrySet().removeIf(entry -> entry.getValue() != INSTANCE.clientFrame);
        }
    }

    public static void closeGlobal() {
        INSTANCE.close();
    }

    public boolean ensureReady(Minecraft client) {
        if (client == null || !RenderSystem.isOnRenderThread()) {
            return false;
        }
        RenderTarget main = client.getMainRenderTarget();
        if (main == null || main.width <= 0 || main.height <= 0) {
            return false;
        }
        int requiredWidth = main.width;
        int requiredHeight = main.height;
        try {
            if (framebuffer == null) {
                framebuffer = new TextureTarget(requiredWidth, requiredHeight, true);
                framebuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                width = requiredWidth;
                height = requiredHeight;
            } else if (width != requiredWidth || height != requiredHeight) {
                framebuffer.resize(requiredWidth, requiredHeight);
                width = requiredWidth;
                height = requiredHeight;
                renderedFrameByTardis.clear();
            }
            return framebuffer.frameBufferId > 0
                    && framebuffer.getColorTextureId() > 0
                    && framebuffer.getDepthTextureId() > 0;
        } catch (Throwable failure) {
            close();
            return false;
        }
    }

    public boolean isReady() {
        return framebuffer != null
                && framebuffer.frameBufferId > 0
                && framebuffer.getColorTextureId() > 0
                && framebuffer.getDepthTextureId() > 0;
    }

    public boolean shouldRenderThisFrame(UUID tardisId) {
        if (tardisId == null) {
            return false;
        }
        Long renderedFrame = renderedFrameByTardis.get(tardisId);
        if (renderedFrame != null && renderedFrame == clientFrame) {
            return false;
        }
        renderedFrameByTardis.put(tardisId, clientFrame);
        return true;
    }

    public void bindAndClear() {
        bindAndClear(0.0f, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Clears the portal target to {@code rgba} then leaves it bound for writing.
     * {@code Framebuffer.clear()} ends with {@code endWrite()} (FBO 0); we re-bind afterward.
     */
    public void bindAndClear(float r, float g, float b, float a) {
        if (!isReady()) {
            throw new IllegalStateException("SOTO portal framebuffer is not ready");
        }
        framebuffer.setClearColor(r, g, b, a);
        framebuffer.bindWrite(true);
        // clear() ends with endWrite() (FBO 0). Re-bind so the portal pass stays offscreen.
        framebuffer.clear();
        framebuffer.bindWrite(true);
    }

    /**
     * Re-bind the portal color/depth target after vanilla draws that may restore the main FBO.
     */
    public void bindForWrite() {
        if (!isReady()) {
            throw new IllegalStateException("SOTO portal framebuffer is not ready");
        }
        framebuffer.bindWrite(true);
    }

    public int colorTextureId() {
        return isReady() ? framebuffer.getColorTextureId() : -1;
    }

    int framebufferFbo() {
        return framebuffer == null ? -1 : framebuffer.frameBufferId;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    @Override
    public void close() {
        renderedFrameByTardis.clear();
        if (framebuffer != null) {
            framebuffer.destroyBuffers();
            framebuffer = null;
        }
        width = 0;
        height = 0;
    }

    long clientFrameForTest() {
        return clientFrame;
    }

    /**
     * Captures mutable render and GL state around the offscreen pass.
     */
    public static final class RenderStateGuard implements AutoCloseable {
        private final int drawFramebuffer;
        private final int readFramebuffer;
        private final int[] viewport;
        private final boolean depthEnabled;
        private final boolean cullEnabled;
        private final boolean blendEnabled;
        private final boolean stencilEnabled;
        private final boolean depthMask;
        private final boolean[] colorMask;
        private final int depthFunc;
        private final int cullFace;
        private final int blendSrcRgb;
        private final int blendDstRgb;
        private final int blendSrcAlpha;
        private final int blendDstAlpha;
        private final int blendEquationRgb;
        private final int blendEquationAlpha;
        private final int stencilFunc;
        private final int stencilRef;
        private final int stencilValueMask;
        private final int stencilWriteMask;
        private final int stencilFail;
        private final int stencilDepthFail;
        private final int stencilDepthPass;
        private final double depthNear;
        private final double depthFar;
        private final int activeTexture;
        private final int boundTexture2d;
        private final int shaderTexture0;
        private final Matrix4f projection;
        private final ProjectionType projectionType;
        private final Matrix4f modelView;
        private final FogParameters fog;
        private final float[] shaderColor;
        private boolean closed;

        private RenderStateGuard() {
            drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
            readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);

            IntBuffer viewportBuffer = BufferUtils.createIntBuffer(4);
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewportBuffer);
            viewport = new int[]{
                    viewportBuffer.get(0),
                    viewportBuffer.get(1),
                    viewportBuffer.get(2),
                    viewportBuffer.get(3)
            };

            depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
            blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
            stencilEnabled = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
            depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
            ByteBuffer colorMaskBuffer = BufferUtils.createByteBuffer(4);
            GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, colorMaskBuffer);
            colorMask = new boolean[]{
                    colorMaskBuffer.get(0) != 0,
                    colorMaskBuffer.get(1) != 0,
                    colorMaskBuffer.get(2) != 0,
                    colorMaskBuffer.get(3) != 0
            };
            depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
            cullFace = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
            blendSrcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
            blendDstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
            blendSrcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
            blendDstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
            blendEquationRgb = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB);
            blendEquationAlpha = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA);
            stencilFunc = GL11.glGetInteger(GL11.GL_STENCIL_FUNC);
            stencilRef = GL11.glGetInteger(GL11.GL_STENCIL_REF);
            stencilValueMask = GL11.glGetInteger(GL11.GL_STENCIL_VALUE_MASK);
            stencilWriteMask = GL11.glGetInteger(GL11.GL_STENCIL_WRITEMASK);
            stencilFail = GL11.glGetInteger(GL11.GL_STENCIL_FAIL);
            stencilDepthFail = GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_FAIL);
            stencilDepthPass = GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_PASS);
            DoubleBuffer depthRange = BufferUtils.createDoubleBuffer(2);
            GL11.glGetDoublev(GL11.GL_DEPTH_RANGE, depthRange);
            depthNear = depthRange.get(0);
            depthFar = depthRange.get(1);
            activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
            boundTexture2d = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            shaderTexture0 = RenderSystem.getShaderTexture(0);

            projection = new Matrix4f(RenderSystem.getProjectionMatrix());
            projectionType = RenderSystem.getProjectionType();
            modelView = new Matrix4f(RenderSystem.getModelViewMatrix());
            fog = RenderSystem.getShaderFog();
            shaderColor = RenderSystem.getShaderColor().clone();
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

            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
            RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            RenderSystem.setProjectionMatrix(projection, projectionType);
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.set(modelView);
            RenderSystem.setShaderFog(fog);
            RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3]);

            if (depthEnabled) {
                RenderSystem.enableDepthTest();
            } else {
                RenderSystem.disableDepthTest();
            }
            if (cullEnabled) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
            if (blendEnabled) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }
            setEnabled(GL11.GL_STENCIL_TEST, stencilEnabled);
            RenderSystem.depthMask(depthMask);
            RenderSystem.colorMask(colorMask[0], colorMask[1], colorMask[2], colorMask[3]);
            RenderSystem.depthFunc(depthFunc);
            GL11.glCullFace(cullFace);
            RenderSystem.blendFuncSeparate(blendSrcRgb, blendDstRgb, blendSrcAlpha, blendDstAlpha);
            GL20.glBlendEquationSeparate(blendEquationRgb, blendEquationAlpha);
            RenderSystem.stencilFunc(stencilFunc, stencilRef, stencilValueMask);
            RenderSystem.stencilMask(stencilWriteMask);
            RenderSystem.stencilOp(stencilFail, stencilDepthFail, stencilDepthPass);
            GL11.glDepthRange(depthNear, depthFar);

            RenderSystem.activeTexture(activeTexture);
            RenderSystem.bindTexture(boundTexture2d);
            RenderSystem.setShaderTexture(0, shaderTexture0);
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
