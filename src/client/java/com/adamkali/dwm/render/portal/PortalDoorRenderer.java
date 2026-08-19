package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.tardis.data.model.PortalAperture;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.interior.TardisPortalGate;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.Function;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

/**
 * Shared BER loop: schedule portal work, peek last texture, composite or placeholder.
 */
public final class PortalDoorRenderer {
    /**
     * Door leaves flush on a later {@link SubmitNodeCollector#order(int)} than the
     * aperture preview so they composite after the portal color stamp.
     */
    public static final int DOOR_OVERLAY_ORDER = 1;

    /**
     * Opaque overwrite ({@code src=ONE, dst=ZERO}). {@link RenderType#hasBlending()} must be
     * true so {@code submitCustomGeometry} routes to {@code translucentCustomGeometry}
     * (after the shell's solid pass). The blend itself replaces dest, so world color
     * cannot show through while strafing.
     */
    static final BlendFunction PORTAL_COMPOSITE_BLEND = new BlendFunction(BlendFactor.ONE, BlendFactor.ZERO);

    /**
     * Entity-cutout clone with {@code writeDepth=false} and opaque overwrite blend.
     * Flushes after the shell (blending flag) without mixing world color, and does not
     * depth-reject inward-swinging leaves.
     * <p>
     * Uses vanilla {@code core/entity} shaders from {@link RenderPipelines#ENTITY_SNIPPET}.
     * Must be {@link RenderPipelines#register}ed before {@link RenderPipelines#getStaticPipelines()}
     * compiles GPU programs — see client mixin and {@link com.adamkali.dwm.DWMClient}.
     */
    public static final RenderPipeline PORTAL_COMPOSITE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "pipeline/portal_composite"))
                    .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                    .withShaderDefine("PER_FACE_LIGHTING")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                    .withCull(false)
                    .withColorTargetState(new ColorTargetState(PORTAL_COMPOSITE_BLEND))
                    .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                    .build()
    );

    private static final Function<Identifier, RenderType> PORTAL_COMPOSITE = Util.memoize(
            texture -> RenderType.create(
                    "dwm_portal_composite",
                    RenderSetup.builder(PORTAL_COMPOSITE_PIPELINE)
                            .withTexture("Sampler0", texture)
                            .useLightmap()
                            .useOverlay()
                            .createRenderSetup()
            )
    );

    private PortalDoorRenderer() {
    }

    /** Touch the pipeline field so {@link RenderPipelines#register} runs before shader compile. */
    public static void ensurePipelineRegistered() {
        if (PORTAL_COMPOSITE_PIPELINE == null) {
            throw new IllegalStateException("Portal composite pipeline failed to register");
        }
    }

    public static boolean shouldRender(float doorSwing) {
        return DWMConfig.getBoolean(DWMConfig.ENABLE_DOOR_PORTALS)
                && PortalSupport.isAvailable()
                && TardisPortalGate.shouldShow(doorSwing);
    }

    public static boolean shouldRender(TardisDoorState doorState) {
        return DWMConfig.getBoolean(DWMConfig.ENABLE_DOOR_PORTALS)
                && PortalSupport.isAvailable()
                && TardisPortalGate.shouldShow(doorState);
    }

    /**
     * Later pass than the aperture preview so swung leaves composite over it.
     * Minecraft flushes {@code submitCustomGeometry} by RenderType, so doors must
     * not share the shell's {@code entityCutout} bucket.
     */
    public static RenderType doorOverlayRenderType(Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    /**
     * Opaque doorway stamp: overwrite blend (late pass, covers world), {@code writeDepth=false}
     * so inward-swinging leaves are not depth-rejected by the aperture quad.
     */
    public static RenderType portalCompositeRenderType() {
        return PORTAL_COMPOSITE.apply(PortalSamplingTexture.ID);
    }

    /**
     * Schedules portal work for END_MAIN and submits deferred doorway composite geometry.
     * Must not touch GL state or open RenderPasses (mid-BER poison on 26.2).
     */
    public static void render(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            PortalScene scene,
            PortalAperture aperture
    ) {
        render(matrices, submitNodeCollector, scene, aperture, 0.0f, 0.0f);
    }

    public static void render(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            PortalScene scene,
            PortalAperture aperture,
            float viewPanU,
            float viewPanV
    ) {
        if (matrices == null || submitNodeCollector == null || scene == null || aperture == null) {
            return;
        }
        PortalScheduler.schedule(scene);
        PortalPerfStats.noteScheduled(scene.key());
        long compositeStart = PortalPerfStats.begin();
        PortalRenderer.PortalTexture portalTexture =
                PortalScheduler.peekCompositeTexture(scene.key());
        matrices.pushPose();
        try {
            if (portalTexture.available()) {
                PortalApertureComposite.drawPortalComposite(
                        matrices,
                        submitNodeCollector,
                        aperture,
                        portalTexture,
                        viewPanU,
                        viewPanV
                );
            } else {
                PortalApertureComposite.drawPlaceholder(matrices, submitNodeCollector, aperture);
            }
        } finally {
            matrices.popPose();
            PortalPerfStats.end(PortalPerfStats.Stage.COMPOSITE, compositeStart);
        }
    }
}
