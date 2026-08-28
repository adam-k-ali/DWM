package com.adamkali.dwm.item;


import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyRegistrar;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
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
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.equipment.ArmorType;
import java.util.function.Function;

public class DWMItems {

    public static final Item SONIC_SECOND_DOCTOR = register(
            SonicScrewdriverItem::new,
            new Item.Properties().stacksTo(1),
            "sonic_second_doctor");
    public static final Item SONIC_THIRD_DOCTOR = register(
            SonicScrewdriverItem::new,
            new Item.Properties().stacksTo(1),
            "sonic_third_doctor");
    public static final Item SONIC_FOURTH_DOCTOR = register(
            SonicScrewdriverItem::new,
            new Item.Properties().stacksTo(1),
            "sonic_fourth_doctor");
    public static final Item SONIC_FIFTH_DOCTOR = register(
            SonicScrewdriverItem::new,
            new Item.Properties().stacksTo(1),
            "sonic_fifth_doctor");
    public static final Item SONIC_SETTING_SHATTER = register(
            props -> new SonicSettingItem(SonicFieldMode.SHATTER, props),
            "sonic_setting_shatter");
    public static final Item SONIC_SETTING_PRIME = register(
            props -> new SonicSettingItem(SonicFieldMode.PRIME, props),
            "sonic_setting_prime");
    public static final Item SONIC_SETTING_DISRUPT = register(
            props -> new SonicSettingItem(SonicFieldMode.DISRUPT, props),
            "sonic_setting_disrupt");
    public static final Item SONIC_SETTING_SHEAR = register(
            props -> new SonicSettingItem(SonicFieldMode.SHEAR, props),
            "sonic_setting_shear");
    public static final Item TARDIS_KEY = register(
            TardisKeyItem::new,
            new Item.Properties().stacksTo(1),
            "tardis_key"
    );
    public static final Item STATTENHEIM_REMOTE = register(
            StattenheimRemoteItem::new,
            new Item.Properties().stacksTo(1),
            "stattenheim_remote"
    );

    public static final Item AZBANTIUM = register(Item::new, "azbantium");

    public static final Item ZEITON_CRYSTALS = register(Item::new, "zeiton_crystals");
    public static final Item ZEITON_POWDER = register(Item::new, "zeiton_powder");
    public static final Item FERRITE_POWDER = register(Item::new, "ferrite_powder");

    public static final Item CIRCUIT_STABILISERS = registerCircuit(TardisCircuit.STABILISERS, "circuit_stabilisers");
    public static final Item CIRCUIT_WAYPOINTS = registerCircuit(TardisCircuit.WAYPOINTS, "circuit_waypoints");
    public static final Item CIRCUIT_FAST_RETURN = registerCircuit(TardisCircuit.FAST_RETURN, "circuit_fast_return");
    public static final Item CIRCUIT_COORDINATE_LOCKS = registerCircuit(TardisCircuit.COORDINATE_LOCKS, "circuit_coordinate_locks");
    public static final Item CIRCUIT_PLANET_LOCATOR = registerCircuit(TardisCircuit.PLANET_LOCATOR, "circuit_planet_locator");
    public static final Item CIRCUIT_TELEPATHIC = registerCircuit(TardisCircuit.TELEPATHIC, "circuit_telepathic");
    public static final Item CIRCUIT_CLOAK = registerCircuit(TardisCircuit.CLOAK, "circuit_cloak");
    public static final Item CIRCUIT_CHAMELEON = registerCircuit(TardisCircuit.CHAMELEON, "circuit_chameleon");
    public static final Item CIRCUIT_REMOTE_SUMMON = registerCircuit(TardisCircuit.REMOTE_SUMMON, "circuit_remote_summon");
    public static final Item CIRCUIT_PLAYER_LOCATOR = registerCircuit(TardisCircuit.PLAYER_LOCATOR, "circuit_player_locator");

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
    public static Item BROAKIR_SPAWN_EGG;
    public static Item FLUTTERWING_SPAWN_EGG;
    public static Item MEWING_DOG_SPAWN_EGG;
    public static Item TIME_LORD_SPAWN_EGG;

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

        BROAKIR_SPAWN_EGG = register(
                SpawnEggItem::new,
                new Item.Properties().spawnEgg(DWMEntityTypes.BROAKIR),
                "broakir_spawn_egg"
        );
        FLUTTERWING_SPAWN_EGG = register(
                SpawnEggItem::new,
                new Item.Properties().spawnEgg(DWMEntityTypes.FLUTTERWING),
                "flutterwing_spawn_egg"
        );
        MEWING_DOG_SPAWN_EGG = register(
                SpawnEggItem::new,
                new Item.Properties().spawnEgg(DWMEntityTypes.MEWING_DOG),
                "mewing_dog_spawn_egg"
        );
        TIME_LORD_SPAWN_EGG = register(
                SpawnEggItem::new,
                new Item.Properties().spawnEgg(DWMEntityTypes.TIME_LORD),
                "time_lord_spawn_egg"
        );

        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.INGREDIENTS).register(content -> {
            content.accept(AZBANTIUM);
            content.accept(ZEITON_CRYSTALS);
            content.accept(ZEITON_POWDER);
            content.accept(FERRITE_POWDER);
        });

        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.TOOLS_AND_UTILITIES).register(content -> {
            content.accept(SONIC_SECOND_DOCTOR);
            content.accept(SONIC_THIRD_DOCTOR);
            content.accept(SONIC_FOURTH_DOCTOR);
            content.accept(SONIC_FIFTH_DOCTOR);
            content.accept(SONIC_SETTING_SHATTER);
            content.accept(SONIC_SETTING_PRIME);
            content.accept(SONIC_SETTING_DISRUPT);
            content.accept(SONIC_SETTING_SHEAR);
            content.accept(TARDIS_KEY);
            content.accept(STATTENHEIM_REMOTE);
            content.accept(CIRCUIT_STABILISERS);
            content.accept(CIRCUIT_WAYPOINTS);
            content.accept(CIRCUIT_FAST_RETURN);
            content.accept(CIRCUIT_COORDINATE_LOCKS);
            content.accept(CIRCUIT_PLANET_LOCATOR);
            content.accept(CIRCUIT_TELEPATHIC);
            content.accept(CIRCUIT_CLOAK);
            content.accept(CIRCUIT_CHAMELEON);
            content.accept(CIRCUIT_REMOTE_SUMMON);
            content.accept(CIRCUIT_PLAYER_LOCATOR);
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

        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.SPAWN_EGGS).register(content -> {
            content.accept(BROAKIR_SPAWN_EGG);
            content.accept(FLUTTERWING_SPAWN_EGG);
            content.accept(MEWING_DOG_SPAWN_EGG);
            content.accept(TIME_LORD_SPAWN_EGG);
        });
    }

    private static Item registerCircuit(TardisCircuit circuit, String id) {
        return register(
                props -> new ConsoleCircuitItem(circuit, props),
                new Item.Properties().stacksTo(16),
                id
        );
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
