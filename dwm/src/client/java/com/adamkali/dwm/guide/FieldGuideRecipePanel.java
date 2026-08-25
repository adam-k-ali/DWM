package com.adamkali.dwm.guide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
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
    private static final Identifier CRAFTING_TABLE_GUI =
            Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");
    private static final Identifier FURNACE_GUI =
            Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier STONECUTTER_GUI =
            Identifier.withDefaultNamespace("textures/gui/container/stonecutter.png");

    /** Scale applied to vanilla station GUI crops so they fit the right book page. */
    private static final float PREVIEW_SCALE = 0.48F;

    private static final int CRAFTING_U = 0;
    private static final int CRAFTING_V = 10;
    private static final int CRAFTING_CROP_WIDTH = 118;
    private static final int CRAFTING_CROP_HEIGHT = 52;

    private static final int FURNACE_U = 26;
    private static final int FURNACE_V = 12;
    private static final int FURNACE_CROP_WIDTH = 102;
    private static final int FURNACE_CROP_HEIGHT = 50;

    private static final int STONECUTTER_U = 0;
    private static final int STONECUTTER_V = 10;
    private static final int STONECUTTER_CROP_WIDTH = 118;
    private static final int STONECUTTER_CROP_HEIGHT = 48;

    private static final int CRAFT_SLOT_ORIGIN_X = 30;
    private static final int CRAFT_SLOT_ORIGIN_Y = 17;
    private static final int CRAFT_RESULT_X = 98;
    private static final int CRAFT_RESULT_Y = 35;
    private static final int SLOT_SIZE = 18;

    private static final int FURNACE_INPUT_X = 56;
    private static final int FURNACE_INPUT_Y = 17;
    private static final int FURNACE_RESULT_X = 116;
    private static final int FURNACE_RESULT_Y = 17;

    private static final int STONECUTTER_INPUT_X = 20;
    private static final int STONECUTTER_INPUT_Y = 14;
    private static final int STONECUTTER_RESULT_X = 96;
    private static final int STONECUTTER_RESULT_Y = 14;

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
        if (page.craftingRecipe() != null) {
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
            int bookLeft,
            int bookTop,
            FieldGuidePage page,
            Station station,
            int mouseX,
            int mouseY
    ) {
        Identifier recipeId = recipeIdFor(page, station);
        if (recipeId == null) {
            return;
        }

        Optional<RecipeHolder<?>> holder = recipeHolder(client, recipeId);
        if (holder.isEmpty()) {
            renderUnavailable(client, graphics, bookLeft, bookTop);
            return;
        }

        Recipe<?> recipe = holder.get().value();
        ContextMap context = displayContext(client);
        Optional<RecipeDisplay> display = recipe.display().stream()
                .filter(candidate -> matchesStation(candidate, station))
                .findFirst();
        if (display.isEmpty()) {
            renderUnavailable(client, graphics, bookLeft, bookTop);
            return;
        }

        int baseY = bookTop + FieldGuideBookLayout.RIGHT_RECIPE_Y;
        switch (station) {
            case CRAFTING -> renderCraftingPreview(graphics, client, bookLeft, baseY, display.get(), context, mouseX, mouseY);
            case SMELTING -> renderFurnacePreview(graphics, client, bookLeft, baseY, display.get(), context, mouseX, mouseY);
            case STONECUTTING -> renderStonecutterPreview(graphics, client, bookLeft, baseY, display.get(), context, mouseX, mouseY);
        }
    }

    private static void renderUnavailable(Minecraft client, GuiGraphicsExtractor graphics, int bookLeft, int bookTop) {
        graphics.text(
                client.font,
                Component.translatable("dwm.guide.recipe.unavailable"),
                bookLeft + FieldGuideBookLayout.RIGHT_PAGE_X,
                bookTop + FieldGuideBookLayout.RIGHT_RECIPE_Y,
                0xFF888888,
                false
        );
    }

    private static int renderCraftingPreview(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int bookLeft,
            int y,
            RecipeDisplay display,
            ContextMap context,
            int mouseX,
            int mouseY
    ) {
        Optional<FieldGuideRecipeGridBuilder.CraftingPreview> preview =
                FieldGuideRecipeGridBuilder.craftingPreview(display, context);
        if (preview.isEmpty()) {
            return y;
        }

        int x = centeredX(bookLeft, CRAFTING_CROP_WIDTH);
        blitStationPreview(graphics, CRAFTING_TABLE_GUI, x, y, CRAFTING_U, CRAFTING_V, CRAFTING_CROP_WIDTH, CRAFTING_CROP_HEIGHT);

        FieldGuideRecipeGridBuilder.CraftingPreview crafting = preview.get();
        for (FieldGuideRecipeGridBuilder.GridSlot slot : crafting.slots()) {
            int slotX = x + scaled(CRAFT_SLOT_ORIGIN_X - CRAFTING_U + slot.column() * SLOT_SIZE);
            int slotY = y + scaled(CRAFT_SLOT_ORIGIN_Y - CRAFTING_V + slot.row() * SLOT_SIZE);
            drawItem(graphics, client.font, slot.stack(), slotX, slotY, mouseX, mouseY);
        }
        drawItem(
                graphics,
                client.font,
                crafting.result(),
                x + scaled(CRAFT_RESULT_X - CRAFTING_U),
                y + scaled(CRAFT_RESULT_Y - CRAFTING_V),
                mouseX,
                mouseY
        );
        return y + scaled(CRAFTING_CROP_HEIGHT);
    }

    private static int renderFurnacePreview(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int bookLeft,
            int y,
            RecipeDisplay display,
            ContextMap context,
            int mouseX,
            int mouseY
    ) {
        if (!(display instanceof FurnaceRecipeDisplay furnace)) {
            return y;
        }

        int x = centeredX(bookLeft, FURNACE_CROP_WIDTH);
        blitStationPreview(graphics, FURNACE_GUI, x, y, FURNACE_U, FURNACE_V, FURNACE_CROP_WIDTH, FURNACE_CROP_HEIGHT);
        ItemStack input = furnace.ingredient().resolveForFirstStack(context);
        ItemStack result = furnace.result().resolveForFirstStack(context);
        drawItem(graphics, client.font, input, x + scaled(FURNACE_INPUT_X - FURNACE_U), y + scaled(FURNACE_INPUT_Y - FURNACE_V), mouseX, mouseY);
        drawItem(graphics, client.font, result, x + scaled(FURNACE_RESULT_X - FURNACE_U), y + scaled(FURNACE_RESULT_Y - FURNACE_V), mouseX, mouseY);
        return y + scaled(FURNACE_CROP_HEIGHT);
    }

    private static int renderStonecutterPreview(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int bookLeft,
            int y,
            RecipeDisplay display,
            ContextMap context,
            int mouseX,
            int mouseY
    ) {
        if (!(display instanceof StonecutterRecipeDisplay stonecutter)) {
            return y;
        }

        int x = centeredX(bookLeft, STONECUTTER_CROP_WIDTH);
        blitStationPreview(graphics, STONECUTTER_GUI, x, y, STONECUTTER_U, STONECUTTER_V, STONECUTTER_CROP_WIDTH, STONECUTTER_CROP_HEIGHT);
        ItemStack input = stonecutter.input().resolveForFirstStack(context);
        ItemStack result = stonecutter.result().resolveForFirstStack(context);
        drawItem(graphics, client.font, input, x + scaled(STONECUTTER_INPUT_X - STONECUTTER_U), y + scaled(STONECUTTER_INPUT_Y - STONECUTTER_V), mouseX, mouseY);
        drawItem(graphics, client.font, result, x + scaled(STONECUTTER_RESULT_X - STONECUTTER_U), y + scaled(STONECUTTER_RESULT_Y - STONECUTTER_V), mouseX, mouseY);
        return y + scaled(STONECUTTER_CROP_HEIGHT);
    }

    private static int centeredX(int bookLeft, int sourceWidth) {
        int previewWidth = scaled(sourceWidth);
        int pageCenter = bookLeft + FieldGuideBookLayout.RIGHT_PAGE_X + FieldGuideBookLayout.RIGHT_PAGE_WIDTH / 2;
        return pageCenter - previewWidth / 2;
    }

    private static void blitStationPreview(
            GuiGraphicsExtractor graphics,
            Identifier texture,
            int x,
            int y,
            int sourceU,
            int sourceV,
            int sourceWidth,
            int sourceHeight
    ) {
        int width = scaled(sourceWidth);
        int height = scaled(sourceHeight);
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF5C4A32);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
                sourceU,
                sourceV,
                width,
                height,
                256,
                256
        );
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

    private static int scaled(int value) {
        return Math.round(value * PREVIEW_SCALE);
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

    private static @Nullable Identifier recipeIdFor(FieldGuidePage page, Station station) {
        return switch (station) {
            case CRAFTING -> page.craftingRecipe();
            case SMELTING -> page.smeltingRecipe();
            case STONECUTTING -> page.stonecuttingRecipe();
        };
    }
}
