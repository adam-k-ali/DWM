package com.adamkali.sightline;

import com.adamkali.sightline.platform.SightlinePlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;

/**
 * Shared client bootstrap used by Fabric / Forge / NeoForge entrypoints.
 */
public final class SightlineBootstrap {
    public static final String MOD_ID = "sightline";
    public static final String SCENARIO_PROPERTY = "sightline";
    public static final String REPORT_PROPERTY = "sightline.report-file";
    public static final String TIMEOUT_PROPERTY = "sightline.step-timeout-seconds";

    private static final Logger LOGGER = LoggerFactory.getLogger("Sightline");

    private SightlineBootstrap() {
    }

    public static void start(SightlinePlatform platform) {
        String scenarioId = System.getProperty(SCENARIO_PROPERTY, "createWorld");
        ScenarioReportWriter reportWriter = new ScenarioReportWriter(Path.of(
                System.getProperty(REPORT_PROPERTY, "build/sightline/report.xml")
        ));
        try {
            ScenarioCatalog catalog = ScenarioCatalog.loadFromResources(SightlineBootstrap.class.getClassLoader());
            ScenarioPlan plan = new ScenarioCompiler(catalog).compile(scenarioId);
            Duration timeout = Duration.ofSeconds(readPositiveLong(TIMEOUT_PROPERTY, 30L));
            ScenarioRunner runner = new ScenarioRunner(plan, reportWriter, timeout, LOGGER);
            platform.registerEndClientTick(runner::tick);
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
