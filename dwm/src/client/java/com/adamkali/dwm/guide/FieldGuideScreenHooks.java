package com.adamkali.dwm.guide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class FieldGuideScreenHooks {
    private static final int PAUSE_BUTTON_WIDTH = 200;
    private static @Nullable Button pauseMenuButton;

    private FieldGuideScreenHooks() {
    }

    public static void initialize() {
        ScreenEvents.AFTER_INIT.register(FieldGuideScreenHooks::afterScreenInit);
    }

    private static void afterScreenInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!(screen instanceof PauseScreen pauseScreen) || !pauseScreen.showsPauseMenu()) {
            return;
        }
        int x = scaledWidth / 2 - PAUSE_BUTTON_WIDTH / 2;
        int y = scaledHeight / 4 + 144;
        pauseMenuButton = Button.builder(Component.translatable("dwm.guide.open_button"), button -> FieldGuideScreens.openDirect(client))
                .bounds(x, y, PAUSE_BUTTON_WIDTH, 20)
                .build();
        Screens.getWidgets(pauseScreen).add(pauseMenuButton);
    }

    public static void clickPauseMenuButton(Minecraft client) {
        Screen screen = client.gui.screen();
        if (!(screen instanceof PauseScreen)) {
            return;
        }
        if (pauseMenuButton != null && pauseMenuButton.active) {
            MouseButtonEvent event = new MouseButtonEvent(
                    pauseMenuButton.getX() + pauseMenuButton.getWidth() / 2.0,
                    pauseMenuButton.getY() + pauseMenuButton.getHeight() / 2.0,
                    new MouseButtonInfo(0, 0)
            );
            screen.mouseClicked(event, false);
            return;
        }
        FieldGuideScreens.openDirect(client);
    }
}
