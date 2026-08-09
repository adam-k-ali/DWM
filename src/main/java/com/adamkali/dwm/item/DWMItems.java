package com.adamkali.dwm.item;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyRegistrar;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class DWMItems {

    public static final Item SONIC_SECOND_DOCTOR = register(
            SonicScrewdriverItem::new,
            "sonic_second_doctor");
    public static final Item SONIC_THIRD_DOCTOR = register(
            SonicScrewdriverItem::new,
            "sonic_third_doctor");
    public static final Item SONIC_FOURTH_DOCTOR = register(
            SonicScrewdriverItem::new,
            "sonic_fourth_doctor");
    public static final Item SONIC_FIFTH_DOCTOR = register(
            SonicScrewdriverItem::new,
            "sonic_fifth_doctor");

    public static Item ASH_SIGN;
    public static Item ASH_HANGING_SIGN;
    public static Item ASH_BOAT;
    public static Item DARK_ASH_SIGN;
    public static Item DARK_ASH_HANGING_SIGN;
    public static Item DARK_ASH_BOAT;
    public static Item CARDINAL_SIGN;
    public static Item CARDINAL_HANGING_SIGN;
    public static Item CARDINAL_BOAT;

    public static void initialize() {
        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyRegistrar.registerItems(family);
        }

        ASH_SIGN = DWMBlocks.ASH.signItem();
        ASH_HANGING_SIGN = DWMBlocks.ASH.hangingSignItem();
        ASH_BOAT = DWMBlocks.ASH.boatItem();
        DARK_ASH_SIGN = DWMBlocks.DARK_ASH.signItem();
        DARK_ASH_HANGING_SIGN = DWMBlocks.DARK_ASH.hangingSignItem();
        DARK_ASH_BOAT = DWMBlocks.DARK_ASH.boatItem();
        CARDINAL_SIGN = DWMBlocks.CARDINAL.signItem();
        CARDINAL_HANGING_SIGN = DWMBlocks.CARDINAL.hangingSignItem();
        CARDINAL_BOAT = DWMBlocks.CARDINAL.boatItem();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.add(SONIC_SECOND_DOCTOR);
            content.add(SONIC_THIRD_DOCTOR);
            content.add(SONIC_FOURTH_DOCTOR);
            content.add(SONIC_FIFTH_DOCTOR);
            for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
                content.add(family.boatItem());
            }
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
                content.add(family.signItem());
                content.add(family.hangingSignItem());
            }
        });
    }

    public static Item register(Function<Item.Settings, Item> item, String id) {
        return register(item, new Item.Settings(), id);
    }

    public static Item register(Function<Item.Settings, Item> factory, Item.Settings settings, String id) {
        Identifier itemID = Identifier.of(DWMReference.MOD_ID, id);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, itemID);
        Item item = factory.apply(settings.registryKey(itemKey));

        return Registry.register(Registries.ITEM, itemID, item);
    }
}
