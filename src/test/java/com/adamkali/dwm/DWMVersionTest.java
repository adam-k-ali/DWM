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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DWMVersionTest {
    @Test
    void releaseVersion_joinsMinecraftAndMod() {
        assertEquals("1.21.4-1.1.0", DWMVersion.releaseVersion("1.21.4", "1.1.0"));
    }

    @Test
    void releaseVersion_unknownWhenMissingParts() {
        assertEquals("unknown", DWMVersion.releaseVersion(null, "1.1.0"));
        assertEquals("unknown", DWMVersion.releaseVersion("1.21.4", null));
        assertEquals("unknown", DWMVersion.releaseVersion("", "1.1.0"));
        assertEquals("unknown", DWMVersion.releaseVersion("1.21.4", " "));
    }

    @Test
    void compareVersions_upToDateWhenEqual() {
        assertEquals(
                DWMVersion.VersionStatus.UP_TO_DATE,
                DWMVersion.compareVersions("1.21.4-1.1.0", "1.21.4-1.1.0")
        );
    }

    @Test
    void compareVersions_outOfDateWhenDifferent() {
        assertEquals(
                DWMVersion.VersionStatus.OUT_OF_DATE,
                DWMVersion.compareVersions("1.21.4-1.0.0", "1.21.4-1.1.0")
        );
    }

    @Test
    void compareVersions_unknownWhenInstalledUnknownOrBlank() {
        assertEquals(
                DWMVersion.VersionStatus.UNKNOWN,
                DWMVersion.compareVersions("unknown", "1.21.4-1.1.0")
        );
        assertEquals(
                DWMVersion.VersionStatus.UNKNOWN,
                DWMVersion.compareVersions(null, "1.21.4-1.1.0")
        );
        assertEquals(
                DWMVersion.VersionStatus.UNKNOWN,
                DWMVersion.compareVersions("1.21.4-1.1.0", null)
        );
    }

    @Test
    void packagedVersionProperties_matchGradleProperties() throws IOException {
        Properties gradle = loadGradleProperties();
        String expected = DWMVersion.releaseVersion(
                gradle.getProperty("minecraft_version"),
                gradle.getProperty("mod_version")
        );

        try (InputStream in = DWMVersion.class.getResourceAsStream("/dwm-version.properties")) {
            assertTrue(in != null, "dwm-version.properties must be on the test classpath");
            Properties packaged = new Properties();
            packaged.load(in);
            assertFalse(
                    packaged.getProperty("minecraft_version", "").contains("${"),
                    "minecraft_version must be expanded by processResources"
            );
            assertFalse(
                    packaged.getProperty("mod_version", "").contains("${"),
                    "mod_version must be expanded by processResources"
            );
            assertEquals(
                    expected,
                    DWMVersion.releaseVersion(
                            packaged.getProperty("minecraft_version"),
                            packaged.getProperty("mod_version")
                    )
            );
        }

        assertEquals(expected, DWMVersion.MOD_VERSION);
    }

    @Test
    void versionJson_matchesGradleProperties() throws IOException {
        Properties gradle = loadGradleProperties();
        String minecraftVersion = gradle.getProperty("minecraft_version");
        String modVersion = gradle.getProperty("mod_version");
        String expected = DWMVersion.releaseVersion(minecraftVersion, modVersion);

        String raw = Files.readString(Path.of("version.json"));
        JSONObject root = new JSONObject(new JSONTokener(raw));
        JSONObject promos = root.getJSONObject("promos");

        assertEquals(expected, promos.getString("latest"));
        assertEquals(expected, promos.getString("recommended"));
        assertTrue(
                root.getJSONObject(minecraftVersion).has(modVersion),
                "version.json must include changelog for " + minecraftVersion + "/" + modVersion
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
