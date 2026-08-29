package com.adamkali.dwm.guide;

import com.adamkali.dwm.gui.FieldGuideScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class FieldGuideScreens {
    private FieldGuideScreens() {
    }

    public static void openDirect(Minecraft client) {
        open(client, null);
    }

    public static void open(Minecraft client, @Nullable Screen parent) {
        if (client.player == null) {
            return;
        }
        client.setScreenAndShow(new FieldGuideScreen(parent, null));
    }
}
