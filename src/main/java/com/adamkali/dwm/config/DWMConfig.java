package com.adamkali.dwm.config;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.HashMap;

public class DWMConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean initialized = false;

    public static final ConfigKey<Boolean> IS_FIRST_START = new ConfigKey<>("isFirstStart", false);

    public static final ConfigKey<Boolean> ENABLE_CHAMELEON_GUI = new ConfigKey<>("enableChameleonGui", false);

    /**
     * Shared exterior BOTI + interior SOTO door portal previews (client FBO).
     * Default on (matches former {@code enableBoti} default).
     */
    public static final ConfigKey<Boolean> ENABLE_DOOR_PORTALS = new ConfigKey<>("enableDoorPortals", true);

    /** Legacy keys migrated into {@link #ENABLE_DOOR_PORTALS} on load. */
    static final String LEGACY_ENABLE_BOTI = "enableBoti";
    static final String LEGACY_ENABLE_SOTO = "enableSoto";

    private static HashMap<String, Object> config = new HashMap<>();

    public static void init() {
        if (initialized) {
            return;
        }
        LOGGER.info("Initializing DWMConfig");

        initialized = true;
        config = DWMConfigManager.load();
        migrateDoorPortals(config);

        // Force IS_FIRST_START to be true if the config is empty
        if (!config.containsKey(IS_FIRST_START.getKey())) {
            config.put(IS_FIRST_START.getKey(), true);
        } else {
            config.put(IS_FIRST_START.getKey(), false);
        }

        LOGGER.info("DWMConfig initialized");
    }

    /**
     * If {@code enableDoorPortals} is absent but either legacy key is present,
     * set {@code enableDoorPortals = enableBoti || enableSoto} and drop the legacy keys.
     */
    static void migrateDoorPortals(HashMap<String, Object> map) {
        if (map == null) {
            return;
        }
        boolean hasLegacy = map.containsKey(LEGACY_ENABLE_BOTI) || map.containsKey(LEGACY_ENABLE_SOTO);
        if (!map.containsKey(ENABLE_DOOR_PORTALS.getKey()) && hasLegacy) {
            boolean boti = asBoolean(map.get(LEGACY_ENABLE_BOTI), true);
            boolean soto = asBoolean(map.get(LEGACY_ENABLE_SOTO), false);
            map.put(ENABLE_DOOR_PORTALS.getKey(), boti || soto);
        }
        map.remove(LEGACY_ENABLE_BOTI);
        map.remove(LEGACY_ENABLE_SOTO);
    }

    private static boolean asBoolean(Object value, boolean defaultValue) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.intValue() != 0;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return defaultValue;
    }

    public static void save() {
        migrateDoorPortals(config);
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
