package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.network.RequestBotiInteriorC2SPayload;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-TARDIS BOTI placement cache. Prefers synced live snapshots (blocks + BE NBT);
 * falls back to the First Doctor blueprint until a snapshot arrives.
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

    /** Number of synced BE NBT entries for tests / debug. */
    public static int getBlockEntityNbtCount(UUID tardisId) {
        if (tardisId == null) {
            return 0;
        }
        CachedSnapshot cached = SNAPSHOTS.get(tardisId);
        return cached == null ? 0 : cached.blockEntityNbt().size();
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

    public static List<BlockEntity> getBlockEntities(UUID tardisId) {
        if (tardisId == null) {
            return List.of();
        }
        CachedSnapshot cached = SNAPSHOTS.get(tardisId);
        if (cached == null) {
            return List.of();
        }
        if (!cached.renderedBlockEntities().isEmpty()) {
            return cached.renderedBlockEntities();
        }
        if (cached.blockEntityNbt().isEmpty()) {
            return List.of();
        }
        List<BlockEntity> rebuilt = buildBlockEntities(cached.blocks(), cached.blockEntityNbt());
        if (!rebuilt.isEmpty()) {
            SNAPSHOTS.put(tardisId, new CachedSnapshot(
                    cached.revision(),
                    cached.blocks(),
                    cached.blockEntityNbt(),
                    rebuilt
            ));
        }
        return rebuilt;
    }

    public static void applySnapshot(UUID tardisId, int revision, Map<BlockPos, BlockState> blocks) {
        applySnapshot(tardisId, revision, blocks, Map.of());
    }

    public static void applySnapshot(
            UUID tardisId,
            int revision,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntities
    ) {
        if (tardisId == null || blocks == null || blocks.isEmpty()) {
            return;
        }
        CachedSnapshot existing = SNAPSHOTS.get(tardisId);
        if (existing != null && revision < existing.revision()) {
            return;
        }
        Map<BlockPos, BlockState> blockCopy = Map.copyOf(blocks);
        Map<BlockPos, NbtCompound> beCopy = copyNbtMap(blockEntities);
        List<BlockEntity> rendered = buildBlockEntities(blockCopy, beCopy);
        SNAPSHOTS.put(tardisId, new CachedSnapshot(revision, blockCopy, beCopy, rendered));
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

    public static void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            float tickDelta,
            UUID tardisId
    ) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        for (Map.Entry<BlockPos, BlockState> entry : getVisibleBlocks(tardisId).entrySet()) {
            BlockPos pos = entry.getKey();
            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderManager.renderBlockAsEntity(entry.getValue(), matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }

        BlockEntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();
        for (BlockEntity blockEntity : getBlockEntities(tardisId)) {
            @SuppressWarnings("unchecked")
            BlockEntityRenderer<BlockEntity> renderer =
                    (BlockEntityRenderer<BlockEntity>) dispatcher.get(blockEntity);
            if (renderer == null) {
                continue;
            }
            BlockPos pos = blockEntity.getPos();
            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            renderer.render(blockEntity, tickDelta, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }
    }

    private static List<BlockEntity> buildBlockEntities(
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntityNbt
    ) {
        if (blockEntityNbt == null || blockEntityNbt.isEmpty()) {
            return List.of();
        }
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client == null ? null : client.world;
        if (world == null) {
            return List.of();
        }
        List<BlockEntity> result = new ArrayList<>(blockEntityNbt.size());
        for (Map.Entry<BlockPos, NbtCompound> entry : blockEntityNbt.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = blocks.get(pos);
            if (state == null || entry.getValue() == null) {
                continue;
            }
            BlockEntity be = BlockEntity.createFromNbt(pos, state, entry.getValue(), world.getRegistryManager());
            if (be == null) {
                continue;
            }
            be.setWorld(world);
            result.add(be);
        }
        return List.copyOf(result);
    }

    private static Map<BlockPos, NbtCompound> copyNbtMap(Map<BlockPos, NbtCompound> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<BlockPos, NbtCompound> copy = new java.util.HashMap<>(source.size());
        for (Map.Entry<BlockPos, NbtCompound> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                copy.put(entry.getKey(), entry.getValue().copy());
            }
        }
        return Map.copyOf(copy);
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

    private record CachedSnapshot(
            int revision,
            Map<BlockPos, BlockState> blocks,
            Map<BlockPos, NbtCompound> blockEntityNbt,
            List<BlockEntity> renderedBlockEntities
    ) {
    }
}
