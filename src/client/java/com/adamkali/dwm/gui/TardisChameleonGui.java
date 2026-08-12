package com.adamkali.dwm.gui;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class TardisChameleonGui extends Screen {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("popup/background");
    private static final TardisChameleonVariant[] variants = TardisChameleonVariant.values();
    private final ClientTardis tardis;
    private final TardisBlockEntity tardisBlockEntity;

    public TardisChameleonGui(ClientTardis tardis) {
        super(Component.literal("Tardis Chameleon"));
        this.tardis = tardis;

        this.tardisBlockEntity = new TardisBlockEntity(tardis.getTardisId(), new BlockPos(0, 0, 0), DWMBlocks.TARDIS_BLOCK.defaultBlockState());
        this.tardisBlockEntity.setLevel(Minecraft.getInstance().level);
    }

    private int currentVariantIndex = 0;
    private String chameleonVariantName = variants[currentVariantIndex].getId().toLanguageKey();

    private static Button upButton;
    private static Button downButton;

    private void setVariant(int variantIndex) {
        if (variantIndex < 0 || variantIndex >= variants.length) {
            return;
        }
        currentVariantIndex = variantIndex;
        chameleonVariantName = variants[currentVariantIndex].getId().toLanguageKey();

        downButton.active = currentVariantIndex != 0;
        upButton.active = currentVariantIndex != variants.length - 1;
        if (upButton.active) {
            Tooltip tooltip = Tooltip.create(Component.translatable(variants[currentVariantIndex + 1].getId().toLanguageKey()));
            upButton.setTooltip(tooltip);
        } else {
            upButton.setTooltip(Tooltip.create(Component.translatable("dwm.gui.no_more_variants")));
        }
        if (downButton.active) {
            Tooltip tooltip = Tooltip.create(Component.translatable(variants[currentVariantIndex - 1].getId().toLanguageKey()));
            downButton.setTooltip(tooltip);
        } else {
            downButton.setTooltip(Tooltip.create(Component.translatable("dwm.gui.no_more_variants")));
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

        upButton = Button.builder(Component.literal(">"), button -> {
            incrementVariant();
        }).bounds(width / 2 + 80, y1 + 40, 20, 20).build();

        downButton = Button.builder(Component.literal("<"), button -> {
            decrementVariant();
        }).bounds(width / 2 - 100, y1 + 40, 20, 20).build();

        Button saveButton = Button.builder(Component.literal("Save"), button -> {
            this.tardis.updateChameleonVariant(variants[currentVariantIndex]);
            onClose();
        }).bounds(width / 2 - 100, this.height / 2 + 50, 95, 20).build();

        Button cancelButton = Button.builder(Component.literal("Cancel"), button -> {
            onClose();
        }).bounds(width / 2 + 5, this.height / 2 + 50, 95, 20).build();

        addRenderableWidget(upButton);
        addRenderableWidget(downButton);
        addRenderableWidget(saveButton);
        addRenderableWidget(cancelButton);

        setVariant(0);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int contentWidth = 256;
        int contentHeight = 256;

        int x1 = (width - contentWidth) / 2;
        int x2 = x1 + contentWidth;
        int y1 = (height) / 2 - contentHeight / 3;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, x1, y1, contentWidth, 160);

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(font, Component.translatable(this.chameleonVariantName), (x1 + x2) / 2, y1 + 45, 0xFFFFFF);
        graphics.text(font, this.getTitle(), x1 + 10, y1 + 10, 0x404040, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
