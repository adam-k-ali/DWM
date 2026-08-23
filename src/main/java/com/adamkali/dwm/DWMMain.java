package com.adamkali.dwm;

import com.adamkali.dwm.platform.DwmServices;
import com.adamkali.dwm.platform.fabric.FabricDwmPlatform;
import net.fabricmc.api.ModInitializer;

public class DWMMain implements ModInitializer {
    @Override
    public void onInitialize() {
        DwmServices.set(new FabricDwmPlatform());
        DwmCommon.init();
    }
}
