package com.adamkali.dwm.guide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class FieldGuideVariantWidget extends AbstractWidget {
    private final Minecraft client;
    private final Identifier recipeId;
    private final boolean selected;
    private final Runnable onSelect;

    public FieldGuideVariantWidget(
            Minecraft client,
            Identifier recipeId,
            boolean selected,
            Runnable onSelect
    ) {
        super(
                0,
                0,
                FieldGuideBookLayout.VARIANT_SLOT_SIZE,
                FieldGuideBookLayout.VARIANT_SLOT_SIZE,
                CommonComponents.EMPTY
        );
        this.client = client;
        this.recipeId = recipeId;
        this.selected = selected;
        this.onSelect = onSelect;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        FieldGuideRecipePanel.renderVariant(
                graphics,
                client,
                getX(),
                getY(),
                recipeId,
                selected,
                mouseX,
                mouseY
        );
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        onSelect.run();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
