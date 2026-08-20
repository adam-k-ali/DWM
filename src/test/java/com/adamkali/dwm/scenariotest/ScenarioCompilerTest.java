package com.adamkali.dwm.scenariotest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioCompilerTest {
    @Test
    void compilesBundledCreateWorldScenario() throws URISyntaxException {
        Path scenarioRoot = Path.of(getClass().getResource("/tests").toURI());

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(scenarioRoot)).compile("createWorld");

        assertEquals(10, plan.steps().size());
        assertEquals("tab", plan.steps().get(3).arguments().get("type"));
        assertEquals("cycle", plan.steps().get(5).arguments().get("type"));
    }

    @Test
    void classifiesDocumentsRecursivelyAndExpandsCommandTemplates() throws URISyntaxException {
        Path fixtureRoot = Path.of(getClass().getResource("/scenario-fixtures/valid").toURI());

        ScenarioCatalog catalog = ScenarioCatalog.load(fixtureRoot);
        ScenarioPlan plan = new ScenarioCompiler(catalog).compile("createWorld.yaml");

        assertEquals(1, catalog.tests().size());
        assertEquals(1, catalog.commands().size());
        assertEquals("assertAndClick", catalog.commands().keySet().iterator().next());
        assertEquals(3, plan.steps().size());
        assertEquals("launchGame", plan.steps().get(0).name());
        assertEquals("assertVisible", plan.steps().get(1).name());
        assertEquals("button", plan.steps().get(1).arguments().get("type"));
        assertEquals("Singleplayer", plan.steps().get(1).arguments().get("name"));
        assertEquals("click", plan.steps().get(2).name());
    }

    @Test
    void rejectsUnsupportedFrontmatterType(@TempDir Path root) throws Exception {
        write(root.resolve("invalid.yaml"), """
                ---
                name: Invalid
                type: suite
                ---
                steps:
                  - launchGame
                """);

        ScenarioException exception = assertThrows(ScenarioException.class, () -> ScenarioCatalog.load(root));

        assertTrue(exception.getMessage().contains("unsupported frontmatter type 'suite'"));
    }

    @Test
    void rejectsDuplicateCommandIdsAcrossDirectories(@TempDir Path root) throws Exception {
        String command = """
                ---
                name: Duplicate
                type: command
                ---
                steps:
                  - launchGame
                """;
        write(root.resolve("first/reused.yaml"), command);
        write(root.resolve("second/reused.yaml"), command);

        ScenarioException exception = assertThrows(ScenarioException.class, () -> ScenarioCatalog.load(root));

        assertTrue(exception.getMessage().contains("Duplicate command id 'reused'"));
    }

    @Test
    void rejectsMissingCommandParameters(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - reusable:
                      type: button
                """);
        write(root.resolve("reusable.yaml"), """
                ---
                name: Reusable
                type: command
                ---
                parameters:
                  - name: type
                    type: string
                  - name: name
                    type: string
                steps:
                  - assertVisible:
                      type: "{{ type }}"
                      name: "{{ name }}"
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("missing parameter 'name'"));
    }

    @Test
    void rejectsRecursiveCommandCycles(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - first
                """);
        write(root.resolve("first.yaml"), """
                ---
                name: First
                type: command
                ---
                steps:
                  - second
                """);
        write(root.resolve("second.yaml"), """
                ---
                name: Second
                type: command
                ---
                steps:
                  - first
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("first -> second -> first"));
    }

    @Test
    void compilesDebugScreenAsANoArgPrimitive(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - launchGame
                  - debugScreen
                """);

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(2, plan.steps().size());
        assertEquals("debugScreen", plan.steps().get(1).name());
        assertTrue(plan.steps().get(1).arguments().isEmpty());
    }

    @Test
    void rejectsDebugScreenArguments(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - debugScreen:
                      type: button
                      name: Singleplayer
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("debugScreen does not accept arguments"));
    }

    @Test
    void compilesCaptureScreenshotAsANoArgPrimitive(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - launchGame
                  - captureScreenshot
                """);

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(2, plan.steps().size());
        assertEquals("captureScreenshot", plan.steps().get(1).name());
        assertTrue(plan.steps().get(1).arguments().isEmpty());
    }

    @Test
    void compilesCaptureScreenshotWithName(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - captureScreenshot:
                      name: after-world-tab
                """);

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(1, plan.steps().size());
        assertEquals("captureScreenshot", plan.steps().get(0).name());
        assertEquals("after-world-tab.png", plan.steps().get(0).arguments().get("name"));
    }

    @Test
    void rejectsCaptureScreenshotUnknownFields(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - captureScreenshot:
                      type: button
                      name: Singleplayer
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("captureScreenshot does not accept 'type'"));
    }

    @Test
    void rejectsCaptureScreenshotBlankName(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - captureScreenshot:
                      name: "  "
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("captureScreenshot requires a non-empty string 'name'"));
    }

    @Test
    void rejectsCaptureScreenshotPathName(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - captureScreenshot:
                      name: ../escape.png
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("without path separators"));
    }

    @Test
    void rejectsUnsupportedElementTypes(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - assertVisible:
                      type: label
                      name: Heading
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("supported types: [button, cycle, tab]"));
    }

    @Test
    void compilesStartVanillaServerAsANoArgPrimitive(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - launchGame
                  - startVanillaServer
                """);

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(2, plan.steps().size());
        assertEquals("startVanillaServer", plan.steps().get(1).name());
        assertTrue(plan.steps().get(1).arguments().isEmpty());
    }

    @Test
    void compilesStartVanillaServerWithIntegerPort(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - startVanillaServer:
                      port: 25565
                """);

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(1, plan.steps().size());
        assertEquals("startVanillaServer", plan.steps().get(0).name());
        assertEquals(25565, plan.steps().get(0).arguments().get("port"));
    }

    @Test
    void compilesStartVanillaServerWithQuotedPort(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - startVanillaServer:
                      port: "25566"
                """);

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(1, plan.steps().size());
        assertEquals(25566, plan.steps().get(0).arguments().get("port"));
    }

    @Test
    void rejectsStartVanillaServerPortZero(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - startVanillaServer:
                      port: 0
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("startVanillaServer port must be an integer between 1 and 65535"));
    }

    @Test
    void rejectsStartVanillaServerPortOutOfRange(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - startVanillaServer:
                      port: 70000
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("startVanillaServer port must be an integer between 1 and 65535"));
    }

    @Test
    void rejectsStartVanillaServerUnknownFields(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - startVanillaServer:
                      type: button
                      port: 25565
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("startVanillaServer does not accept 'type'"));
    }

    private static void write(Path path, String content) throws Exception {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
