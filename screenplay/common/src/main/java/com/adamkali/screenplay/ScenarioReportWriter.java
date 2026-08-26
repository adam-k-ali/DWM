package com.adamkali.screenplay;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
        writeCases(
                plan.id(),
                plan.name(),
                List.of(new CaseResult(plan.id(), plan.name(), results, failure)),
                diagnostics,
                false
        );
    }

    void writeSuite(SuitePlan suite, List<CaseResult> cases, String diagnostics) {
        writeCases(suite.id(), suite.name(), cases, diagnostics, true);
    }

    void writeBootstrapFailure(String scenarioId, Throwable failure) {
        ScenarioPlan plan = new ScenarioPlan(scenarioId, scenarioId, List.of());
        write(plan, List.of(), message(failure), message(failure) + "\n");
    }

    private void writeCases(
            String id,
            String name,
            List<CaseResult> cases,
            String diagnostics,
            boolean suite
    ) {
        try {
            Files.createDirectories(reportFile.getParent());
            Files.writeString(reportFile, xml(name, cases), StandardCharsets.UTF_8);
            Files.writeString(
                    reportFile.resolveSibling("diagnostics.txt"),
                    diagnostics == null || diagnostics.isBlank() ? "No diagnostics.\n" : diagnostics,
                    StandardCharsets.UTF_8
            );
            String metrics = suite
                    ? suiteMetricsJson(id, name, cases)
                    : metricsJson(new ScenarioPlan(id, name, List.of()), cases.getFirst().steps(), cases.getFirst().failure());
            Files.writeString(metricsFile(), metrics, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new ScenarioException("Could not write scenario report to " + reportFile, exception);
        }
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
        appendStepsJson(json, results, "    ");
        json.append("]\n}\n");
        return json.toString();
    }

    static String suiteMetricsJson(SuitePlan suite, List<CaseResult> cases) {
        return suiteMetricsJson(suite.id(), suite.name(), cases);
    }

    static String suiteMetricsJson(String id, String name, List<CaseResult> cases) {
        List<StepResult> flattened = new ArrayList<>();
        for (CaseResult result : cases) {
            flattened.addAll(result.steps());
        }
        long totalNanos = flattened.stream().mapToLong(StepResult::durationNanos).sum();
        boolean passed = cases.stream().allMatch(result -> result.failure() == null);
        StringBuilder json = new StringBuilder();
        json.append("{\n")
                .append("  \"schemaVersion\": ").append(METRICS_SCHEMA_VERSION).append(",\n")
                .append("  \"scenarioId\": ").append(jsonString(id)).append(",\n")
                .append("  \"scenarioName\": ").append(jsonString(name)).append(",\n")
                .append("  \"suite\": true,\n")
                .append("  \"passed\": ").append(passed).append(",\n")
                .append("  \"totalMs\": ").append(formatMs(totalNanos)).append(",\n")
                .append("  \"cases\": [");
        for (int i = 0; i < cases.size(); i++) {
            CaseResult result = cases.get(i);
            long caseNanos = result.steps().stream().mapToLong(StepResult::durationNanos).sum();
            if (i > 0) {
                json.append(',');
            }
            json.append("\n    {\n")
                    .append("      \"id\": ").append(jsonString(result.id())).append(",\n")
                    .append("      \"name\": ").append(jsonString(result.name())).append(",\n")
                    .append("      \"passed\": ").append(result.failure() == null).append(",\n")
                    .append("      \"totalMs\": ").append(formatMs(caseNanos)).append(",\n")
                    .append("      \"steps\": [");
            appendStepsJson(json, result.steps(), "        ");
            json.append("]\n    }");
        }
        if (!cases.isEmpty()) {
            json.append('\n').append("  ");
        }
        json.append("],\n")
                .append("  \"steps\": [");
        appendStepsJson(json, flattened, "    ");
        json.append("]\n}\n");
        return json.toString();
    }

    private static void appendStepsJson(StringBuilder json, List<StepResult> results, String indent) {
        for (int i = 0; i < results.size(); i++) {
            StepResult result = results.get(i);
            if (i > 0) {
                json.append(',');
            }
            json.append('\n').append(indent).append('{')
                    .append("\"name\": ").append(jsonString(result.name())).append(", ")
                    .append("\"ms\": ").append(formatMs(result.durationNanos())).append(", ")
                    .append("\"passed\": ").append(result.passed())
                    .append('}');
        }
        if (!results.isEmpty()) {
            json.append('\n').append(indent, 0, Math.max(0, indent.length() - 2));
        }
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

    private static String xml(String suiteName, List<CaseResult> cases) {
        int failureCount = (int) cases.stream().filter(result -> result.failure() != null).count();
        double duration = cases.stream()
                .flatMap(result -> result.steps().stream())
                .mapToLong(StepResult::durationNanos)
                .sum() / 1_000_000_000.0;
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<testsuite name=\"").append(escape(suiteName)).append("\" tests=\"")
                .append(cases.size()).append("\" failures=\"")
                .append(failureCount).append("\" time=\"")
                .append(String.format(Locale.ROOT, "%.3f", duration))
                .append("\">\n");
        for (CaseResult result : cases) {
            double caseDuration = result.steps().stream().mapToLong(StepResult::durationNanos).sum()
                    / 1_000_000_000.0;
            xml.append("  <testcase classname=\"screenplay\" name=\"").append(escape(result.id()))
                    .append("\" time=\"").append(String.format(Locale.ROOT, "%.3f", caseDuration)).append("\">");
            if (result.failure() != null) {
                xml.append("\n    <failure message=\"").append(escape(result.failure())).append("\">")
                        .append(escape(result.failure())).append("</failure>\n  ");
            }
            xml.append("</testcase>\n");
        }
        xml.append("  <system-out>").append(escape(stepSummary(cases))).append("</system-out>\n")
                .append("</testsuite>\n");
        return xml.toString();
    }

    private static String stepSummary(List<CaseResult> cases) {
        StringBuilder summary = new StringBuilder();
        for (CaseResult result : cases) {
            summary.append('[').append(result.id()).append("]\n");
            for (StepResult step : result.steps()) {
                summary.append(step.passed() ? "PASS " : "FAIL ")
                        .append(step.name())
                        .append(" (")
                        .append(String.format(Locale.ROOT, "%.3fs", step.durationNanos() / 1_000_000_000.0))
                        .append(")\n");
            }
            if (result.failure() != null) {
                summary.append("FAIL ").append(result.failure()).append('\n');
            }
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

    record CaseResult(String id, String name, List<StepResult> steps, String failure) {
        CaseResult {
            steps = List.copyOf(steps);
        }
    }
}
