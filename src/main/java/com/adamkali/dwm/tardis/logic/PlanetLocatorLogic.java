package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.interior.TardisDimensions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Pure helpers for cycling destination dimensions (excluding the TARDIS interior).
 */
public final class PlanetLocatorLogic {
    private PlanetLocatorLogic() {
    }

    /**
     * Sorted loaded world keys excluding {@link TardisDimensions#TARDIS_WORLD_KEY}.
     */
    public static List<RegistryKey<World>> dimensions(@Nullable MinecraftServer server) {
        if (server == null) {
            return List.of();
        }
        List<RegistryKey<World>> keys = new ArrayList<>();
        for (ServerWorld world : server.getWorlds()) {
            RegistryKey<World> key = world.getRegistryKey();
            if (TardisDimensions.isTardisWorld(key)) {
                continue;
            }
            keys.add(key);
        }
        keys.sort(Comparator.comparing(k -> k.getValue().toString()));
        return List.copyOf(keys);
    }

    /**
     * Filters and sorts an arbitrary world-key list, excluding the TARDIS interior.
     * Useful for unit tests without a live server.
     */
    public static List<RegistryKey<World>> filterTravelDimensions(
            @Nullable List<RegistryKey<World>> keys
    ) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        List<RegistryKey<World>> filtered = new ArrayList<>();
        for (RegistryKey<World> key : keys) {
            if (key == null || TardisDimensions.isTardisWorld(key)) {
                continue;
            }
            filtered.add(key);
        }
        filtered.sort(Comparator.comparing(k -> k.getValue().toString()));
        return List.copyOf(filtered);
    }

    /**
     * Next dimension after {@code currentId} in {@code dimensions}, wrapping to the first.
     * Null / missing current selects the first entry. Empty list → empty.
     */
    public static Optional<Identifier> nextDimension(
            @Nullable String currentId,
            List<RegistryKey<World>> dimensions
    ) {
        if (dimensions == null || dimensions.isEmpty()) {
            return Optional.empty();
        }
        if (currentId == null || currentId.isBlank()) {
            return Optional.of(dimensions.getFirst().getValue());
        }
        Identifier current = Identifier.tryParse(currentId);
        int index = -1;
        if (current != null) {
            for (int i = 0; i < dimensions.size(); i++) {
                if (dimensions.get(i).getValue().equals(current)) {
                    index = i;
                    break;
                }
            }
        }
        int next = index < 0 ? 0 : (index + 1) % dimensions.size();
        return Optional.of(dimensions.get(next).getValue());
    }
}
