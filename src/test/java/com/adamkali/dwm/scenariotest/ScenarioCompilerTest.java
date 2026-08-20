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

    private static void write(Path path, String content) throws Exception {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
