package com.adamkali.dwm.scenariotest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

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
    void compilesOpenInventoryAsANoArgPrimitive(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - launchGame
                  - openInventory
                """);

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(2, plan.steps().size());
        assertEquals("openInventory", plan.steps().get(1).name());
        assertTrue(plan.steps().get(1).arguments().isEmpty());
    }

    @Test
    void rejectsOpenInventoryArguments(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - openInventory:
                      type: button
                      name: Singleplayer
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("openInventory does not accept arguments"));
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
                      type: slider
                      name: Heading
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("supported types: [button, cycle, tab, editbox, label, screen]"));
    }

    @Test
    void compilesLabelSelector(@TempDir Path root) throws Exception {
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

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(1, plan.steps().size());
        assertEquals("label", plan.steps().get(0).arguments().get("type"));
        assertEquals("Heading", plan.steps().get(0).arguments().get("name"));
    }

    @Test
    void compilesScreenSelector(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - assertVisible:
                      type: screen
                      name: LevelLoadingScreen
                  - waitUntil:
                      notVisible:
                        type: screen
                        name: LevelLoadingScreen
                """);

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(2, plan.steps().size());
        assertEquals("screen", plan.steps().get(0).arguments().get("type"));
        assertEquals("LevelLoadingScreen", plan.steps().get(0).arguments().get("name"));
        @SuppressWarnings("unchecked")
        Map<String, Object> notVisible = (Map<String, Object>) plan.steps().get(1).arguments().get("notVisible");
        assertEquals("screen", notVisible.get("type"));
        assertEquals("LevelLoadingScreen", notVisible.get("name"));
        assertEquals("waitUntil notVisible \"LevelLoadingScreen\"", plan.steps().get(1).displayName());
    }

    @Test
    void rejectsClickOnScreenSelector(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - click:
                      type: screen
                      name: TitleScreen
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("step 'click' cannot target type 'screen'"));
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

    @Test
    void compilesBundledJoinVanillaServerScenario() throws URISyntaxException {
        Path scenarioRoot = Path.of(getClass().getResource("/tests").toURI());

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(scenarioRoot)).compile("joinVanillaServer");

        assertEquals(16, plan.steps().size());
        assertEquals("startVanillaServer", plan.steps().get(1).name());
        assertEquals("editbox", plan.steps().get(6).arguments().get("type"));
        assertEquals("Server Address", plan.steps().get(6).arguments().get("name"));
        assertEquals("assertVisible", plan.steps().get(6).name());
        assertEquals("click", plan.steps().get(7).name());
        assertEquals("keyboardInput", plan.steps().get(8).name());
        assertEquals("localhost:25565", plan.steps().get(8).arguments().get("text"));
        assertEquals("keyboardInput \"localhost:25565\"", plan.steps().get(8).displayName());
        assertEquals("waitUntil", plan.steps().get(12).name());
        @SuppressWarnings("unchecked")
        Map<String, Object> notVisible = (Map<String, Object>) plan.steps().get(12).arguments().get("notVisible");
        assertEquals("screen", notVisible.get("type"));
        assertEquals("LevelLoadingScreen", notVisible.get("name"));
        assertEquals("waitUntil notVisible \"LevelLoadingScreen\"", plan.steps().get(12).displayName());
        assertEquals("openInventory", plan.steps().get(13).name());
        assertTrue(plan.steps().get(13).arguments().isEmpty());
        assertEquals("debugScreen", plan.steps().get(14).name());
        assertEquals("captureScreenshot", plan.steps().get(15).name());
    }

    @Test
    void compilesKeyboardInputWithTextMap(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - keyboardInput:
                      text: "hello"
                """);

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(1, plan.steps().size());
        assertEquals("keyboardInput", plan.steps().get(0).name());
        assertEquals("hello", plan.steps().get(0).arguments().get("text"));
    }

    @Test
    void compilesKeyboardInputScalar(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - keyboardInput: "localhost:25565"
                """);

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(1, plan.steps().size());
        assertEquals("keyboardInput", plan.steps().get(0).name());
        assertEquals("localhost:25565", plan.steps().get(0).arguments().get("text"));
    }

    @Test
    void compilesEditboxSelector(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - assertVisible:
                      type: editbox
                      name: "Server Address"
                  - click:
                      type: editbox
                      name: "Server Address"
                """);

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(2, plan.steps().size());
        assertEquals("editbox", plan.steps().get(0).arguments().get("type"));
        assertEquals("Server Address", plan.steps().get(0).arguments().get("name"));
        assertEquals("editbox", plan.steps().get(1).arguments().get("type"));
    }

    @Test
    void rejectsKeyboardInputBlankText(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - keyboardInput:
                      text: "  "
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("keyboardInput requires a non-empty string 'text'"));
    }

    @Test
    void rejectsKeyboardInputUnknownFields(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - keyboardInput:
                      name: Server Address
                      text: localhost
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("keyboardInput does not accept 'name'"));
    }

    @Test
    void rejectsKeyboardInputWithoutText(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - keyboardInput
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("keyboardInput requires a non-empty string 'text'"));
    }

    @Test
    void compilesWaitUntilVisible(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - waitUntil:
                      visible:
                        type: button
                        name: Singleplayer
                """);

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(1, plan.steps().size());
        assertEquals("waitUntil", plan.steps().get(0).name());
        @SuppressWarnings("unchecked")
        Map<String, Object> visible = (Map<String, Object>) plan.steps().get(0).arguments().get("visible");
        assertEquals("button", visible.get("type"));
        assertEquals("Singleplayer", visible.get("name"));
        assertEquals("waitUntil visible \"Singleplayer\"", plan.steps().get(0).displayName());
    }

    @Test
    void compilesWaitUntilNotVisibleListSelector(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - waitUntil:
                      notVisible:
                        - type: label
                          name: "Connecting to the server..."
                """);

        ScenarioPlan plan = new ScenarioCompiler(ScenarioCatalog.load(root)).compile("test");

        assertEquals(1, plan.steps().size());
        @SuppressWarnings("unchecked")
        Map<String, Object> notVisible = (Map<String, Object>) plan.steps().get(0).arguments().get("notVisible");
        assertEquals("label", notVisible.get("type"));
        assertEquals("Connecting to the server...", notVisible.get("name"));
    }

    @Test
    void rejectsWaitUntilWithoutCondition(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - waitUntil
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("waitUntil requires exactly one of 'visible' or 'notVisible'"));
    }

    @Test
    void rejectsWaitUntilWithBothConditions(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - waitUntil:
                      visible:
                        type: button
                        name: Singleplayer
                      notVisible:
                        type: label
                        name: Heading
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("waitUntil requires exactly one of 'visible' or 'notVisible'"));
    }

    @Test
    void rejectsWaitUntilUnknownFields(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - waitUntil:
                      timeout: 30
                      notVisible:
                        type: label
                        name: Heading
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("waitUntil does not accept 'timeout'"));
    }

    @Test
    void rejectsWaitUntilInvalidNestedSelector(@TempDir Path root) throws Exception {
        write(root.resolve("test.yaml"), """
                ---
                name: Test
                type: test
                ---
                steps:
                  - waitUntil:
                      notVisible:
                        type: slider
                        name: Heading
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> new ScenarioCompiler(catalog).compile("test")
        );

        assertTrue(exception.getMessage().contains("unsupported element type 'slider'"));
    }

    private static void write(Path path, String content) throws Exception {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
