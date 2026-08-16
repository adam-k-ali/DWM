package com.adamkali.dwm.item;


import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyRegistrar;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.equipment.ArmorType;
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
    public static final Item TARDIS_KEY = register(
            TardisKeyItem::new,
            new Item.Properties().stacksTo(1),
            "tardis_key"
    );

    public static final Item AZBANTIUM = register(Item::new, "azbantium");

    public static final Item AZBANTIUM_SWORD = register(
            Item::new,
            new Item.Properties().sword(DWMToolMaterials.AZBANTIUM, 3.0F, -2.4F),
            "azbantium_sword"
    );
    public static final Item AZBANTIUM_SHOVEL = register(
            props -> new ShovelItem(DWMToolMaterials.AZBANTIUM, 1.5F, -3.0F, props),
            "azbantium_shovel"
    );
    public static final Item AZBANTIUM_PICKAXE = register(
            Item::new,
            new Item.Properties().pickaxe(DWMToolMaterials.AZBANTIUM, 1.0F, -2.8F),
            "azbantium_pickaxe"
    );
    public static final Item AZBANTIUM_AXE = register(
            props -> new AxeItem(DWMToolMaterials.AZBANTIUM, 5.0F, -3.0F, props),
            "azbantium_axe"
    );
    public static final Item AZBANTIUM_HOE = register(
            props -> new HoeItem(DWMToolMaterials.AZBANTIUM, -3.0F, 0.0F, props),
            "azbantium_hoe"
    );

    public static final Item AZBANTIUM_HELMET = register(
            Item::new,
            new Item.Properties().humanoidArmor(DWMArmorMaterials.AZBANTIUM, ArmorType.HELMET),
            "azbantium_helmet"
    );
    public static final Item AZBANTIUM_CHESTPLATE = register(
            Item::new,
            new Item.Properties().humanoidArmor(DWMArmorMaterials.AZBANTIUM, ArmorType.CHESTPLATE),
            "azbantium_chestplate"
    );
    public static final Item AZBANTIUM_LEGGINGS = register(
            Item::new,
            new Item.Properties().humanoidArmor(DWMArmorMaterials.AZBANTIUM, ArmorType.LEGGINGS),
            "azbantium_leggings"
    );
    public static final Item AZBANTIUM_BOOTS = register(
            Item::new,
            new Item.Properties().humanoidArmor(DWMArmorMaterials.AZBANTIUM, ArmorType.BOOTS),
            "azbantium_boots"
    );

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

        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.INGREDIENTS).register(content -> {
            content.accept(AZBANTIUM);
        });

        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.TOOLS_AND_UTILITIES).register(content -> {
            content.accept(SONIC_SECOND_DOCTOR);
            content.accept(SONIC_THIRD_DOCTOR);
            content.accept(SONIC_FOURTH_DOCTOR);
            content.accept(SONIC_FIFTH_DOCTOR);
            content.accept(TARDIS_KEY);
            content.accept(AZBANTIUM_SHOVEL);
            content.accept(AZBANTIUM_PICKAXE);
            content.accept(AZBANTIUM_AXE);
            content.accept(AZBANTIUM_HOE);
            for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
                content.accept(family.boatItem());
            }
        });

        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.COMBAT).register(content -> {
            content.accept(AZBANTIUM_SWORD);
            content.accept(AZBANTIUM_HELMET);
            content.accept(AZBANTIUM_CHESTPLATE);
            content.accept(AZBANTIUM_LEGGINGS);
            content.accept(AZBANTIUM_BOOTS);
        });

        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.FUNCTIONAL_BLOCKS).register(content -> {
            for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
                content.accept(family.signItem());
                content.accept(family.hangingSignItem());
            }
        });
    }

    public static Item register(Function<Item.Properties, Item> item, String id) {
        return register(item, new Item.Properties(), id);
    }

    public static Item register(Function<Item.Properties, Item> factory, Item.Properties settings, String id) {
        Identifier itemID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, itemID);
        Item item = factory.apply(settings.setId(itemKey));

        return Registry.register(BuiltInRegistries.ITEM, itemID, item);
    }
}
