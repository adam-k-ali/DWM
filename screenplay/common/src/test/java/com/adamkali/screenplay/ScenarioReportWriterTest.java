package com.adamkali.screenplay;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioReportWriterTest {
    @TempDir
    Path tempDir;

    @Test
    void metricsJson_includesSchemaScenarioTotalsAndSteps() {
        ScenarioPlan plan = new ScenarioPlan("placeBlock", "Place Block", List.of());
        List<ScenarioReportWriter.StepResult> results = List.of(
                new ScenarioReportWriter.StepResult("useItem", 12_300_000L, true),
                new ScenarioReportWriter.StepResult("waitUntil block \"minecraft:dirt\"", 48_000_000L, true)
        );

        JSONObject json = new JSONObject(ScenarioReportWriter.metricsJson(plan, results, null));

        assertEquals(ScenarioReportWriter.METRICS_SCHEMA_VERSION, json.getInt("schemaVersion"));
        assertEquals("placeBlock", json.getString("scenarioId"));
        assertEquals("Place Block", json.getString("scenarioName"));
        assertTrue(json.getBoolean("passed"));
        assertEquals(60.3, json.getDouble("totalMs"), 0.001);
        JSONArray steps = json.getJSONArray("steps");
        assertEquals(2, steps.length());
        assertEquals("useItem", steps.getJSONObject(0).getString("name"));
        assertEquals(12.3, steps.getJSONObject(0).getDouble("ms"), 0.001);
        assertTrue(steps.getJSONObject(0).getBoolean("passed"));
        assertEquals("waitUntil block \"minecraft:dirt\"", steps.getJSONObject(1).getString("name"));
        assertEquals(48.0, steps.getJSONObject(1).getDouble("ms"), 0.001);
    }

    @Test
    void metricsJson_marksFailedWhenFailurePresent() {
        ScenarioPlan plan = new ScenarioPlan("createWorld", "Create World", List.of());
        String json = ScenarioReportWriter.metricsJson(
                plan,
                List.of(new ScenarioReportWriter.StepResult("launchGame", 1_000_000L, false)),
                "Timed out"
        );

        JSONObject parsed = new JSONObject(json);
        assertFalse(parsed.getBoolean("passed"));
        assertFalse(parsed.getJSONArray("steps").getJSONObject(0).getBoolean("passed"));
        assertEquals(1.0, parsed.getDouble("totalMs"), 0.001);
    }

    @Test
    void metricsJson_bootstrapFailureHasEmptySteps() {
        ScenarioPlan plan = new ScenarioPlan("broken", "broken", List.of());
        JSONObject json = new JSONObject(ScenarioReportWriter.metricsJson(plan, List.of(), "boom"));

        assertFalse(json.getBoolean("passed"));
        assertEquals(0.0, json.getDouble("totalMs"), 0.001);
        assertEquals(0, json.getJSONArray("steps").length());
    }

    @Test
    void writeSuite_emitsMultiCaseXmlAndSuiteMetrics() throws Exception {
        Path reportFile = tempDir.resolve("report.xml");
        ScenarioReportWriter writer = new ScenarioReportWriter(reportFile);
        SuitePlan suite = new SuitePlan(
                "worldSuite",
                "World Suite",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(new ScenarioPlan("memberA", "Member A", List.of()))
        );
        List<ScenarioReportWriter.CaseResult> cases = List.of(
                new ScenarioReportWriter.CaseResult(
                        "before-all",
                        "before-all",
                        List.of(new ScenarioReportWriter.StepResult("launchGame", 10_000_000L, true)),
                        null
                ),
                new ScenarioReportWriter.CaseResult(
                        "memberA",
                        "Member A",
                        List.of(new ScenarioReportWriter.StepResult("waitTicks \"1\"", 5_000_000L, false)),
                        "before-each: boom"
                ),
                new ScenarioReportWriter.CaseResult(
                        "after-all",
                        "after-all",
                        List.of(),
                        null
                )
        );

        writer.writeSuite(suite, cases, "suite: worldSuite\n");

        String xml = Files.readString(reportFile);
        assertTrue(xml.contains("tests=\"3\""));
        assertTrue(xml.contains("failures=\"1\""));
        assertTrue(xml.contains("name=\"before-all\""));
        assertTrue(xml.contains("name=\"memberA\""));
        assertTrue(xml.contains("before-each: boom"));

        JSONObject metrics = new JSONObject(Files.readString(writer.metricsFile()));
        assertTrue(metrics.getBoolean("suite"));
        assertEquals("worldSuite", metrics.getString("scenarioId"));
        assertFalse(metrics.getBoolean("passed"));
        assertEquals(3, metrics.getJSONArray("cases").length());
        assertEquals("memberA", metrics.getJSONArray("cases").getJSONObject(1).getString("id"));
        assertFalse(metrics.getJSONArray("cases").getJSONObject(1).getBoolean("passed"));
        assertEquals(2, metrics.getJSONArray("steps").length());
    }

    @Test
    void write_emitsMetricsBesideReport() throws Exception {
        Path reportFile = tempDir.resolve("report.xml");
        ScenarioReportWriter writer = new ScenarioReportWriter(reportFile);
        ScenarioPlan plan = new ScenarioPlan("placeBlock", "Place Block", List.of());
        List<ScenarioReportWriter.StepResult> results = List.of(
                new ScenarioReportWriter.StepResult("useItem", 5_000_000L, true)
        );

        writer.write(plan, results, null, "ok\n");

        Path metricsFile = writer.metricsFile();
        assertTrue(Files.isRegularFile(metricsFile));
        JSONObject json = new JSONObject(Files.readString(metricsFile));
        assertEquals("placeBlock", json.getString("scenarioId"));
        assertEquals(5.0, json.getDouble("totalMs"), 0.001);
        assertTrue(Files.isRegularFile(reportFile));
        assertTrue(Files.isRegularFile(tempDir.resolve("diagnostics.txt")));
    }
}
