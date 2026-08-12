package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.network.SyncPortalPerfS2CPayload;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Config-gated CPU wall-time instrumentation for the shared BOTI/SOTO portal pipeline.
 * Hot paths early-return when {@link DWMConfig#SHOW_PORTAL_PERF_DEBUG} is off.
 * <p>
 * HUD/log read rolling-window averages ({@link #AVG_WINDOW}) over a ring history
 * ({@link #HISTORY_FRAMES}), not raw per-frame spikes.
 */
public final class PortalPerfStats {
    public static final int HISTORY_FRAMES = 120;
    public static final int AVG_WINDOW = 60;
    private static final double EMA_ALPHA = 0.2;

    private static final EnumMap<Stage, Long> STAGE_NS = new EnumMap<>(Stage.class);
    private static final FrameSample[] HISTORY = new FrameSample[HISTORY_FRAMES];
    private static int historySize;
    private static int historyWrite;

    private static PortalKey activeKey;
    private static Outcome outcome = Outcome.IDLE;
    private static int chunkCount;
    private static int meshChunkCount;
    private static int entityCount;
    private static int chunksKept;
    private static int chunksCulled;
    private static long meshBakeNsThisFrame;
    private static int meshBakeCountThisFrame;
    private static int meshBakeSkipCountThisFrame;
    private static long lastMeshBakeNs;
    private static int entityUpdatesThisFrame;
    private static int entitySpawnsThisFrame;
    private static int entityRemovesThisFrame;
    private static double maxPoseDeltaThisFrame;
    private static int identityInterpThisFrame;
    private static int advanceInterpThisFrame;
    private static float partialTickThisFrame = Float.NaN;
    private static float itemAgeInTicksThisFrame = Float.NaN;
    /**
     * Inter-frame pose baselines for {@link #noteLerpedPose}. Keyed by stream so BOTI/SOTO never share.
     * Updated at most once per entity per published frame ({@link #SAMPLED_THIS_FRAME}).
     */
    private static final Map<PoseTrackKey, PoseXyz> LAST_POSES = new ConcurrentHashMap<>();
    private static final Set<PoseTrackKey> SAMPLED_THIS_FRAME = ConcurrentHashMap.newKeySet();
    private static volatile ServerDiag serverDiag = ServerDiag.NONE;
    private static volatile Snapshot published = Snapshot.IDLE;
    private static volatile DisplaySnapshot display = DisplaySnapshot.IDLE;
    private static double totalEmaMs;
    private static boolean wasEnabled;
    /** Non-null in unit tests to bypass {@link DWMConfig} (null = use config). */
    private static Boolean enabledOverrideForTest;

    private PortalPerfStats() {
    }

    public static boolean isEnabled() {
        if (enabledOverrideForTest != null) {
            return enabledOverrideForTest;
        }
        return DWMConfig.getBoolean(DWMConfig.SHOW_PORTAL_PERF_DEBUG);
    }

    /** Returns {@code System.nanoTime()} when enabled, else {@code -1} (no-op token). */
    public static long begin() {
        return isEnabled() ? System.nanoTime() : -1L;
    }

    public static void end(Stage stage, long startNs) {
        if (startNs < 0L || !isEnabled() || stage == null) {
            return;
        }
        addStageNs(stage, System.nanoTime() - startNs);
    }

    public static void addStageNs(Stage stage, long ns) {
        if (!isEnabled() || stage == null || ns <= 0L) {
            return;
        }
        STAGE_NS.merge(stage, ns, Long::sum);
    }

    public static void noteScheduled(PortalKey key) {
        if (!isEnabled() || key == null) {
            return;
        }
        activeKey = key;
        if (outcome == Outcome.IDLE) {
            outcome = Outcome.SCHEDULED;
        }
    }

    public static void beginOffMain(PortalKey key) {
        if (!isEnabled() || key == null) {
            return;
        }
        activeKey = key;
        outcome = Outcome.RENDERED;
    }

    public static void setOutcome(Outcome value) {
        if (!isEnabled() || value == null) {
            return;
        }
        outcome = value;
    }

    public static void setSceneCounts(int chunks, int meshes, int entities) {
        if (!isEnabled()) {
            return;
        }
        chunkCount = Math.max(0, chunks);
        meshChunkCount = Math.max(0, meshes);
        entityCount = Math.max(0, entities);
    }

    public static void setCullCounts(int kept, int culled) {
        if (!isEnabled()) {
            return;
        }
        chunksKept = Math.max(0, kept);
        chunksCulled = Math.max(0, culled);
    }

    public static void noteMeshBake(long bakeNs) {
        if (!isEnabled() || bakeNs <= 0L) {
            return;
        }
        meshBakeNsThisFrame += bakeNs;
        meshBakeCountThisFrame++;
        lastMeshBakeNs = bakeNs;
        addStageNs(Stage.MESH_BAKE, bakeNs);
    }

    /** Records a skipped mesh bake (unchanged chunk content with drawable mesh). */
    public static void noteBakeSkip() {
        if (!isEnabled()) {
            return;
        }
        meshBakeSkipCountThisFrame++;
    }

    public static void noteEntityUpdate() {
        if (!isEnabled()) {
            return;
        }
        entityUpdatesThisFrame++;
    }

    public static void noteEntitySpawn() {
        if (!isEnabled()) {
            return;
        }
        entitySpawnsThisFrame++;
    }

    public static void noteEntityRemove() {
        if (!isEnabled()) {
            return;
        }
        entityRemovesThisFrame++;
    }

    /** Records a pose movement sample; keeps the max for the current frame. */
    public static void notePoseDelta(double delta) {
        if (!isEnabled() || !(delta > 0.0)) {
            return;
        }
        if (delta > maxPoseDeltaThisFrame) {
            maxPoseDeltaThisFrame = delta;
        }
    }

    /**
     * Samples a ghost entity's lerped pose for inter-frame movement stats.
     * <p>
     * Only the first sample per {@code (kind, tardisId, entityUuid)} in a published frame
     * compares against the previous frame's pose and updates the baseline. Later calls in the
     * same frame (e.g. repeated {@code getRenderableEntities}) are ignored so intra-frame
     * re-entry cannot shrink or replace the inter-frame delta.
     */
    public static void noteLerpedPose(
            PortalStreamKind kind,
            UUID tardisId,
            UUID entityUuid,
            double x,
            double y,
            double z
    ) {
        if (!isEnabled() || kind == null || tardisId == null || entityUuid == null) {
            return;
        }
        PoseTrackKey key = new PoseTrackKey(kind, tardisId, entityUuid);
        if (!SAMPLED_THIS_FRAME.add(key)) {
            return;
        }
        PoseXyz previous = LAST_POSES.put(key, new PoseXyz(x, y, z));
        if (previous == null) {
            return;
        }
        double dx = x - previous.x();
        double dy = y - previous.y();
        double dz = z - previous.z();
        notePoseDelta(Math.sqrt(dx * dx + dy * dy + dz * dz));
    }

    /** Drops pose baselines for one entity (remove / despawn). */
    public static void clearPoseTracking(PortalStreamKind kind, UUID tardisId, UUID entityUuid) {
        if (kind == null || tardisId == null || entityUuid == null) {
            return;
        }
        PoseTrackKey key = new PoseTrackKey(kind, tardisId, entityUuid);
        LAST_POSES.remove(key);
        SAMPLED_THIS_FRAME.remove(key);
    }

    /** Drops pose baselines for an entire portal stream (invalidate). */
    public static void clearPoseTracking(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return;
        }
        LAST_POSES.keySet().removeIf(k -> k.kind() == kind && k.tardisId().equals(tardisId));
        SAMPLED_THIS_FRAME.removeIf(k -> k.kind() == kind && k.tardisId().equals(tardisId));
    }

    /** Drops all pose baselines (client disconnect / invalidateAll). */
    public static void clearAllPoseTracking() {
        LAST_POSES.clear();
        SAMPLED_THIS_FRAME.clear();
    }

    public static void noteIdentityInterp() {
        if (!isEnabled()) {
            return;
        }
        identityInterpThisFrame++;
    }

    public static void noteAdvanceInterp() {
        if (!isEnabled()) {
            return;
        }
        advanceInterpThisFrame++;
    }

    public static void notePartialTick(float partialTick) {
        if (!isEnabled()) {
            return;
        }
        partialTickThisFrame = partialTick;
    }

    public static void noteItemAgeInTicks(float ageInTicks) {
        if (!isEnabled()) {
            return;
        }
        itemAgeInTicksThisFrame = ageInTicks;
    }

    /** Applies the latest ~1 Hz server portal-sync diagnostics snapshot. */
    public static void applyServerDiag(SyncPortalPerfS2CPayload payload) {
        if (!isEnabled() || payload == null || Float.isNaN(payload.msptMs())) {
            return;
        }
        serverDiag = ServerDiag.fromPayload(payload);
        DisplaySnapshot current = display;
        if (current != null && current != DisplaySnapshot.IDLE) {
            display = current.withServerDiag(serverDiag);
        }
    }

    /**
     * Publishes the current frame into history + display averages and resets transient timers.
     */
    public static void publishFrame() {
        boolean enabled = isEnabled();
        if (!enabled) {
            if (wasEnabled) {
                PortalPerfDebugLog.close();
                serverDiag = ServerDiag.NONE;
                LAST_POSES.clear();
                SAMPLED_THIS_FRAME.clear();
                wasEnabled = false;
            }
            return;
        }
        wasEnabled = true;

        EnumMap<Stage, Long> stages = new EnumMap<>(Stage.class);
        stages.putAll(STAGE_NS);
        long totalNs = stages.getOrDefault(Stage.OFF_MAIN_TOTAL, 0L);
        if (totalNs <= 0L) {
            totalNs = stages.getOrDefault(Stage.FLUSH_TOTAL, 0L);
        }
        double totalMs = nsToMs(totalNs);
        totalEmaMs = updateEma(totalEmaMs, totalMs, EMA_ALPHA);
        Stage maxStage = findMaxStage(stages);

        EnumMap<Stage, Double> stageMs = new EnumMap<>(Stage.class);
        for (Stage stage : Stage.HUD_ORDER) {
            stageMs.put(stage, nsToMs(stages.getOrDefault(stage, 0L)));
        }

        FrameSample sample = new FrameSample(
                MapCopy.copyDoubles(stageMs),
                totalMs,
                meshBakeCountThisFrame,
                meshBakeSkipCountThisFrame,
                entityUpdatesThisFrame,
                entitySpawnsThisFrame,
                entityRemovesThisFrame,
                maxPoseDeltaThisFrame,
                identityInterpThisFrame,
                advanceInterpThisFrame,
                partialTickThisFrame,
                itemAgeInTicksThisFrame
        );
        pushHistory(sample);

        Snapshot snap = new Snapshot(
                activeKey,
                outcome,
                MapCopy.copy(stages),
                chunkCount,
                meshChunkCount,
                entityCount,
                chunksKept,
                chunksCulled,
                meshBakeNsThisFrame,
                meshBakeCountThisFrame,
                lastMeshBakeNs,
                totalMs,
                totalEmaMs,
                maxStage,
                partialTickThisFrame,
                itemAgeInTicksThisFrame
        );
        published = snap;
        display = buildDisplay(snap);
        STAGE_NS.clear();
        meshBakeNsThisFrame = 0L;
        meshBakeCountThisFrame = 0;
        meshBakeSkipCountThisFrame = 0;
        entityUpdatesThisFrame = 0;
        entitySpawnsThisFrame = 0;
        entityRemovesThisFrame = 0;
        maxPoseDeltaThisFrame = 0.0;
        identityInterpThisFrame = 0;
        advanceInterpThisFrame = 0;
        partialTickThisFrame = Float.NaN;
        itemAgeInTicksThisFrame = Float.NaN;
        // Allow the next END_MAIN flush to sample each entity once against LAST_POSES.
        SAMPLED_THIS_FRAME.clear();

        PortalPerfDebugLog.maybeAppend(display);
    }

    /** Package-visible for tests: max pose delta accumulated since the last {@link #publishFrame()}. */
    static double maxPoseDeltaThisFrameForTest() {
        return maxPoseDeltaThisFrame;
    }

    public static Snapshot snapshot() {
        return published;
    }

    public static DisplaySnapshot displaySnapshot() {
        return display;
    }

    public static List<String> formatLines(DisplaySnapshot snap) {
        return formatLinesPure(snap);
    }

    /** Chronological total-ms history (oldest → newest), length {@code historySize()}. */
    public static float[] historyTotalsMs() {
        int size = historySize;
        float[] out = new float[size];
        for (int i = 0; i < size; i++) {
            out[i] = (float) historyAt(i).totalMs();
        }
        return out;
    }

    public static int historySize() {
        return historySize;
    }

    /** Pure formatter for tests / HUD — averages first. */
    static List<String> formatLinesPure(DisplaySnapshot snap) {
        List<String> lines = new ArrayList<>();
        if (snap == null || snap == DisplaySnapshot.IDLE || snap.key() == null) {
            lines.add("Portal Perf [idle]");
            return lines;
        }
        PortalKey key = snap.key();
        lines.add(String.format(
                Locale.ROOT,
                "Portal Perf [%s %s]",
                key.kind().name(),
                shortId(key.tardisId())
        ));
        lines.add(String.format(
                Locale.ROOT,
                "outcome: %s  avg: %.2fms (ema %.2f)  n=%d",
                snap.outcome().label(),
                snap.avgTotalMs(),
                snap.emaTotalMs(),
                snap.windowCount()
        ));
        lines.add(formatStagePair(snap, Stage.FLUSH_TOTAL, "flush", Stage.OFF_MAIN_TOTAL, "offMain"));
        lines.add(formatStageQuad(
                snap,
                Stage.SKY_FOG, "skyFog",
                Stage.TERRAIN_OPAQUE, "opaque",
                Stage.TERRAIN_CUTOUT, "cutout",
                Stage.TERRAIN_TRANSLUCENT, "trans"
        ));
        lines.add(formatStageTriple(
                snap,
                Stage.GHOST_FEATURES, "features",
                Stage.PASS_BATCH_REBUILD, "batch",
                Stage.MESH_BAKE, "bake"
        ));
        lines.add(String.format(
                Locale.ROOT,
                "chunks: %d mesh: %d ent: %d  cull: %d/%d",
                snap.chunkCount(),
                snap.meshChunkCount(),
                snap.entityCount(),
                snap.chunksKept(),
                snap.chunksCulled()
        ));
        double bakeAvg = snap.avgStageMs().getOrDefault(Stage.MESH_BAKE, 0.0);
        if (bakeAvg > 0.0 || snap.avgBakeCount() > 0.0 || snap.avgBakeSkipCount() > 0.0) {
            lines.add(String.format(
                    Locale.ROOT,
                    "bakeAvg: %.2fms  bakeCount/frame: %.2f  bakeSkip/frame: %.2f",
                    bakeAvg,
                    snap.avgBakeCount(),
                    snap.avgBakeSkipCount()
            ));
        }
        lines.add(String.format(
                Locale.ROOT,
                "ent upd: %.2f  spawn/rm: %.2f/%.2f  poseΔ: %.4f  partial: %.3f  itemAge: %.2f  id/adv: %.2f/%.2f",
                snap.avgEntityUpdates(),
                snap.avgEntitySpawns(),
                snap.avgEntityRemoves(),
                snap.avgMaxPoseDelta(),
                snap.partialTickUsed(),
                snap.itemAgeInTicks(),
                snap.avgIdentityInterp(),
                snap.avgAdvanceInterp()
        ));
        if (snap.serverDiag() != null && snap.serverDiag().isPresent()) {
            ServerDiag srv = snap.serverDiag();
            lines.add(String.format(
                    Locale.ROOT,
                    "srv mspt: %.1f  sync: %.2fms  upd: %d  spawn: %d  resync: %d  viewers: %d",
                    srv.msptMs(),
                    srv.syncFlushMs(),
                    srv.entityUpdates(),
                    srv.entitySpawns(),
                    srv.fullResyncs(),
                    srv.viewers()
            ));
        }
        if (snap.maxAvgStage() != null) {
            lines.add(String.format(
                    Locale.ROOT,
                    "maxAvg: %s (%.2fms)",
                    snap.maxAvgStage().label(),
                    snap.avgStageMs().getOrDefault(snap.maxAvgStage(), 0.0)
            ));
        }
        return lines;
    }

    static double updateEma(double previous, double sampleMs, double alpha) {
        if (previous <= 0.0) {
            return sampleMs;
        }
        return previous * (1.0 - alpha) + sampleMs * alpha;
    }

    static double nsToMs(long ns) {
        return ns / 1_000_000.0;
    }

    static Stage findMaxStage(EnumMap<Stage, Long> stages) {
        Stage max = null;
        long maxNs = 0L;
        for (Stage stage : Stage.HUD_ORDER) {
            long ns = stages.getOrDefault(stage, 0L);
            if (ns > maxNs) {
                maxNs = ns;
                max = stage;
            }
        }
        return max;
    }

    static Stage findMaxAvgStage(EnumMap<Stage, Double> avgMs) {
        Stage max = null;
        double maxMs = 0.0;
        for (Stage stage : Stage.HUD_ORDER) {
            double ms = avgMs.getOrDefault(stage, 0.0);
            if (ms > maxMs) {
                maxMs = ms;
                max = stage;
            }
        }
        return max;
    }

    static double windowAverage(double[] samples, int count) {
        if (count <= 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = 0; i < count; i++) {
            sum += samples[i];
        }
        return sum / count;
    }

    static String shortId(UUID id) {
        if (id == null) {
            return "????????";
        }
        return id.toString().substring(0, 8);
    }

    /** Test helper: push a synthetic sample without requiring config/timers. */
    static void pushSampleForTest(EnumMap<Stage, Double> stageMs, double totalMs, int bakeCount) {
        pushSampleForTest(stageMs, totalMs, bakeCount, 0);
    }

    static void pushSampleForTest(EnumMap<Stage, Double> stageMs, double totalMs, int bakeCount, int bakeSkipCount) {
        pushSampleForTest(stageMs, totalMs, bakeCount, bakeSkipCount, 0, 0.0, 0, 0, Float.NaN, Float.NaN);
    }

    static void pushSampleForTest(
            EnumMap<Stage, Double> stageMs,
            double totalMs,
            int bakeCount,
            int bakeSkipCount,
            int entityUpdates,
            double maxPoseDelta,
            int identityInterp,
            int advanceInterp,
            float partialTick,
            float itemAgeInTicks
    ) {
        EnumMap<Stage, Double> copy = MapCopy.copyDoubles(stageMs);
        for (Stage stage : Stage.HUD_ORDER) {
            copy.putIfAbsent(stage, 0.0);
        }
        pushHistory(new FrameSample(
                copy,
                totalMs,
                bakeCount,
                bakeSkipCount,
                entityUpdates,
                0,
                0,
                maxPoseDelta,
                identityInterp,
                advanceInterp,
                partialTick,
                itemAgeInTicks
        ));
        totalEmaMs = updateEma(totalEmaMs, totalMs, EMA_ALPHA);
        display = buildDisplay(new Snapshot(
                activeKey != null ? activeKey : PortalKey.soto(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                outcome == Outcome.IDLE ? Outcome.RENDERED : outcome,
                new EnumMap<>(Stage.class),
                chunkCount,
                meshChunkCount,
                entityCount,
                chunksKept,
                chunksCulled,
                0L,
                bakeCount,
                0L,
                totalMs,
                totalEmaMs,
                findMaxAvgStage(copy),
                partialTick,
                itemAgeInTicks
        ));
    }

    static void setIdentityForTest(PortalKey key, Outcome outcomeValue) {
        activeKey = key;
        outcome = outcomeValue;
    }

    static void resetForTests() {
        STAGE_NS.clear();
        for (int i = 0; i < HISTORY.length; i++) {
            HISTORY[i] = null;
        }
        historySize = 0;
        historyWrite = 0;
        activeKey = null;
        outcome = Outcome.IDLE;
        chunkCount = 0;
        meshChunkCount = 0;
        entityCount = 0;
        chunksKept = 0;
        chunksCulled = 0;
        meshBakeNsThisFrame = 0L;
        meshBakeCountThisFrame = 0;
        meshBakeSkipCountThisFrame = 0;
        lastMeshBakeNs = 0L;
        entityUpdatesThisFrame = 0;
        entitySpawnsThisFrame = 0;
        entityRemovesThisFrame = 0;
        maxPoseDeltaThisFrame = 0.0;
        identityInterpThisFrame = 0;
        advanceInterpThisFrame = 0;
        partialTickThisFrame = Float.NaN;
        itemAgeInTicksThisFrame = Float.NaN;
        LAST_POSES.clear();
        SAMPLED_THIS_FRAME.clear();
        serverDiag = ServerDiag.NONE;
        published = Snapshot.IDLE;
        display = DisplaySnapshot.IDLE;
        totalEmaMs = 0.0;
        wasEnabled = false;
        enabledOverrideForTest = null;
    }

    static void setEnabledOverrideForTest(Boolean enabled) {
        enabledOverrideForTest = enabled;
    }

    private record PoseTrackKey(PortalStreamKind kind, UUID tardisId, UUID entityUuid) {
    }

    private record PoseXyz(double x, double y, double z) {
    }

    private static void pushHistory(FrameSample sample) {
        HISTORY[historyWrite] = sample;
        historyWrite = (historyWrite + 1) % HISTORY_FRAMES;
        if (historySize < HISTORY_FRAMES) {
            historySize++;
        }
    }

    private static FrameSample historyAt(int chronologicalIndex) {
        int start = (historyWrite - historySize + HISTORY_FRAMES) % HISTORY_FRAMES;
        return HISTORY[(start + chronologicalIndex) % HISTORY_FRAMES];
    }

    private static DisplaySnapshot buildDisplay(Snapshot latest) {
        int window = Math.min(AVG_WINDOW, historySize);
        EnumMap<Stage, Double> avgStages = new EnumMap<>(Stage.class);
        for (Stage stage : Stage.HUD_ORDER) {
            double[] samples = new double[window];
            for (int i = 0; i < window; i++) {
                FrameSample sample = historyAt(historySize - window + i);
                samples[i] = sample.stageMs().getOrDefault(stage, 0.0);
            }
            avgStages.put(stage, windowAverage(samples, window));
        }
        double[] totals = new double[window];
        double[] bakeCounts = new double[window];
        double[] bakeSkipCounts = new double[window];
        double[] entityUpdates = new double[window];
        double[] entitySpawns = new double[window];
        double[] entityRemoves = new double[window];
        double[] maxPoseDeltas = new double[window];
        double[] identityInterps = new double[window];
        double[] advanceInterps = new double[window];
        for (int i = 0; i < window; i++) {
            FrameSample sample = historyAt(historySize - window + i);
            totals[i] = sample.totalMs();
            bakeCounts[i] = sample.bakeCount();
            bakeSkipCounts[i] = sample.bakeSkipCount();
            entityUpdates[i] = sample.entityUpdates();
            entitySpawns[i] = sample.entitySpawns();
            entityRemoves[i] = sample.entityRemoves();
            maxPoseDeltas[i] = sample.maxPoseDelta();
            identityInterps[i] = sample.identityInterp();
            advanceInterps[i] = sample.advanceInterp();
        }
        double avgTotal = windowAverage(totals, window);
        double avgBake = windowAverage(bakeCounts, window);
        double avgBakeSkip = windowAverage(bakeSkipCounts, window);
        Stage maxAvg = findMaxAvgStage(avgStages);
        float partial = latest.partialTickUsed();
        float itemAge = latest.itemAgeInTicks();
        if (window > 0) {
            FrameSample newest = historyAt(historySize - 1);
            if (Float.isNaN(partial) && !Float.isNaN(newest.partialTick())) {
                partial = newest.partialTick();
            }
            if (Float.isNaN(itemAge) && !Float.isNaN(newest.itemAgeInTicks())) {
                itemAge = newest.itemAgeInTicks();
            }
        }
        return new DisplaySnapshot(
                latest.key(),
                latest.outcome(),
                MapCopy.copyDoubles(avgStages),
                latest.chunkCount(),
                latest.meshChunkCount(),
                latest.entityCount(),
                latest.chunksKept(),
                latest.chunksCulled(),
                avgTotal,
                latest.totalEmaMs(),
                window,
                avgBake,
                avgBakeSkip,
                maxAvg,
                windowAverage(entityUpdates, window),
                windowAverage(entitySpawns, window),
                windowAverage(entityRemoves, window),
                windowAverage(maxPoseDeltas, window),
                Float.isNaN(partial) ? 0.0f : partial,
                Float.isNaN(itemAge) ? 0.0f : itemAge,
                windowAverage(identityInterps, window),
                windowAverage(advanceInterps, window),
                serverDiag
        );
    }

    private static String formatStagePair(DisplaySnapshot snap, Stage a, String la, Stage b, String lb) {
        return markMax(snap, a, la) + formatAvgMs(snap, a)
                + "  " + markMax(snap, b, lb) + formatAvgMs(snap, b);
    }

    private static String formatStageTriple(
            DisplaySnapshot snap,
            Stage a, String la,
            Stage b, String lb,
            Stage c, String lc
    ) {
        return markMax(snap, a, la) + formatAvgMs(snap, a)
                + "  " + markMax(snap, b, lb) + formatAvgMs(snap, b)
                + "  " + markMax(snap, c, lc) + formatAvgMs(snap, c);
    }

    private static String formatStageQuad(
            DisplaySnapshot snap,
            Stage a, String la,
            Stage b, String lb,
            Stage c, String lc,
            Stage d, String ld
    ) {
        return markMax(snap, a, la) + formatAvgMs(snap, a)
                + "  " + markMax(snap, b, lb) + formatAvgMs(snap, b)
                + "  " + markMax(snap, c, lc) + formatAvgMs(snap, c)
                + "  " + markMax(snap, d, ld) + formatAvgMs(snap, d);
    }

    private static String markMax(DisplaySnapshot snap, Stage stage, String label) {
        if (snap.maxAvgStage() == stage) {
            return "*" + label + ": ";
        }
        return label + ": ";
    }

    private static String formatAvgMs(DisplaySnapshot snap, Stage stage) {
        return String.format(Locale.ROOT, "%.2f", snap.avgStageMs().getOrDefault(stage, 0.0));
    }

    public enum Stage {
        FLUSH_TOTAL("flush"),
        OFF_MAIN_TOTAL("offMain"),
        SKY_FOG("skyFog"),
        TERRAIN_OPAQUE("opaque"),
        TERRAIN_CUTOUT("cutout"),
        TERRAIN_TRANSLUCENT("trans"),
        GHOST_FEATURES("features"),
        MESH_BAKE("bake"),
        PASS_BATCH_REBUILD("batch"),
        COMPOSITE("composite");

        static final Stage[] HUD_ORDER = {
                FLUSH_TOTAL,
                OFF_MAIN_TOTAL,
                SKY_FOG,
                TERRAIN_OPAQUE,
                TERRAIN_CUTOUT,
                TERRAIN_TRANSLUCENT,
                GHOST_FEATURES,
                PASS_BATCH_REBUILD,
                MESH_BAKE,
                COMPOSITE
        };

        /** Leaf stages for stacked bar (excludes nested flush/offMain totals). */
        public static final Stage[] LEAF_STAGES = {
                SKY_FOG,
                TERRAIN_OPAQUE,
                TERRAIN_CUTOUT,
                TERRAIN_TRANSLUCENT,
                GHOST_FEATURES,
                PASS_BATCH_REBUILD,
                MESH_BAKE,
                COMPOSITE
        };

        private final String label;

        Stage(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum Outcome {
        IDLE("idle"),
        SCHEDULED("scheduled"),
        NOT_READY("notReady"),
        ONCE_PER_FRAME("oncePerFrame"),
        FRAME_CACHE_HIT("frameCacheHit"),
        RENDERED("rendered"),
        FBO_FAIL("fboFail");

        private final String label;

        Outcome(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public record Snapshot(
            PortalKey key,
            Outcome outcome,
            EnumMap<Stage, Long> stageNs,
            int chunkCount,
            int meshChunkCount,
            int entityCount,
            int chunksKept,
            int chunksCulled,
            long meshBakeNs,
            int meshBakeCount,
            long lastMeshBakeNs,
            double totalMs,
            double totalEmaMs,
            Stage maxStage,
            float partialTickUsed,
            float itemAgeInTicks
    ) {
        static final Snapshot IDLE = new Snapshot(
                null,
                Outcome.IDLE,
                new EnumMap<>(Stage.class),
                0, 0, 0, 0, 0,
                0L, 0, 0L,
                0.0, 0.0,
                null,
                Float.NaN,
                Float.NaN
        );
    }

    public record DisplaySnapshot(
            PortalKey key,
            Outcome outcome,
            EnumMap<Stage, Double> avgStageMs,
            int chunkCount,
            int meshChunkCount,
            int entityCount,
            int chunksKept,
            int chunksCulled,
            double avgTotalMs,
            double emaTotalMs,
            int windowCount,
            double avgBakeCount,
            double avgBakeSkipCount,
            Stage maxAvgStage,
            double avgEntityUpdates,
            double avgEntitySpawns,
            double avgEntityRemoves,
            double avgMaxPoseDelta,
            float partialTickUsed,
            float itemAgeInTicks,
            double avgIdentityInterp,
            double avgAdvanceInterp,
            ServerDiag serverDiag
    ) {
        static final DisplaySnapshot IDLE = new DisplaySnapshot(
                null,
                Outcome.IDLE,
                new EnumMap<>(Stage.class),
                0, 0, 0, 0, 0,
                0.0, 0.0, 0, 0.0, 0.0,
                null,
                0.0, 0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0, 0.0,
                ServerDiag.NONE
        );

        DisplaySnapshot withServerDiag(ServerDiag diag) {
            return new DisplaySnapshot(
                    key,
                    outcome,
                    avgStageMs,
                    chunkCount,
                    meshChunkCount,
                    entityCount,
                    chunksKept,
                    chunksCulled,
                    avgTotalMs,
                    emaTotalMs,
                    windowCount,
                    avgBakeCount,
                    avgBakeSkipCount,
                    maxAvgStage,
                    avgEntityUpdates,
                    avgEntitySpawns,
                    avgEntityRemoves,
                    avgMaxPoseDelta,
                    partialTickUsed,
                    itemAgeInTicks,
                    avgIdentityInterp,
                    avgAdvanceInterp,
                    diag == null ? ServerDiag.NONE : diag
            );
        }
    }

    public record ServerDiag(
            float msptMs,
            float syncFlushMs,
            float syncEntitiesMs,
            int entityUpdates,
            int entitySpawns,
            int entityRemoves,
            int fullResyncs,
            int viewers,
            int activeStreams,
            int serverTick
    ) {
        static final ServerDiag NONE = new ServerDiag(
                Float.NaN, 0.0f, 0.0f, 0, 0, 0, 0, 0, 0, 0
        );

        static ServerDiag fromPayload(SyncPortalPerfS2CPayload payload) {
            return new ServerDiag(
                    payload.msptMs(),
                    payload.syncFlushMs(),
                    payload.syncEntitiesMs(),
                    payload.entityUpdates(),
                    payload.entitySpawns(),
                    payload.entityRemoves(),
                    payload.fullResyncs(),
                    payload.viewers(),
                    payload.activeStreams(),
                    payload.serverTick()
            );
        }

        public boolean isPresent() {
            return !Float.isNaN(msptMs);
        }
    }

    private record FrameSample(
            EnumMap<Stage, Double> stageMs,
            double totalMs,
            int bakeCount,
            int bakeSkipCount,
            int entityUpdates,
            int entitySpawns,
            int entityRemoves,
            double maxPoseDelta,
            int identityInterp,
            int advanceInterp,
            float partialTick,
            float itemAgeInTicks
    ) {
    }

    private static final class MapCopy {
        private MapCopy() {
        }

        static EnumMap<Stage, Long> copy(EnumMap<Stage, Long> source) {
            EnumMap<Stage, Long> copy = new EnumMap<>(Stage.class);
            copy.putAll(source);
            return copy;
        }

        static EnumMap<Stage, Double> copyDoubles(EnumMap<Stage, Double> source) {
            EnumMap<Stage, Double> copy = new EnumMap<>(Stage.class);
            copy.putAll(source);
            return copy;
        }
    }
}
