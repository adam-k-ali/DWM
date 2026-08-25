package com.adamkali.dwm.tardis.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Helpers for selecting an online player as a TARDIS travel destination.
 */
public final class PlayerLocatorLogic {
    private PlayerLocatorLogic() {
    }

    public record PlayerEntry(UUID uuid, String name, String dimension, int x, int y, int z) {
    }

    /**
     * Online players excluding {@code excludePlayerUuid} (typically the operator), sorted by name.
     */
    public static List<PlayerEntry> listOnlineExcluding(
            @Nullable MinecraftServer server,
            @Nullable UUID excludePlayerUuid
    ) {
        return onlinePlayers(server, excludePlayerUuid);
    }

    /** Alias used by console/network code. */
    public static List<PlayerEntry> onlinePlayers(
            @Nullable MinecraftServer server,
            @Nullable UUID excludePlayerUuid
    ) {
        if (server == null) {
            return List.of();
        }
        List<PlayerEntry> entries = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player == null) {
                continue;
            }
            UUID id = player.getUUID();
            if (excludePlayerUuid != null && excludePlayerUuid.equals(id)) {
                continue;
            }
            String name = player.getName().getString();
            if (name == null || name.isBlank()) {
                name = id.toString();
            }
            String dimension = player.level().dimension().identifier().toString();
            BlockPos pos = player.blockPosition();
            entries.add(new PlayerEntry(id, name, dimension, pos.getX(), pos.getY(), pos.getZ()));
        }
        entries.sort(Comparator.comparing(e -> e.name(), String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(entries);
    }

    /**
     * Pure filter used by unit tests without a live player list.
     */
    public static List<PlayerEntry> filterExcluding(
            @Nullable List<PlayerEntry> players,
            @Nullable UUID excludePlayerUuid
    ) {
        if (players == null || players.isEmpty()) {
            return List.of();
        }
        List<PlayerEntry> filtered = new ArrayList<>();
        for (PlayerEntry entry : players) {
            if (entry == null || entry.uuid() == null) {
                continue;
            }
            if (excludePlayerUuid != null && excludePlayerUuid.equals(entry.uuid())) {
                continue;
            }
            filtered.add(entry);
        }
        filtered.sort(Comparator.comparing(e -> e.name() == null ? "" : e.name(), String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(filtered);
    }

    public static boolean isOnline(@Nullable MinecraftServer server, @Nullable UUID playerUuid) {
        return findOnline(server, playerUuid).isPresent();
    }

    /** Alias used by network validation. */
    public static Optional<ServerPlayer> findOnline(
            @Nullable MinecraftServer server,
            @Nullable UUID playerUuid
    ) {
        return resolve(server, playerUuid);
    }

    public static Optional<ServerPlayer> resolve(
            @Nullable MinecraftServer server,
            @Nullable UUID playerUuid
    ) {
        if (server == null || playerUuid == null) {
            return Optional.empty();
        }
        ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
        return Optional.ofNullable(player);
    }
}
