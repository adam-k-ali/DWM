package com.adamkali.dwm.scenariotest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class ScenarioReportWriter {
    private final Path reportFile;

    ScenarioReportWriter(Path reportFile) {
        this.reportFile = reportFile;
    }

    void write(ScenarioPlan plan, List<StepResult> results, String failure, String diagnostics) {
        try {
            Files.createDirectories(reportFile.getParent());
            Files.writeString(reportFile, xml(plan, results, failure), StandardCharsets.UTF_8);
            Files.writeString(
                    reportFile.resolveSibling("diagnostics.txt"),
                    diagnostics == null || diagnostics.isBlank() ? "No diagnostics.\n" : diagnostics,
                    StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new ScenarioException("Could not write scenario report to " + reportFile, exception);
        }
    }

    void writeBootstrapFailure(String scenarioId, Throwable failure) {
        ScenarioPlan plan = new ScenarioPlan(scenarioId, scenarioId, List.of());
        write(plan, List.of(), message(failure), message(failure) + "\n");
    }

    private static String xml(ScenarioPlan plan, List<StepResult> results, String failure) {
        int failureCount = failure == null ? 0 : 1;
        double duration = results.stream().mapToLong(StepResult::durationNanos).sum() / 1_000_000_000.0;
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<testsuite name=\"").append(escape(plan.name())).append("\" tests=\"1\" failures=\"")
                .append(failureCount).append("\" time=\"").append(String.format(java.util.Locale.ROOT, "%.3f", duration))
                .append("\">\n")
                .append("  <testcase classname=\"dwm.scenario\" name=\"").append(escape(plan.id()))
                .append("\" time=\"").append(String.format(java.util.Locale.ROOT, "%.3f", duration)).append("\">");
        if (failure != null) {
            xml.append("\n    <failure message=\"").append(escape(failure)).append("\">")
                    .append(escape(failure)).append("</failure>\n  ");
        }
        xml.append("</testcase>\n")
                .append("  <system-out>").append(escape(stepSummary(results))).append("</system-out>\n")
                .append("</testsuite>\n");
        return xml.toString();
    }

    private static String stepSummary(List<StepResult> results) {
        StringBuilder summary = new StringBuilder();
        for (StepResult result : results) {
            summary.append(result.passed() ? "PASS " : "FAIL ")
                    .append(result.name())
                    .append(" (")
                    .append(String.format(java.util.Locale.ROOT, "%.3fs", result.durationNanos() / 1_000_000_000.0))
                    .append(")\n");
        }
        return summary.toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static String message(Throwable failure) {
        return failure.getMessage() == null ? failure.getClass().getName() : failure.getMessage();
    }

    record StepResult(String name, long durationNanos, boolean passed) {
    }
}
