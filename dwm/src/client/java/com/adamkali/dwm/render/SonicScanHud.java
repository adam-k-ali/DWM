package com.adamkali.dwm.render;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.item.SonicScanLogic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

/**
 * Compact Scan readout (donut meters + labeled rows) in the top-left after a successful sonic Scan.
 */
@Environment(EnvType.CLIENT)
public final class SonicScanHud {
    private static final Identifier ELEMENT_ID =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sonic_scan_hud");

    private static final long DISPLAY_MS = 5000L;
    private static final long FADE_MS = 400L;
    private static final int MAX_WIDTH = 148;
    private static final int CORNER_INSET = 6;
    private static final int PAD = 5;
    private static final int TITLE_HEIGHT = 10;
    private static final int LINE_HEIGHT = 9;
    private static final int METER_ROW_HEIGHT = 14;
    private static final int RING_SIZE = 12;
    private static final int DIVIDER_HEIGHT = 7;

    private static final int PANEL_RGB = 0x101820;
    private static final int BORDER_RGB = 0x3A5A58;
    private static final int DIVIDER_RGB = 0x2A4442;
    private static final int TRACK_RGB = 0x1A2A28;
    private static final int FILL_RGB = 0xACF4EE;
    private static final int TITLE_RGB = 0xACF4EE;
    private static final int VALUE_RGB = 0xD7E8E5;

    private static @Nullable SonicScanLogic.Snapshot snapshot;
    private static long shownAtMs;

    private SonicScanHud() {
    }

