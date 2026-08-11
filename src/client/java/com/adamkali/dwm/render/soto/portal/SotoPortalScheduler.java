package com.adamkali.dwm.render.soto.portal;

import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decouples SOTO portal GPU work from mid-BER submit.
 * <p>
 * BER only schedules a request and composites the last completed portal texture (deferred
 * geometry). Actual FBO clear/mesh draws run on {@link net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents#END_MAIN}
 * after the main world pass has finished — mid-BER portal work blacks out the world/items on 26.2.
 */
public final class SotoPortalScheduler {
    private static final Map<UUID, Request> PENDING = new ConcurrentHashMap<>();
    private static final SotoPortalRenderer RENDERER = new SotoPortalRenderer();

    private SotoPortalScheduler() {
    }

    public static void schedule(
            UUID tardisId,
            BlockPos interiorDoorPos,
            Direction interiorDoorFacing,
            float tickDelta
    ) {
        if (tardisId == null || interiorDoorPos == null || interiorDoorFacing == null) {
            return;
        }
        SotoGhostExterior.requestIfNeeded(tardisId);
        PENDING.put(tardisId, new Request(tardisId, interiorDoorPos, interiorDoorFacing, tickDelta));
    }

    /**
     * Returns the last completed portal texture for compositing during BER (may be one frame late).
     */
    public static SotoPortalRenderer.PortalTexture peekCompositeTexture(UUID tardisId) {
        return RENDERER.peekLastRendered(tardisId);
    }

    public static void flushEndMain() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || !SotoPortalSupport.isAvailable()) {
            PENDING.clear();
            return;
        }
        List<Request> batch = new ArrayList<>(PENDING.values());
        PENDING.clear();
        for (Request request : batch) {
            try {
                RENDERER.renderOffMainPass(
                        request.tardisId(),
                        request.interiorDoorPos(),
                        request.interiorDoorFacing(),
                        request.tickDelta()
                );
            } catch (Throwable t) {
                SotoPortalSupport.disableForSession("Portal END_MAIN flush failed", t);
                break;
            }
        }
    }

    private record Request(
            UUID tardisId,
            BlockPos interiorDoorPos,
            Direction interiorDoorFacing,
            float tickDelta
    ) {
    }
}
