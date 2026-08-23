package com.adamkali.screenplay;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateWorldProcessTest {
    @Test
    void normalizeAppliesTestFriendlyDefaults() {
        Map<String, Object> normalized = CreateWorldProcess.normalize(Map.of());

        assertEquals("flat", normalized.get("worldType"));
        assertEquals("creative", normalized.get("gameMode"));
        assertEquals("peaceful", normalized.get("difficulty"));
        assertEquals(true, normalized.get("allowCommands"));
        assertFalse(normalized.containsKey("name"));
    }

    @Test
    void parseWorldTypeAcceptsAliasesAndPresetPaths() {
        assertEquals("flat", CreateWorldProcess.parseWorldType(null));
        assertEquals("flat", CreateWorldProcess.parseWorldType("superflat"));
        assertEquals("flat", CreateWorldProcess.parseWorldType("minecraft:flat"));
        assertEquals("normal", CreateWorldProcess.parseWorldType("default"));
        assertEquals("large_biomes", CreateWorldProcess.parseWorldType("largeBiomes"));
        assertEquals("amplified", CreateWorldProcess.parseWorldType("amplified"));
        assertEquals("single_biome_surface", CreateWorldProcess.parseWorldType("singleBiome"));
        assertEquals("flat_all_dimensions", CreateWorldProcess.parseWorldType("flat_all_dimensions"));
        assertEquals("dwm:custom", CreateWorldProcess.parseWorldType("dwm:custom"));
    }

    @Test
    void parseWorldTypeRejectsBlank() {
        ScenarioException blank = assertThrows(
                ScenarioException.class,
                () -> CreateWorldProcess.parseWorldType("  ")
        );
        ScenarioException wrongType = assertThrows(
                ScenarioException.class,
                () -> CreateWorldProcess.parseWorldType(1)
        );

        assertTrue(blank.getMessage().contains("createWorld worldType must be a non-empty string"));
        assertTrue(wrongType.getMessage().contains("createWorld worldType must be a non-empty string"));
    }

    @Test
    void parseGameModeAndDifficultyRejectUnknownValues() {
        assertEquals("creative", CreateWorldProcess.parseGameMode(null));
        assertEquals("survival", CreateWorldProcess.parseGameMode("Survival"));
        assertEquals("peaceful", CreateWorldProcess.parseDifficulty(null));
        assertEquals("hard", CreateWorldProcess.parseDifficulty("HARD"));

        ScenarioException gameMode = assertThrows(
                ScenarioException.class,
                () -> CreateWorldProcess.parseGameMode("adventure")
        );
        ScenarioException difficulty = assertThrows(
                ScenarioException.class,
                () -> CreateWorldProcess.parseDifficulty("peaceful-plus")
        );

        assertTrue(gameMode.getMessage().contains("createWorld gameMode must be one of"));
        assertTrue(difficulty.getMessage().contains("createWorld difficulty must be one of"));
    }

    @Test
    void parseAllowCommandsRequiresBoolean() {
        assertTrue(CreateWorldProcess.parseAllowCommands(null));
        assertFalse(CreateWorldProcess.parseAllowCommands(false));

        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> CreateWorldProcess.parseAllowCommands("true")
        );

        assertTrue(exception.getMessage().contains("createWorld allowCommands must be a boolean"));
    }

    @Test
    void parseNameRequiresNonEmptyString() {
        assertEquals("Scenario World", CreateWorldProcess.parseName("Scenario World"));

        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> CreateWorldProcess.parseName("  ")
        );

        assertTrue(exception.getMessage().contains("createWorld name must be a non-empty string"));
    }

    @Test
    void normalizeRejectsUnknownKeys() {
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> CreateWorldProcess.normalize(Map.of("seed", "1"))
        );

        assertTrue(exception.getMessage().contains("createWorld does not accept 'seed'"));
    }
}
