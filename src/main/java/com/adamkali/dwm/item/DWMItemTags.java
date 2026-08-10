package com.adamkali.dwm.item;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class DWMItemTags {
    public static final TagKey<Item> SONIC_SCREWDRIVERS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sonic_screwdrivers"));
    public static final TagKey<Item> GALLIFREY_STONE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "gallifrey_stone"));
    public static final TagKey<Item> CITADEL = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "citadel"));
    public static final TagKey<Item> ASH_LOGS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "ash_logs"));
    public static final TagKey<Item> DARK_ASH_LOGS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dark_ash_logs"));
    public static final TagKey<Item> CARDINAL_LOGS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cardinal_logs"));
}
