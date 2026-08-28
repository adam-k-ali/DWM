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
    public static final PlayerTrigger SONIC_INSTALL_SHATTER = register("sonic_install_shatter");
    public static final PlayerTrigger SONIC_INSTALL_PRIME = register("sonic_install_prime");
    public static final PlayerTrigger SONIC_INSTALL_DISRUPT = register("sonic_install_disrupt");
    public static final PlayerTrigger SONIC_INSTALL_SHEAR = register("sonic_install_shear");
    public static final PlayerTrigger SONIC_CYCLE_SETTING = register("sonic_cycle_setting");
    public static final PlayerTrigger SONIC_SHATTER = register("sonic_shatter");
    public static final PlayerTrigger SONIC_PRIME = register("sonic_prime");
    public static final PlayerTrigger SONIC_DISRUPT = register("sonic_disrupt");
    public static final PlayerTrigger SONIC_SHEAR = register("sonic_shear");
    public static final PlayerTrigger SONIC_PING = register("sonic_ping");
    public static final PlayerTrigger FIND_TARDIS = register("find_tardis");
    public static final PlayerTrigger CLAIM_TARDIS = register("claim_tardis");
    public static final PlayerTrigger FIRST_HOP = register("first_hop");
    public static final PlayerTrigger BIND_KEY = register("bind_key");
    public static final PlayerTrigger TARDIS_REFUEL = register("tardis_refuel");
    public static final PlayerTrigger FIRST_CIRCUIT = register("first_circuit");
    public static final PlayerTrigger FIRST_OTHER_WORLD = register("first_other_world");
    public static final PlayerTrigger FIRST_GALLIFREY = register("first_gallifrey");

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
