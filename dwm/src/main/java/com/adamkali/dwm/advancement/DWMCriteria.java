package com.adamkali.dwm.advancement;

import com.adamkali.dwm.DWMReference;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Custom first-hour advancement triggers. Vanilla item/block-use triggers only fire through
 * {@code ServerPlayerGameMode}, so GameTests and several production call sites need these.
 */
public final class DWMCriteria {
    public static final PlayerTrigger SONIC_IRON_DOOR = register("sonic_iron_door");
    public static final PlayerTrigger FIND_TARDIS = register("find_tardis");
    public static final PlayerTrigger CLAIM_TARDIS = register("claim_tardis");
    public static final PlayerTrigger FIRST_HOP = register("first_hop");
    public static final PlayerTrigger BIND_KEY = register("bind_key");

    private DWMCriteria() {
    }

    public static void initialize() {
        // Static fields register on class load; this method forces that from mod init.
    }

    private static PlayerTrigger register(String path) {
        return Registry.register(
                BuiltInRegistries.TRIGGER_TYPES,
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path),
                new PlayerTrigger()
        );
    }
}
