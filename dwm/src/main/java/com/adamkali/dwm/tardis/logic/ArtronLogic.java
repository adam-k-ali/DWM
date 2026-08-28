package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Single persisted artron tank: missing/null Gson means full; found Type 40s start low.
 */
public final class ArtronLogic {
    public static final int CAPACITY = 500;
    public static final int FOUND_START = 30;
    public static final int FILL_AMOUNT = 25;
    public static final int SAME_WORLD_COST = 10;
    public static final int DIMENSION_CHANGE_COST = 30;

    public static final String ARTRON_KEY = "dwm.console.artron";
    public static final String ARTRON_EMPTY_KEY = "dwm.console.artron_empty";
    public static final String ARTRON_FULL_KEY = "dwm.console.artron_full";
    public static final String ARTRON_USE_CRYSTALS_KEY = "dwm.console.artron_use_crystals";
    public static final String NOT_ENOUGH_KEY = "dwm.console.not_enough_artron";

    private ArtronLogic() {
    }

    /**
     * {@code null} model or missing Gson field means a full tank (legacy worlds / unset).
     */
    public static int read(@Nullable TardisDataModel model) {
        if (model == null || model.artron == null) {
            return CAPACITY;
        }
        return Mth.clamp(model.artron, 0, CAPACITY);
    }

    public static float needle(@Nullable TardisDataModel model) {
        return needleFrom(read(model));
    }

    public static float needleFrom(int artron) {
        return Mth.clamp(artron, 0, CAPACITY) / (float) CAPACITY;
    }

    public static int percent(int artron) {
        return Math.round(needleFrom(artron) * 100.0F);
    }

    public static Component reservesMessage(int artron) {
        if (artron <= 0) {
            return Component.translatable(ARTRON_EMPTY_KEY);
        }
        return Component.translatable(ARTRON_KEY, percent(artron));
    }

    public static String spendRefuseKey(int artron) {
        return artron <= 0 ? ARTRON_EMPTY_KEY : NOT_ENOUGH_KEY;
    }

    public static String spendRefuseKey(@Nullable TardisDataModel model) {
        return spendRefuseKey(read(model));
    }

    /**
     * Same-world hops cost {@link #SAME_WORLD_COST}; a proven dimension change costs
     * {@link #DIMENSION_CHANGE_COST}. Blank or missing ids cannot prove a change.
     */
    public static int cost(@Nullable String originDimension, @Nullable String destinationDimension) {
        if (originDimension == null || originDimension.isBlank()
                || destinationDimension == null || destinationDimension.isBlank()) {
            return SAME_WORLD_COST;
        }
        return originDimension.equals(destinationDimension) ? SAME_WORLD_COST : DIMENSION_CHANGE_COST;
    }

    /**
     * Creative ({@code instabuild}) always succeeds without deducting.
     * Otherwise deducts {@code cost} when the tank can afford it.
     */
    public static boolean trySpend(@Nullable TardisDataModel model, int cost, boolean instabuild) {
        if (instabuild) {
            return true;
        }
        if (model == null || cost < 0) {
            return false;
        }
        int current = read(model);
        if (current < cost) {
            return false;
        }
        write(model, current - cost);
        return true;
    }

    /**
     * Found Type 40 starting reserve (three local hops).
     */
    public static void applyFoundStart(@Nullable TardisDataModel model) {
        if (model == null) {
            return;
        }
        write(model, FOUND_START);
    }

    public static FillResult tryFill(
            @Nullable TardisDataModel model,
            @Nullable ItemStack stack,
            boolean instabuild
    ) {
        FillResult result = tryFill(model, classify(stack));
        if (result == FillResult.FILLED && !instabuild && stack != null) {
            stack.shrink(1);
        }
        return result;
    }

    public static FillResult tryFill(@Nullable TardisDataModel model, HeldFuel held) {
        if (model == null) {
            return FillResult.READ;
        }
        if (held == HeldFuel.POWDER) {
            return FillResult.POWDER_HINT;
        }
        if (held != HeldFuel.CRYSTALS) {
            return FillResult.READ;
        }
        int current = read(model);
        if (current >= CAPACITY) {
            return FillResult.ALREADY_FULL;
        }
        write(model, Math.min(CAPACITY, current + FILL_AMOUNT));
        return FillResult.FILLED;
    }

    public static HeldFuel classify(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return HeldFuel.NONE;
        }
        if (stack.getItem() == DWMItems.ZEITON_POWDER) {
            return HeldFuel.POWDER;
        }
        if (stack.getItem() == DWMItems.ZEITON_CRYSTALS) {
            return HeldFuel.CRYSTALS;
        }
        return HeldFuel.NONE;
    }

    private static void write(TardisDataModel model, int artron) {
        model.artron = Mth.clamp(artron, 0, CAPACITY);
        model.setChanged();
    }

    public enum HeldFuel {
        NONE,
        POWDER,
        CRYSTALS
    }

    public enum FillResult {
        READ,
        POWDER_HINT,
        ALREADY_FULL,
        FILLED
    }
}
