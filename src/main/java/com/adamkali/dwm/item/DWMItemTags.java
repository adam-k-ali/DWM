package com.adamkali.dwm.item;

import com.adamkali.dwm.DWMReference;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class DWMItemTags {
    public static final TagKey<Item> SONIC_SCREWDRIVERS = TagKey.of(RegistryKeys.ITEM, Identifier.of(DWMReference.MOD_ID, "sonic_screwdrivers"));
}
