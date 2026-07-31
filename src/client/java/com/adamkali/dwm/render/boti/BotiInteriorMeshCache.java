package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.network.RequestBotiInteriorC2SPayload;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-TARDIS BOTI placement cache. Prefers synced live snapshots; falls back to the First Doctor
 * blueprint until a snapshot arrives. Block-entity visuals are reserved for a later format version.
 */
public final class BotiInteriorMeshCache {
    private static final long REQUEST_COOLDOWN_MS = 2000L;

    private static final Map<UUID, CachedSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_REQUEST_MS = new ConcurrentHashMap<>();
    private static Map<BlockPos, BlockState> blueprintFallback = Map.of();

    private BotiInteriorMeshCache() {
    }

    public static boolean hasSnapshot(UUID tardisId) {
        return tardisId != null && SNAPSHOTS.containsKey(tardisId);
    }

    public static Map<BlockPos, BlockState> getVisibleBlocks(UUID tardisId) {
        if (tardisId != null) {
            CachedSnapshot cached = SNAPSHOTS.get(tardisId);
            if (cached != null) {
                return cached.blocks();
            }
            requestIfNeeded(tardisId);
        }
        return blueprintVisibleBlocks();
    }

    public static void applySnapshot(UUID tardisId, int revision, Map<BlockPos, BlockState> blocks) {
        if (tardisId == null || blocks == null || blocks.isEmpty()) {
            return;
        }
        CachedSnapshot existing = SNAPSHOTS.get(tardisId);
        if (existing != null && revision < existing.revision()) {
            return;
        }
        SNAPSHOTS.put(tardisId, new CachedSnapshot(revision, Map.copyOf(blocks)));
        LAST_REQUEST_MS.remove(tardisId);
    }

    public static void invalidate(UUID tardisId) {
        if (tardisId != null) {
            SNAPSHOTS.remove(tardisId);
            LAST_REQUEST_MS.remove(tardisId);
        }
    }

    public static void invalidateAll() {
        SNAPSHOTS.clear();
        LAST_REQUEST_MS.clear();
        blueprintFallback = Map.of();
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, UUID tardisId) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        for (Map.Entry<BlockPos, BlockState> entry : getVisibleBlocks(tardisId).entrySet()) {
            BlockPos pos = entry.getKey();
            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderManager.renderBlockAsEntity(entry.getValue(), matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }
    }

    private static void requestIfNeeded(UUID tardisId) {
        long now = System.currentTimeMillis();
        Long last = LAST_REQUEST_MS.get(tardisId);
        if (last != null && now - last < REQUEST_COOLDOWN_MS) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getNetworkHandler() == null) {
            return;
        }
        LAST_REQUEST_MS.put(tardisId, now);
        ClientPlayNetworking.send(new RequestBotiInteriorC2SPayload(tardisId));
    }

    private static Map<BlockPos, BlockState> blueprintVisibleBlocks() {
        if (blueprintFallback.isEmpty()) {
            blueprintFallback = Map.copyOf(FirstDoctorConsoleRoomLayout.botiVisiblePlacements());
        }
        return blueprintFallback;
    }

    private record CachedSnapshot(int revision, Map<BlockPos, BlockState> blocks) {
    }
}
