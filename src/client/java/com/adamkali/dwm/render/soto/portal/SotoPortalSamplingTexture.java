package com.adamkali.dwm.render.soto.portal;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

import java.util.OptionalDouble;

/**
 * Samples the SOTO portal {@link SotoPortalRenderTarget} color attachment via TextureManager.
 * Does not own the underlying GPU textures (portal target does).
 */
public final class SotoPortalSamplingTexture extends AbstractTexture {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("dwm", "soto_portal_fb");

    private static SotoPortalSamplingTexture instance;
    private static GpuSampler clampSampler;

    private SotoPortalSamplingTexture() {
    }

    public static void ensureRegistered() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getTextureManager() == null) {
            return;
        }
        if (instance == null) {
            instance = new SotoPortalSamplingTexture();
            client.getTextureManager().register(ID, instance);
        }
        if (clampSampler == null) {
            try {
                clampSampler = RenderSystem.getDevice().createSampler(
                        AddressMode.CLAMP_TO_EDGE,
                        AddressMode.CLAMP_TO_EDGE,
                        FilterMode.LINEAR,
                        FilterMode.LINEAR,
                        1,
                        OptionalDouble.empty()
                );
            } catch (Throwable ignored) {
                // Device may not be ready yet; retry on next bind.
            }
        }
    }

    public static void bindPortalColor(SotoPortalRenderTarget target) {
        ensureRegistered();
        if (instance == null || target == null || !target.isReady()) {
            return;
        }
        GpuTexture color = target.colorTexture();
        GpuTextureView view = target.colorTextureView();
        if (color == null || view == null) {
            return;
        }
        instance.texture = color;
        instance.textureView = view;
        instance.sampler = clampSampler;
    }

    @Override
    public void close() {
        // Portal FBO owns the GPU textures; only clear local refs.
        texture = null;
        textureView = null;
        sampler = null;
    }
}
