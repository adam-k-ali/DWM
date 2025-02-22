package com.adamkali.dwm.analytics;

import com.adamkali.dwm.DWMReference;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DWMStatistics {
    public static final Identifier SONIC_SCREWDRIVER_USE = register("sonic_screwdriver_use");

    public static void initialize() {
    }


    private static Identifier register(String key) {
        Identifier identifier = Identifier.of(DWMReference.MOD_ID, key);
        Registry.register(Registries.CUSTOM_STAT, identifier, identifier);

        return identifier;
    }

}
