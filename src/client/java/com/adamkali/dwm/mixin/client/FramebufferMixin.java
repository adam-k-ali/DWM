package com.adamkali.dwm.mixin.client;

import com.adamkali.dwm.render.boti.BotiStencilSupport;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.GpuTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

/**
 * Upgrades depth attachments to depth+stencil so exterior BOTI can mask the door aperture.
 * <p>
 * Minecraft 26.2 allocates framebuffer depth via {@link GpuDevice#createTexture} with
 * {@link GpuFormat#D32_FLOAT}. This mixin swaps that format for {@link GpuFormat#D24_UNORM_S8_UINT}
 * so the default OpenGL backend still exposes a stencil aspect.
 * <p>
 * <b>SOTO sibling note:</b> the previous {@code bindWrite} redirect for portal MAIN_TARGET
 * theft is gone — {@link RenderTarget} no longer has {@code bindWrite}. Portal offscreen
 * redirect must be re-homed onto the 26.2 texture-view / RenderPass MAIN_TARGET path in
 * {@code render/soto/portal/**}; do not reintroduce a broken {@code bindWrite} inject here.
 */
@Mixin(RenderTarget.class)
public abstract class FramebufferMixin {
    @Redirect(
            method = "createBuffers",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/GpuDevice;createTexture(Ljava/util/function/Supplier;ILcom/mojang/blaze3d/GpuFormat;IIII)Lcom/mojang/blaze3d/textures/GpuTexture;"
            )
    )
    private GpuTexture dwm$depthStencilTexture(
            GpuDevice device,
            Supplier<String> label,
            int usage,
            GpuFormat format,
            int width,
            int height,
            int depth,
            int mipLevels
    ) {
        if (format == GpuFormat.D32_FLOAT) {
            BotiStencilSupport.clearDepthStencilTextureReady();
            try {
                GpuTexture texture = device.createTexture(
                        label,
                        usage,
                        GpuFormat.D24_UNORM_S8_UINT,
                        width,
                        height,
                        depth,
                        mipLevels
                );
                BotiStencilSupport.markDepthStencilTextureReady();
                return texture;
            } catch (Throwable t) {
                BotiStencilSupport.disableForSession("Failed to allocate depth+stencil texture", t);
            }
        }
        return device.createTexture(label, usage, format, width, height, depth, mipLevels);
    }
}
