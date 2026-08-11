package com.adamkali.dwm.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Vanilla creative-tab {@link ResourceKey}s are package-private on 26.2; recreate them by id.
 */
public final class DWMCreativeTabs {
    public static final ResourceKey<CreativeModeTab> BUILDING_BLOCKS = key("building_blocks");
    public static final ResourceKey<CreativeModeTab> NATURAL_BLOCKS = key("natural_blocks");
    public static final ResourceKey<CreativeModeTab> FUNCTIONAL_BLOCKS = key("functional_blocks");
    public static final ResourceKey<CreativeModeTab> REDSTONE_BLOCKS = key("redstone_blocks");
    public static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES = key("tools_and_utilities");

    private DWMCreativeTabs() {
    }

    private static ResourceKey<CreativeModeTab> key(String path) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace(path));
    }
}
