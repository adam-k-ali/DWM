package com.adamkali.dwm.tardis.portal;

import com.adamkali.dwm.config.DWMConfig;
import net.minecraft.server.MinecraftServer;

/**
 * Config-gated server-side portal stream diagnostics.
 * Accumulates per-tick counters/phase timings and publishes a ~1 Hz {@link Snapshot}.
 */
public final class PortalStreamPerfStats {
    public static final int PUBLISH_INTERVAL_TICKS = 20;

    private static long flushMetaNs;
    private static long flushSotoNs;
    private static long flushBotiNs;
    private static long syncEntitiesNs;
    private static int entitySpawns;
    private static int entityUpdates;
    private static int entityRemoves;
    private static int chunkPackets;
    private static int metaPackets;
    private static int chunkSamples;
    private static int entitiesScanned;
    private static int fullResyncs;
    private static int maxViewers;
    private static int maxActiveStreams;
    private static int streamsThisTick;
    private static double lastMsptMs;
    private static int lastServerTick;

    private static volatile Snapshot published = Snapshot.IDLE;

    private PortalStreamPerfStats() {
    }

    public static boolean isEnabled() {
        return DWMConfig.getBoolean(DWMConfig.SHOW_PORTAL_PERF_DEBUG);
    }

    /** Returns {@code System.nanoTime()} when enabled, else {@code -1}. */
    public static long begin() {
        return isEnabled() ? System.nanoTime() : -1L;
    }

    public static void beginTick(MinecraftServer server) {
        if (!isEnabled() || server == null) {
            return;
        }
        streamsThisTick = 0;
        lastMsptMs = server.getAverageTickTimeNanos() / 1_000_000.0;
        lastServerTick = server.getTickCount();
    }

    /** Fold this tick's active-stream count into the window max. Call once at end of tick. */
    public static void endTick() {
        if (!isEnabled()) {
            return;
        }
        if (streamsThisTick > maxActiveStreams) {
            maxActiveStreams = streamsThisTick;
        }
    }

    public static void endFlushMeta(long startNs) {
        addPhaseNs(startNs, Phase.FLUSH_META);
    }

    public static void endFlushSoto(long startNs) {
        addPhaseNs(startNs, Phase.FLUSH_SOTO);
    }

    public static void endFlushBoti(long startNs) {
        addPhaseNs(startNs, Phase.FLUSH_BOTI);
    }

    public static void endSyncEntities(long startNs) {
        addPhaseNs(startNs, Phase.SYNC_ENTITIES);
    }

    public static void noteEntitySpawn() {
        if (isEnabled()) {
            entitySpawns++;
        }
    }

    public static void noteEntityUpdate() {
        if (isEnabled()) {
            entityUpdates++;
        }
    }

    public static void noteEntityRemove() {
        if (isEnabled()) {
            entityRemoves++;
        }
    }

    public static void noteChunkPacket() {
        if (isEnabled()) {
            chunkPackets++;
        }
    }

    public static void noteMetaPacket() {
        if (isEnabled()) {
            metaPackets++;
        }
    }

    public static void noteChunkSample() {
        if (isEnabled()) {
            chunkSamples++;
        }
    }

    public static void noteEntitiesScanned(int count) {
        if (isEnabled() && count > 0) {
            entitiesScanned += count;
        }
    }

    public static void noteFullResync() {
        if (isEnabled()) {
            fullResyncs++;
        }
    }

    public static void noteStreamViewers(int viewerCount) {
        if (!isEnabled() || viewerCount <= 0) {
            return;
        }
        streamsThisTick++;
        if (viewerCount > maxViewers) {
            maxViewers = viewerCount;
        }
    }

    /**
     * Every {@link #PUBLISH_INTERVAL_TICKS} ticks, builds a window snapshot and resets accumulators.
     *
     * @return published snapshot when a publish occurred; otherwise {@code null}
     */
    public static Snapshot maybePublish(int tickCounter) {
        if (!isEnabled() || tickCounter % PUBLISH_INTERVAL_TICKS != 0) {
            return null;
        }
        Snapshot snap = buildSnapshot();
        published = snap;
        resetWindow();
        return snap;
    }

