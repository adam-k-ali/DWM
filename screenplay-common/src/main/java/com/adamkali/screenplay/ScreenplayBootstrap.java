package com.adamkali.screenplay;

import com.adamkali.screenplay.platform.ScreenplayPlatform;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Shared client bootstrap used by Fabric / Forge / NeoForge entrypoints.
 */
public final class ScreenplayBootstrap {
    public static final String MOD_ID = "screenplay";
    public static final String SCENARIO_PROPERTY = "screenplay";
    public static final String REPORT_PROPERTY = "screenplay.report-file";
    public static final String TIMEOUT_PROPERTY = "screenplay.step-timeout-seconds";
    /** Path-separator list of filesystem scenario roots (set by the Screenplay Gradle plugin). */
    public static final String TESTS_DIRS_PROPERTY = "screenplay.tests-dirs";

    private static final Logger LOGGER = LoggerFactory.getLogger("Screenplay");

    private ScreenplayBootstrap() {
    }

    public static void start(ScreenplayPlatform platform) {
        String scenarioId = System.getProperty(SCENARIO_PROPERTY, "createWorld");
        ScenarioReportWriter reportWriter = new ScenarioReportWriter(Path.of(
                System.getProperty(REPORT_PROPERTY, "build/screenplay/report.xml")
        ));
        try {
            ScenarioCatalog catalog = loadCatalog();
            ScenarioPlan plan = new ScenarioCompiler(catalog).compile(scenarioId);
            Duration timeout = Duration.ofSeconds(readPositiveLong(TIMEOUT_PROPERTY, 30L));
            ScenarioRunner runner = new ScenarioRunner(plan, reportWriter, timeout, LOGGER);
            runner.startWatchdog();
            platform.registerEndClientTick(runner::tick);
            startMainThreadPump(runner);
            LOGGER.info("Loaded scenario '{}' with {} primitive steps", plan.id(), plan.steps().size());
        } catch (RuntimeException exception) {
            LOGGER.error("Could not start scenario '{}'", scenarioId, exception);
            try {
                reportWriter.writeBootstrapFailure(scenarioId, exception);
            } catch (RuntimeException reportFailure) {
                LOGGER.error("Could not write scenario bootstrap failure", reportFailure);
            }
            System.exit(1);
        }
    }

    /**
     * Pump scenario steps onto the Minecraft main thread at ~20 Hz.
     * Loader tick events can stall under headless Forge/NeoForge while frames still render;
     * {@link Minecraft#execute(Runnable)} keeps Screenplay advancing regardless.
     */
    private static void startMainThreadPump(ScenarioRunner runner) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "screenplay-pump");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Minecraft client = Minecraft.getInstance();
                if (client != null) {
                    client.execute(() -> runner.tick(client));
                }
            } catch (Throwable ignored) {
                // Minecraft may not be ready yet; keep pumping.
            }
        }, 100L, 50L, TimeUnit.MILLISECONDS);
    }

    private static ScenarioCatalog loadCatalog() {
        List<Path> filesystemRoots = readTestsDirs();
        ClassLoader own = ScreenplayBootstrap.class.getClassLoader();
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        return ScenarioCatalog.load(filesystemRoots, own, context);
    }

    private static List<Path> readTestsDirs() {
        String raw = System.getProperty(TESTS_DIRS_PROPERTY);
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<Path> roots = new java.util.ArrayList<>();
        for (String entry : raw.split(java.util.regex.Pattern.quote(File.pathSeparator))) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            roots.add(Path.of(entry.trim()));
        }
        return List.copyOf(roots);
    }

    private static long readPositiveLong(String property, long defaultValue) {
        String value = System.getProperty(property);
        if (value == null) {
            return defaultValue;
        }
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new NumberFormatException("must be positive");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new ScenarioException("System property '" + property
                    + "' must be a positive integer, but was '" + value + "'");
        }
    }
}
