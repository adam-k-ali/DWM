package com.adamkali.dwm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * Update check against the published {@code version.json} catalog.
 * The installed release string is {@code minecraft_version-mod_version} from
 * {@code gradle.properties}, expanded into {@code dwm-version.properties} at build time.
 */
public class DWMVersion {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String VERSION_CHECK_URL = "https://modding.s3.eu-west-1.amazonaws.com/dwm/version.json";
    private static final String VERSION_RESOURCE = "/dwm-version.properties";

    /**
     * Installed release id in promo format ({@code minecraft_version-mod_version}),
     * derived from gradle.properties via the packaged properties resource.
     */
    public static final String MOD_VERSION = loadInstalledReleaseVersion();

    public static VersionStatus checkVersion() {
        try {
            String latest = getLatestVersion();
            VersionStatus status = compareVersions(MOD_VERSION, latest);
            if (status == VersionStatus.OUT_OF_DATE) {
                LOGGER.warn("Doctor Who Mod is out of date! Recommended version is " + latest);
            }
            return status;
        } catch (IOException e) {
            LOGGER.error("Failed to check for updates", e);
            return VersionStatus.UNKNOWN;
        }
    }

    /**
     * Builds the promo-format release id used in {@code version.json} promos.
     */
    public static String releaseVersion(String minecraftVersion, String modVersion) {
        if (minecraftVersion == null || modVersion == null
                || minecraftVersion.isBlank() || modVersion.isBlank()) {
            return "unknown";
        }
        return minecraftVersion + "-" + modVersion;
    }

    /**
     * Compares an installed release id to the remote recommended promo string.
     */
    public static VersionStatus compareVersions(String installed, String recommended) {
        if (installed == null || recommended == null
                || installed.isBlank() || recommended.isBlank()
                || "unknown".equals(installed)) {
            return VersionStatus.UNKNOWN;
        }
        if (installed.equals(recommended)) {
            return VersionStatus.UP_TO_DATE;
        }
        return VersionStatus.OUT_OF_DATE;
    }

    static String loadInstalledReleaseVersion() {
        try (InputStream in = DWMVersion.class.getResourceAsStream(VERSION_RESOURCE)) {
            if (in == null) {
                LOGGER.error("Missing classpath resource {}", VERSION_RESOURCE);
                return "unknown";
            }
            Properties props = new Properties();
            props.load(in);
            String minecraftVersion = props.getProperty("minecraft_version");
            String modVersion = props.getProperty("mod_version");
            if (minecraftVersion != null && minecraftVersion.contains("${")) {
                LOGGER.error("dwm-version.properties was not expanded at build time");
                return "unknown";
            }
            return releaseVersion(minecraftVersion, modVersion);
        } catch (IOException e) {
            LOGGER.error("Failed to read {}", VERSION_RESOURCE, e);
            return "unknown";
        }
    }

    private static String getLatestVersion() throws IOException {
        URL url = URI.create(VERSION_CHECK_URL).toURL();
        URLConnection request = url.openConnection();
        request.connect();

        JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
        JsonObject rootobj = root.getAsJsonObject();

        return rootobj.get("promos").getAsJsonObject().get("recommended").getAsString();
    }

    public enum VersionStatus {
        UP_TO_DATE,
        OUT_OF_DATE,
        UNKNOWN
    }
}
