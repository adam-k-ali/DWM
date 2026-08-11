package com.adamkali.dwm.gui;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.network.OpenPlayerLocatorScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class PlayerLocatorScreen extends Screen {
    private final ClientTardis tardis;
    private final List<OpenPlayerLocatorScreen.PlayerEntry> players;
    private int selectedIndex = -1;

    public PlayerLocatorScreen(ClientTardis tardis, List<OpenPlayerLocatorScreen.PlayerEntry> players) {
        super(Component.translatable("dwm.gui.player_locator.title"));
        this.tardis = tardis;
        this.players = new ArrayList<>(players);
    }

    @Override
    protected void init() {
        int panelLeft = width / 2 - 120;
        int panelTop = height / 2 - 90;
        int y = panelTop + 30;
        for (int i = 0; i < players.size(); i++) {
            final int index = i;
            OpenPlayerLocatorScreen.PlayerEntry entry = players.get(i);
            addRenderableWidget(Button.builder(Component.literal(entry.name()), button -> selectedIndex = index)
                    .bounds(panelLeft + 10, y, 220, 20)
                    .build());
            y += 22;
        }

        Button selectButton = Button.builder(Component.translatable("dwm.gui.player_locator.select"), button -> {
            UUID id = selectedPlayerId();
            if (id != null) {
                tardis.selectPlayer(id);
                onClose();
            }
        }).bounds(panelLeft + 10, height / 2 + 70, 105, 20).build();

        Button doneButton = Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(panelLeft + 125, height / 2 + 70, 105, 20)
                .build();

        addRenderableWidget(selectButton);
        addRenderableWidget(doneButton);
    }

    private UUID selectedPlayerId() {
        if (selectedIndex < 0 || selectedIndex >= players.size()) {
            return null;
        }
        return players.get(selectedIndex).uuid();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x1 = width / 2 - 128;
        int y1 = height / 2 - 110;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Identifier.withDefaultNamespace("textures/gui/demo_background.png"),
                x1,
                y1,
                0,
                0,
                256,
                256,
                256,
                256
        );
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(font, getTitle(), x1 + 10, y1 + 10, 0x404040, false);
        if (players.isEmpty()) {
            graphics.centeredText(
                    font,
                    Component.translatable("dwm.gui.player_locator.empty"),
                    width / 2,
                    height / 2 - 20,
                    0xFFFFFF
            );
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
