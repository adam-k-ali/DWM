package com.adamkali.dwm.item;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DWMEntityTypes;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SignItem;
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
    public static Item ASH_BOAT;

    public static void initialize() {
        ASH_SIGN = register(
                settings -> new SignItem(DWMBlocks.ASH_SIGN, DWMBlocks.ASH_WALL_SIGN, settings),
                new Item.Settings().maxCount(16),
                "ash_sign"
        );
        ASH_BOAT = register(
                settings -> new BoatItem(DWMEntityTypes.ASH_BOAT, settings),
                new Item.Settings().maxCount(1),
                "ash_boat"
        );

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.add(SONIC_SECOND_DOCTOR);
            content.add(SONIC_THIRD_DOCTOR);
            content.add(SONIC_FOURTH_DOCTOR);
            content.add(SONIC_FIFTH_DOCTOR);
            content.add(ASH_BOAT);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            content.add(ASH_SIGN);
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
