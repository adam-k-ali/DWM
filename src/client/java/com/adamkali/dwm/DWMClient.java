package com.adamkali.dwm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public class DWMClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MinecraftClient.getInstance().getGameVersion();
    }


}