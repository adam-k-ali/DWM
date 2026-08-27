package com.adamkali.dwm.gui.layout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;

/**
 * Inactive rectangle that participates in layout. Used for hairlines and solid bars.
 */
@Environment(EnvType.CLIENT)
public final class FillWidget extends AbstractWidget {
    private final int color;

    public FillWidget(int width, int height, int color) {
        super(0, 0, width, height, CommonComponents.EMPTY);
        this.color = color;
        this.active = false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(getX(), getY(), getX() + width, getY() + height, color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return NarratableEntry.NarrationPriority.NONE;
    }
}
