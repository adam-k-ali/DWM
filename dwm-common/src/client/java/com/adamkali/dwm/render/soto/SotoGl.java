package com.adamkali.dwm.render.soto;

import org.lwjgl.opengl.GL11;

/**
 * OpenGL state helpers for SOTO/BOTI-style stencil compositing.
 * <p>
 * Minecraft 26.2 removed {@code RenderSystem} stencil/depth/color/cull helpers; on the default
 * OpenGL backend these map directly to {@link GL11}. Prefer this over {@code RenderSystem} so the
 * portal path stays compile-clean without depending on removed immediate-mode APIs.
 */
public final class SotoGl {
    private SotoGl() {
    }

    public static void enableDepthTest() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public static void disableDepthTest() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    public static void depthFunc(int func) {
        GL11.glDepthFunc(func);
    }

    public static void depthMask(boolean mask) {
        GL11.glDepthMask(mask);
    }

    public static void enableCull() {
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public static void disableCull() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    public static void enableBlend() {
        GL11.glEnable(GL11.GL_BLEND);
    }

    public static void disableBlend() {
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        GL11.glColorMask(red, green, blue, alpha);
    }

    public static void stencilFunc(int func, int ref, int mask) {
        GL11.glStencilFunc(func, ref, mask);
    }

    public static void stencilMask(int mask) {
        GL11.glStencilMask(mask);
    }

    public static void stencilOp(int fail, int zfail, int zpass) {
        GL11.glStencilOp(fail, zfail, zpass);
    }

    public static void clearStencil(int value) {
        GL11.glClearStencil(value);
    }
}
