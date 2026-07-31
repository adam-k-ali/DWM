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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Near-live BOTI snapshot sync: dirty on interior edits, coalesced flush every few ticks.
 */
public final class BotiInteriorSyncService {
    private static final int FLUSH_INTERVAL_TICKS = 3;

    private static final Set<UUID> DIRTY = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, BotiInteriorSnapshot> LAST_SNAPSHOT = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> REVISIONS = new ConcurrentHashMap<>();
    private static int tickCounter;

    private BotiInteriorSyncService() {
    }

    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient() && world.getRegistryKey().equals(TardisDimensions.TARDIS_WORLD_KEY)) {
                markDirtyAt(pos);
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient() && world.getRegistryKey().equals(TardisDimensions.TARDIS_WORLD_KEY)) {
                markDirtyAt(hitResult.getBlockPos());
                markDirtyAt(hitResult.getBlockPos().offset(hitResult.getSide()));
            }
            return ActionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(BotiInteriorSyncService::onEndTick);

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            DIRTY.clear();
            LAST_SNAPSHOT.clear();
            REVISIONS.clear();
            BotiPlotIndex.clear();
            tickCounter = 0;
        });
    }

    public static void markDirty(UUID tardisId) {
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
    public static boolean sendToPlayer(ServerPlayerEntity player, UUID tardisId) {
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
        if (tickCounter % FLUSH_INTERVAL_TICKS != 0 || DIRTY.isEmpty()) {
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

    private static BotiInteriorSnapshot buildSnapshot(MinecraftServer server, UUID tardisId) {
        ServerWorld interiorWorld = server.getWorld(TardisDimensions.TARDIS_WORLD_KEY);
        if (interiorWorld == null) {
            return null;
        }
        BotiPlotIndex.register(tardisId);
        int revision = REVISIONS.merge(tardisId, 1, Integer::sum);
        Map<BlockPos, net.minecraft.block.BlockState> blocks = BotiInteriorSampler.sample(interiorWorld, tardisId);
        Map<BlockPos, net.minecraft.nbt.NbtCompound> blockEntities =
                BotiInteriorSampler.sampleBlockEntities(interiorWorld, tardisId);
        BotiInteriorSnapshot snapshot = BotiInteriorSnapshot.of(tardisId, revision, blocks, blockEntities);
        LAST_SNAPSHOT.put(tardisId, snapshot);
        return snapshot;
    }

    private static void pushToExteriorTrackers(MinecraftServer server, BotiInteriorSnapshot snapshot) {
        TardisDataModel model = TardisDataLoader.get(snapshot.tardisId());
        if (model == null || !model.hasExteriorLocation || model.exteriorDimension == null) {
            return;
        }
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(model.exteriorDimension));
        ServerWorld exteriorWorld = server.getWorld(worldKey);
        if (exteriorWorld == null) {
            return;
        }
        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        SyncBotiInteriorS2CPayload payload = SyncBotiInteriorS2CPayload.fromSnapshot(snapshot);
        for (ServerPlayerEntity player : PlayerLookup.tracking(exteriorWorld, exteriorPos)) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
