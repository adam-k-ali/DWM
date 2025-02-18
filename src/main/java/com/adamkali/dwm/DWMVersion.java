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

public class DWMVersion {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String VERSION_CHECK_URL = "https://modding.s3.eu-west-1.amazonaws.com/dwm/version.json";

    public static final String MOD_VERSION = "1.12.4-1.0.0";

    public static VersionStatus checkVersion() {
        try {
            URL url = URI.create(VERSION_CHECK_URL).toURL();
            URLConnection request = url.openConnection();
            request.connect();

            JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
            JsonObject rootobj = root.getAsJsonObject();
            String latest = rootobj.get("promos").getAsJsonObject().get("recommended").getAsString();

            if (!latest.equals(MOD_VERSION)) {
                LOGGER.warn("Doctor Who Mod is out of date! Latest version is " + latest);
                return VersionStatus.OUT_OF_DATE;
            }
            return VersionStatus.UP_TO_DATE;
        } catch (IOException e) {
            LOGGER.error("Failed to check for updates", e);
            return VersionStatus.UNKNOWN;
        }
    }

    public enum VersionStatus {
        UP_TO_DATE,
        OUT_OF_DATE,
        UNKNOWN
    }
}
