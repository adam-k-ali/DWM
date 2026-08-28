package com.adamkali.dwm.item;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * Field modes for the sonic screwdriver. Cycle order is declaration order.
 */
public enum SonicFieldMode implements StringRepresentable {
    OPEN("open", "Open"),
    SHATTER("shatter", "Shatter"),
    PRIME("prime", "Prime"),
    DISRUPT("disrupt", "Disrupt"),
    SHEAR("shear", "Shear"),
    SEAL("seal", "Seal"),
    SCAN("scan", "Scan"),
    PING("ping", "Ping");

    public static final StringRepresentable.EnumCodec<SonicFieldMode> CODEC =
            StringRepresentable.fromEnum(SonicFieldMode::values);

    private final String id;
    private final String displayName;

    SonicFieldMode(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String translationKey() {
        return "dwm.sonic.mode." + id;
    }

    public boolean isTardisMode() {
        return this == SEAL || this == SCAN || this == PING;
    }

    public static SonicFieldMode[] cycleOrder() {
        return values();
    }

    public @Nullable SonicFieldMode next() {
        SonicFieldMode[] modes = values();
        int next = ordinal() + 1;
        return next < modes.length ? modes[next] : null;
    }

    public static @Nullable SonicFieldMode byId(String id) {
        return CODEC.byName(id);
    }

    /**
     * Steps through all modes in cycle order with wrap-around. Used by the field-mode HUD carousel.
     */
    public SonicFieldMode step(int direction) {
        SonicFieldMode[] modes = cycleOrder();
        int step = direction < 0 ? -1 : 1;
        return modes[Math.floorMod(ordinal() + step, modes.length)];
    }

    /**
     * Shortest signed distance from {@code center} to {@code mode} in cycle order.
     * Used to lay out the field-mode HUD carousel around the preview selection.
     */
    public static int signedOffset(SonicFieldMode center, SonicFieldMode mode) {
        int diff = mode.ordinal() - center.ordinal();
        int count = values().length;
        if (diff > count / 2) {
            diff -= count;
        }
        if (diff < -count / 2) {
            diff += count;
        }
        return diff;
    }

    /** Item icon representing this mode's primary target (HUD carousel). */
    public ItemStack targetIconStack() {
        return switch (this) {
            case OPEN -> new ItemStack(Items.IRON_DOOR);
            case SHATTER -> new ItemStack(Items.GLASS_PANE);
            case PRIME -> new ItemStack(Items.TNT);
            case DISRUPT -> new ItemStack(Items.SLIME_BALL);
            case SHEAR -> new ItemStack(Items.SHEARS);
            case SEAL -> new ItemStack(DWMItems.TARDIS_KEY);
            case SCAN -> new ItemStack(Items.COMPASS);
            case PING -> new ItemStack(Items.ENDER_EYE);
        };
    }

    public String recipeHintKey() {
        return switch (this) {
            case OPEN -> "dwm.sonic.recipe_hint.open";
            case SHATTER -> "dwm.sonic.recipe_hint.shatter";
            case PRIME -> "dwm.sonic.recipe_hint.prime";
            case DISRUPT -> "dwm.sonic.recipe_hint.disrupt";
            case SHEAR -> "dwm.sonic.recipe_hint.shear";
            case SEAL, SCAN, PING -> "dwm.sonic.recipe_hint.tardis_pair";
        };
    }
}
