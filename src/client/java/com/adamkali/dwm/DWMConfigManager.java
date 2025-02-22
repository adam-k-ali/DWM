package com.adamkali.dwm;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class DWMConfigManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Path CONFIG_PATH = Path.of("config/dwm.json");

    static void save(HashMap<String, Object> config) {
        String json = new Gson().toJson(config);
        LOGGER.info("Saving config: " + json);
        try {
            Files.write(CONFIG_PATH, json.getBytes());
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    static HashMap<String, Object> load() {
        if (!Files.exists(CONFIG_PATH)) {
            return new HashMap<>();
        }
        try {
            String json = Files.readString(CONFIG_PATH);
            LOGGER.info("Loading config: " + json);
            return new Gson().fromJson(json, HashMap.class);
        } catch (IOException e) {
            LOGGER.error("Failed to load config", e);
        }
        return new HashMap<>();
    }
}
