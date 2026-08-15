package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.render.portal.PortalDoorRenderer;
import com.adamkali.dwm.render.portal.PortalKey;
import com.adamkali.dwm.render.portal.PortalScene;
import com.adamkali.dwm.tardis.data.model.PortalAperture;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.UUID;
import net.minecraft.client.renderer.SubmitNodeCollector;

/**
 * Exterior BOTI: thin facade that builds an interior look-in {@link PortalScene} and
 * delegates schedule/composite to {@link PortalDoorRenderer}.
 * <p>
 * Hitch-fixed camera at the interior door plane looking into the console room; composite UV
 * crop uses each chameleon's {@link PortalAperture}.
 */
public final class TardisBotiRenderer {
    /** Interior door opening center (local structure coords). */
    public static final double INTERIOR_DOOR_CENTER_X = 5.5;
    /** Door blocks at y=1..2 span [1, 3); geometric center. */
    public static final double INTERIOR_DOOR_CENTER_Y = 2.0;
    /** Door bank sits on local z=1 in the shipped console-room template. */
    public static final double INTERIOR_DOOR_PLANE_Z = 1.0;

    private TardisBotiRenderer() {
    }

    public static boolean shouldRender(TardisDoorState doorState) {
        return PortalDoorRenderer.shouldRender(doorState);
    }

    /**
     * Schedules portal work for END_MAIN and submits deferred doorway composite geometry.
     * Must not touch GL state mid-BER (Minecraft 26.2).
     */
    public static void render(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            float tickDelta,
            UUID tardisId,
            TardisChameleonVariant variant
    ) {
        if (tardisId == null || variant == null) {
            return;
        }
        PortalAperture aperture = variant.getAperture();
        PortalScene scene = new PortalScene(
                PortalKey.boti(tardisId),
                tickDelta,
                new BotiPortalContent(tardisId)
        );
        PortalDoorRenderer.render(matrices, submitNodeCollector, scene, aperture);
    }

    /** Expose layout size for tests / debug. */
    public static int interiorSizeX() {
        return FirstDoctorConsoleRoomLayout.SIZE_X;
    }
}