    public static void initialize() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, ELEMENT_ID, SonicScanHud::extract);
    }

    public static void show(SonicScanLogic.Snapshot next) {
        snapshot = next;
        shownAtMs = Util.getMillis();
    }

    public static void clear() {
        snapshot = null;
        shownAtMs = 0L;
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        if (snapshot == null || SonicFieldModeHudController.isActive()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return;
        }
        long elapsed = Util.getMillis() - shownAtMs;
        if (elapsed >= DISPLAY_MS) {
            clear();
            return;
        }
        float alpha = elapsed > DISPLAY_MS - FADE_MS
                ? Mth.clamp((DISPLAY_MS - elapsed) / (float) FADE_MS, 0.0F, 1.0F)
                : 1.0F;
        if (alpha <= 0.01F) {
            return;
        }

        int width = Math.min(MAX_WIDTH, Math.max(1, graphics.guiWidth() - 2 * CORNER_INSET));
        int height = panelHeight(snapshot);
        int x = CORNER_INSET;
        int y = CORNER_INSET;
        renderPanel(graphics, client.font, snapshot, x, y, width, height, alpha);
    }

    private static int panelHeight(SonicScanLogic.Snapshot snap) {
        int height = PAD + TITLE_HEIGHT;
        if (snap.noSignal()) {
            height += LINE_HEIGHT;
        } else {
            height += METER_ROW_HEIGHT * 3 + LINE_HEIGHT;
        }
        height += DIVIDER_HEIGHT + LINE_HEIGHT + LINE_HEIGHT + METER_ROW_HEIGHT + PAD;
        return height;
    }

    private static void renderPanel(
            GuiGraphicsExtractor graphics,
            Font font,
            SonicScanLogic.Snapshot snap,
            int x,
            int y,
            int width,
            int height,
            float alpha
    ) {
        graphics.fill(x, y, x + width, y + height, color(BORDER_RGB, alpha * 0.85F));
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, color(PANEL_RGB, alpha * 0.72F));

        int innerLeft = x + PAD;
        int innerRight = x + width - PAD;
        int cursorY = y + PAD;
        graphics.text(font, Component.translatable("dwm.sonic.scan.title"), innerLeft, cursorY, color(TITLE_RGB, alpha), false);
        cursorY += TITLE_HEIGHT;

        if (snap.noSignal()) {
            graphics.text(
                    font,
                    Component.translatable("dwm.sonic.scan.no_signal"),
                    innerLeft,
                    cursorY,
                    color(VALUE_RGB, alpha),
                    false
            );
            cursorY += LINE_HEIGHT;
        } else {
            cursorY = drawMeterRow(
                    graphics, font, innerLeft, innerRight, cursorY, alpha,
                    Component.translatable("dwm.sonic.scan.oxygen"), snap.oxygen());
            cursorY = drawMeterRow(
                    graphics, font, innerLeft, innerRight, cursorY, alpha,
                    Component.translatable("dwm.sonic.scan.temperature"), snap.temperature());
            cursorY = drawMeterRow(
                    graphics, font, innerLeft, innerRight, cursorY, alpha,
                    Component.translatable("dwm.sonic.scan.radiation"), snap.radiation());
            graphics.text(
                    font,
                    labeledValue("dwm.sonic.scan.waterlogged", yesNo(snap.waterlogged())),
                    innerLeft,
                    cursorY,
                    color(VALUE_RGB, alpha),
                    false
            );
            cursorY += LINE_HEIGHT;
        }

        int dividerY = cursorY + 2;
        graphics.fill(innerLeft, dividerY, innerRight, dividerY + 1, color(DIVIDER_RGB, alpha));
        cursorY += DIVIDER_HEIGHT;

        int midX = innerLeft + (innerRight - innerLeft) / 2;
        graphics.text(
                font,
                labeledValue("dwm.sonic.scan.locked", yesNo(snap.locked())),
                innerLeft,
                cursorY,
                color(VALUE_RGB, alpha),
                false
        );
        graphics.text(
                font,
                labeledValue("dwm.sonic.scan.cloaked", yesNo(snap.cloaked())),
                midX,
                cursorY,
                color(VALUE_RGB, alpha),
                false
        );
        cursorY += LINE_HEIGHT;
        graphics.text(
                font,
                Component.translatable("dwm.sonic.scan.phase", snap.phase().name()),
                innerLeft,
                cursorY,
                color(VALUE_RGB, alpha),
                false
        );
        cursorY += LINE_HEIGHT;
        if (snap.artronEmpty()) {
            graphics.text(
                    font,
                    Component.translatable("dwm.sonic.scan.artron_empty"),
                    innerLeft,
                    cursorY,
                    color(VALUE_RGB, alpha),
                    false
            );
        } else {
            drawMeterRow(
                    graphics, font, innerLeft, innerRight, cursorY, alpha,
                    Component.translatable("dwm.sonic.scan.artron"), snap.artronPercent());
        }
    }

    private static int drawMeterRow(
            GuiGraphicsExtractor graphics,
            Font font,
            int left,
            int right,
            int y,
            float alpha,
            Component label,
            int percent
    ) {
        int ringX = right - RING_SIZE;
        drawRing(graphics, ringX, y, percent, alpha);
        int textY = y + (RING_SIZE - LINE_HEIGHT) / 2;
        graphics.text(
                font,
                Component.translatable(
                        "dwm.sonic.scan.labeled",
                        label,
                        Component.translatable("dwm.sonic.scan.percent", percent)
                ),
                left,
                textY,
                color(VALUE_RGB, alpha),
                false
        );
        return y + METER_ROW_HEIGHT;
    }

    private static void drawRing(GuiGraphicsExtractor graphics, int x, int y, int percent, float alpha) {
        float outer = RING_SIZE / 2.0F;
        float inner = outer - 2.5F;
        float sweep = Mth.clamp(percent, 0, 100) / 100.0F * (float) (Math.PI * 2.0);
        int track = color(TRACK_RGB, alpha);
        int fill = color(FILL_RGB, alpha);
        for (int px = 0; px < RING_SIZE; px++) {
            for (int py = 0; py < RING_SIZE; py++) {
                float dx = px + 0.5F - outer;
                float dy = py + 0.5F - outer;
                float dist = Mth.sqrt(dx * dx + dy * dy);
                if (dist < inner || dist > outer) {
                    continue;
                }
                float angle = (float) Math.atan2(dx, -dy);
                if (angle < 0.0F) {
                    angle += (float) (Math.PI * 2.0);
                }
                int pixelColor = angle <= sweep ? fill : track;
                graphics.fill(x + px, y + py, x + px + 1, y + py + 1, pixelColor);
            }
        }
    }

    private static Component labeledValue(String key, Component value) {
        return Component.translatable("dwm.sonic.scan.labeled", Component.translatable(key), value);
    }

    private static Component yesNo(boolean value) {
        return Component.translatable(value ? "dwm.sonic.scan.yes" : "dwm.sonic.scan.no");
    }

    private static int color(int rgb, float alpha) {
        float a = Mth.clamp(alpha, 0.0F, 1.0F);
        return ARGB.colorFromFloat(
                a,
                ARGB.red(ARGB.opaque(rgb)) / 255.0F,
                ARGB.green(ARGB.opaque(rgb)) / 255.0F,
                ARGB.blue(ARGB.opaque(rgb)) / 255.0F
        );
    }
}
