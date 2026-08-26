package com.adamkali.dwm.render;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.item.SonicFieldMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * In-world carousel HUD for sonic field-mode selection (above the hotbar).
 */
@Environment(EnvType.CLIENT)
public final class SonicFieldModeHud {
    private static final Identifier ELEMENT_ID =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sonic_field_mode_hud");
    private static final Identifier SLOT = sprite("slot");
    private static final Identifier SLOT_LOCKED = sprite("slot_locked");
    private static final Identifier INDICATOR_PANEL = sprite("indicator_panel");
    private static final Identifier[] SELECTED_SLOTS = {
            sprite("slot_selected_0"),
            sprite("slot_selected_1"),
            sprite("slot_selected_2"),
            sprite("slot_selected_3")
    };

    private static final int HOTBAR_OFFSET = 67;
    private static final int HINT_OFFSET = 18;
    private static final int LABEL_BELOW_CENTER = 5;
    private static final int RECIPE_OFFSET = 12;
    private static final int INDICATOR_SLOT_SIZE = 24;
    private static final int INDICATOR_ICON_SIZE = 14;
    private static final int DIM_TEXT_COLOR = ARGB.opaque(0xA0A0A0);
    private static final int HINT_COLOR = ARGB.opaque(0xD7E8E5);
    private static final int MODE_COLOR = ARGB.opaque(0xACF4EE);

    private SonicFieldModeHud() {
    }

