package com.adamkali.opendwm.item;

import com.adamkali.opendwm.DWMMain;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;


@Mod.EventBusSubscriber(modid = DWMMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DWMItems {
    public static final RegistryObject<Item> SONIC_THIRD_DOCTOR = DWMMain.ITEMS.register("sonic_third_doctor",
            () -> new SonicScrewdriverItem(new Item.Properties().setId(DWMMain.ITEMS.key("sonic_third_doctor"))));

    @SubscribeEvent
    public static void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        System.out.println("Adding item to creative mode tab");
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(SONIC_THIRD_DOCTOR);
        }
    }


}
