package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import com.adamkali.dwm.tardis.portal.PortalStreamSyncService;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TardisLogic {
    public static InteractionResult toggleDoor(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return InteractionResult.FAIL;
        }

        float doorSwing = tardis.doorState.doorSwing;
        if (doorSwing > 0.0f && doorSwing < 1.0f) {
            return InteractionResult.PASS;
        }

        if (DoorLockLogic.blocksOpen(tardis, tardis.doorState.isOpen)) {
            return InteractionResult.FAIL;
        }

        tardis.doorState.isOpen = !tardis.doorState.isOpen;
        tardis.setChanged();
        PortalStreamSyncService.setMetaChanged(tardisId);
        return InteractionResult.SUCCESS;
    }

    public static TardisDoorState getDoorState(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return null;
        }
        return tardis.doorState;
    }

    public static void updateDoorState(UUID tardisId) {
        updateDoorState(tardisId, null);
    }

    /**
     * Advances door swing toward the current open/closed target.
     * When {@code world} has a server, at most one step runs per server tick so exterior and
     * interior tickers cannot double-speed the shared animation.
     */
    public static void updateDoorState(UUID tardisId, @Nullable Level world) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return;
        }
        MinecraftServer server = world == null ? null : world.getServer();
        if (server != null) {
            int tick = server.getTickCount();
            if (tardis.lastDoorSwingServerTick == tick) {
                return;
            }
            tardis.lastDoorSwingServerTick = tick;
        }
        float doorSwing = tardis.doorState.doorSwing;
        if (tardis.doorState.isOpen) {
            doorSwing = Math.min(doorSwing + 0.05f, 1f);
        } else {
            doorSwing = Math.max(doorSwing - 0.05f, 0f);
        }
        tardis.doorState.doorSwing = doorSwing;
        tardis.setChanged();
    }

    public static void setVariant(UUID tardisId, TardisChameleonVariant variant) {
        setVariant(tardisId, variant, null);
    }

    /**
     * Sets the chameleon variant and optionally syncs it onto the interior console BE for holograms.
     */
    public static void setVariant(
            UUID tardisId,
            TardisChameleonVariant variant,
            @Nullable MinecraftServer server
    ) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return;
        }

        tardis.variant = variant;
        tardis.setChanged();
        PortalStreamSyncService.setMetaChanged(tardisId);
        FirstDoctorConsoleSync.syncFromModel(server, tardisId);
    }

    /**
     * Advances to the next {@link TardisChameleonVariant} (wrapping), syncing the console hologram.
     *
     * @return the new variant, or empty if the TARDIS is missing
     */
    public static Optional<TardisChameleonVariant> cycleVariant(
            UUID tardisId,
            @Nullable MinecraftServer server
    ) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return Optional.empty();
        }
        TardisChameleonVariant[] values = TardisChameleonVariant.values();
        TardisChameleonVariant current = tardis.variant == null
                ? TardisChameleonVariant.TT_CAPSULE
                : tardis.variant;
        int nextIndex = (current.ordinal() + 1) % values.length;
        TardisChameleonVariant next = values[nextIndex];
        setVariant(tardisId, next, server);
        return Optional.of(next);
    }

    public static TardisChameleonVariant getVariant(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return null;
        }
        return tardis.variant;
    }

    /**
     * Cycles {@link TardisDataModel#selectedBiome} through biomes tagged for the effective
     * destination dimension. Resets destination mode to {@link DestinationMode#BIOME}.
     * Returns the newly selected biome id, or empty if none are available.
     */
    public static Optional<Identifier> cycleSelectedBiome(UUID tardisId, MinecraftServer server) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null || server == null) {
            return Optional.empty();
        }
        Registry<Biome> biomes = server.registryAccess().lookupOrThrow(Registries.BIOME);
        List<ResourceKey<Biome>> list =
                BiomeSelectorLogic.biomesForDimension(biomes, effectiveDestinationDimension(tardis));
        Optional<Identifier> next = BiomeSelectorLogic.nextBiome(tardis.selectedBiome, list);
        if (next.isEmpty()) {
            return Optional.empty();
        }
        tardis.selectedBiome = next.get().toString();
        tardis.clearNonBiomeDestinationSelection();
        return next;
    }

    /**
     * Cycles {@link TardisDataModel#selectedDimension} through loaded worlds (excluding the
     * TARDIS interior), then resets {@link TardisDataModel#selectedBiome} to the first biome
     * tagged for that dimension (or {@code null} if none). Resets destination mode to
     * {@link DestinationMode#BIOME}.
     */
    public static Optional<Identifier> cycleSelectedDimension(UUID tardisId, MinecraftServer server) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null || server == null) {
            return Optional.empty();
        }
        List<ResourceKey<Level>> list = PlanetLocatorLogic.dimensions(server);
        String current = effectiveDestinationDimension(tardis);
        Optional<Identifier> next = PlanetLocatorLogic.nextDimension(current, list);
        if (next.isEmpty()) {
            return Optional.empty();
        }
        tardis.selectedDimension = next.get().toString();
        Registry<Biome> biomes = server.registryAccess().lookupOrThrow(Registries.BIOME);
        List<ResourceKey<Biome>> biomeList =
                BiomeSelectorLogic.biomesForDimension(biomes, tardis.selectedDimension);
        if (biomeList.isEmpty()) {
            tardis.selectedBiome = null;
        } else {
            tardis.selectedBiome = biomeList.getFirst().identifier().toString();
        }
        tardis.clearNonBiomeDestinationSelection();
        return next;
    }

    public static void setDestinationMode(UUID tardisId, DestinationMode mode) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return;
        }
        tardis.setDestinationMode(mode);
    }

    public static boolean selectWaypoint(UUID tardisId, @Nullable UUID waypointId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return false;
        }
        if (waypointId == null) {
            return WaypointLogic.clearSelection(tardis);
        }
        return WaypointLogic.select(tardis, waypointId);
    }

    /**
     * Selects an online player as destination. {@code playerUuid} null clears the selection.
     * Does not re-validate online status here — callers with a server should use
     * {@link #selectPlayer(UUID, UUID, MinecraftServer)}.
     */
    public static boolean selectPlayer(UUID tardisId, @Nullable UUID playerUuid) {
        return selectPlayer(tardisId, playerUuid, null);
    }

    /**
     * Selects a player destination. {@code playerUuid} null clears via {@link WaypointLogic#clearSelection}.
     * When {@code server} is non-null and selecting, requires the player to be online.
     */
    public static boolean selectPlayer(
            UUID tardisId,
            @Nullable UUID playerUuid,
            @Nullable MinecraftServer server
    ) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return false;
        }
        if (playerUuid == null) {
            return WaypointLogic.clearSelection(tardis);
        }
        if (server != null && !PlayerLocatorLogic.isOnline(server, playerUuid)) {
            return false;
        }
        tardis.selectedPlayerUuid = playerUuid;
        tardis.selectedWaypointId = null;
        tardis.setDestinationMode(DestinationMode.PLAYER);
        return true;
    }

    public static Optional<TardisWaypoint> saveWaypoint(UUID tardisId, @Nullable String name) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return Optional.empty();
        }
        return WaypointLogic.add(tardis, name);
    }

    public static boolean deleteWaypoint(UUID tardisId, UUID waypointId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return false;
        }
        return WaypointLogic.delete(tardis, waypointId);
    }

    public static boolean renameWaypoint(UUID tardisId, UUID waypointId, @Nullable String name) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return false;
        }
        return WaypointLogic.rename(tardis, waypointId, name);
    }

    /**
     * Destination dimension used for biome listing and travel: {@code selectedDimension} when set,
     * otherwise {@code exteriorDimension}.
     */
    public static @Nullable String effectiveDestinationDimension(@Nullable TardisDataModel tardis) {
        if (tardis == null) {
            return null;
        }
        if (tardis.selectedDimension != null && !tardis.selectedDimension.isBlank()) {
            return tardis.selectedDimension;
        }
        return tardis.exteriorDimension;
    }

    public static @Nullable String getSelectedBiome(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return null;
        }
        return tardis.selectedBiome;
    }

    public static @Nullable String getSelectedDimension(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return null;
        }
        return tardis.selectedDimension;
    }

    public static List<TardisWaypoint> getWaypoints(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return List.of();
        }
        return List.copyOf(tardis.getWaypoints());
    }

    public static DestinationMode getDestinationMode(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return DestinationMode.BIOME;
        }
        return tardis.getDestinationMode();
    }

    public static boolean isCloaked(@Nullable UUID tardisId) {
        return CloakLogic.isCloaked(TardisDataLoader.get(tardisId));
    }

    public static boolean areDoorsLocked(@Nullable UUID tardisId) {
        return DoorLockLogic.isLocked(TardisDataLoader.get(tardisId));
    }

    public static TardisTravelPhase getTravelPhase(@Nullable UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return TardisTravelPhase.IDLE;
        }
        return tardis.getTravelPhase();
    }
}
