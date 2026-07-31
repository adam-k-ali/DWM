package com.adamkali.dwm.mixin.client;

import com.adamkali.dwm.render.boti.BotiStencilSupport;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.IntBuffer;

/**
 * Upgrades depth attachments to depth+stencil so exterior BOTI can mask the door aperture.
 * <p>
 * Some drivers (notably macOS) accept {@code GL_DEPTH_STENCIL_ATTACHMENT} but still report
 * zero stencil bits; attaching the same packed texture to both depth and stencil slots is more
 * reliable.
 */
@Mixin(Framebuffer.class)
public abstract class FramebufferMixin {
    private static final int GL_DEPTH_COMPONENT = 6402;

    @Redirect(
            method = "initFbo",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V"
            )
    )
    private void dwm$depthStencilTexImage(
            int target,
            int level,
            int internalFormat,
            int width,
            int height,
            int border,
            int format,
            int type,
            IntBuffer pixels
    ) {
        if (internalFormat == GL_DEPTH_COMPONENT && format == GL_DEPTH_COMPONENT) {
            BotiStencilSupport.clearDepthStencilTextureReady();
            try {
                GlStateManager._texImage2D(
                        target,
                        level,
                        GL30.GL_DEPTH24_STENCIL8,
                        width,
                        height,
                        border,
                        GL30.GL_DEPTH_STENCIL,
                        GL30.GL_UNSIGNED_INT_24_8,
                        null
                );
                BotiStencilSupport.markDepthStencilTextureReady();
                return;
            } catch (Throwable t) {
                BotiStencilSupport.disableForSession("Failed to allocate depth+stencil texture", t);
            }
        }
        GlStateManager._texImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
    }

    @Redirect(
            method = "initFbo",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V"
            )
    )
    private void dwm$depthStencilAttachment(int target, int attachment, int textureTarget, int texture, int level) {
        if (attachment == GL30.GL_DEPTH_ATTACHMENT && BotiStencilSupport.isDepthStencilTextureReady()) {
            try {
                // Dual-attach packed depth+stencil texture (more reliable than DEPTH_STENCIL_ATTACHMENT alone).
                GlStateManager._glFramebufferTexture2D(
                        target, GL30.GL_DEPTH_ATTACHMENT, textureTarget, texture, level);
                GlStateManager._glFramebufferTexture2D(
                        target, GL30.GL_STENCIL_ATTACHMENT, textureTarget, texture, level);
                return;
            } catch (Throwable t) {
                BotiStencilSupport.disableForSession("Failed to attach depth+stencil buffer", t);
            }
        }
        GlStateManager._glFramebufferTexture2D(target, attachment, textureTarget, texture, level);
    }
}
