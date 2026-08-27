package com.adamkali.dwm.guide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;

@Environment(EnvType.CLIENT)
public final class FieldGuideRecipeWidget extends AbstractWidget {
    private final Minecraft client;
    private final FieldGuidePage page;
    private final FieldGuideRecipePanel.Station station;
    private final int craftingVariantIndex;

    public FieldGuideRecipeWidget(
            Minecraft client,
            FieldGuidePage page,
            FieldGuideRecipePanel.Station station,
            int craftingVariantIndex
    ) {
        super(0, 0, FieldGuideRecipePanel.PANEL_WIDTH, FieldGuideRecipePanel.PANEL_HEIGHT, CommonComponents.EMPTY);
        this.client = client;
        this.page = page;
        this.station = station;
        this.craftingVariantIndex = craftingVariantIndex;
        this.active = false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        FieldGuideRecipePanel.render(
                graphics,
                client,
                getX(),
                getY(),
                page,
                station,
                craftingVariantIndex,
                mouseX,
                mouseY
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return NarratableEntry.NarrationPriority.NONE;
    }
}
