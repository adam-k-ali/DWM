package com.adamkali.dwm.neoforge;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.DwmCommon;
import com.adamkali.dwm.DwmCommonClient;
import com.adamkali.dwm.platform.DwmClientServices;
import com.adamkali.dwm.platform.DwmServices;
import com.adamkali.dwm.platform.neoforge.NeoForgeDwmClientPlatform;
import com.adamkali.dwm.platform.neoforge.NeoForgeDwmPlatform;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(DWMReference.MOD_ID)
public final class NeoForgeDwmMod {
    public NeoForgeDwmMod(IEventBus modBus) {
        DwmServices.set(new NeoForgeDwmPlatform(modBus));
        // Fabric-style Registry.register in DwmCommon needs open vanilla registries.
        NeoForgeRegistryBootstrap.unlockForFabricStyleRegistration();
        DwmCommon.init();
        NeoForgeRegistryBootstrap.syncBlockItemMapAfterRegistration();
        NeoForgeRegistryBootstrap.initBlockStateCachesAfterRegistration();

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            DwmClientServices.set(new NeoForgeDwmClientPlatform(modBus));
            DwmCommonClient.init();
        }
    }
}
