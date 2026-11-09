package com.adamkali.dwm.mixin.client;

import com.adamkali.dwm.render.portal.PortalViewerState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.renderer.GameRenderer;

/**
 * Captures this frame's level projection (including view bob, hurt tilt and nausea) so the
 * parallax door portal renders with exactly the matrix the main pass uses.
 */
@Mixin(GameRenderer.class)
public class GameRendererPortalMixin {
    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
            )
    )
    private Matrix4f dwm$captureLevelProjection(Matrix4f projection) {
        PortalViewerState.captureLevelProjection(projection);
        return projection;
    }
}
