package com.adamkali.dwm.gui;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class TardisChameleonGui extends Screen {
    private static final TardisChameleonVariant[] variants = TardisChameleonVariant.values();
    private final ClientTardis tardis;
    private final TardisBlockEntity tardisBlockEntity;

    public TardisChameleonGui(ClientTardis tardis) {
        super(Text.literal("Tardis Chameleon"));
        this.tardis = tardis;

        this.tardisBlockEntity = new TardisBlockEntity(tardis.getTardisId(), new BlockPos(0, 0, 0), DWMBlocks.TARDIS_BLOCK.getDefaultState());
        this.tardisBlockEntity.setWorld(MinecraftClient.getInstance().world);
    }

    private int currentVariantIndex = 0;
    private String chameleonVariantName = variants[currentVariantIndex].getId().toTranslationKey();

    private static ButtonWidget upButton;
    private static ButtonWidget downButton;

    private void setVariant(int variantIndex) {
        if (variantIndex < 0 || variantIndex >= variants.length) {
            return;
        }
        currentVariantIndex = variantIndex;
        chameleonVariantName = variants[currentVariantIndex].getId().toTranslationKey();
        
        downButton.active = currentVariantIndex != 0;
        upButton.active = currentVariantIndex != variants.length - 1;
        if (upButton.active) {
            Tooltip tooltip = Tooltip.of(Text.translatable(variants[currentVariantIndex + 1].getId().toTranslationKey()));
            upButton.setTooltip(tooltip);
        } else {
            upButton.setTooltip(Tooltip.of(Text.translatable("dwm.gui.no_more_variants")));
        }
        if (downButton.active) {
            Tooltip tooltip = Tooltip.of(Text.translatable(variants[currentVariantIndex - 1].getId().toTranslationKey()));
            downButton.setTooltip(tooltip);
        } else {
            downButton.setTooltip(Tooltip.of(Text.translatable("dwm.gui.no_more_variants")));
        }
    }

    private void incrementVariant() {
        currentVariantIndex = (currentVariantIndex + 1) % variants.length;
        setVariant(currentVariantIndex);
    }

    private void decrementVariant() {
        currentVariantIndex = (currentVariantIndex - 1 + variants.length) % variants.length;
        setVariant(currentVariantIndex);
    }

    @Override
    protected void init() {
        int contentHeight = 256;
        int y1 = (height) / 2 - contentHeight / 3;

        upButton = ButtonWidget.builder(Text.literal(">"), button -> {
            incrementVariant();
        }).dimensions(width / 2 + 80, y1 + 40, 20, 20).build();

        downButton = ButtonWidget.builder(Text.literal("<"), button -> {
            decrementVariant();
        }).dimensions(width / 2 - 100, y1 + 40, 20, 20).build();

        ButtonWidget saveButton = ButtonWidget.builder(Text.literal("Save"), button -> {
            this.tardis.updateChameleonVariant(variants[currentVariantIndex]);
            close();
        }).dimensions(width / 2 - 100, this.height / 2 + 50, 95, 20).build();

        ButtonWidget cancelButton = ButtonWidget.builder(Text.literal("Cancel"), button -> {
            close();
        }).dimensions(width / 2 + 5, this.height / 2 + 50, 95, 20).build();


        addDrawableChild(upButton);
        addDrawableChild(downButton);
        addDrawableChild(saveButton);
        addDrawableChild(cancelButton);

        setVariant(0);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int contentWidth = 256;
        int contentHeight = 256;

        int x1 = (width - contentWidth) / 2;
        int x2 = x1 + contentWidth;
        int y1 = (height) / 2 - contentHeight / 3;
        context.drawTexture(RenderLayer::getGuiTextured, Identifier.ofVanilla("textures/gui/demo_background.png"), x1, y1, 0, 0, 256, 256, 256, 256);

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, Text.translatable(this.chameleonVariantName), (x1 + x2) / 2, y1 + 45, 0xFFFFFF);
        context.drawText(textRenderer, this.getTitle(), x1 + 10, y1 + 10, 0x404040, false);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
