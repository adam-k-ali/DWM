package com.adamkali.dwm.item;

import com.adamkali.dwm.DWMMain;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;


@Mod.EventBusSubscriber(modid = DWMMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DWMItems {

    public static final RegistryObject<Item> SONIC_SECOND_DOCTOR = DWMMain.ITEMS.register("sonic_second_doctor",
            () -> new SonicScrewdriverItem(new Item.Properties().setId(DWMMain.ITEMS.key("sonic_second_doctor"))));
    public static final RegistryObject<Item> SONIC_THIRD_DOCTOR = DWMMain.ITEMS.register("sonic_third_doctor",
            () -> new SonicScrewdriverItem(new Item.Properties().setId(DWMMain.ITEMS.key("sonic_third_doctor"))));
    public static final RegistryObject<Item> SONIC_FOURTH_DOCTOR = DWMMain.ITEMS.register("sonic_fourth_doctor",
            () -> new SonicScrewdriverItem(new Item.Properties().setId(DWMMain.ITEMS.key("sonic_fourth_doctor"))));
    public static final RegistryObject<Item> SONIC_FIFTH_DOCTOR = DWMMain.ITEMS.register("sonic_fifth_doctor",
            () -> new SonicScrewdriverItem(new Item.Properties().setId(DWMMain.ITEMS.key("sonic_fifth_doctor"))));

    @SubscribeEvent
    public static void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(SONIC_SECOND_DOCTOR);
            event.accept(SONIC_THIRD_DOCTOR);
            event.accept(SONIC_FOURTH_DOCTOR);
            event.accept(SONIC_FIFTH_DOCTOR);
        }
    }


}
