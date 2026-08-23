package com.adamkali.dwm.scenariotest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

final class ScenarioReportWriter {
    static final int METRICS_SCHEMA_VERSION = 1;

    private final Path reportFile;

    ScenarioReportWriter(Path reportFile) {
        this.reportFile = reportFile;
    }

    Path metricsFile() {
        return reportFile.resolveSibling("metrics.json");
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
            Files.writeString(metricsFile(), metricsJson(plan, results, failure), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new ScenarioException("Could not write scenario report to " + reportFile, exception);
        }
    }

    void writeBootstrapFailure(String scenarioId, Throwable failure) {
        ScenarioPlan plan = new ScenarioPlan(scenarioId, scenarioId, List.of());
        write(plan, List.of(), message(failure), message(failure) + "\n");
    }

    static String metricsJson(ScenarioPlan plan, List<StepResult> results, String failure) {
        long totalNanos = results.stream().mapToLong(StepResult::durationNanos).sum();
        StringBuilder json = new StringBuilder();
        json.append("{\n")
                .append("  \"schemaVersion\": ").append(METRICS_SCHEMA_VERSION).append(",\n")
                .append("  \"scenarioId\": ").append(jsonString(plan.id())).append(",\n")
                .append("  \"scenarioName\": ").append(jsonString(plan.name())).append(",\n")
                .append("  \"passed\": ").append(failure == null).append(",\n")
                .append("  \"totalMs\": ").append(formatMs(totalNanos)).append(",\n")
                .append("  \"steps\": [");
        for (int i = 0; i < results.size(); i++) {
            StepResult result = results.get(i);
            if (i > 0) {
                json.append(',');
            }
            json.append("\n    {")
                    .append("\"name\": ").append(jsonString(result.name())).append(", ")
                    .append("\"ms\": ").append(formatMs(result.durationNanos())).append(", ")
                    .append("\"passed\": ").append(result.passed())
                    .append('}');
        }
        if (!results.isEmpty()) {
            json.append('\n').append("  ");
        }
        json.append("]\n}\n");
        return json.toString();
    }

    private static String formatMs(long durationNanos) {
        return String.format(Locale.ROOT, "%.1f", durationNanos / 1_000_000.0);
    }

    private static String jsonString(String value) {
        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c < 0x20) {
                        escaped.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                }
            }
        }
        escaped.append('"');
        return escaped.toString();
    }

    private static String xml(ScenarioPlan plan, List<StepResult> results, String failure) {
        int failureCount = failure == null ? 0 : 1;
        double duration = results.stream().mapToLong(StepResult::durationNanos).sum() / 1_000_000_000.0;
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<testsuite name=\"").append(escape(plan.name())).append("\" tests=\"1\" failures=\"")
                .append(failureCount).append("\" time=\"").append(String.format(Locale.ROOT, "%.3f", duration))
                .append("\">\n")
                .append("  <testcase classname=\"dwm.scenario\" name=\"").append(escape(plan.id()))
                .append("\" time=\"").append(String.format(Locale.ROOT, "%.3f", duration)).append("\">");
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
                    .append(String.format(Locale.ROOT, "%.3fs", result.durationNanos() / 1_000_000_000.0))
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
