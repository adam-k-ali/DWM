package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.tardis.portal.PortalStreamKind;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dirty / last-writer tracking for the shared portal FBO.
 * <p>
 * A portal key may reuse the last color texture across frames only when it is clean
 * and was the last successful writer (shared FBO: last writer wins).
 */
public final class PortalFrameCache {
    private static final Set<PortalKey> CLEAN = ConcurrentHashMap.newKeySet();
    private static volatile PortalKey lastWriter;

    private PortalFrameCache() {
    }

    public static PortalKey toPortalKey(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return null;
        }
        return switch (kind) {
            case BOTI -> PortalKey.boti(tardisId);
            case SOTO -> PortalKey.soto(tardisId);
        };
    }

    public static void markDirty(PortalStreamKind kind, UUID tardisId) {
        markDirty(toPortalKey(kind, tardisId));
    }

    public static void markDirty(PortalKey key) {
        if (key == null) {
            return;
        }
        CLEAN.remove(key);
    }

    public static void markAllDirty() {
        CLEAN.clear();
        lastWriter = null;
    }

    /**
     * Missing / unknown keys are treated as dirty (must redraw).
     */
    public static boolean isDirty(PortalKey key) {
        return key == null || !CLEAN.contains(key);
    }

    public static void clearDirty(PortalKey key) {
        if (key != null) {
            CLEAN.add(key);
        }
    }

    public static boolean wasLastWriter(PortalKey key) {
        return key != null && key.equals(lastWriter);
    }

    public static PortalKey lastWriter() {
        return lastWriter;
    }

    /**
     * Records that {@code key} just wrote the shared FBO.
     * Marks the previous last writer dirty when it differs.
     *
     * @return previous last writer, or null
     */
    public static PortalKey noteRendered(PortalKey key) {
        if (key == null) {
            return null;
        }
        PortalKey previous = lastWriter;
        lastWriter = key;
        if (previous != null && !previous.equals(key)) {
            markDirty(previous);
        }
        return previous;
    }

    /**
     * Keys that must drop {@code renderedReady} after {@code key} overwrote the shared FBO.
     */
    public static Set<PortalKey> overwrittenReadyKeys(PortalKey key, Set<PortalKey> readyKeys) {
        if (key == null || readyKeys == null || readyKeys.isEmpty()) {
            return Set.of();
        }
        Set<PortalKey> overwritten = new HashSet<>();
        for (PortalKey ready : readyKeys) {
            if (ready != null && !ready.equals(key)) {
                markDirty(ready);
                overwritten.add(ready);
            }
        }
        return overwritten;
    }

    /**
     * FBO resize/close: no prior color texture remains valid.
     */
    public static void invalidateForResize() {
        markAllDirty();
    }

    static void resetForTests() {
        CLEAN.clear();
        lastWriter = null;
    }
}
