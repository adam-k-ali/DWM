package com.adamkali.dwm.item;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Capability rules for {@link SonicState} on sonic stacks (same three-path shape as circuit fitted):
 * <ul>
 *   <li>Missing component (legacy / {@code /give} / creative) → all field modes unlocked</li>
 *   <li>Crafted result → Open only</li>
 *   <li>First mutation on a missing-component stack writes a fully-unlocked component</li>
 * </ul>
 */
public final class SonicStateLogic {
    public static final String SETTING_KEY = "dwm.sonic.setting";
    public static final String WRONG_SETTING_KEY = "dwm.sonic.wrong_setting";
    public static final String NEEDS_SETTING_KEY = "dwm.sonic.needs_setting";
    public static final String SETTING_NOT_INSTALLED_KEY = "dwm.sonic.setting_not_installed";
    public static final String NEEDS_SETTING_ITEM_KEY = "dwm.sonic.needs_setting_item";
    public static final String WRONG_SETTING_DETAIL_KEY = "dwm.sonic.wrong_setting_detail";
    public static final String SETTING_NOT_INSTALLED_DETAIL_KEY = "dwm.sonic.setting_not_installed_detail";
    public static final String SETTING_INSTALLED_KEY = "dwm.sonic.setting_installed";
    public static final String SETTING_ALREADY_INSTALLED_KEY = "dwm.sonic.setting_already_installed";
    public static final String SETTING_LOCKED_HINT_KEY = "dwm.sonic.setting_locked_hint";
    public static final String TARDIS_NOT_RECOGNISED_KEY = "dwm.sonic.tardis_not_recognised";
    public static final String TARDIS_PAIRED_KEY = "dwm.sonic.tardis_paired";
    public static final String WRONG_SETTING_SEAL_OR_SCAN_KEY = "dwm.sonic.wrong_setting_seal_or_scan";
    public static final String SCAN_OVERLAY_KEY = "dwm.sonic.scan";
    public static final String PING_LOCATED_KEY = "dwm.sonic.ping.located";
    public static final String PING_CLOAK_NOT_FITTED_KEY = "dwm.sonic.ping.cloak_not_fitted";
    public static final String PING_CLOAK_NOT_ENGAGED_KEY = "dwm.sonic.ping.cloak_not_engaged";
    public static final String PING_NO_SIGNAL_KEY = "dwm.sonic.ping.no_signal";

    private SonicStateLogic() {
    }

