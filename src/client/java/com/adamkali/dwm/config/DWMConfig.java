package com.adamkali.dwm.config;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.HashMap;

public class DWMConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean initialized = false;

    public static final String IS_FIRST_START = "isFirstStart";

    private static HashMap<String, Object> config = new HashMap<>();

    private static void init() {
        if (initialized) {
            return;
        }
        LOGGER.info("Initializing DWMConfig");

        initialized = true;
        config = DWMConfigManager.load();

        DWMConfig.register(IS_FIRST_START, true);

        LOGGER.info("DWMConfig initialized");
    }

    public static void save() {
        DWMConfigManager.save(config);
    }

    private static void register(String key, Object defaultValue) {
        if (!config.containsKey(key)) {
            config.put(key, defaultValue);
        }
    }

    private static void requireInitialized() {
        if (!initialized) {
            init();
        }
    }

    public static boolean getBoolean(String key) {
        requireInitialized();
        return (boolean) config.get(key);
    }

    public static void setBoolean(String key, boolean value) {
        config.put(key, value);
    }
}
