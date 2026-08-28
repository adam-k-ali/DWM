package com.adamkali.screenplay;

import com.adamkali.screenplay.platform.ScreenplayPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * Shared client bootstrap used by Fabric / Forge / NeoForge entrypoints.
 */
public final class ScreenplayBootstrap {
    public static final String MOD_ID = "screenplay";
    public static final String SCENARIO_PROPERTY = "screenplay";
    public static final String REPORT_PROPERTY = "screenplay.report-file";
    public static final String TIMEOUT_PROPERTY = "screenplay.step-timeout-seconds";
    public static final String TESTS_DIRS_PROPERTY = "screenplay.tests-dirs";

    private static final Logger LOGGER = LoggerFactory.getLogger("Screenplay");

    private ScreenplayBootstrap() {
    }

    public static void start(ScreenplayPlatform platform) {
        String scenarioId = System.getProperty(SCENARIO_PROPERTY);
        if (scenarioId == null || scenarioId.isBlank()) {
            // Loaded as a runtime dependency (e.g. DWM runClient) without a scenario selected.
            return;
        }
        ScenarioReportWriter reportWriter = new ScenarioReportWriter(Path.of(
                System.getProperty(REPORT_PROPERTY, "build/screenplay/report.xml")
        ));
        try {
            ScenarioCatalog catalog = loadCatalog(ScreenplayBootstrap.class.getClassLoader());
            Duration timeout = Duration.ofSeconds(readPositiveLong(TIMEOUT_PROPERTY, 30L));
            Boolean recordOverride = ScreenRecorder.readCliOverride();
            ScenarioDocument.Type type = catalog.resolveExecutableType(scenarioId);
            if (type == ScenarioDocument.Type.SUITE) {
                SuitePlan suite = new ScenarioCompiler(catalog).compileSuite(scenarioId);
                boolean record = ScreenRecorder.resolveRecord(recordOverride, suite.record());
                ScenarioRunner runner = new ScenarioRunner(suite, reportWriter, timeout, LOGGER, record);
                platform.registerEndClientTick(runner::tick);
                LOGGER.info(
                        "Loaded suite '{}' with {} tests (before-all={}, before-each={}, after-each={}, after-all={}, record={})",
                        suite.id(),
                        suite.tests().size(),
                        suite.beforeAll().size(),
                        suite.beforeEach().size(),
                        suite.afterEach().size(),
                        suite.afterAll().size(),
                        record
                );
            } else {
                ScenarioPlan plan = new ScenarioCompiler(catalog).compile(scenarioId);
                boolean record = ScreenRecorder.resolveRecord(recordOverride, plan.record());
                ScenarioRunner runner = new ScenarioRunner(plan, reportWriter, timeout, LOGGER, record);
                platform.registerEndClientTick(runner::tick);
                LOGGER.info(
                        "Loaded scenario '{}' with {} primitive steps (record={})",
                        plan.id(),
                        plan.steps().size(),
                        record
                );
            }
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

    static ScenarioCatalog loadCatalog(ClassLoader classLoader) {
        String testsDirs = System.getProperty(TESTS_DIRS_PROPERTY);
        if (testsDirs == null || testsDirs.isBlank()) {
            return ScenarioCatalog.loadFromResources(classLoader);
        }
        List<Path> roots = ScenarioCatalog.parseTestsDirs(testsDirs);
        if (roots.isEmpty()) {
            return ScenarioCatalog.loadFromResources(classLoader);
        }
        return ScenarioCatalog.load(roots, classLoader);
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