    /**
     * Effective state for gameplay. Missing component means fully unlocked (does not write).
     */
    public static SonicState effective(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return SonicState.fullyUnlocked();
        }
        SonicState stored = stack.get(DWMDataComponents.SONIC_STATE);
        return stored == null ? SonicState.fullyUnlocked() : stored;
    }

    public static boolean hasComponent(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.has(DWMDataComponents.SONIC_STATE);
    }

    public static boolean isUnlocked(ItemStack stack, SonicFieldMode mode) {
        return effective(stack).isUnlocked(mode);
    }

    public static SonicFieldMode selected(ItemStack stack) {
        return effective(stack).selected();
    }

    /**
     * Writes Open-only state (crafted survival sonic).
     */
    public static void applyCraftedOpenOnly(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        stack.set(DWMDataComponents.SONIC_STATE, SonicState.craftedOpenOnly());
    }

    /**
     * Installs {@code mode} if missing. Returns {@code true} if newly installed,
     * {@code false} if already unlocked (or stack is not a sonic).
     * First mutation on a missing-component stack materialises fully-unlocked state first.
     */
    public static boolean install(ItemStack stack, SonicFieldMode mode) {
        if (stack == null || stack.isEmpty() || mode == null
                || mode == SonicFieldMode.OPEN || mode.isTardisMode()) {
            return false;
        }
        SonicState current = materialiseForMutation(stack);
        if (current.isUnlocked(mode)) {
            return false;
        }
        stack.set(DWMDataComponents.SONIC_STATE, current.withUnlocked(mode));
        return true;
    }

    /**
     * True when Seal / Scan / Ping are still locked — crafted Open-only sonics need the TARDIS handshake.
     * Creative {@code /give} stacks already have every mode via {@link SonicState#fullyUnlocked()}.
     */
    public static boolean needsHandshake(SonicState state) {
        if (state == null) {
            return false;
        }
        return !state.isUnlocked(SonicFieldMode.SEAL)
                || !state.isUnlocked(SonicFieldMode.SCAN)
                || !state.isUnlocked(SonicFieldMode.PING);
    }

    public static boolean needsHandshake(ItemStack stack) {
        return needsHandshake(effective(stack));
    }

    /**
     * Unlocks Seal / Scan / Ping together and marks the sonic as paired with a TARDIS.
     *
     * @return {@code true} when state changed
     */
    public static boolean pairWithTardis(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        SonicState current = materialiseForMutation(stack);
        SonicState paired = pair(current);
        if (paired.equals(current)) {
            return false;
        }
        stack.set(DWMDataComponents.SONIC_STATE, paired);
        return true;
    }

    /** Test helper: handshake then return the same stack. */
    public static ItemStack pairWithTardisStack(ItemStack stack) {
        pairWithTardis(stack);
        return stack;
    }

    /** Pure handshake mutation used by unit tests. */
    public static SonicState pair(SonicState current) {
        if (current == null) {
            return SonicState.craftedOpenOnly().withTardisPaired(true)
                    .withUnlocked(SonicFieldMode.SEAL)
                    .withUnlocked(SonicFieldMode.SCAN)
                    .withUnlocked(SonicFieldMode.PING);
        }
        return current
                .withUnlocked(SonicFieldMode.SEAL)
                .withUnlocked(SonicFieldMode.SCAN)
                .withUnlocked(SonicFieldMode.PING)
                .withTardisPaired(true);
    }

    /**
     * Selects {@code mode} if unlocked. Returns {@code true} if the selection changed.
     */
    public static boolean select(ItemStack stack, SonicFieldMode mode) {
        if (stack == null || stack.isEmpty() || mode == null) {
            return false;
        }
        SonicState current = materialiseForMutation(stack);
        if (!current.isUnlocked(mode)) {
            return false;
        }
        if (current.selected() == mode) {
            return false;
        }
        stack.set(DWMDataComponents.SONIC_STATE, current.withSelected(mode));
        return true;
    }

    /**
     * Cycles to the next unlocked field mode in cycle order (skip locked).
     * No-op when fewer than two modes are unlocked. Returns the new selection, or null if unchanged.
     */
    public static @Nullable SonicFieldMode cycleUnlocked(ItemStack stack, int direction) {
        SonicFieldMode next = peekCycle(stack, direction);
        if (next == null) {
            return null;
        }
        if (!select(stack, next)) {
            return null;
        }
        return next;
    }

    /**
     * Next unlocked mode in cycle order without mutating the stack. Null if fewer than two unlocked
     * or the selection would not change.
     */
    public static @Nullable SonicFieldMode peekCycle(ItemStack stack, int direction) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        SonicState current = effective(stack);
        List<SonicFieldMode> unlocked = unlockedInCycleOrder(current);
        if (unlocked.size() < 2) {
            return null;
        }
        int index = unlocked.indexOf(current.selected());
        if (index < 0) {
            index = 0;
        }
        int step = direction < 0 ? -1 : 1;
        int nextIndex = Math.floorMod(index + step, unlocked.size());
        SonicFieldMode next = unlocked.get(nextIndex);
        return next == current.selected() ? null : next;
    }

    public static List<SonicFieldMode> unlockedInCycleOrder(SonicState state) {
        List<SonicFieldMode> list = new ArrayList<>();
        for (SonicFieldMode mode : SonicFieldMode.cycleOrder()) {
            if (state.isUnlocked(mode)) {
                list.add(mode);
            }
        }
        return list;
    }

    /**
     * Copies {@code sonic_state} from {@code source} onto {@code target} (casing craft).
     * If source has no component, target also has none (fully unlocked by absence).
     */
    public static void copyState(ItemStack source, ItemStack target) {
        if (source == null || target == null || target.isEmpty()) {
            return;
        }
        SonicState stored = source.get(DWMDataComponents.SONIC_STATE);
        if (stored == null) {
            target.remove(DWMDataComponents.SONIC_STATE);
        } else {
            target.set(DWMDataComponents.SONIC_STATE, stored);
        }
    }

    /**
     * Ensures a mutable component exists. Missing component → write fully unlocked, then return it.
     */
    private static SonicState materialiseForMutation(ItemStack stack) {
        SonicState stored = stack.get(DWMDataComponents.SONIC_STATE);
        if (stored != null) {
            return stored;
        }
        SonicState full = SonicState.fullyUnlocked();
        stack.set(DWMDataComponents.SONIC_STATE, full);
        return full;
    }

    /** Test helper: open-only without going through craft recipes. */
    public static ItemStack openOnlyStack(ItemStack stack) {
        applyCraftedOpenOnly(stack);
        return stack;
    }

    /** Test helper: unlock a set of modes with a given selection. */
    public static ItemStack withModes(ItemStack stack, EnumSet<SonicFieldMode> unlocked, SonicFieldMode selected) {
        stack.set(DWMDataComponents.SONIC_STATE, new SonicState(unlocked, selected, false));
        return stack;
    }
}
