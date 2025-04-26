package com.adamkali.dwm.gui;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.TardisChameleonVariant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class TardisChameleonGui extends Screen {
    private static final TardisChameleonVariant[] variants = TardisChameleonVariant.values();
    private final ClientTardis tardis;

    public TardisChameleonGui(ClientTardis tardis) {
        super(Text.literal("Tardis Chameleon"));
        this.tardis = tardis;
    }

    private int currentVariantIndex = 0;
    private String chameleonVariantName = variants[currentVariantIndex].getId().toTranslationKey();

    private void incrementVariant() {
        currentVariantIndex = (currentVariantIndex + 1) % variants.length;
        chameleonVariantName = variants[currentVariantIndex].getId().toTranslationKey();
    }

    private void decrementVariant() {
        currentVariantIndex = (currentVariantIndex - 1 + variants.length) % variants.length;
        chameleonVariantName = variants[currentVariantIndex].getId().toTranslationKey();
    }

    @Override
    protected void init() {
        ButtonWidget upButton = ButtonWidget.builder(Text.literal("Up"), button -> {
            incrementVariant();
        }).dimensions(width / 2 - 100, height / 2 - 40, 200, 20).build();

        ButtonWidget downButton = ButtonWidget.builder(Text.literal("Down"), button -> {
            decrementVariant();
        }).dimensions(width / 2 - 100, height / 2 + 20, 200, 20).build();

        ButtonWidget saveButton = ButtonWidget.builder(Text.literal("Save"), button -> {
            this.tardis.updateChameleonVariant(variants[currentVariantIndex]);
        }).dimensions(width / 2 - 100, height / 2 + 60, 200, 20).build();

        ButtonWidget cancelButton = ButtonWidget.builder(Text.literal("Cancel"), button -> {
            close();
        }).dimensions(width / 2 - 100, height / 2 + 100, 200, 20).build();


        addDrawableChild(upButton);
        addDrawableChild(downButton);
        addDrawableChild(saveButton);
        addDrawableChild(cancelButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, Text.literal(this.chameleonVariantName), width / 2, height / 2, 0xFFFFFF);
    }
}
