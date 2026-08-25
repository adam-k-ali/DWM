package com.adamkali.dwm.guide;

import com.adamkali.dwm.gui.FieldGuideScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;

@Environment(EnvType.CLIENT)
public final class FieldGuideScreens {
    private FieldGuideScreens() {
    }

    public static void openDirect(Minecraft client) {
        if (client.player == null) {
            return;
        }
        client.setScreenAndShow(new FieldGuideScreen());
    }

    public static void openViaPauseMenu(Minecraft client) {
        if (client.player == null) {
            return;
        }
        client.setScreenAndShow(new PauseScreen(true));
        FieldGuideScreenHooks.clickPauseMenuButton(client);
    }
}
