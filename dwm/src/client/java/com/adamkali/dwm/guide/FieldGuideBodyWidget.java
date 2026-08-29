package com.adamkali.dwm.guide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Inactive wrapped-body slice. Height is {@code lines.size() * LINE_HEIGHT}.
 */
@Environment(EnvType.CLIENT)
public final class FieldGuideBodyWidget extends AbstractWidget {
    private final Font font;
    private final List<FormattedCharSequence> lines;

    public FieldGuideBodyWidget(Font font, List<FormattedCharSequence> lines) {
        super(
                0,
                0,
                FieldGuideBookLayout.RIGHT_PAGE_WIDTH,
                lines.size() * FieldGuideBookLayout.LINE_HEIGHT,
                CommonComponents.EMPTY
        );
        this.font = font;
        this.lines = List.copyOf(lines);
        this.active = false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int y = getY();
        for (FormattedCharSequence line : lines) {
            graphics.text(font, line, getX(), y, FieldGuideBookLayout.TEXT_COLOR, false);
            y += FieldGuideBookLayout.LINE_HEIGHT;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return NarratableEntry.NarrationPriority.NONE;
    }
}
