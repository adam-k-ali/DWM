package com.adamkali.screenplay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioCatalogMergeTest {
    @Test
    void mergeLoadsFilesystemTestsAndClasspathCommandsOnly(@TempDir Path tempDir) throws IOException {
        Path testsDir = tempDir.resolve("tests");
        Files.createDirectories(testsDir);
        writeYaml(testsDir.resolve("myGui.yaml"), """
                ---
                name: My GUI
                type: test
                ---
                steps:
                  - launchGame
                  - assertAndClick:
                      type: button
                      name: "Singleplayer"
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(List.of(testsDir), getClass().getClassLoader());

        assertTrue(catalog.tests().containsKey("myGui"));
        assertFalse(catalog.tests().containsKey("createWorld"));
        assertFalse(catalog.suites().containsKey("creativeWorldSuite"));
        assertNotNull(catalog.command("assertAndClick"));
        assertEquals("Assert and Click", catalog.command("assertAndClick").name());
        assertEquals(List.of(testsDir.toAbsolutePath().normalize()), catalog.filesystemRoots());
    }

    @Test
    void filesystemCommandWinsOverClasspathDuplicate(@TempDir Path tempDir) throws IOException {
        Path testsDir = tempDir.resolve("tests");
        Files.createDirectories(testsDir);
        writeYaml(testsDir.resolve("myGui.yaml"), """
                ---
                name: My GUI
                type: test
                ---
                steps:
                  - launchGame
                """);
        writeYaml(testsDir.resolve("assertAndClick.yaml"), """
                ---
                name: Filesystem Assert
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

        ScenarioCatalog catalog = ScenarioCatalog.load(List.of(testsDir), getClass().getClassLoader());

        assertEquals("Filesystem Assert", catalog.command("assertAndClick").name());
        assertEquals("assertAndClick.yaml", catalog.command("assertAndClick").source());
    }

    @Test
    void unknownScenarioMentionsFilesystemRoots(@TempDir Path tempDir) throws IOException {
        Path testsDir = tempDir.resolve("tests");
        Files.createDirectories(testsDir);
        writeYaml(testsDir.resolve("myGui.yaml"), """
                ---
                name: My GUI
                type: test
                ---
                steps:
                  - launchGame
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(List.of(testsDir), getClass().getClassLoader());
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> catalog.resolveExecutableType("createWorld")
        );
        assertTrue(exception.getMessage().contains("createWorld"));
        assertTrue(exception.getMessage().contains(testsDir.toAbsolutePath().normalize().toString()));
    }

    @Test
    void parseTestsDirsSplitsOnPathSeparator() {
        Path first = Path.of("one");
        Path second = Path.of("two");
        String joined = first + File.pathSeparator + " " + second;
        assertEquals(List.of(first, second), ScenarioCatalog.parseTestsDirs(joined));
        assertEquals(List.of(), ScenarioCatalog.parseTestsDirs("  "));
        assertEquals(List.of(), ScenarioCatalog.parseTestsDirs(null));
    }

    @Test
    void bootstrapUsesTestsDirsProperty(@TempDir Path tempDir) throws IOException {
        Path testsDir = tempDir.resolve("tests");
        Files.createDirectories(testsDir);
        writeYaml(testsDir.resolve("myGui.yaml"), """
                ---
                name: My GUI
                type: test
                ---
                steps:
                  - launchGame
                """);
        System.setProperty(ScreenplayBootstrap.TESTS_DIRS_PROPERTY, testsDir.toAbsolutePath().toString());
        try {
            ScenarioCatalog catalog = ScreenplayBootstrap.loadCatalog(getClass().getClassLoader());
            assertTrue(catalog.tests().containsKey("myGui"));
            assertFalse(catalog.tests().containsKey("createWorld"));
            assertNotNull(catalog.command("assertAndClick"));
        } finally {
            System.clearProperty(ScreenplayBootstrap.TESTS_DIRS_PROPERTY);
        }
    }

    @Test
    void bootstrapFallsBackToClasspathWhenTestsDirsUnset() {
        System.clearProperty(ScreenplayBootstrap.TESTS_DIRS_PROPERTY);
        ScenarioCatalog catalog = ScreenplayBootstrap.loadCatalog(getClass().getClassLoader());
        assertTrue(catalog.tests().containsKey("createWorld"));
        assertNotNull(catalog.command("assertAndClick"));
    }

    private static void writeYaml(Path path, String contents) throws IOException {
        Files.writeString(path, contents.stripIndent(), StandardCharsets.UTF_8);
    }
}
