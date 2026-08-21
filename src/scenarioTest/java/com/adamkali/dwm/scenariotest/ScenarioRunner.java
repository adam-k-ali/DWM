package com.adamkali.dwm.scenariotest;

import com.adamkali.dwm.scenariotest.primitive.ScenarioPrimitive;
import com.adamkali.dwm.scenariotest.primitive.ScenarioPrimitiveContext;
import com.adamkali.dwm.scenariotest.primitive.ScenarioPrimitives;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

final class ScenarioRunner {
    private final ScenarioPlan plan;
    private final ScenarioReportWriter reportWriter;
    private final Duration stepTimeout;
    private final WidgetFinder widgetFinder = new WidgetFinder();
    private final ScreenshotCapture screenshotCapture;
    private final VanillaServerProcess vanillaServer;
    private final CreateWorldProcess createWorld;
    private final Logger logger;
    private final List<ScenarioReportWriter.StepResult> results = new ArrayList<>();

    private int stepIndex;
    private long stepStartedNanos;
    private boolean finished;
    private boolean executing;

    ScenarioRunner(
            ScenarioPlan plan,
            ScenarioReportWriter reportWriter,
            Duration stepTimeout,
            Logger logger
    ) {
        this.plan = plan;
        this.reportWriter = reportWriter;
        this.stepTimeout = stepTimeout;
        this.logger = logger;
        this.screenshotCapture = new ScreenshotCapture(logger);
        this.vanillaServer = new VanillaServerProcess(logger);
        this.createWorld = new CreateWorldProcess(logger);
    }

    void tick(Minecraft client) {
        if (finished || executing) {
            return;
        }
        if (stepIndex >= plan.steps().size()) {
            finish(client, null);
            return;
        }

        ScenarioPlan.Step step = plan.steps().get(stepIndex);
        if (stepStartedNanos == 0L) {
            stepStartedNanos = System.nanoTime();
            logger.info("Scenario step {}/{}: {}", stepIndex + 1, plan.steps().size(), step.displayName());
        }

        try {
            executing = true;
            boolean completed = execute(step, client);
            executing = false;
            if (completed) {
                results.add(new ScenarioReportWriter.StepResult(
                        step.displayName(),
                        System.nanoTime() - stepStartedNanos,
                        true
                ));
                stepIndex++;
                stepStartedNanos = 0L;
                return;
            }
            Duration timeout = timeoutFor(step);
            if (System.nanoTime() - stepStartedNanos > timeout.toNanos()) {
                throw new ScenarioException("Timed out after " + timeout.toSeconds()
                        + "s waiting for " + step.displayName() + " from " + step.source());
            }
        } catch (RuntimeException exception) {
            executing = false;
            results.add(new ScenarioReportWriter.StepResult(
                    step.displayName(),
                    System.nanoTime() - stepStartedNanos,
                    false
            ));
            finish(client, exception);
        }
    }

    private boolean execute(ScenarioPlan.Step step, Minecraft client) {
        ScenarioPrimitive primitive = ScenarioPrimitives.find(step.name());
        if (primitive == null) {
            throw new ScenarioException("Unsupported primitive step '" + step.name() + "'");
        }
        return primitive.execute(new ScenarioPrimitiveContext(
                client, step, widgetFinder, screenshotCapture, vanillaServer, createWorld, logger));
    }

    private Duration timeoutFor(ScenarioPlan.Step step) {
        ScenarioPrimitive primitive = ScenarioPrimitives.find(step.name());
        return primitive == null ? stepTimeout : primitive.timeout(stepTimeout);
    }

    private void finish(Minecraft client, RuntimeException failure) {
        finished = true;
        vanillaServer.stop();
        String failureMessage = failure == null ? null
                : failure.getMessage() == null ? failure.getClass().getName() : failure.getMessage();
        String diagnostics = diagnostics(client, failureMessage);
        try {
            reportWriter.write(plan, results, failureMessage, diagnostics);
        } catch (RuntimeException reportFailure) {
            logger.error("Could not write scenario report", reportFailure);
            if (failure == null) {
                failure = reportFailure;
            }
        }

        int exitCode = failure == null ? 0 : 1;
        if (failure == null) {
            logger.info("Scenario '{}' passed ({} steps)", plan.id(), results.size());
        } else {
            logger.error("Scenario '{}' failed: {}", plan.id(), failureMessage, failure);
        }
        System.exit(exitCode);
    }

    private String diagnostics(Minecraft client, String failure) {
        StringBuilder diagnostics = new StringBuilder();
        diagnostics.append("scenario: ").append(plan.id()).append('\n');
        diagnostics.append("completedSteps: ").append(stepIndex).append('/').append(plan.steps().size()).append('\n');
        Screen screen = client.gui.screen();
        diagnostics.append("screen: ")
                .append(screen == null ? "<none>" : screen.getClass().getName())
                .append('\n');
        diagnostics.append("visibleWidgets: ").append(widgetFinder.visibleWidgets(screen)).append('\n');
        if (failure != null) {
            diagnostics.append("failure: ").append(failure).append('\n');
        }
        return diagnostics.toString();
    }
}
