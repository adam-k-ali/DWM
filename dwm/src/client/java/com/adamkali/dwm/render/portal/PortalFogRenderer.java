package com.adamkali.dwm.render.portal;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;

/**
 * Owns the fog UBO used by deferred portal passes. The vanilla world fog buffer cannot be
 * overwritten because the main scene may still reference it.
 */
public final class PortalFogRenderer {
    private static FogRenderer renderer;

    private PortalFogRenderer() {
    }

    public static GpuBufferSlice apply(float start, float end, float red, float green, float blue) {
        GpuBufferSlice previous = RenderSystem.getShaderFog();
        FogRenderer fogRenderer = renderer();
        fogRenderer.updateBuffer(buildFogData(start, end, red, green, blue));
        GpuBufferSlice applied = fogRenderer.getBuffer(FogRenderer.FogMode.WORLD);
        // Draws recorded with this slice may still be pending when the next apply() runs (sky/cloud fog,
        // then terrain fog, within one portal frame). Rotate so the next apply writes a different ring
        // slot (fence-guarded) instead of overwriting the UBO those draws are about to read.
        fogRenderer.endFrame();
        RenderSystem.setShaderFog(applied);
        return previous;
    }

    static FogData buildFogData(float start, float end, float red, float green, float blue) {
        if (start < 0.0F || end <= start) {
            throw new IllegalArgumentException("Portal fog end must be greater than its non-negative start");
        }
        FogData fog = new FogData();
        fog.color = new Vector4f(red, green, blue, 1.0F);
        fog.environmentalStart = start;
        fog.environmentalEnd = end;
        fog.renderDistanceStart = start;
        fog.renderDistanceEnd = end;
        fog.skyEnd = end;
        fog.cloudEnd = end;
        return fog;
    }

    public static void restore(GpuBufferSlice previous) {
        if (previous != null) {
            RenderSystem.setShaderFog(previous);
        }
    }

    public static void endFrame() {
        if (renderer != null) {
            renderer.endFrame();
        }
    }

    public static void closeGlobal() {
        if (renderer != null) {
            renderer.close();
            renderer = null;
        }
    }

    private static FogRenderer renderer() {
        if (renderer == null) {
            renderer = new FogRenderer();
        }
        return renderer;
    }
}
