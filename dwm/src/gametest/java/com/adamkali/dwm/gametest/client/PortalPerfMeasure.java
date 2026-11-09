package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.render.portal.PortalScheduler;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Measures what a door portal costs the client in a given camera state: frame time (mean / p95 / max) and how
 * many portal scenes were scheduled vs culled. Results are written to {@code perf/portal_perf.json} in the run
 * directory so the numbers can be compared between runs.
 */
final class PortalPerfMeasure {
    private static final int WARMUP_TICKS = 20;
    private static final Object LOCK = new Object();
    private static final List<Double> FRAME_MS = new ArrayList<>();
    private static boolean installed;
    private static long lastFrameNs;

    private PortalPerfMeasure() {
    }

    record Sample(String name, int frames, double meanFrameMs, double p95FrameMs, double maxFrameMs,
                  long scheduled, long culled) {
        String json() {
            return String.format(Locale.ROOT,
                    "{\"name\":\"%s\",\"frames\":%d,\"meanFrameMs\":%.3f,\"p95FrameMs\":%.3f,"
                            + "\"maxFrameMs\":%.3f,\"scheduled\":%d,\"culled\":%d}",
                    name, frames, meanFrameMs, p95FrameMs, maxFrameMs, scheduled, culled);
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%-24s frames=%-4d mean=%7.2fms p95=%7.2fms max=%7.2fms scheduled=%d culled=%d",
                    name, frames, meanFrameMs, p95FrameMs, maxFrameMs, scheduled, culled);
        }
    }

    /** Registers the frame timer once; safe to call repeatedly. */
    static void install() {
        synchronized (LOCK) {
            if (installed) {
                return;
            }
            installed = true;
        }
        LevelRenderEvents.START_MAIN.register(ctx -> {
            long now = System.nanoTime();
            synchronized (LOCK) {
                if (lastFrameNs != 0L) {
                    FRAME_MS.add((now - lastFrameNs) / 1_000_000.0);
                }
                lastFrameNs = now;
            }
        });
    }

    /** Lets the scene settle, then records {@code ticks} ticks of frames and the schedule/cull counter deltas. */
    static Sample measure(ClientGameTestContext context, String name, int ticks) {
        install();
        context.waitTicks(WARMUP_TICKS);
        long scheduled0 = PortalScheduler.scheduledTotal();
        long culled0 = PortalScheduler.culledTotal();
        synchronized (LOCK) {
            FRAME_MS.clear();
            lastFrameNs = 0L;
        }
        context.waitTicks(ticks);
        long scheduled = PortalScheduler.scheduledTotal() - scheduled0;
        long culled = PortalScheduler.culledTotal() - culled0;
        double[] frames;
        synchronized (LOCK) {
            frames = FRAME_MS.stream().mapToDouble(Double::doubleValue).toArray();
        }
        if (frames.length == 0) {
            throw new AssertionError("No frames rendered while measuring '" + name + "'");
        }
        Arrays.sort(frames);
        double sum = Arrays.stream(frames).sum();
        double p95 = frames[Math.min(frames.length - 1, (int) Math.ceil(frames.length * 0.95) - 1)];
        Sample sample = new Sample(name, frames.length, sum / frames.length, p95, frames[frames.length - 1],
                scheduled, culled);
        System.out.println("[portal-perf] " + sample);
        return sample;
    }

    static void writeReport(String fileName, List<Sample> samples) {
        Path dir = FabricLoader.getInstance().getGameDir().resolve("perf");
        StringBuilder sb = new StringBuilder("{\n  \"samples\": [\n");
        for (int i = 0; i < samples.size(); i++) {
            sb.append("    ").append(samples.get(i).json()).append(i + 1 < samples.size() ? ",\n" : "\n");
        }
        sb.append("  ]\n}\n");
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(fileName), sb.toString());
        } catch (IOException e) {
            throw new AssertionError("Could not write perf report " + fileName, e);
        }
    }

    /**
     * Asserts the work counters for the culling states and a generous frame-time ceiling.
     * Frame times are only compared loosely: software GL is far too noisy for tight limits.
     */
    static void assertCullingEffective(String portal, Sample baseline, Sample culled, Sample visible) {
        if (culled.scheduled() != 0 || culled.culled() == 0) {
            throw new AssertionError(portal + ": culled state must schedule nothing and cull every frame, got " + culled);
        }
        if (visible.scheduled() == 0) {
            throw new AssertionError(portal + ": visible state never scheduled the portal: " + visible);
        }
        if (visible.culled() != 0) {
            throw new AssertionError(portal + ": visible state must not be culled: " + visible);
        }
        if (baseline.scheduled() != 0) {
            throw new AssertionError(portal + ": door closed must not schedule: " + baseline);
        }
        // Culled should not cost meaningfully more than a closed door.
        if (culled.meanFrameMs() > baseline.meanFrameMs() * 3.0 + 20.0) {
            throw new AssertionError(portal + ": culled frames are much slower than baseline: " + culled + " vs " + baseline);
        }
        // Visible portals must stay in the same order of magnitude as the empty scene.
        if (visible.meanFrameMs() > baseline.meanFrameMs() * 25.0 + 500.0) {
            throw new AssertionError(portal + ": visible portal frame time regressed: " + visible + " vs " + baseline);
        }
    }
}
