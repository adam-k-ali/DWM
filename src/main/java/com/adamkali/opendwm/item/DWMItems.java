package com.adamkali.opendwm.item;

import com.adamkali.opendwm.DWMMain;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;


@Mod.EventBusSubscriber(modid = DWMMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DWMItems {
    public static final RegistryObject<Item> SONIC_THIRD_DOCTOR = DWMMain.ITEMS.register("sonic_third_doctor",
            () -> new SonicScrewdriverItem(new Item.Properties().setId(DWMMain.ITEMS.key("sonic_third_doctor"))));

}
