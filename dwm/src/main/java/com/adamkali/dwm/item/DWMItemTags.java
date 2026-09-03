package com.adamkali.dwm.item;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class DWMItemTags {
    public static final TagKey<Item> SONIC_SCREWDRIVERS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sonic_screwdrivers"));
    public static final TagKey<Item> GALLIFREY_STONE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "gallifrey_stone"));
    public static final TagKey<Item> ORANGE_SAND = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "orange_sand"));
    public static final TagKey<Item> CITADEL = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "citadel"));
    public static final TagKey<Item> GALLIFREY_PLANTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "gallifrey_plants"));
    public static final TagKey<Item> ASH_LOGS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "ash_logs"));
    public static final TagKey<Item> DARK_ASH_LOGS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dark_ash_logs"));
    public static final TagKey<Item> CARDINAL_LOGS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cardinal_logs"));
    public static final TagKey<Item> PETRIFIED_BLOCKS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "petrified_blocks"));
    public static final TagKey<Item> PETRIFIED_LOGS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "petrified_logs"));
    public static final TagKey<Item> AZBANTIUM_ORES = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "azbantium_ores"));
    public static final TagKey<Item> ZEITON_ORES = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "zeiton_ores"));
    public static final TagKey<Item> REPAIRS_AZBANTIUM_EQUIPMENT = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "repairs_azbantium_equipment")
    );

    /** Vanilla still ships item tag JSON; ItemTags no longer exposes constants in 26.2. */
    public static final TagKey<Item> DOORS = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("doors"));
    public static final TagKey<Item> TRAPDOORS = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("trapdoors"));
}
