package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.tardis.data.model.PortalAperture;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.interior.TardisPortalGate;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Shared BER loop: schedule portal work, peek last texture, composite or placeholder.
 */
public final class PortalDoorRenderer {
    /**
     * Door leaves flush on a later {@link SubmitNodeCollector#order(int)} than the
     * aperture preview so they composite after the portal color stamp.
     */
    public static final int DOOR_OVERLAY_ORDER = 1;

    private PortalDoorRenderer() {
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
     * Vanilla emissive translucent pipeline: compiled with the game shaders and
     * {@code writeDepth=false}, so inward-swinging leaves are not depth-rejected
     * by the aperture quad.
     */
    public static RenderType portalCompositeRenderType() {
        return RenderTypes.entityTranslucentEmissive(PortalSamplingTexture.ID);
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
                        portalTexture
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
