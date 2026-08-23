package com.adamkali.dwm;

import com.adamkali.dwm.platform.DwmClientServices;
import com.adamkali.dwm.platform.fabric.FabricDwmClientPlatform;
import net.fabricmc.api.ClientModInitializer;

public class DWMClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DwmClientServices.set(new FabricDwmClientPlatform());
        DwmCommonClient.init();
    }
}
