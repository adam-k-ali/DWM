package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.config.DWMConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.util.EnumMap;
import java.util.List;

/**
 * F3-style top-left overlay for shared portal pipeline timings (averages + graphs).
 * Gated by {@link DWMConfig#SHOW_PORTAL_PERF_DEBUG}.
 */
public final class PortalPerfDebugHud {
    private static final Identifier ELEMENT_ID =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "portal_perf_debug_hud");
    private static final int TEXT_COLOR = ARGB.opaque(0xE0E0E0);
    private static final int MAX_COLOR = ARGB.opaque(0xFFDD66);
    private static final int PANEL_COLOR = 0x88000000;
    private static final int SPARK_COLOR = ARGB.opaque(0x66AAFF);
    private static final int LINE_HEIGHT = 10;
    private static final int PAD_X = 4;
    private static final int PAD_Y = 4;
    private static final int SPARK_WIDTH = 120;
    private static final int SPARK_HEIGHT = 24;
    private static final int STACK_WIDTH = 180;
    private static final int STACK_HEIGHT = 10;

    private static final EnumMap<PortalPerfStats.Stage, Integer> LEAF_COLORS = new EnumMap<>(PortalPerfStats.Stage.class);

    static {
        LEAF_COLORS.put(PortalPerfStats.Stage.SKY_FOG, ARGB.opaque(0x88CCFF));
        LEAF_COLORS.put(PortalPerfStats.Stage.TERRAIN_OPAQUE, ARGB.opaque(0x55DD66));
        LEAF_COLORS.put(PortalPerfStats.Stage.TERRAIN_CUTOUT, ARGB.opaque(0xDDCC44));
        LEAF_COLORS.put(PortalPerfStats.Stage.TERRAIN_TRANSLUCENT, ARGB.opaque(0x66DDDD));
        LEAF_COLORS.put(PortalPerfStats.Stage.GHOST_FEATURES, ARGB.opaque(0xDD77FF));
        LEAF_COLORS.put(PortalPerfStats.Stage.PASS_BATCH_REBUILD, ARGB.opaque(0xFF8844));
        LEAF_COLORS.put(PortalPerfStats.Stage.MESH_BAKE, ARGB.opaque(0xFF5555));
        LEAF_COLORS.put(PortalPerfStats.Stage.COMPOSITE, ARGB.opaque(0xAAAAAA));
    }

    private PortalPerfDebugHud() {
    }

    public static void initialize() {
        // No DEBUG element in Fabric 1.21+ VanillaHudElements; MISC_OVERLAYS is the early HUD slot.
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, ELEMENT_ID, PortalPerfDebugHud::extract);
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        if (!DWMConfig.getBoolean(DWMConfig.SHOW_PORTAL_PERF_DEBUG)) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return;
        }
        PortalPerfStats.DisplaySnapshot snap = PortalPerfStats.displaySnapshot();
        List<String> lines = PortalPerfStats.formatLines(snap);
        int y = PAD_Y;
        for (String line : lines) {
            int color = line.startsWith("*") || line.startsWith("maxAvg:") ? MAX_COLOR : TEXT_COLOR;
            graphics.text(client.font, line, PAD_X, y, color);
            y += LINE_HEIGHT;
        }
        y += 2;
        y = drawSparkline(graphics, PAD_X, y);
        y += 4;
        drawStackedBar(graphics, client, snap, PAD_X, y);
    }

    private static int drawSparkline(GuiGraphicsExtractor graphics, int x, int y) {
        float[] totals = PortalPerfStats.historyTotalsMs();
        graphics.fill(x, y, x + SPARK_WIDTH, y + SPARK_HEIGHT, PANEL_COLOR);
        if (totals.length == 0) {
            return y + SPARK_HEIGHT;
        }
        float max = 0.001f;
        for (float total : totals) {
            if (total > max) {
                max = total;
            }
        }
        int samples = Math.min(totals.length, SPARK_WIDTH);
        int start = Math.max(0, totals.length - samples);
        for (int i = 0; i < samples; i++) {
            float value = totals[start + i];
            int barH = Math.max(1, Math.round((value / max) * (SPARK_HEIGHT - 2)));
            int bx = x + i;
            int by = y + SPARK_HEIGHT - 1 - barH;
            graphics.fill(bx, by, bx + 1, y + SPARK_HEIGHT - 1, SPARK_COLOR);
        }
        return y + SPARK_HEIGHT;
    }

    private static void drawStackedBar(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            PortalPerfStats.DisplaySnapshot snap,
            int x,
            int y
    ) {
        graphics.fill(x, y, x + STACK_WIDTH, y + STACK_HEIGHT, PANEL_COLOR);
        if (snap == null || snap == PortalPerfStats.DisplaySnapshot.IDLE) {
            return;
        }
        double sum = 0.0;
        for (PortalPerfStats.Stage stage : PortalPerfStats.Stage.LEAF_STAGES) {
            sum += Math.max(0.0, snap.avgStageMs().getOrDefault(stage, 0.0));
        }
        if (sum <= 0.0) {
            return;
        }
        int cursor = x;
        for (PortalPerfStats.Stage stage : PortalPerfStats.Stage.LEAF_STAGES) {
            double ms = Math.max(0.0, snap.avgStageMs().getOrDefault(stage, 0.0));
            if (ms <= 0.0) {
                continue;
            }
            int width = Math.max(1, (int) Math.round((ms / sum) * STACK_WIDTH));
            if (cursor + width > x + STACK_WIDTH) {
                width = x + STACK_WIDTH - cursor;
            }
            if (width <= 0) {
                break;
            }
            int color = LEAF_COLORS.getOrDefault(stage, TEXT_COLOR);
            graphics.fill(cursor, y, cursor + width, y + STACK_HEIGHT, color);
            cursor += width;
        }
        int legendY = y + STACK_HEIGHT + 2;
        int legendX = x;
        for (PortalPerfStats.Stage stage : PortalPerfStats.Stage.LEAF_STAGES) {
            double ms = snap.avgStageMs().getOrDefault(stage, 0.0);
            if (ms <= 0.0) {
                continue;
            }
            int color = LEAF_COLORS.getOrDefault(stage, TEXT_COLOR);
            String label = stage.label();
            graphics.text(client.font, label, legendX, legendY, color);
            legendX += client.font.width(label) + 6;
            if (legendX > x + STACK_WIDTH + 40) {
                legendX = x;
                legendY += LINE_HEIGHT;
            }
        }
    }
}
