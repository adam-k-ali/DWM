package com.adamkali.dwm.item;

import com.adamkali.dwm.DWMReference;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class DWMItemTags {
    public static final TagKey<Item> SONIC_SCREWDRIVERS = TagKey.of(RegistryKeys.ITEM, Identifier.of(DWMReference.MOD_ID, "sonic_screwdrivers"));
    public static final TagKey<Item> GALLIFREY_STONE = TagKey.of(RegistryKeys.ITEM, Identifier.of(DWMReference.MOD_ID, "gallifrey_stone"));
    public static final TagKey<Item> CITADEL = TagKey.of(RegistryKeys.ITEM, Identifier.of(DWMReference.MOD_ID, "citadel"));
    public static final TagKey<Item> ASH_LOGS = TagKey.of(RegistryKeys.ITEM, Identifier.of(DWMReference.MOD_ID, "ash_logs"));
    public static final TagKey<Item> DARK_ASH_LOGS = TagKey.of(RegistryKeys.ITEM, Identifier.of(DWMReference.MOD_ID, "dark_ash_logs"));
    public static final TagKey<Item> CARDINAL_LOGS = TagKey.of(RegistryKeys.ITEM, Identifier.of(DWMReference.MOD_ID, "cardinal_logs"));
}
