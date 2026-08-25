package com.adamkali.dwm.guide;

import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Maps recipe display data to a 3×3 crafting grid for Field Guide previews.
 */
public final class FieldGuideRecipeGridBuilder {
    public static final int GRID_SIZE = 3;

    private FieldGuideRecipeGridBuilder() {
    }

    public record GridSlot(int column, int row, ItemStack stack) {
    }

    public record CraftingPreview(List<GridSlot> slots, ItemStack result) {
        public static CraftingPreview empty() {
            return new CraftingPreview(List.of(), ItemStack.EMPTY);
        }
    }

    public static Optional<CraftingPreview> craftingPreview(RecipeDisplay display, ContextMap context) {
        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            return Optional.of(buildShaped(shaped.width(), shaped.height(), resolveStacks(shaped.ingredients(), context),
                    shaped.result().resolveForFirstStack(context)));
        }
        if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            return Optional.of(buildShapeless(resolveStacks(shapeless.ingredients(), context),
                    shapeless.result().resolveForFirstStack(context)));
        }
        return Optional.empty();
    }

    static CraftingPreview buildShaped(int width, int height, List<ItemStack> ingredients, ItemStack result) {
        List<GridSlot> slots = new ArrayList<>();
        for (int index = 0; index < ingredients.size(); index++) {
            ItemStack stack = ingredients.get(index);
            if (stack.isEmpty()) {
                continue;
            }
            addShapedSlot(slots, width, height, index, stack);
        }
        return new CraftingPreview(slots, result);
    }

    static CraftingPreview buildShapeless(List<ItemStack> ingredients, ItemStack result) {
        List<GridSlot> slots = new ArrayList<>();
        int slot = 0;
        for (ItemStack stack : ingredients) {
            if (stack.isEmpty()) {
                continue;
            }
            addShapelessSlot(slots, slot, stack);
            slot++;
            if (slot >= GRID_SIZE * GRID_SIZE) {
                break;
            }
        }
        return new CraftingPreview(slots, result);
    }

    /**
     * Maps occupied shaped-crafting indices to grid coordinates without requiring bound item stacks.
     */
    static List<GridSlot> shapedSlotLayout(int width, int height, java.util.function.IntPredicate occupied) {
        List<GridSlot> slots = new ArrayList<>();
        for (int index = 0; index < width * height; index++) {
            if (!occupied.test(index)) {
                continue;
            }
            addShapedSlot(slots, width, height, index, ItemStack.EMPTY);
        }
        return slots;
    }

    /**
     * Maps shapeless ingredient count to left-to-right grid coordinates without requiring bound item stacks.
     */
    static List<GridSlot> shapelessSlotLayout(int ingredientCount) {
        List<GridSlot> slots = new ArrayList<>();
        for (int slot = 0; slot < ingredientCount && slot < GRID_SIZE * GRID_SIZE; slot++) {
            addShapelessSlot(slots, slot, ItemStack.EMPTY);
        }
        return slots;
    }

    private static void addShapedSlot(List<GridSlot> slots, int width, int height, int index, ItemStack stack) {
        int column = index % width;
        int row = index / width;
        if (row >= height) {
            return;
        }
        slots.add(new GridSlot(column, row, stack));
    }

    private static void addShapelessSlot(List<GridSlot> slots, int slotIndex, ItemStack stack) {
        slots.add(new GridSlot(slotIndex % GRID_SIZE, slotIndex / GRID_SIZE, stack));
    }

    private static List<ItemStack> resolveStacks(List<SlotDisplay> ingredients, ContextMap context) {
        return ingredients.stream().map(display -> display.resolveForFirstStack(context)).toList();
    }
}
