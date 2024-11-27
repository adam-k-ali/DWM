package com.adamkali.opendwm.sound;

import com.adamkali.opendwm.DWMMain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = DWMMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DWMSounds {
    public static final RegistryObject<SoundEvent> SONIC_THIRD_DOCTOR = DWMMain.SOUNDS.register("sonic_screwdriver",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DWMMain.MODID, "sonic_screwdriver")));
}
