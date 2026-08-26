package com.adamkali.screenplay;

import com.adamkali.screenplay.primitive.ScenarioPrimitive;
import com.adamkali.screenplay.primitive.ScenarioPrimitiveContext;
import com.adamkali.screenplay.primitive.ScenarioPrimitives;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

final class ScenarioRunner {
    private enum Phase {
        BEFORE_ALL,
        BEFORE_EACH,
        TEST,
        AFTER_EACH,
        AFTER_ALL,
        DONE
    }

    private final ScenarioPlan singlePlan;
    private final SuitePlan suite;
    private final boolean isSuite;
    private final ScenarioReportWriter reportWriter;
    private final Duration stepTimeout;
    private final WidgetFinder widgetFinder = new WidgetFinder();
    private final ScreenshotCapture screenshotCapture;
    private final VanillaServerProcess vanillaServer;
    private final CreateWorldProcess createWorld;
    private final Logger logger;

    private final List<ScenarioReportWriter.CaseResult> cases = new ArrayList<>();
    private List<ScenarioReportWriter.StepResult> currentSteps = new ArrayList<>();
    private String currentCaseId;
    private String currentCaseName;
    private String currentFailure;

    private Phase phase;
    private int memberIndex;
    private int stepIndex;
    private long stepStartedNanos;
    private boolean finished;
    private boolean executing;
    private boolean abortRemaining;
    private RuntimeException firstFailure;

    ScenarioRunner(
            ScenarioPlan plan,
            ScenarioReportWriter reportWriter,
            Duration stepTimeout,
            Logger logger
    ) {
        this.singlePlan = plan;
        this.suite = null;
        this.isSuite = false;
        this.reportWriter = reportWriter;
        this.stepTimeout = stepTimeout;
        this.logger = logger;
        this.screenshotCapture = new ScreenshotCapture(logger);
        this.vanillaServer = new VanillaServerProcess(logger);
        this.createWorld = new CreateWorldProcess(logger);
        this.phase = Phase.TEST;
        beginCase(plan.id());
    }

    ScenarioRunner(
            SuitePlan suite,
            ScenarioReportWriter reportWriter,
            Duration stepTimeout,
            Logger logger
    ) {
        this.singlePlan = null;
        this.suite = suite;
        this.isSuite = true;
        this.reportWriter = reportWriter;
        this.stepTimeout = stepTimeout;
        this.logger = logger;
        this.screenshotCapture = new ScreenshotCapture(logger);
        this.vanillaServer = new VanillaServerProcess(logger);
        this.createWorld = new CreateWorldProcess(logger);
        this.memberIndex = 0;
        if (!suite.beforeAll().isEmpty()) {
            this.phase = Phase.BEFORE_ALL;
            beginCase("before-all");
        } else if (!suite.beforeEach().isEmpty()) {
            this.phase = Phase.BEFORE_EACH;
            beginCase(suite.tests().getFirst().id());
        } else {
            this.phase = Phase.TEST;
            beginCase(suite.tests().getFirst().id());
        }
    }

