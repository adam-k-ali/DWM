package com.adamkali.dwm;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionJsonSyncTest {
    @Test
    void versionJson_matchesGradleProperties() throws IOException {
        Properties gradle = loadGradleProperties();
        String minecraftVersion = gradle.getProperty("minecraft_version");
        String modVersion = gradle.getProperty("mod_version");
        String expected = minecraftVersion + "-" + modVersion;

        String raw = Files.readString(Path.of("version.json"));
        JSONObject root = new JSONObject(new JSONTokener(raw));
        JSONObject promos = root.getJSONObject("promos");

        assertEquals(expected, promos.getString("latest"));
        assertEquals(expected, promos.getString("recommended"));
        assertTrue(
                root.getJSONObject(minecraftVersion).has(modVersion),
                "version.json must include changelog for " + minecraftVersion + "/" + modVersion
        );
        JSONObject entry = root.getJSONObject(minecraftVersion).getJSONObject(modVersion);
        assertTrue(
                entry.has("summary") && !entry.getString("summary").isBlank(),
                "version.json changelog for " + minecraftVersion + "/" + modVersion
                        + " must include a non-blank summary"
        );
    }

    private static Properties loadGradleProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Path.of("gradle.properties"))) {
            props.load(in);
        }
        return props;
    }
}
