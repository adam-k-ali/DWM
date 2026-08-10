package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.network.SyncBotiInteriorS2CPayload;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Near-live BOTI snapshot sync: dirty on interior edits and while entities occupy a plot.
 * Entity-occupied plots flush every tick; block-only dirty is coalesced every few ticks.
 */
public final class BotiInteriorSyncService {
    private static final int FLUSH_INTERVAL_TICKS = 3;

    private static final Set<UUID> DIRTY = ConcurrentHashMap.newKeySet();
    /** Plots that currently have entities (or just cleared — removed after one empty flush). */
    private static final Set<UUID> ENTITY_ACTIVE = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, BotiInteriorSnapshot> LAST_SNAPSHOT = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> REVISIONS = new ConcurrentHashMap<>();
    private static int tickCounter;

    private BotiInteriorSyncService() {
    }

    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClientSide() && world.dimension().equals(TardisDimensions.TARDIS_WORLD_KEY)) {
                markDirtyAt(pos);
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && world.dimension().equals(TardisDimensions.TARDIS_WORLD_KEY)) {
                markDirtyAt(hitResult.getBlockPos());
                markDirtyAt(hitResult.getBlockPos().relative(hitResult.getDirection()));
            }
            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(BotiInteriorSyncService::onEndTick);

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            DIRTY.clear();
            ENTITY_ACTIVE.clear();
            LAST_SNAPSHOT.clear();
            REVISIONS.clear();
            BotiPlotIndex.clear();
            tickCounter = 0;
        });
    }

    public static void setChanged(UUID tardisId) {
        if (tardisId != null) {
            DIRTY.add(tardisId);
        }
    }

    public static void markDirtyAt(BlockPos worldPos) {
        UUID tardisId = BotiPlotIndex.resolve(worldPos);
        if (tardisId != null) {
            DIRTY.add(tardisId);
        }
    }

    public static BotiInteriorSnapshot getLastSnapshot(UUID tardisId) {
        return LAST_SNAPSHOT.get(tardisId);
    }

    /**
     * Builds (or returns cached) snapshot and sends it to {@code player}.
     * Skips send when the footprint has no visible blocks (interior not generated yet).
     */
    public static boolean sendToPlayer(ServerPlayer player, UUID tardisId) {
        MinecraftServer server = player.getServer();
        if (server == null || tardisId == null) {
            return false;
        }
        BotiInteriorSnapshot snapshot = buildSnapshot(server, tardisId);
        if (snapshot == null || snapshot.blocks().isEmpty()) {
            return false;
        }
        ServerPlayNetworking.send(player, SyncBotiInteriorS2CPayload.fromSnapshot(snapshot));
        return true;
    }

    private static void onEndTick(MinecraftServer server) {
        tickCounter++;
        markEntityOccupiedPlotsDirty(server);
        if (DIRTY.isEmpty()) {
            return;
        }
        // Entity-occupied plots flush every tick (~20 Hz poses); block-only dirty stays coalesced.
        int interval = intersectsEntityActive(DIRTY) ? 1 : FLUSH_INTERVAL_TICKS;
        if (tickCounter % interval != 0) {
            return;
        }
        Set<UUID> toFlush = Set.copyOf(DIRTY);
        DIRTY.removeAll(toFlush);
        for (UUID tardisId : toFlush) {
            BotiInteriorSnapshot snapshot = buildSnapshot(server, tardisId);
            if (snapshot == null || snapshot.blocks().isEmpty()) {
                continue;
            }
            pushToExteriorTrackers(server, snapshot);
        }
    }

    private static boolean intersectsEntityActive(Set<UUID> dirty) {
        for (UUID tardisId : dirty) {
            if (ENTITY_ACTIVE.contains(tardisId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Keeps plots dirty while entities are present, and one more flush after they leave
     * so clients clear the last rendered entities.
     */
    private static void markEntityOccupiedPlotsDirty(MinecraftServer server) {
        ServerLevel interiorWorld = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interiorWorld == null) {
            return;
        }
        for (UUID tardisId : BotiPlotIndex.registeredIds()) {
            boolean hasEntities = BotiInteriorSampler.hasEntities(interiorWorld, tardisId);
            if (hasEntities || ENTITY_ACTIVE.contains(tardisId)) {
                setChanged(tardisId);
                if (hasEntities) {
                    ENTITY_ACTIVE.add(tardisId);
                    // Keep wander AI alive for exterior BOTI while the interior has no players.
                    BotiInteriorSampler.keepMobAiActive(interiorWorld, tardisId);
                } else {
                    ENTITY_ACTIVE.remove(tardisId);
                }
            }
        }
    }

    private static BotiInteriorSnapshot buildSnapshot(MinecraftServer server, UUID tardisId) {
        ServerLevel interiorWorld = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interiorWorld == null) {
            return null;
        }
        BotiPlotIndex.register(tardisId);
        int revision = REVISIONS.merge(tardisId, 1, Integer::sum);
        Map<BlockPos, net.minecraft.world.level.block.state.BlockState> blocks = BotiInteriorSampler.sample(interiorWorld, tardisId);
        Map<BlockPos, net.minecraft.nbt.CompoundTag> blockEntities =
                BotiInteriorSampler.sampleBlockEntities(interiorWorld, tardisId);
        List<BotiEntitySample> entities = BotiInteriorSampler.sampleEntities(interiorWorld, tardisId);
        BotiInteriorSnapshot snapshot = BotiInteriorSnapshot.of(tardisId, revision, blocks, blockEntities, entities);
        LAST_SNAPSHOT.put(tardisId, snapshot);
        return snapshot;
    }

    private static void pushToExteriorTrackers(MinecraftServer server, BotiInteriorSnapshot snapshot) {
        TardisDataModel model = TardisDataLoader.get(snapshot.tardisId());
        if (model == null || !model.hasExteriorLocation || model.exteriorDimension == null) {
            return;
        }
        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, Identifier.parse(model.exteriorDimension));
        ServerLevel exteriorWorld = server.getLevel(worldKey);
        if (exteriorWorld == null) {
            return;
        }
        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        SyncBotiInteriorS2CPayload payload = SyncBotiInteriorS2CPayload.fromSnapshot(snapshot);
        for (ServerPlayer player : PlayerLookup.tracking(exteriorWorld, exteriorPos)) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
