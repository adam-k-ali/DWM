package com.adamkali.dwm.render.portal;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

import net.minecraft.client.Minecraft;

/**
 * Appends ~1 Hz JSONL portal-perf samples while the debug HUD is enabled.
 * Truncated once per client launch via {@link #resetForSession()}.
 */
public final class PortalPerfDebugLog {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_NAME = "dwm-portal-perf.jsonl";
    private static final long APPEND_INTERVAL_MS = 1000L;

    private static BufferedWriter writer;
    private static long lastAppendMs;
    private static boolean ioFailedLogged;
    private static Path logPath;

    private PortalPerfDebugLog() {
    }

    /**
     * Truncates (or creates) the session log file under {@code {gameDir}/logs/}.
     * Call once from client init so each game start begins a fresh log.
     */
    public static void resetForSession() {
        close();
        ioFailedLogged = false;
        lastAppendMs = 0L;
        try {
            Path path = resolveLogPath();
            if (path == null) {
                return;
            }
            Files.createDirectories(path.getParent());
            Files.writeString(path, "", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logPath = path;
        } catch (IOException e) {
            logIoFailure("Failed to truncate portal perf log", e);
        }
    }

    public static void maybeAppend(PortalPerfStats.DisplaySnapshot snap) {
        if (snap == null || snap == PortalPerfStats.DisplaySnapshot.IDLE || snap.key() == null) {
            return;
        }
        if (!PortalPerfStats.isEnabled()) {
            close();
            return;
        }
        long now = System.currentTimeMillis();
        if (lastAppendMs > 0L && now - lastAppendMs < APPEND_INTERVAL_MS) {
            return;
        }
        lastAppendMs = now;
        String line = formatLine(now, snap);
        appendLine(line);
    }

    public static void close() {
        BufferedWriter current = writer;
        writer = null;
        if (current == null) {
            return;
        }
        try {
            current.flush();
            current.close();
        } catch (IOException e) {
            logIoFailure("Failed to close portal perf log", e);
        }
    }

    /** Pure formatter for tests. */
    static String formatLine(long tsMs, PortalPerfStats.DisplaySnapshot snap) {
        PortalKey key = snap.key();
        StringBuilder sb = new StringBuilder(256);
        sb.append('{');
        field(sb, "ts", tsMs, true);
        field(sb, "kind", key != null ? key.kind().name() : "null", false);
        field(sb, "tardisId", key != null ? key.tardisId().toString() : "", false);
        field(sb, "outcome", snap.outcome().label(), false);
        field(sb, "avgTotalMs", snap.avgTotalMs(), true);
        field(sb, "emaTotalMs", snap.emaTotalMs(), true);
        field(sb, "window", snap.windowCount(), true);
        sb.append("\"avgStages\":{");
        boolean firstStage = true;
        for (PortalPerfStats.Stage stage : PortalPerfStats.Stage.HUD_ORDER) {
            if (!firstStage) {
                sb.append(',');
            }
            firstStage = false;
            sb.append('"').append(stage.label()).append("\":");
            appendNumber(sb, snap.avgStageMs().getOrDefault(stage, 0.0));
        }
        sb.append("},");
        field(sb, "chunks", snap.chunkCount(), true);
        field(sb, "mesh", snap.meshChunkCount(), true);
        field(sb, "entities", snap.entityCount(), true);
        field(sb, "cullKept", snap.chunksKept(), true);
        field(sb, "cullCulled", snap.chunksCulled(), true);
        field(sb, "avgBakeCount", snap.avgBakeCount(), true);
        field(sb, "avgBakeSkipCount", snap.avgBakeSkipCount(), true);
        field(sb, "avgEntityUpdates", snap.avgEntityUpdates(), true);
        field(sb, "avgEntitySpawns", snap.avgEntitySpawns(), true);
        field(sb, "avgEntityRemoves", snap.avgEntityRemoves(), true);
        field(sb, "maxPoseDelta", snap.avgMaxPoseDelta(), true);
        field(sb, "partialTickUsed", (double) snap.partialTickUsed(), true);
        field(sb, "itemAgeInTicks", (double) snap.itemAgeInTicks(), true);
        field(sb, "avgIdentityInterp", snap.avgIdentityInterp(), true);
        field(sb, "avgAdvanceInterp", snap.avgAdvanceInterp(), true);
        PortalPerfStats.ServerDiag srv = snap.serverDiag();
        if (srv != null && srv.isPresent()) {
            field(sb, "msptMs", (double) srv.msptMs(), true);
            field(sb, "syncFlushMs", (double) srv.syncFlushMs(), true);
            field(sb, "srvEntityUpdates", srv.entityUpdates(), true);
            field(sb, "srvEntitySpawns", srv.entitySpawns(), true);
            field(sb, "srvFullResyncs", srv.fullResyncs(), true);
            field(sb, "srvViewers", srv.viewers(), true);
        }
        // field() always appends comma — strip final comma before closing
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }
        sb.append('}');
        return sb.toString();
    }

    static Path resolveLogPathForTest(Path gameDir) {
        return gameDir.resolve("logs").resolve(FILE_NAME);
    }

    private static Path resolveLogPath() {
        if (logPath != null) {
            return logPath;
        }
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.gameDirectory == null) {
            return null;
        }
        logPath = client.gameDirectory.toPath().resolve("logs").resolve(FILE_NAME);
        return logPath;
    }

    private static void appendLine(String line) {
        try {
            Path path = resolveLogPath();
            if (path == null) {
                return;
            }
            if (writer == null) {
                Files.createDirectories(path.getParent());
                writer = Files.newBufferedWriter(
                        path,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            }
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            logIoFailure("Failed to append portal perf log", e);
            close();
        }
    }

    private static void field(StringBuilder sb, String name, Object value, boolean numeric) {
        sb.append('"').append(name).append("\":");
        if (numeric) {
            if (value instanceof Double d) {
                appendNumber(sb, d);
            } else if (value instanceof Float f) {
                appendNumber(sb, f);
            } else {
                sb.append(value);
            }
        } else {
            sb.append('"').append(escape(String.valueOf(value))).append('"');
        }
        sb.append(',');
    }

    private static void appendNumber(StringBuilder sb, double value) {
        sb.append(String.format(Locale.ROOT, "%.4f", value));
    }

    private static String escape(String raw) {
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void logIoFailure(String message, Exception e) {
        if (ioFailedLogged) {
            return;
        }
        ioFailedLogged = true;
        LOGGER.warn(message, e);
    }

    static void resetForTests() {
        close();
        lastAppendMs = 0L;
        ioFailedLogged = false;
        logPath = null;
    }
}
