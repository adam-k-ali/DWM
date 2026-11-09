package com.adamkali.dwm.mixin.client;

import com.adamkali.dwm.render.portal.PortalDoorRenderer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderPipelines;

/**
 * Registers the opaque no-depth-write portal composite pipelines (UV-cropped and parallax) before vanilla
 * compiles {@link RenderPipelines#getStaticPipelines()}.
 */
@Mixin(RenderPipelines.class)
public class RenderPipelinesMixin {
    @Inject(method = "getStaticPipelines", at = @At("RETURN"), cancellable = true)
    private static void dwm$ensurePortalCompositePipeline(CallbackInfoReturnable<List<RenderPipeline>> cir) {
        PortalDoorRenderer.ensurePipelineRegistered();
        List<RenderPipeline> list = cir.getReturnValue();
        List<RenderPipeline> copy = new ArrayList<>(list == null ? List.of() : list);
        boolean changed = false;
        for (RenderPipeline pipeline : List.of(
                PortalDoorRenderer.PORTAL_COMPOSITE_PIPELINE,
                PortalDoorRenderer.PORTAL_SCREEN_COMPOSITE_PIPELINE)) {
            if (!copy.contains(pipeline)) {
                copy.add(pipeline);
                changed = true;
            }
        }
        if (changed) {
            cir.setReturnValue(List.copyOf(copy));
        }
    }
}