    public static Snapshot snapshot() {
        return published;
    }

    /** Pure helper for tests: convert accumulated window fields into a snapshot. */
    static Snapshot buildSnapshotPure(
            double msptMs,
            long flushMetaNsValue,
            long flushSotoNsValue,
            long flushBotiNsValue,
            long syncEntitiesNsValue,
            int entitySpawnsValue,
            int entityUpdatesValue,
            int entityRemovesValue,
            int chunkPacketsValue,
            int metaPacketsValue,
            int chunkSamplesValue,
            int entitiesScannedValue,
            int fullResyncsValue,
            int viewersValue,
            int activeStreamsValue,
            int serverTick
    ) {
        double flushMetaMs = nsToMs(flushMetaNsValue);
        double flushSotoMs = nsToMs(flushSotoNsValue);
        double flushBotiMs = nsToMs(flushBotiNsValue);
        double syncEntitiesMs = nsToMs(syncEntitiesNsValue);
        return new Snapshot(
                msptMs,
                flushMetaMs,
                flushSotoMs,
                flushBotiMs,
                syncEntitiesMs,
                flushMetaMs + flushSotoMs + flushBotiMs,
                entitySpawnsValue,
                entityUpdatesValue,
                entityRemovesValue,
                chunkPacketsValue,
                metaPacketsValue,
                chunkSamplesValue,
                entitiesScannedValue,
                fullResyncsValue,
                viewersValue,
                activeStreamsValue,
                serverTick
        );
    }

    static void resetForTests() {
        resetWindow();
        lastMsptMs = 0.0;
        lastServerTick = 0;
        published = Snapshot.IDLE;
    }

    public static void clear() {
        resetForTests();
    }

    private static Snapshot buildSnapshot() {
        return buildSnapshotPure(
                lastMsptMs,
                flushMetaNs,
                flushSotoNs,
                flushBotiNs,
                syncEntitiesNs,
                entitySpawns,
                entityUpdates,
                entityRemoves,
                chunkPackets,
                metaPackets,
                chunkSamples,
                entitiesScanned,
                fullResyncs,
                maxViewers,
                maxActiveStreams,
                lastServerTick
        );
    }

    private static void resetWindow() {
        flushMetaNs = 0L;
        flushSotoNs = 0L;
        flushBotiNs = 0L;
        syncEntitiesNs = 0L;
        entitySpawns = 0;
        entityUpdates = 0;
        entityRemoves = 0;
        chunkPackets = 0;
        metaPackets = 0;
        chunkSamples = 0;
        entitiesScanned = 0;
        fullResyncs = 0;
        maxViewers = 0;
        maxActiveStreams = 0;
        streamsThisTick = 0;
    }

    private static void addPhaseNs(long startNs, Phase phase) {
        if (startNs < 0L || !isEnabled()) {
            return;
        }
        long elapsed = System.nanoTime() - startNs;
        if (elapsed <= 0L) {
            return;
        }
        switch (phase) {
            case FLUSH_META -> flushMetaNs += elapsed;
            case FLUSH_SOTO -> flushSotoNs += elapsed;
            case FLUSH_BOTI -> flushBotiNs += elapsed;
            case SYNC_ENTITIES -> syncEntitiesNs += elapsed;
        }
    }

    static double nsToMs(long ns) {
        return ns / 1_000_000.0;
    }

    private enum Phase {
        FLUSH_META,
        FLUSH_SOTO,
        FLUSH_BOTI,
        SYNC_ENTITIES
    }

    public record Snapshot(
            double msptMs,
            double flushMetaMs,
            double flushSotoMs,
            double flushBotiMs,
            double syncEntitiesMs,
            double syncFlushMs,
            int entitySpawns,
            int entityUpdates,
            int entityRemoves,
            int chunkPackets,
            int metaPackets,
            int chunkSamples,
            int entitiesScanned,
            int fullResyncs,
            int viewers,
            int activeStreams,
            int serverTick
    ) {
        public static final Snapshot IDLE = new Snapshot(
                Double.NaN,
                0.0, 0.0, 0.0, 0.0, 0.0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        );

        public boolean isPresent() {
            return !Double.isNaN(msptMs);
        }
    }
}
