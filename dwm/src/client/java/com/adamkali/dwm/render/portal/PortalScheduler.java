package com.adamkali.dwm.render.portal;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Decouples portal GPU work from mid-BER submit.
 * <p>
 * BER only schedules a {@link PortalScene} and composites the last completed portal texture.
 * Actual FBO clear/mesh draws run on {@code LevelRenderEvents.END_MAIN}.
 */
public final class PortalScheduler {
    private static final Map<PortalKey, PortalScene> PENDING = new ConcurrentHashMap<>();
    private static final PortalRenderer RENDERER = new PortalRenderer();

    private static final AtomicLong SCHEDULED_TOTAL = new AtomicLong();
    private static final AtomicLong CULLED_TOTAL = new AtomicLong();

    private PortalScheduler() {
    }

    /** Doorway was submitted but skipped by visibility culling (always-on, cheap; for tests/perf reports). */
    public static void noteCulled(PortalKey key) {
        CULLED_TOTAL.incrementAndGet();
        PortalPerfStats.noteCulled(key);
    }

    /** Cumulative number of scenes scheduled since startup. */
    public static long scheduledTotal() {
        return SCHEDULED_TOTAL.get();
    }

    /** Cumulative number of doorway submissions culled since startup. */
    public static long culledTotal() {
        return CULLED_TOTAL.get();
    }

    static int pendingCountForTest() {
        return PENDING.size();
    }

    static void resetForTests() {
        PENDING.clear();
        SCHEDULED_TOTAL.set(0L);
        CULLED_TOTAL.set(0L);
    }

    public static void schedule(PortalScene scene) {
        if (scene == null) {
            return;
        }
        PENDING.put(scene.key(), scene);
        SCHEDULED_TOTAL.incrementAndGet();
    }

    /**
     * Returns the last completed portal texture for compositing during BER (may be one frame late).
     */
    public static PortalRenderer.PortalTexture peekCompositeTexture(PortalKey key) {
        return RENDERER.peekLastRendered(key);
    }

    public static void flushEndMain() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || !PortalSupport.isAvailable()) {
            PENDING.clear();
            return;
        }
        List<PortalScene> batch = new ArrayList<>(PENDING.values());
        PENDING.clear();
        if (batch.isEmpty()) {
            return;
        }
        long flushStart = PortalPerfStats.begin();
        try {
            for (PortalScene scene : batch) {
                try {
                    RENDERER.renderOffMainPass(scene);
                } catch (Throwable t) {
                    PortalSupport.disableForSession("Portal END_MAIN flush failed", t);
                    break;
                }
            }
        } finally {
            PortalPerfStats.end(PortalPerfStats.Stage.FLUSH_TOTAL, flushStart);
            if (PortalPerfStats.isEnabled()) {
                PortalPerfStats.publishFrame();
            }
        }
    }
}
