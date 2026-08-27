package com.adamkali.screenplay;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenRecorderTest {
    @AfterEach
    void clearProperties() {
        System.clearProperty(ScreenRecorder.RECORD_PROPERTY);
    }

    @Test
    void resolveRecord_cliOverridesYaml() {
        assertTrue(ScreenRecorder.resolveRecord(true, false));
        assertFalse(ScreenRecorder.resolveRecord(false, true));
        assertTrue(ScreenRecorder.resolveRecord(null, true));
        assertFalse(ScreenRecorder.resolveRecord(null, false));
    }

    @Test
    void readCliOverride_parsesTrueFalseAndUnset() {
        assertNull(ScreenRecorder.readCliOverride());

        System.setProperty(ScreenRecorder.RECORD_PROPERTY, "true");
        assertEquals(true, ScreenRecorder.readCliOverride());

        System.setProperty(ScreenRecorder.RECORD_PROPERTY, "FALSE");
        assertEquals(false, ScreenRecorder.readCliOverride());

        System.setProperty(ScreenRecorder.RECORD_PROPERTY, "maybe");
        ScenarioException exception = assertThrows(ScenarioException.class, ScreenRecorder::readCliOverride);
        assertTrue(exception.getMessage().contains("must be true or false"));
    }

    @Test
    void evenPositive_roundsDownOddSizes() {
        assertEquals(1280, ScreenRecorder.evenPositive(1280, 800));
        assertEquals(720, ScreenRecorder.evenPositive(721, 800));
        assertEquals(800, ScreenRecorder.evenPositive(0, 800));
        assertEquals(2, ScreenRecorder.evenPositive(1, 1));
    }

    @Test
    void sanitizeFileName_rejectsPathSeparators() {
        assertEquals("createWorld", ScreenRecorder.sanitizeFileName("createWorld"));
        assertThrows(ScenarioException.class, () -> ScreenRecorder.sanitizeFileName("../escape"));
        assertThrows(ScenarioException.class, () -> ScreenRecorder.sanitizeFileName("a/b"));
    }

    @Test
    void buildFfmpegCommand_includesX11grabWindowRegion(@TempDir Path tempDir) {
        Path output = tempDir.resolve("createWorld.mp4");
        List<String> command = ScreenRecorder.buildFfmpegCommand(":99", 213, 272, 854, 480, output);

        assertEquals("ffmpeg", command.get(0));
        assertTrue(command.contains("x11grab"));
        assertTrue(command.contains("854x480"));
        assertTrue(command.contains(":99+213,272"));
        assertEquals(output.toAbsolutePath().toString(), command.getLast());
    }

    @Test
    void catalogParsesRecordFrontmatter(@TempDir Path root) throws Exception {
        Files.writeString(root.resolve("recorded.yaml"), """
                ---
                name: Recorded
                type: test
                record: true
                ---
                steps:
                  - launchGame
                """);
        Files.writeString(root.resolve("plain.yaml"), """
                ---
                name: Plain
                type: test
                ---
                steps:
                  - launchGame
                """);
        Files.writeString(root.resolve("recordedSuite.yaml"), """
                ---
                name: Recorded Suite
                type: suite
                record: true
                ---
                tests:
                  - plain
                """);

        ScenarioCatalog catalog = ScenarioCatalog.load(root);
        assertTrue(catalog.requireTest("recorded").record());
        assertFalse(catalog.requireTest("plain").record());
        assertTrue(catalog.requireSuite("recordedSuite").record());

        ScenarioPlan recordedPlan = new ScenarioCompiler(catalog).compile("recorded");
        assertTrue(recordedPlan.record());
        SuitePlan suitePlan = new ScenarioCompiler(catalog).compileSuite("recordedSuite");
        assertTrue(suitePlan.record());
        assertFalse(suitePlan.tests().getFirst().record());
    }

    @Test
    void catalogRejectsNonBooleanRecord(@TempDir Path root) throws Exception {
        Files.writeString(root.resolve("invalid.yaml"), """
                ---
                name: Invalid
                type: test
                record: maybe
                ---
                steps:
                  - launchGame
                """);

        ScenarioException exception = assertThrows(ScenarioException.class, () -> ScenarioCatalog.load(root));
        assertTrue(exception.getMessage().contains("'record' must be a boolean"));
    }
}
