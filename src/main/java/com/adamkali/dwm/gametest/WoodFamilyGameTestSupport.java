package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyFeature;

/**
 * Shared helpers for wood-family GameTests. Per-family test classes remain the
 * entrypoints registered in {@code fabric.mod.json}; they pass their
 * {@link RegisteredWoodFamily} when using feature-gated assertions.
 */
public final class WoodFamilyGameTestSupport {
    private WoodFamilyGameTestSupport() {
    }

    public static boolean hasDoor(RegisteredWoodFamily family) {
        return family.has(WoodFamilyFeature.DOOR);
    }

    public static boolean hasTrapdoor(RegisteredWoodFamily family) {
        return family.has(WoodFamilyFeature.TRAPDOOR);
    }
}
