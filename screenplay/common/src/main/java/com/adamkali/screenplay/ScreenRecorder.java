package com.adamkali.screenplay;

import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Captures the Minecraft client display via ffmpeg x11grab into an MP4 under
 * {@code {gameDirectory}/recordings/{id}.mp4}.
 */
public final class ScreenRecorder {
    public static final String RECORD_PROPERTY = "screenplay.record";

    private static final int STOP_WAIT_SECONDS = 8;
    private static final int DESTROY_WAIT_SECONDS = 3;
    private static final int DEFAULT_FPS = 30;

    private final Logger logger;
    private final boolean enabled;
    private final Object lock = new Object();

    private Process process;
    private Path outputPath;
    private Path logFile;
    private Thread shutdownHook;
    private boolean started;
    private boolean stopped;

    ScreenRecorder(Logger logger, boolean enabled) {
        this.logger = logger;
        this.enabled = enabled;
    }

    /**
     * Reads the optional CLI override from {@link #RECORD_PROPERTY}.
     *
     * @return {@code true}/{@code false} when set, or {@code null} when unset
     */
    public static Boolean readCliOverride() {
        String value = System.getProperty(RECORD_PROPERTY);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if ("true".equalsIgnoreCase(trimmed)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmed)) {
            return false;
        }
        throw new ScenarioException("System property '" + RECORD_PROPERTY
                + "' must be true or false, but was '" + value + "'");
    }

    /**
     * Explicit CLI override wins; otherwise use the YAML frontmatter flag.
     */
    public static boolean resolveRecord(Boolean cliOverride, boolean yamlRecord) {
        if (cliOverride != null) {
            return cliOverride;
        }
        return yamlRecord;
    }

    void ensureStarted(Minecraft client, String scenarioId) {
        if (!enabled) {
            return;
        }
        synchronized (lock) {
            if (started || stopped) {
                return;
            }
            start(client, scenarioId);
            started = true;
        }
    }

    void stop() {
        synchronized (lock) {
            if (stopped) {
                return;
            }
            stopped = true;
            if (process == null) {
                removeShutdownHook();
                return;
            }
            try {
                if (process.isAlive()) {
                    try {
                        OutputStream stdin = process.getOutputStream();
                        stdin.write('q');
                        stdin.flush();
                    } catch (IOException ignored) {
                        // Fall through to destroy if stdin is closed.
                    }
                    if (!process.waitFor(STOP_WAIT_SECONDS, TimeUnit.SECONDS)) {
                        process.destroy();
                        if (!process.waitFor(DESTROY_WAIT_SECONDS, TimeUnit.SECONDS)) {
                            process.destroyForcibly();
                            process.waitFor(DESTROY_WAIT_SECONDS, TimeUnit.SECONDS);
                        }
                    }
                }
                if (outputPath != null && Files.isRegularFile(outputPath)) {
                    logger.info("Screen recording saved {}", outputPath.toAbsolutePath());
                } else if (started) {
                    logger.warn("Screen recording did not produce an output file"
                            + (logFile == null ? "" : "; see " + logFile.toAbsolutePath()));
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            } finally {
                process = null;
                removeShutdownHook();
            }
        }
    }

    private void start(Minecraft client, String scenarioId) {
        if (scenarioId == null || scenarioId.isBlank()) {
            throw new ScenarioException("Screen recording requires a non-empty scenario id");
        }
        String display = System.getenv("DISPLAY");
        if (display == null || display.isBlank()) {
            throw new ScenarioException(
                    "Screen recording requires $DISPLAY (use -PscreenplayDisplay=display or xvfb)");
        }
        if (!ffmpegAvailable()) {
            throw new ScenarioException(
                    "Screen recording requires ffmpeg on PATH. Install with: apt install ffmpeg");
        }

        Path recordingsDir = client.gameDirectory.toPath().resolve("recordings");
        try {
            Files.createDirectories(recordingsDir);
        } catch (IOException exception) {
            throw new ScenarioException("Could not create recordings directory " + recordingsDir, exception);
        }
        outputPath = recordingsDir.resolve(sanitizeFileName(scenarioId) + ".mp4");
        logFile = recordingsDir.resolve(sanitizeFileName(scenarioId) + ".ffmpeg.log");

        int width = evenPositive(client.getWindow().getWidth(), 1280);
        int height = evenPositive(client.getWindow().getHeight(), 720);
        List<String> command = buildFfmpegCommand(display, width, height, outputPath);

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.to(logFile.toFile()));

        try {
            process = builder.start();
        } catch (IOException exception) {
            throw new ScenarioException("Could not start ffmpeg screen recording", exception);
        }
        if (!process.isAlive()) {
            throw new ScenarioException("ffmpeg exited immediately while starting screen recording"
                    + (logFile == null ? "" : ". See " + logFile.toAbsolutePath()));
        }

        shutdownHook = new Thread(this::stop, "screenplay-screen-recorder-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        logger.info(
                "Started screen recording pid {} -> {} ({}x{} @ {}fps on DISPLAY={})",
                process.pid(),
                outputPath.toAbsolutePath(),
                width,
                height,
                DEFAULT_FPS,
                display
        );
    }

    static List<String> buildFfmpegCommand(String display, int width, int height, Path outputPath) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-f");
        command.add("x11grab");
        command.add("-framerate");
        command.add(Integer.toString(DEFAULT_FPS));
        command.add("-video_size");
        command.add(width + "x" + height);
        command.add("-i");
        command.add(display);
        command.add("-c:v");
        command.add("libx264");
        command.add("-pix_fmt");
        command.add("yuv420p");
        command.add("-preset");
        command.add("ultrafast");
        command.add(outputPath.toAbsolutePath().toString());
        return command;
    }

    static String sanitizeFileName(String id) {
        String trimmed = id.trim();
        if (trimmed.isEmpty()) {
            throw new ScenarioException("Screen recording id must be non-empty");
        }
        if (trimmed.contains("/") || trimmed.contains("\\") || trimmed.contains("..")) {
            throw new ScenarioException("Screen recording id must not contain path separators: '" + id + "'");
        }
        return trimmed;
    }

    static int evenPositive(int value, int fallback) {
        int resolved = value > 0 ? value : fallback;
        if ((resolved & 1) != 0) {
            resolved -= 1;
        }
        return Math.max(resolved, 2);
    }

    private static boolean ffmpegAvailable() {
        try {
            Process which = new ProcessBuilder("which", "ffmpeg").redirectErrorStream(true).start();
            if (!which.waitFor(5, TimeUnit.SECONDS)) {
                which.destroyForcibly();
                return false;
            }
            return which.exitValue() == 0;
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    private void removeShutdownHook() {
        if (shutdownHook == null) {
            return;
        }
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException ignored) {
            // JVM is already shutting down.
        }
        shutdownHook = null;
    }
}
