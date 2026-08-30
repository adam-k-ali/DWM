package com.adamkali.dwm.gui;

import com.adamkali.dwm.DWMModMenuIntegration;
import com.adamkali.dwm.guide.FieldGuideScreens;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class DWMModMenuScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;

    private final Screen parent;

    public DWMModMenuScreen(Screen parent) {
        super(Component.translatable("config.dwm.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = width / 2 - BUTTON_WIDTH / 2;
        int y = height / 4 + 48;

        Button openGuide = Button.builder(
                Component.translatable("dwm.guide.open_button"),
                button -> FieldGuideScreens.open(minecraft, this)
        ).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        boolean inWorld = minecraft != null && minecraft.player != null && minecraft.level != null;
        openGuide.active = inWorld;
        if (!inWorld) {
            openGuide.setTooltip(Tooltip.create(Component.translatable("dwm.guide.modmenu.needs_world")));
        }
        addRenderableWidget(openGuide);

        addRenderableWidget(Button.builder(
                Component.translatable("dwm.config.open_button"),
                button -> minecraft.setScreenAndShow(DWMModMenuIntegration.createConfigScreen(this))
        ).bounds(x, y + BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(x, y + BUTTON_SPACING * 2, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.gui.setScreen(parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(
                font,
                title,
                width / 2 - font.width(title) / 2,
                height / 4 + 24,
                0xFFFFFFFF,
                true
        );
    }
}
