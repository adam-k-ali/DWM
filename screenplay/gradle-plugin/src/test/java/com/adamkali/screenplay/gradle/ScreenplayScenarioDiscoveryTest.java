package com.adamkali.screenplay.gradle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenplayScenarioDiscoveryTest {
    @Test
    void requestedIdWinsWithoutScanning(@TempDir File tempDir) {
        assertEquals("fieldGuide", ScreenplayScenarioDiscovery.resolveScenarioId(" fieldGuide ", List.of(tempDir)));
        assertTrue(ScreenplayScenarioDiscovery.needsStarter(tempDir));
        assertFalse(new File(tempDir, ScreenplayScenarioDiscovery.STARTER_FILENAME).isFile());
    }

    @Test
    void writesStarterAndSelectsItWhenNoYamlExists(@TempDir File tempDir) throws IOException {
        assertTrue(ScreenplayScenarioDiscovery.needsStarter(tempDir));
        String id = ScreenplayScenarioDiscovery.resolveScenarioId(null, List.of(tempDir));
        File starter = new File(tempDir, ScreenplayScenarioDiscovery.STARTER_FILENAME);
        assertEquals("myFirstTest", id);
        assertTrue(starter.isFile());
        assertTrue(Files.readString(starter.toPath(), StandardCharsets.UTF_8).contains("type: test"));
        assertFalse(ScreenplayScenarioDiscovery.needsStarter(tempDir));
    }

    @Test
    void selectsSoleStandaloneTest(@TempDir File tempDir) throws IOException {
        writeYaml(new File(tempDir, "onlyOne.yaml"), """
                ---
                name: Only One
                type: test
                ---
                steps:
                  - launchGame
                """);
        assertEquals("onlyOne", ScreenplayScenarioDiscovery.resolveScenarioId(null, List.of(tempDir)));
    }

    @Test
    void rejectsMultipleTestsWithoutExplicitId(@TempDir File tempDir) throws IOException {
        writeYaml(new File(tempDir, "alpha.yaml"), """
                ---
                name: Alpha
                type: test
                ---
                steps:
                  - launchGame
                """);
        writeYaml(new File(tempDir, "beta.yaml"), """
                ---
                name: Beta
                type: test
                ---
                steps:
                  - launchGame
                """);
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ScreenplayScenarioDiscovery.resolveScenarioId(null, List.of(tempDir))
        );
        assertTrue(exception.getMessage().contains("-Pscreenplay="));
        assertTrue(exception.getMessage().contains("alpha"));
        assertTrue(exception.getMessage().contains("beta"));
    }

    @Test
    void discoverSkipsSuiteMembers(@TempDir File tempDir) throws IOException {
        writeYaml(new File(tempDir, "creativeWorldSuite.yaml"), """
                ---
                name: Suite
                type: suite
                ---
                tests:
                  - memberTest
                """);
        writeYaml(new File(tempDir, "memberTest.yaml"), """
                ---
                name: Member
                type: test
                ---
                steps:
                  - launchGame
                """);
        writeYaml(new File(tempDir, "standalone.yaml"), """
                ---
                name: Standalone
                type: test
                ---
                steps:
                  - launchGame
                """);
        ScreenplayScenarioDiscovery.Discovery discovery =
                ScreenplayScenarioDiscovery.discoverRunIds(List.of(tempDir));
        assertEquals(List.of("creativeWorldSuite", "standalone"), List.copyOf(discovery.runIds()));
        assertEquals(List.of("standalone"), List.copyOf(discovery.standaloneTestIds()));
    }

    @Test
    void needsStarterWhenDirectoryMissing(@TempDir File tempDir) {
        File missing = new File(tempDir, "tests");
        assertTrue(ScreenplayScenarioDiscovery.needsStarter(missing));
        File starter = ScreenplayScenarioDiscovery.writeStarter(missing);
        assertTrue(starter.isFile());
        assertEquals(ScreenplayScenarioDiscovery.STARTER_FILENAME, starter.getName());
    }

    @Test
    void bakedVersionsArePresent() {
        assertFalse(ScreenplayPluginVersions.screenplayVersion().contains("$"));
        assertFalse(ScreenplayPluginVersions.minecraftVersion().contains("$"));
        assertEquals(25, ScreenplayPluginVersions.requiredJavaVersion());
    }

    private static void writeYaml(File file, String contents) throws IOException {
        Files.writeString(file.toPath(), contents.stripIndent(), StandardCharsets.UTF_8);
    }
}
