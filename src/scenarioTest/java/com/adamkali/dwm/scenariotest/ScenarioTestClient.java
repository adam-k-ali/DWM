package com.adamkali.dwm.scenariotest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;

public final class ScenarioTestClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("DWMScenarioTest");
    private static final String SCENARIO_PROPERTY = "dwm.scenario";
    private static final String REPORT_PROPERTY = "dwm.scenario.report-file";
    private static final String TIMEOUT_PROPERTY = "dwm.scenario.step-timeout-seconds";

    @Override
    public void onInitializeClient() {
        String scenarioId = System.getProperty(SCENARIO_PROPERTY, "createWorld");
        ScenarioReportWriter reportWriter = new ScenarioReportWriter(Path.of(
                System.getProperty(REPORT_PROPERTY, "build/scenario-test/report.xml")
        ));
        try {
            ScenarioCatalog catalog = ScenarioCatalog.loadFromResources(getClass().getClassLoader());
            ScenarioPlan plan = new ScenarioCompiler(catalog).compile(scenarioId);
            Duration timeout = Duration.ofSeconds(readPositiveLong(TIMEOUT_PROPERTY, 30L));
            ScenarioRunner runner = new ScenarioRunner(plan, reportWriter, timeout, LOGGER);
            ClientTickEvents.END_CLIENT_TICK.register(runner::tick);
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
