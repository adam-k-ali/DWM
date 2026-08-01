package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks exterior footprint locations so world edits / entity occupancy can dirty SOTO snapshots.
 */
public final class SotoExteriorIndex {
    private record ExteriorKey(RegistryKey<World> worldKey, BlockPos exteriorPos) {
    }

    private static final Map<UUID, ExteriorKey> BY_TARDIS = new ConcurrentHashMap<>();
    /** Packed exterior block key → tardisId (same dimension may host multiple TARDIS). */
    private static final Map<String, UUID> BY_EXTERIOR_BLOCK = new ConcurrentHashMap<>();

    private SotoExteriorIndex() {
    }

    public static void clear() {
        BY_TARDIS.clear();
        BY_EXTERIOR_BLOCK.clear();
    }

    /**
     * Removes tracking for a TARDIS exterior (e.g. while dematerialised / in flight).
     */
    public static void unregister(@Nullable UUID tardisId) {
        if (tardisId == null) {
            return;
        }
        ExteriorKey previous = BY_TARDIS.remove(tardisId);
        if (previous != null) {
            BY_EXTERIOR_BLOCK.remove(blockKey(previous.worldKey(), previous.exteriorPos()));
        }
    }

    public static void register(UUID tardisId, RegistryKey<World> worldKey, BlockPos exteriorPos) {
        if (tardisId == null || worldKey == null || exteriorPos == null) {
            return;
        }
        ExteriorKey previous = BY_TARDIS.put(tardisId, new ExteriorKey(worldKey, exteriorPos.toImmutable()));
        if (previous != null) {
            BY_EXTERIOR_BLOCK.remove(blockKey(previous.worldKey(), previous.exteriorPos()));
        }
        BY_EXTERIOR_BLOCK.put(blockKey(worldKey, exteriorPos), tardisId);
    }

    public static void register(UUID tardisId, TardisDataModel model) {
        if (tardisId == null || model == null || !model.hasExteriorLocation || model.exteriorDimension == null) {
            return;
        }
        RegistryKey<World> worldKey = RegistryKey.of(
                net.minecraft.registry.RegistryKeys.WORLD,
                net.minecraft.util.Identifier.of(model.exteriorDimension)
        );
        register(tardisId, worldKey, new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ));
    }

    public static boolean isRegistered(UUID tardisId) {
        return tardisId != null && BY_TARDIS.containsKey(tardisId);
    }

    public static Set<UUID> registeredIds() {
        return Collections.unmodifiableSet(BY_TARDIS.keySet());
    }

    public static @Nullable BlockPos getExteriorPos(UUID tardisId) {
        ExteriorKey key = BY_TARDIS.get(tardisId);
        return key == null ? null : key.exteriorPos();
    }

    public static @Nullable RegistryKey<World> getWorldKey(UUID tardisId) {
        ExteriorKey key = BY_TARDIS.get(tardisId);
        return key == null ? null : key.worldKey();
    }

    /**
     * Resolves the TARDIS whose exterior footprint contains {@code worldPos} in {@code worldKey}.
     */
    public static @Nullable UUID resolve(RegistryKey<World> worldKey, BlockPos worldPos) {
        if (worldKey == null || worldPos == null) {
            return null;
        }
        for (Map.Entry<UUID, ExteriorKey> entry : BY_TARDIS.entrySet()) {
            ExteriorKey key = entry.getValue();
            if (!key.worldKey().equals(worldKey)) {
                continue;
            }
            BlockPos footprintOrigin = SotoExteriorSampler.footprintOrigin(key.exteriorPos());
            if (SotoExteriorSampler.isInsideFootprint(worldPos, footprintOrigin)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static String blockKey(RegistryKey<World> worldKey, BlockPos pos) {
        return worldKey.getValue() + "|" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
