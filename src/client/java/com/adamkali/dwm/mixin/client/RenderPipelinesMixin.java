package com.adamkali.dwm.mixin.client;

import com.adamkali.dwm.render.portal.PortalDoorRenderer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.client.renderer.RenderPipelines;

/**
 * Registers the opaque no-depth-write portal composite pipeline before vanilla
 * compiles {@link RenderPipelines#getStaticPipelines()}.
 */
@Mixin(RenderPipelines.class)
public class RenderPipelinesMixin {
    @Inject(method = "getStaticPipelines", at = @At("HEAD"))
    private static void dwm$ensurePortalCompositePipeline(CallbackInfoReturnable<List<RenderPipeline>> cir) {
        PortalDoorRenderer.ensurePipelineRegistered();
    }
}
