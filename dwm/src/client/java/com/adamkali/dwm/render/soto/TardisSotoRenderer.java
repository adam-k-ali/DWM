package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.render.portal.PortalDoorFrame;
import com.adamkali.dwm.render.portal.PortalDoorRenderer;
import com.adamkali.dwm.render.portal.PortalKey;
import com.adamkali.dwm.render.portal.PortalScene;
import com.adamkali.dwm.tardis.data.model.PortalAperture;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.UUID;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Interior SOTO: thin facade that builds an exterior look-out {@link PortalScene} and
 * delegates schedule/composite to {@link PortalDoorRenderer}.
 */
public final class TardisSotoRenderer {
    /**
     * Blocks past the TARDIS door face for the SOTO look-out hitch plane.
     * Keeps the preview origin just in front of the shell instead of at the door face / shell body.
     */
    public static final double PREVIEW_FORWARD_OFFSET = 0.5;

    /**
     * Eye height above the TARDIS block base for the exterior hitch.
     */
    public static final double PREVIEW_EYE_HEIGHT = 1.75;

    /**
     * Exterior door opening center in footprint-relative coords
     * (TARDIS block at relative (5, 1, 5) — matches {@code SotoExteriorSampler.RELATIVE_TARDIS_POS}).
     * {@link #EXTERIOR_DOOR_PLANE_Z} is the hitch plane just in front of the shell
     * ({@code doorFaceZ - PREVIEW_FORWARD_OFFSET}), not the block center.
     */
    static final double EXTERIOR_DOOR_CENTER_X = 5.5;
    static final double EXTERIOR_DOOR_CENTER_Y = 1.0 + PREVIEW_EYE_HEIGHT;
    static final double EXTERIOR_DOOR_PLANE_Z = 5.0 - PREVIEW_FORWARD_OFFSET;

    public TardisSotoRenderer() {
    }

    public static boolean shouldRender(float doorSwing) {
        return PortalDoorRenderer.shouldRender(doorSwing);
    }

    /**
     * World-space centre of the 3x2 interior door bank for visibility culling.
     * {@code bankOrigin} is the lower/slot-0 cell; the bank grows along {@code facing.getCounterClockWise()}.
     */
    public static Vec3 doorCenter(BlockPos bankOrigin, Direction facing) {
        Direction along = facing.getCounterClockWise();
        return new Vec3(
                bankOrigin.getX() + 0.5 + along.getStepX(),
                bankOrigin.getY() + 1.0,
                bankOrigin.getZ() + 0.5 + along.getStepZ()
        );
    }

    /**
     * Schedules portal work for END_MAIN and submits deferred doorway composite geometry.
     * Must not touch GL state or open RenderPasses (mid-BER poison on 26.2).
     */
    public void render(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            float tickDelta,
            UUID tardisId,
            BlockPos interiorDoorPos,
            Direction interiorDoorFacing
    ) {
        render(matrices, submitNodeCollector, tickDelta, tardisId, interiorDoorPos, interiorDoorFacing, true);
    }

    /**
     * @param schedulePortal false when the doorway is not visible (see {@code PortalVisibility});
     *                       the stream is still requested, only the off-main render is skipped.
     */
    public void render(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            float tickDelta,
            UUID tardisId,
            BlockPos interiorDoorPos,
            Direction interiorDoorFacing,
            boolean schedulePortal
    ) {
        render(matrices, submitNodeCollector, tickDelta, tardisId, interiorDoorPos, interiorDoorFacing,
                schedulePortal, null);
    }

    /**
     * @param interiorDoor parallax mode: the interior doorway in world space. Non-null renders the
     *                     exterior from the viewer-relative camera and composites in screen space.
     */
    public void render(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            float tickDelta,
            UUID tardisId,
            BlockPos interiorDoorPos,
            Direction interiorDoorFacing,
            boolean schedulePortal,
            PortalDoorFrame interiorDoor
    ) {
        if (tardisId == null || interiorDoorPos == null || interiorDoorFacing == null) {
            return;
        }
        SotoPortalContent content = new SotoPortalContent(tardisId, interiorDoor);
        content.requestSync();
        PortalScene scene = new PortalScene(PortalKey.soto(tardisId), tickDelta, content);
        PortalDoorRenderer.render(
                matrices,
                submitNodeCollector,
                scene,
                PortalAperture.CLASSIC_INTERIOR_DOORS,
                0.0f,
                0.0f,
                schedulePortal,
                interiorDoor != null
        );
    }
}
