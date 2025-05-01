package com.adamkali.dwm.config;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.HashMap;

public class DWMConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean initialized = false;

    public static final ConfigKey<Boolean> IS_FIRST_START = new ConfigKey<>("isFirstStart", false);

    public static final ConfigKey<Boolean> ENABLE_CHAMELEON_GUI = new ConfigKey<>("enableChameleonGui", false);

    private static HashMap<String, Object> config = new HashMap<>();

    public static void init() {
        if (initialized) {
            return;
        }
        LOGGER.info("Initializing DWMConfig");

        initialized = true;
        config = DWMConfigManager.load();

        // Force IS_FIRST_START to be true if the config is empty
        if (!config.containsKey(IS_FIRST_START.getKey())) {
            config.put(IS_FIRST_START.getKey(), true);
        } else {
            config.put(IS_FIRST_START.getKey(), false);
        }

        LOGGER.info("DWMConfig initialized");
    }

    public static void save() {
        DWMConfigManager.save(config);
    }

    private static void requireInitialized() {
        if (!initialized) {
            init();
        }
    }

    public static boolean getBoolean(ConfigKey<Boolean> key) {
        requireInitialized();
        if (!config.containsKey(key.getKey())) {
            config.put(key.getKey(), key.getDefaultValue());
        }
        return (boolean) config.get(key.getKey());
    }

    public static void setBoolean(ConfigKey<Boolean> key, boolean value) {
        config.put(key.getKey(), value);
    }

    public static class ConfigKey<T> {
        private final String key;
        private final T defaultValue;

        public ConfigKey(String key, T defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public String getKey() {
            return key;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

    }
}
