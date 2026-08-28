package com.adamkali.screenplay.gradle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Versions baked into the plugin jar from {@code screenplay/gradle.properties}.
 */
public final class ScreenplayPluginVersions {
    private static final Properties PROPERTIES = load();

    private ScreenplayPluginVersions() {
    }

    public static String screenplayVersion() {
        return required("screenplay_version");
    }

    public static String minecraftVersion() {
        return required("minecraft_version");
    }

    public static int requiredJavaVersion() {
        return 25;
    }

    private static String required(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing " + key + " in screenplay-plugin.properties");
        }
        return value.trim();
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream stream = ScreenplayPluginVersions.class.getResourceAsStream("screenplay-plugin.properties")) {
            if (stream == null) {
                throw new IllegalStateException("screenplay-plugin.properties is missing from the plugin jar");
            }
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read screenplay-plugin.properties", exception);
        }
        return properties;
    }
}
