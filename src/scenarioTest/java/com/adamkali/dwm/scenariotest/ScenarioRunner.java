package com.adamkali.dwm.scenariotest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class ScenarioRunner {
    private static final Duration VANILLA_SERVER_TIMEOUT_FLOOR = Duration.ofSeconds(120);

    private final ScenarioPlan plan;
    private final ScenarioReportWriter reportWriter;
    private final Duration stepTimeout;
    private final WidgetFinder widgetFinder = new WidgetFinder();
    private final ScreenshotCapture screenshotCapture;
    private final VanillaServerProcess vanillaServer;
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
        Screen screen = client.gui.screen();
        return switch (step.name()) {
            case "launchGame" -> screen instanceof TitleScreen;
            case "assertVisible" -> widgetFinder.find(screen, step.arguments()).isPresent();
            case "click" -> click(screen, step);
            case "debugScreen" -> {
                logger.info("{}", widgetFinder.describeVisibleWidgets(screen));
                yield true;
            }
            case "captureScreenshot" -> screenshotCapture.tick(client, (String) step.arguments().get("name"));
            case "startVanillaServer" -> vanillaServer.tick(VanillaServerProcess.parsePort(step.arguments().get("port")));
            case "keyboardInput" -> keyboardInput(screen, step);
            default -> throw new ScenarioException("Unsupported primitive step '" + step.name() + "'");
        };
    }

    private boolean click(Screen screen, ScenarioPlan.Step step) {
        Optional<AbstractWidget> widget = widgetFinder.find(screen, step.arguments());
        if (widget.isEmpty() || !widget.get().active) {
            return false;
        }
        AbstractWidget target = widget.get();
        logger.info("Activating {} on {} (visible widgets: {})",
                target.getClass().getName(),
                screen == null ? "<none>" : screen.getClass().getName(),
                widgetFinder.visibleWidgets(screen));
        MouseButtonEvent event = new MouseButtonEvent(
                target.getX() + target.getWidth() / 2.0,
                target.getY() + target.getHeight() / 2.0,
                new MouseButtonInfo(0, 0)
        );
        return screen.mouseClicked(event, false);
    }

    private boolean keyboardInput(Screen screen, ScenarioPlan.Step step) {
        if (screen == null || !(screen.getFocused() instanceof EditBox editBox) || !editBox.canConsumeInput()) {
            return false;
        }
        String text = (String) step.arguments().get("text");
        logger.info("Typing {} characters into {} on {}",
                text.length(),
                editBox.getClass().getName(),
                screen.getClass().getName());
        for (int index = 0; index < text.length(); ) {
            int codepoint = text.codePointAt(index);
            if (!screen.charTyped(new CharacterEvent(codepoint))) {
                throw new ScenarioException("keyboardInput rejected codepoint at index " + index
                        + " in \"" + text + "\" from " + step.source());
            }
            index += Character.charCount(codepoint);
        }
        return true;
    }

    private Duration timeoutFor(ScenarioPlan.Step step) {
        if (!"startVanillaServer".equals(step.name())) {
            return stepTimeout;
        }
        return stepTimeout.compareTo(VANILLA_SERVER_TIMEOUT_FLOOR) >= 0
                ? stepTimeout
                : VANILLA_SERVER_TIMEOUT_FLOOR;
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