    void tick(Minecraft client) {
        if (finished || executing || phase == Phase.DONE) {
            return;
        }

        List<ScenarioPlan.Step> steps = currentPhaseSteps();
        if (stepIndex >= steps.size()) {
            onPhaseComplete(client);
            return;
        }

        ScenarioPlan.Step step = steps.get(stepIndex);
        if (stepStartedNanos == 0L) {
            stepStartedNanos = System.nanoTime();
            logger.info("{} step {}/{}: {}", phaseLabel(), stepIndex + 1, steps.size(), step.displayName());
        }

        try {
            executing = true;
            boolean completed = execute(step, client);
            executing = false;
            if (completed) {
                currentSteps.add(new ScenarioReportWriter.StepResult(
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
            currentSteps.add(new ScenarioReportWriter.StepResult(
                    step.displayName(),
                    System.nanoTime() - stepStartedNanos,
                    false
            ));
            onFailure(client, exception);
        }
    }

    private void onPhaseComplete(Minecraft client) {
        switch (phase) {
            case BEFORE_ALL -> {
                finishCase();
                memberIndex = 0;
                startMember();
            }
            case BEFORE_EACH -> {
                phase = Phase.TEST;
                stepIndex = 0;
                stepStartedNanos = 0L;
            }
            case TEST -> {
                if (!isSuite) {
                    finishCase();
                    finish(client);
                    return;
                }
                if (!suite.afterEach().isEmpty()) {
                    phase = Phase.AFTER_EACH;
                    stepIndex = 0;
                    stepStartedNanos = 0L;
                } else {
                    finishCase();
                    advanceAfterMember(client);
                }
            }
            case AFTER_EACH -> {
                finishCase();
                advanceAfterMember(client);
            }
            case AFTER_ALL -> {
                finishCase();
                finish(client);
            }
            case DONE -> {
            }
        }
    }

    private void onFailure(Minecraft client, RuntimeException exception) {
        String failureMessage = failureMessage(exception);
        if (currentFailure == null) {
            currentFailure = failurePrefix() + failureMessage;
        }
        if (firstFailure == null) {
            firstFailure = exception;
        }

        if (!isSuite) {
            finishCase();
            finish(client);
            return;
        }

        switch (phase) {
            case BEFORE_ALL -> {
                finishCase();
                goAfterAll(client);
            }
            case BEFORE_EACH, TEST -> {
                abortRemaining = true;
                if (!suite.afterEach().isEmpty()) {
                    phase = Phase.AFTER_EACH;
                    stepIndex = 0;
                    stepStartedNanos = 0L;
                } else {
                    finishCase();
                    advanceAfterMember(client);
                }
            }
            case AFTER_EACH -> {
                abortRemaining = true;
                finishCase();
                advanceAfterMember(client);
            }
            case AFTER_ALL -> {
                finishCase();
                finish(client);
            }
            case DONE -> {
            }
        }
    }

    private void startMember() {
        ScenarioPlan member = suite.tests().get(memberIndex);
        beginCase(member.id());
        if (!suite.beforeEach().isEmpty()) {
            phase = Phase.BEFORE_EACH;
        } else {
            phase = Phase.TEST;
        }
        stepIndex = 0;
        stepStartedNanos = 0L;
    }

    private void advanceAfterMember(Minecraft client) {
        if (abortRemaining || memberIndex + 1 >= suite.tests().size()) {
            goAfterAll(client);
            return;
        }
        memberIndex++;
        startMember();
    }

    private void goAfterAll(Minecraft client) {
        if (!suite.afterAll().isEmpty()) {
            phase = Phase.AFTER_ALL;
            beginCase("after-all");
        } else {
            finish(client);
        }
    }

    private void beginCase(String id) {
        currentCaseId = id;
        if (!isSuite) {
            currentCaseName = singlePlan.name();
        } else if ("before-all".equals(id) || "after-all".equals(id)) {
            currentCaseName = id;
        } else {
            currentCaseName = suite.tests().get(memberIndex).name();
        }
        currentSteps = new ArrayList<>();
        currentFailure = null;
        stepIndex = 0;
        stepStartedNanos = 0L;
    }

    private void finishCase() {
        cases.add(new ScenarioReportWriter.CaseResult(
                currentCaseId,
                currentCaseName,
                currentSteps,
                currentFailure
        ));
        currentSteps = new ArrayList<>();
        currentFailure = null;
    }

    private List<ScenarioPlan.Step> currentPhaseSteps() {
        return switch (phase) {
            case BEFORE_ALL -> suite.beforeAll();
            case BEFORE_EACH -> suite.beforeEach();
            case TEST -> isSuite ? suite.tests().get(memberIndex).steps() : singlePlan.steps();
            case AFTER_EACH -> suite.afterEach();
            case AFTER_ALL -> suite.afterAll();
            case DONE -> List.of();
        };
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

    private void finish(Minecraft client) {
        finished = true;
        phase = Phase.DONE;
        vanillaServer.stop();
        String failureMessage = firstFailure == null ? null
                : firstFailure.getMessage() == null
                ? firstFailure.getClass().getName()
                : firstFailure.getMessage();
        String diagnostics = diagnostics(client, failureMessage);
        try {
            if (isSuite) {
                reportWriter.writeSuite(suite, cases, diagnostics);
            } else {
                ScenarioReportWriter.CaseResult only = cases.getFirst();
                reportWriter.write(singlePlan, only.steps(), only.failure(), diagnostics);
            }
        } catch (RuntimeException reportFailure) {
            logger.error("Could not write scenario report", reportFailure);
            if (firstFailure == null) {
                firstFailure = reportFailure;
            }
        }

        int exitCode = firstFailure == null && cases.stream().allMatch(result -> result.failure() == null)
                ? 0 : 1;
        if (exitCode == 0) {
            if (isSuite) {
                logger.info("Suite '{}' passed ({} cases)", suite.id(), cases.size());
            } else {
                logger.info("Scenario '{}' passed ({} steps)", singlePlan.id(), cases.getFirst().steps().size());
            }
        } else if (isSuite) {
            logger.error("Suite '{}' failed: {}", suite.id(), failureMessage, firstFailure);
        } else {
            logger.error("Scenario '{}' failed: {}", singlePlan.id(), failureMessage, firstFailure);
        }
        System.exit(exitCode);
    }

    private String failurePrefix() {
        if (!isSuite) {
            return "";
        }
        return switch (phase) {
            case BEFORE_ALL -> "before-all: ";
            case BEFORE_EACH -> "before-each: ";
            case AFTER_EACH -> "after-each: ";
            case AFTER_ALL -> "after-all: ";
            case TEST, DONE -> "";
        };
    }

    private String failureMessage(RuntimeException failure) {
        return failure.getMessage() == null ? failure.getClass().getName() : failure.getMessage();
    }

    private String phaseLabel() {
        if (!isSuite) {
            return "Scenario";
        }
        return switch (phase) {
            case BEFORE_ALL -> "Suite before-all";
            case BEFORE_EACH -> "Suite before-each [" + currentCaseId + "]";
            case TEST -> "Suite test [" + currentCaseId + "]";
            case AFTER_EACH -> "Suite after-each [" + currentCaseId + "]";
            case AFTER_ALL -> "Suite after-all";
            case DONE -> "Suite";
        };
    }

    private String diagnostics(Minecraft client, String failure) {
        StringBuilder diagnostics = new StringBuilder();
        if (isSuite) {
            diagnostics.append("suite: ").append(suite.id()).append('\n');
            diagnostics.append("phase: ").append(phase).append('\n');
            diagnostics.append("member: ").append(currentCaseId == null ? "<none>" : currentCaseId).append('\n');
        } else {
            diagnostics.append("scenario: ").append(singlePlan.id()).append('\n');
            diagnostics.append("completedSteps: ").append(stepIndex).append('/')
                    .append(singlePlan.steps().size()).append('\n');
        }
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
