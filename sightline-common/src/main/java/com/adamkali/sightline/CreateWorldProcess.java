package com.adamkali.sightline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class CreateWorldProcess {
    static final String DEFAULT_WORLD_TYPE = "flat";
    static final String DEFAULT_GAME_MODE = "creative";
    static final String DEFAULT_DIFFICULTY = "peaceful";
    static final boolean DEFAULT_ALLOW_COMMANDS = true;

    private static final Set<String> KEYS = Set.of(
            "worldType", "gameMode", "difficulty", "allowCommands", "name");
    private static final Set<String> GAME_MODES = Set.of("survival", "hardcore", "creative");
    private static final Set<String> DIFFICULTIES = Set.of("peaceful", "easy", "normal", "hard");

    private final Logger logger;

    private boolean opened;
    private boolean created;
    private boolean completed;

    CreateWorldProcess(Logger logger) {
        this.logger = logger;
    }

    public static Map<String, Object> normalize(Map<String, Object> arguments) {
        for (String key : arguments.keySet()) {
            if (!KEYS.contains(key)) {
                throw new ScenarioException("createWorld does not accept '" + key + "'");
            }
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("worldType", parseWorldType(arguments.get("worldType")));
        normalized.put("gameMode", parseGameMode(arguments.get("gameMode")));
        normalized.put("difficulty", parseDifficulty(arguments.get("difficulty")));
        normalized.put("allowCommands", parseAllowCommands(arguments.get("allowCommands")));
        if (arguments.containsKey("name")) {
            normalized.put("name", parseName(arguments.get("name")));
        }
        return normalized;
    }

    public static String parseWorldType(Object value) {
        if (value == null) {
            return DEFAULT_WORLD_TYPE;
        }
        if (!(value instanceof String string) || string.isBlank()) {
            throw new ScenarioException("createWorld worldType must be a non-empty string");
        }
        String lower = string.trim().toLowerCase(Locale.ROOT);
        String path = lower.startsWith("minecraft:") ? lower.substring("minecraft:".length()) : lower;
        if (path.contains(":")) {
            return lower;
        }
        return switch (path) {
            case "default", "normal" -> "normal";
            case "superflat", "flat" -> "flat";
            case "largebiomes", "large_biomes" -> "large_biomes";
            case "amplified" -> "amplified";
            case "singlebiome", "single_biome", "single_biome_surface" -> "single_biome_surface";
            default -> path;
        };
    }

    public static String parseGameMode(Object value) {
        if (value == null) {
            return DEFAULT_GAME_MODE;
        }
        if (!(value instanceof String string) || string.isBlank()) {
            throw new ScenarioException("createWorld gameMode must be one of " + GAME_MODES);
        }
        String gameMode = string.trim().toLowerCase(Locale.ROOT);
        if (!GAME_MODES.contains(gameMode)) {
            throw new ScenarioException("createWorld gameMode must be one of " + GAME_MODES);
        }
        return gameMode;
    }

    public static String parseDifficulty(Object value) {
        if (value == null) {
            return DEFAULT_DIFFICULTY;
        }
        if (!(value instanceof String string) || string.isBlank()) {
            throw new ScenarioException("createWorld difficulty must be one of " + DIFFICULTIES);
        }
        String difficulty = string.trim().toLowerCase(Locale.ROOT);
        if (!DIFFICULTIES.contains(difficulty)) {
            throw new ScenarioException("createWorld difficulty must be one of " + DIFFICULTIES);
        }
        return difficulty;
    }

    public static boolean parseAllowCommands(Object value) {
        if (value == null) {
            return DEFAULT_ALLOW_COMMANDS;
        }
        if (!(value instanceof Boolean allowCommands)) {
            throw new ScenarioException("createWorld allowCommands must be a boolean");
        }
        return allowCommands;
    }

    public static String parseName(Object value) {
        if (!(value instanceof String string) || string.isBlank()) {
            throw new ScenarioException("createWorld name must be a non-empty string");
        }
        return string;
    }

    public boolean tick(Minecraft client, Map<String, Object> arguments) {
        if (completed) {
            throw new ScenarioException("createWorld can only run once per scenario");
        }
        if (!opened) {
            CreateWorldScreen.openFresh(client, () -> client.gui.setScreen(new TitleScreen()));
            opened = true;
            logger.info("Opened Create World");
            return false;
        }
        if (!created) {
            Screen screen = client.gui.screen();
            if (!(screen instanceof CreateWorldScreen createWorldScreen)) {
                return false;
            }
            applySettings(createWorldScreen.getUiState(), arguments);
            logger.info("Creating world type={} gameMode={} difficulty={} allowCommands={} name={}",
                    arguments.get("worldType"),
                    arguments.get("gameMode"),
                    arguments.get("difficulty"),
                    arguments.get("allowCommands"),
                    arguments.getOrDefault("name", "<vanilla>"));
            createWorldScreen.onCreate();
            created = true;
            return false;
        }
        if (isWorldReady(client)) {
            logger.info("World is ready");
            completed = true;
            return true;
        }
        return false;
    }

    private static void applySettings(WorldCreationUiState uiState, Map<String, Object> arguments) {
        uiState.setWorldType(findWorldType(uiState, (String) arguments.get("worldType")));
        uiState.setGameMode(toGameMode((String) arguments.get("gameMode")));
        uiState.setDifficulty(toDifficulty((String) arguments.get("difficulty")));
        uiState.setAllowCommands((Boolean) arguments.get("allowCommands"));
        Object name = arguments.get("name");
        if (name instanceof String worldName) {
            uiState.setName(worldName);
        }
    }

    private static WorldCreationUiState.WorldTypeEntry findWorldType(
            WorldCreationUiState uiState,
            String requested
    ) {
        List<WorldCreationUiState.WorldTypeEntry> entries = new ArrayList<>();
        entries.addAll(uiState.getNormalPresetList());
        entries.addAll(uiState.getAltPresetList());
        Set<String> available = new TreeSet<>();
        for (WorldCreationUiState.WorldTypeEntry entry : entries) {
            Identifier id = presetId(entry);
            if (id == null) {
                continue;
            }
            available.add(id.toString());
            if (matchesPreset(id, requested)) {
                return entry;
            }
        }
        throw new ScenarioException("createWorld worldType '" + requested
                + "' was not found. Available: " + available);
    }

    private static Identifier presetId(WorldCreationUiState.WorldTypeEntry entry) {
        Holder<WorldPreset> preset = entry.preset();
        if (preset == null) {
            return null;
        }
        return preset.unwrapKey().map(ResourceKey::identifier).orElse(null);
    }

    private static boolean matchesPreset(Identifier id, String requested) {
        if (requested.contains(":")) {
            return id.toString().equals(requested);
        }
        return id.getPath().equals(requested) || id.toString().equals("minecraft:" + requested);
    }

    private static WorldCreationUiState.SelectedGameMode toGameMode(String gameMode) {
        return switch (gameMode) {
            case "survival" -> WorldCreationUiState.SelectedGameMode.SURVIVAL;
            case "hardcore" -> WorldCreationUiState.SelectedGameMode.HARDCORE;
            case "creative" -> WorldCreationUiState.SelectedGameMode.CREATIVE;
            default -> throw new ScenarioException("createWorld gameMode must be one of " + GAME_MODES);
        };
    }

    private static Difficulty toDifficulty(String difficulty) {
        return switch (difficulty) {
            case "peaceful" -> Difficulty.PEACEFUL;
            case "easy" -> Difficulty.EASY;
            case "normal" -> Difficulty.NORMAL;
            case "hard" -> Difficulty.HARD;
            default -> throw new ScenarioException("createWorld difficulty must be one of " + DIFFICULTIES);
        };
    }

    private static boolean isWorldReady(Minecraft client) {
        if (client.player == null || client.level == null) {
            return false;
        }
        return !isLoadingGui(client.gui.screen());
    }

    private static boolean isLoadingGui(Screen screen) {
        if (screen == null) {
            return false;
        }
        if (screen instanceof CreateWorldScreen || screen instanceof LevelLoadingScreen) {
            return true;
        }
        String simpleName = screen.getClass().getSimpleName();
        return simpleName.contains("Loading") || simpleName.contains("Receiving") || simpleName.contains("Progress");
    }
}
