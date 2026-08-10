package com.adamkali.dwm.analytics;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class DWMStatistics {
    public static final Identifier SONIC_SCREWDRIVER_USE = register("sonic_screwdriver_use");

    public static void initialize() {
    }


    private static Identifier register(String key) {
        Identifier identifier = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, key);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, identifier, identifier);

        return identifier;
    }

}
