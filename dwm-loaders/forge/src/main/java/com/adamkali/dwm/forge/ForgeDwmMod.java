package com.adamkali.dwm.forge;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.DwmCommon;
import com.adamkali.dwm.DwmCommonClient;
import com.adamkali.dwm.platform.DwmClientServices;
import com.adamkali.dwm.platform.DwmServices;
import com.adamkali.dwm.platform.forge.ForgeDwmClientPlatform;
import com.adamkali.dwm.platform.forge.ForgeDwmPlatform;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(DWMReference.MOD_ID)
public final class ForgeDwmMod {
    public ForgeDwmMod() {
        ForgeDwmPlatform platform = new ForgeDwmPlatform();
        DwmServices.set(platform);
        DwmCommon.init();
        platform.buildNetwork();

        if (FMLLoader.getDist() == Dist.CLIENT) {
            DwmClientServices.set(new ForgeDwmClientPlatform(platform));
            DwmCommonClient.init();
        }
    }
}