    public static void initialize() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, ELEMENT_ID, SonicFieldModeHud::extract);
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            return;
        }
        ItemStack heldSonic = SonicFieldModeHudController.heldSonic(player);
        boolean carouselActive = SonicFieldModeHudController.isActive();
        if (carouselActive) {
            renderCarousel(graphics, client, player);
        } else if (SonicCarouselLayout.shouldShowIndicator(!heldSonic.isEmpty(), carouselActive)) {
            renderIndicator(
                    graphics,
                    client,
                    SonicFieldModeHudController.selectedMode(player),
                    !player.getActiveEffects().isEmpty()
            );
        }
    }

    private static void renderCarousel(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            LocalPlayer player
    ) {
        SonicFieldMode preview = SonicFieldModeHudController.previewMode();
        long nowMs = Util.getMillis();
        int centerX = graphics.guiWidth() / 2;
        int rowY = graphics.guiHeight() - HOTBAR_OFFSET;
        int labelY = rowY + SonicCarouselLayout.BASE_SLOT_SIZE + LABEL_BELOW_CENTER;

        Component hint = Component.translatable("dwm.gui.sonic_field_mode.hint");
        graphics.centeredText(client.font, hint, centerX, rowY - HINT_OFFSET, HINT_COLOR);

        List<CarouselSlot> slots = buildCarouselSlots(preview, centerX, rowY, nowMs);
        slots.sort(Comparator.comparingDouble(slot -> -Math.abs(slot.visualOffset())));

        for (CarouselSlot slot : slots) {
            boolean unlocked = SonicFieldModeHudController.isUnlocked(player, slot.mode());
            boolean selected = slot.mode() == preview;
            Identifier slotSprite = selected
                    ? SELECTED_SLOTS[SonicFieldModeHudController.selectionPhase(nowMs)]
                    : SLOT;
            int tint = tintForSlot(slot.visualOffset(), unlocked);
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    slotSprite,
                    slot.geometry().slotX(),
                    slot.geometry().slotY(),
                    slot.geometry().slotSize(),
                    slot.geometry().slotSize(),
                    tint
            );

            SonicCarouselLayout.SlotGeometry geometry = slot.geometry();
            renderScaledItem(
                    graphics,
                    slot.mode().targetIconStack(),
                    geometry.iconX(),
                    geometry.iconY(),
                    geometry.iconSize()
            );
            if (!unlocked) {
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        SLOT_LOCKED,
                        geometry.slotX(),
                        geometry.slotY(),
                        geometry.slotSize(),
                        geometry.slotSize()
                );
            }
        }

        Component modeLabel;
        if (SonicFieldModeHudController.isUnlocked(player, preview)) {
            modeLabel = Component.translatable(preview.translationKey());
        } else {
            modeLabel = Component.translatable("dwm.gui.sonic_field_mode.locked", Component.translatable(preview.translationKey()));
        }
        graphics.centeredText(
                client.font,
                modeLabel,
                centerX,
                labelY,
                SonicFieldModeHudController.isUnlocked(player, preview) ? HINT_COLOR : DIM_TEXT_COLOR
        );

        if (!SonicFieldModeHudController.isUnlocked(player, preview)) {
            Component recipeHint = Component.translatable(
                    "dwm.gui.sonic_field_mode.locked_hint",
                    Component.translatable(preview.translationKey()),
                    Component.translatable(preview.recipeHintKey())
            );
            graphics.centeredText(client.font, recipeHint, centerX, labelY + RECIPE_OFFSET, DIM_TEXT_COLOR);
        }
    }

    private static void renderIndicator(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            SonicFieldMode activeMode,
            boolean statusEffectsVisible
    ) {
        int panelX = SonicCarouselLayout.indicatorX(graphics.guiWidth());
        int panelY = SonicCarouselLayout.indicatorY(statusEffectsVisible);
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                INDICATOR_PANEL,
                panelX,
                panelY,
                SonicCarouselLayout.INDICATOR_WIDTH,
                SonicCarouselLayout.INDICATOR_HEIGHT
        );

        int slotX = panelX + 5;
        int slotY = panelY + 3;
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                SELECTED_SLOTS[3],
                slotX,
                slotY,
                INDICATOR_SLOT_SIZE,
                INDICATOR_SLOT_SIZE
        );
        renderScaledItem(
                graphics,
                activeMode.targetIconStack(),
                slotX + (INDICATOR_SLOT_SIZE - INDICATOR_ICON_SIZE) / 2,
                slotY + (INDICATOR_SLOT_SIZE - INDICATOR_ICON_SIZE) / 2,
                INDICATOR_ICON_SIZE
        );
        graphics.text(
                client.font,
                Component.translatable(activeMode.translationKey()),
                panelX + 38,
                panelY + 11,
                MODE_COLOR,
                false
        );
    }

    private static List<CarouselSlot> buildCarouselSlots(
            SonicFieldMode preview,
            int centerX,
            int rowY,
            long nowMs
    ) {
        float visualScroll = SonicFieldModeHudController.visualScroll(nowMs);
        float targetScroll = SonicFieldModeHudController.targetScroll();
        float selectedScaleBoost = SonicFieldModeHudController.selectedScaleBoost(nowMs);
        List<CarouselSlot> slots = new ArrayList<>();
        for (SonicFieldMode mode : SonicFieldMode.cycleOrder()) {
            float visualOffset =
                    SonicCarouselLayout.visualOffset(preview, mode, visualScroll, targetScroll);
            float boost = mode == preview ? selectedScaleBoost : 0.0f;
            SonicCarouselLayout.SlotGeometry geometry =
                    SonicCarouselLayout.slot(visualOffset, centerX, rowY, boost);
            slots.add(new CarouselSlot(mode, visualOffset, geometry));
        }
        return slots;
    }

    private static void renderScaledItem(
            GuiGraphicsExtractor graphics,
            ItemStack stack,
            int x,
            int y,
            int size
    ) {
        float scale = size / (float) SonicCarouselLayout.BASE_ICON_SIZE;
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(x, y);
            graphics.pose().scale(scale, scale);
            graphics.item(stack, 0, 0);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    private static int tintForSlot(float offset, boolean unlocked) {
        float distance = Math.abs(offset);
        float alpha = distance <= 1.0f ? 1.0f : Math.max(0.48f, 1.15f - distance * 0.22f);
        float brightness = unlocked ? 1.0f : 0.55f;
        return ARGB.colorFromFloat(alpha, brightness, brightness, brightness);
    }

    private static Identifier sprite(String name) {
        return Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sonic_field_mode/" + name);
    }

    private record CarouselSlot(
            SonicFieldMode mode,
            float visualOffset,
            SonicCarouselLayout.SlotGeometry geometry
    ) {
    }
}
