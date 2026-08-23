package com.adamkali.dwm;

import com.adamkali.dwm.block.DWMWoodTypes;
import com.adamkali.dwm.platform.DwmServices;
import com.adamkali.dwm.platform.fabric.FabricDwmPlatform;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

/**
 * Ensures Minecraft registries are bootstrapped for unit tests that touch blocks/items.
 * Also installs the Fabric {@link DwmServices} platform so shared registration helpers work.
 */
public final class MinecraftTestBootstrap {
    private static boolean bootstrapped;

    private MinecraftTestBootstrap() {
    }

    public static synchronized void ensure() {
        if (bootstrapped) {
            return;
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        DwmServices.set(new FabricDwmPlatform());
        DWMWoodTypes.initialize();
        bootstrapped = true;
    }
}
