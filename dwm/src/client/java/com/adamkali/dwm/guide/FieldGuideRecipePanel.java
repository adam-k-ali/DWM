package com.adamkali.dwm.guide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class FieldGuideRecipePanel {
    public static final int PANEL_WIDTH = 138;
    public static final int PANEL_HEIGHT = 70;
    private static final int SLOT_SIZE = 20;
    private static final int SLOT_BORDER_COLOR = 0xFF6F6251;
    private static final int SLOT_COLOR = 0xFFD8CDB9;
    private static final int PANEL_BORDER_COLOR = 0xFF9D845A;
    static final int PANEL_COLOR = 0xFFE2D2AD;

    private FieldGuideRecipePanel() {
    }

    public enum Station {
        CRAFTING("dwm.guide.recipe.crafting"),
        SMELTING("dwm.guide.recipe.smelting"),
        STONECUTTING("dwm.guide.recipe.stonecutting");

        private final String labelKey;

        Station(String labelKey) {
            this.labelKey = labelKey;
        }

        public Component label() {
            return Component.translatable(labelKey);
        }
    }

    public static List<Station> availableStations(FieldGuidePage page) {
        List<Station> stations = new java.util.ArrayList<>(3);
        if (!page.craftingRecipes().isEmpty()) {
            stations.add(Station.CRAFTING);
        }
        if (page.smeltingRecipe() != null) {
            stations.add(Station.SMELTING);
        }
        if (page.stonecuttingRecipe() != null) {
            stations.add(Station.STONECUTTING);
        }
        return stations;
    }

    public static void render(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int x,
            int y,
            FieldGuidePage page,
            Station station,
            int craftingVariantIndex,
            int mouseX,
            int mouseY
    ) {
        Identifier recipeId = recipeIdFor(page, station, craftingVariantIndex);
        if (recipeId == null) {
            return;
        }

        Optional<RecipeHolder<?>> holder = recipeHolder(client, recipeId);
        if (holder.isEmpty()) {
            renderUnavailable(client, graphics, x, y);
            return;
        }

        Recipe<?> recipe = holder.get().value();
        ContextMap context = displayContext(client);
        Optional<RecipeDisplay> display = recipe.display().stream()
                .filter(candidate -> matchesStation(candidate, station))
                .findFirst();
        if (display.isEmpty()) {
            renderUnavailable(client, graphics, x, y);
            return;
        }

        switch (station) {
            case CRAFTING -> renderCraftingPreview(graphics, client, x, y, display.get(), context, mouseX, mouseY);
            case SMELTING -> renderFurnacePreview(graphics, client, x, y, display.get(), context, mouseX, mouseY);
            case STONECUTTING -> renderStonecutterPreview(graphics, client, x, y, display.get(), context, mouseX, mouseY);
        }
    }

    public static void renderVariant(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int x,
            int y,
            Identifier recipeId,
            boolean selected,
            int mouseX,
            int mouseY
    ) {
        graphics.fill(
                x,
                y,
                x + FieldGuideBookLayout.VARIANT_SLOT_SIZE,
                y + FieldGuideBookLayout.VARIANT_SLOT_SIZE,
                selected ? FieldGuideBookLayout.ACCENT_COLOR : SLOT_BORDER_COLOR
        );
        graphics.fill(
                x + 1,
                y + 1,
                x + FieldGuideBookLayout.VARIANT_SLOT_SIZE - 1,
                y + FieldGuideBookLayout.VARIANT_SLOT_SIZE - 1,
                PANEL_COLOR
        );
        drawItem(
                graphics,
                client.font,
                craftingResultStack(client, recipeId),
                x + FieldGuideBookLayout.VARIANT_ICON_PAD,
                y + FieldGuideBookLayout.VARIANT_ICON_PAD,
                mouseX,
                mouseY
        );
    }

    private static void renderUnavailable(Minecraft client, GuiGraphicsExtractor graphics, int x, int y) {
        graphics.text(
                client.font,
                Component.translatable("dwm.guide.recipe.unavailable"),
                x,
                y,
                0xFF888888,
                false
        );
    }

    private static void renderCraftingPreview(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int x,
            int y,
            RecipeDisplay display,
            ContextMap context,
            int mouseX,
            int mouseY
    ) {
        Optional<FieldGuideRecipeGridBuilder.CraftingPreview> preview =
                FieldGuideRecipeGridBuilder.craftingPreview(display, context);
        if (preview.isEmpty()) {
            return;
        }

        renderPanel(graphics, x, y);
        int gridX = x + 7;
        int gridY = y + 5;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                renderSlot(graphics, gridX + column * SLOT_SIZE, gridY + row * SLOT_SIZE);
            }
        }

        FieldGuideRecipeGridBuilder.CraftingPreview crafting = preview.get();
        for (FieldGuideRecipeGridBuilder.GridSlot slot : crafting.slots()) {
            int slotX = gridX + slot.column() * SLOT_SIZE;
            int slotY = gridY + slot.row() * SLOT_SIZE;
            drawItemInSlot(graphics, client.font, slot.stack(), slotX, slotY, mouseX, mouseY);
        }
        graphics.text(client.font, Component.literal(">"), x + 76, y + 29, FieldGuideBookLayout.INDEX_HEADER_COLOR, false);
        int resultX = x + 103;
        int resultY = y + 25;
        renderSlot(graphics, resultX, resultY);
        drawItem(
                graphics,
                client.font,
                crafting.result(),
                resultX + 2,
                resultY + 2,
                mouseX,
                mouseY
        );
    }

    private static void renderFurnacePreview(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int x,
            int y,
            RecipeDisplay display,
            ContextMap context,
            int mouseX,
            int mouseY
    ) {
        if (!(display instanceof FurnaceRecipeDisplay furnace)) {
            return;
        }

        renderPanel(graphics, x, y);
        int inputX = x + 20;
        int outputX = x + 98;
        int slotY = y + 19;
        renderSlot(graphics, inputX, slotY);
        renderSlot(graphics, outputX, slotY);
        graphics.text(client.font, Component.literal(">"), x + 68, y + 26, FieldGuideBookLayout.INDEX_HEADER_COLOR, false);
        graphics.fill(x + 46, y + 47, x + 49, y + 55, 0xFFE59A35);
        graphics.fill(x + 50, y + 43, x + 53, y + 55, 0xFFC9652C);
        graphics.fill(x + 54, y + 48, x + 57, y + 55, 0xFFE59A35);
        ItemStack input = furnace.ingredient().resolveForFirstStack(context);
        ItemStack result = furnace.result().resolveForFirstStack(context);
        drawItemInSlot(graphics, client.font, input, inputX, slotY, mouseX, mouseY);
        drawItemInSlot(graphics, client.font, result, outputX, slotY, mouseX, mouseY);
    }

    private static void renderStonecutterPreview(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int x,
            int y,
            RecipeDisplay display,
            ContextMap context,
            int mouseX,
            int mouseY
    ) {
        if (!(display instanceof StonecutterRecipeDisplay stonecutter)) {
            return;
        }

        renderPanel(graphics, x, y);
        int inputX = x + 20;
        int outputX = x + 98;
        int slotY = y + 24;
        renderSlot(graphics, inputX, slotY);
        renderSlot(graphics, outputX, slotY);
        graphics.text(client.font, Component.literal(">"), x + 68, y + 31, FieldGuideBookLayout.INDEX_HEADER_COLOR, false);
        graphics.fill(x + 47, y + 17, x + 58, y + 19, 0xFF70675B);
        graphics.fill(x + 50, y + 14, x + 55, y + 22, 0xFF8A8175);
        ItemStack input = stonecutter.input().resolveForFirstStack(context);
        ItemStack result = stonecutter.result().resolveForFirstStack(context);
        drawItemInSlot(graphics, client.font, input, inputX, slotY, mouseX, mouseY);
        drawItemInSlot(graphics, client.font, result, outputX, slotY, mouseX, mouseY);
    }

    private static void renderPanel(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, PANEL_BORDER_COLOR);
        graphics.fill(x + 2, y + 2, x + PANEL_WIDTH - 2, y + PANEL_HEIGHT - 2, PANEL_COLOR);
    }

    private static void renderSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, SLOT_BORDER_COLOR);
        graphics.fill(x + 2, y + 2, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, SLOT_COLOR);
        graphics.fill(x + 2, y + 2, x + SLOT_SIZE - 2, y + 3, 0xFFF1E9DA);
    }

    private static void drawItemInSlot(
            GuiGraphicsExtractor graphics,
            Font font,
            ItemStack stack,
            int slotX,
            int slotY,
            int mouseX,
            int mouseY
    ) {
        drawItem(graphics, font, stack, slotX + 2, slotY + 2, mouseX, mouseY);
    }

    private static void drawItem(
            GuiGraphicsExtractor graphics,
            Font font,
            ItemStack stack,
            int x,
            int y,
            int mouseX,
            int mouseY
    ) {
        if (stack.isEmpty()) {
            return;
        }
        graphics.item(stack, x, y);
        if (stack.getCount() > 1) {
            graphics.itemDecorations(font, stack, x, y, String.valueOf(stack.getCount()));
        }
        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
            graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
        }
    }

    private static boolean matchesStation(RecipeDisplay display, Station station) {
        return switch (station) {
            case CRAFTING -> display instanceof net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay
                    || display instanceof net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
            case SMELTING -> display instanceof FurnaceRecipeDisplay;
            case STONECUTTING -> display instanceof StonecutterRecipeDisplay;
        };
    }

    private static ContextMap displayContext(Minecraft client) {
        if (client.level != null) {
            return SlotDisplayContext.fromLevel(client.level);
        }
        if (client.getSingleplayerServer() != null) {
            return SlotDisplayContext.fromLevel(client.getSingleplayerServer().overworld());
        }
        return new ContextMap.Builder().create(SlotDisplayContext.CONTEXT);
    }

    private static Optional<RecipeHolder<?>> recipeHolder(Minecraft client, Identifier recipeId) {
        if (client.getSingleplayerServer() == null) {
            return Optional.empty();
        }
        RecipeManager recipes = client.getSingleplayerServer().getRecipeManager();
        return recipes.byKey(ResourceKey.create(Registries.RECIPE, recipeId));
    }

    public static Identifier resultId(Minecraft client, Identifier recipeId) {
        ItemStack stack = craftingResultStack(client, recipeId);
        if (stack.isEmpty()) {
            return recipeId;
        }
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return itemId != null ? itemId : recipeId;
    }

    private static ItemStack craftingResultStack(Minecraft client, Identifier recipeId) {
        Optional<RecipeHolder<?>> holder = recipeHolder(client, recipeId);
        if (holder.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ContextMap context = displayContext(client);
        return holder.get().value().display().stream()
                .filter(display -> matchesStation(display, Station.CRAFTING))
                .findFirst()
                .flatMap(display -> FieldGuideRecipeGridBuilder.craftingPreview(display, context))
                .map(FieldGuideRecipeGridBuilder.CraftingPreview::result)
                .orElse(ItemStack.EMPTY);
    }

    private static @Nullable Identifier recipeIdFor(FieldGuidePage page, Station station, int craftingVariantIndex) {
        return switch (station) {
            case CRAFTING -> {
                List<Identifier> recipes = page.craftingRecipes();
                if (recipes.isEmpty()) {
                    yield null;
                }
                int index = Math.clamp(craftingVariantIndex, 0, recipes.size() - 1);
                yield recipes.get(index);
            }
            case SMELTING -> page.smeltingRecipe();
            case STONECUTTING -> page.stonecuttingRecipe();
        };
    }
}
