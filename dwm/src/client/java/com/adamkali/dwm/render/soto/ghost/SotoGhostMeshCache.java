package com.adamkali.dwm.render.soto.ghost;

import com.adamkali.dwm.render.portal.PortalCameraTransform;
import com.adamkali.dwm.render.portal.PortalFrameCache;
import com.adamkali.dwm.render.portal.PortalPerfStats;
import com.adamkali.dwm.render.portal.PortalSceneStore;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-chunk CPU meshes baked from {@link SotoGhostExterior} on chunk apply, drawn via
 * hitch-culled pass-level GPU batches (one draw per {@link TerrainPass}).
 * Keyed by (PortalStreamKind, UUID) so BOTI and SOTO share the same mesh infrastructure.
 */
public final class SotoGhostMeshCache {
    private static final int QUAD_VERTEX_STRIDE = 4;
    private static final int QUAD_INDEX_STRIDE = 6;

    private static final Map<PortalSceneStore.SceneKey, Map<Long, ChunkMesh>> MESHES = new ConcurrentHashMap<>();
    private static final Map<PortalSceneStore.SceneKey, PassBatchState> PASS_BATCHES = new ConcurrentHashMap<>();
    private static final Map<PortalSceneStore.SceneKey, GhostBakeScheduler> BAKE_QUEUES = new ConcurrentHashMap<>();

    private SotoGhostMeshCache() {
    }

    public static boolean hasMeshes(PortalStreamKind kind, UUID tardisId) {
        return meshChunkCount(kind, tardisId) > 0;
    }

    public static int meshChunkCount(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return 0;
        }
        Map<Long, ChunkMesh> byChunk = MESHES.get(new PortalSceneStore.SceneKey(kind, tardisId));
        if (byChunk == null || byChunk.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (ChunkMesh mesh : byChunk.values()) {
            if (mesh != null && mesh.isDrawable()) {
                count++;
            }
        }
        return count;
    }

    /** True when a drawable (non-MARKER) mesh exists for this chunk column. */
    public static boolean hasDrawableChunk(PortalStreamKind kind, UUID tardisId, int chunkX, int chunkZ) {
        if (kind == null || tardisId == null) {
            return false;
        }
        Map<Long, ChunkMesh> byChunk = MESHES.get(new PortalSceneStore.SceneKey(kind, tardisId));
        if (byChunk == null) {
            return false;
        }
        ChunkMesh mesh = byChunk.get(ChunkPos.pack(chunkX, chunkZ));
        return mesh != null && mesh.isDrawable();
    }

    /** Max chunks baked per frame by {@link #drainBakeQueue}. */
    static final int BAKE_BUDGET_CHUNKS = 4;
    /** Soft time budget per frame (at least one chunk is always baked). */
    static final long BAKE_BUDGET_NANOS = 4_000_000L;

    /** Queues a new or changed chunk for baking; the bake happens in {@link #drainBakeQueue}. */
    public static void onChunkApplied(PortalStreamKind kind, UUID tardisId, int chunkX, int chunkZ, SotoGhostExterior ghost) {
        enqueue(kind, tardisId, chunkX, chunkZ, ghost, GhostBakeScheduler.Priority.PRIMARY);
    }

    /** Queues a neighbour whose border (AO / light) may have changed. Coalesces with other pending bakes. */
    public static void onChunkRefresh(PortalStreamKind kind, UUID tardisId, int chunkX, int chunkZ, SotoGhostExterior ghost) {
        enqueue(kind, tardisId, chunkX, chunkZ, ghost, GhostBakeScheduler.Priority.REFRESH);
    }

    private static void enqueue(
            PortalStreamKind kind, UUID tardisId, int chunkX, int chunkZ,
            SotoGhostExterior ghost, GhostBakeScheduler.Priority priority
    ) {
        if (kind == null || tardisId == null || ghost == null) {
            return;
        }
        PortalSceneStore.SceneKey sceneKey = new PortalSceneStore.SceneKey(kind, tardisId);
        BAKE_QUEUES.computeIfAbsent(sceneKey, ignored -> new GhostBakeScheduler())
                .enqueue(ChunkPos.pack(chunkX, chunkZ), priority);
    }

    /** Chunks waiting to be baked for this scene. */
    public static int pendingBakeCount(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return 0;
        }
        GhostBakeScheduler queue = BAKE_QUEUES.get(new PortalSceneStore.SceneKey(kind, tardisId));
        return queue == null ? 0 : queue.size();
    }

    /**
     * Bakes queued chunks for every scene under one shared per-frame budget. Called once per frame on
     * the render thread from {@code LevelRenderEvents.START_MAIN}, independently of portal drawing:
     * portals only draw once they have meshes ({@code isReady}) and reuse cached frames, so draining
     * from the draw path would never produce the first mesh. Scenes that baked are marked dirty so the
     * cached portal frame picks the new meshes up. Returns the number of chunks baked.
     */
    public static int drainAllBakeQueues() {
        return drainAllBakeQueues(BAKE_BUDGET_CHUNKS, BAKE_BUDGET_NANOS, System::nanoTime);
    }

    static int drainAllBakeQueues(int maxChunks, long maxNanos, java.util.function.LongSupplier clock) {
        if (BAKE_QUEUES.isEmpty()) {
            return 0;
        }
        long start = clock.getAsLong();
        int total = 0;
        int pending = 0;
        List<PortalSceneStore.SceneKey> keys = new ArrayList<>(BAKE_QUEUES.keySet());
        keys.sort(Comparator.comparing((PortalSceneStore.SceneKey k) -> k.kind()).thenComparing(PortalSceneStore.SceneKey::tardisId));
        for (PortalSceneStore.SceneKey key : keys) {
            int chunksLeft = maxChunks - total;
            long nanosLeft = maxNanos - (clock.getAsLong() - start);
            if (chunksLeft > 0 && (total == 0 || nanosLeft > 0L)) {
                int baked = drainBakeQueue(key.kind(), key.tardisId(), chunksLeft, Math.max(1L, nanosLeft), clock);
                if (baked > 0) {
                    total += baked;
                    PortalFrameCache.markDirty(key.kind(), key.tardisId());
                }
            }
            pending += pendingBakeCount(key.kind(), key.tardisId());
        }
        PortalPerfStats.noteBakeFrame(total, pending);
        return total;
    }

    /**
     * Bakes queued chunks for one scene under the given budget (render thread). Returns the number
     * of chunks baked.
     */
    public static int drainBakeQueue(PortalStreamKind kind, UUID tardisId) {
        return drainBakeQueue(kind, tardisId, BAKE_BUDGET_CHUNKS, BAKE_BUDGET_NANOS, System::nanoTime);
    }

    static int drainBakeQueue(
            PortalStreamKind kind, UUID tardisId, int maxChunks, long maxNanos, java.util.function.LongSupplier clock
    ) {
        if (kind == null || tardisId == null) {
            return 0;
        }
        PortalSceneStore.SceneKey sceneKey = new PortalSceneStore.SceneKey(kind, tardisId);
        GhostBakeScheduler queue = BAKE_QUEUES.get(sceneKey);
        SotoGhostExterior ghost = SotoGhostExterior.get(kind, tardisId);
        if (queue == null || queue.size() == 0) {
            return 0;
        }
        if (ghost == null) {
            queue.clear();
            return 0;
        }
        BlockPos origin = ghost.footprintOrigin();
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;
        return queue.drain(maxChunks, maxNanos, clock, packed -> {
            double dx = ChunkPos.getX(packed) - originChunkX;
            double dz = ChunkPos.getZ(packed) - originChunkZ;
            return dx * dx + dz * dz;
        }, packed -> bakeNow(sceneKey, ChunkPos.getX(packed), ChunkPos.getZ(packed), ghost));
    }

    private static void bakeNow(PortalSceneStore.SceneKey sceneKey, int chunkX, int chunkZ, SotoGhostExterior ghost) {
        long key = ChunkPos.pack(chunkX, chunkZ);
        Map<BlockPos, BlockState> blocks = ghost.blocksInChunk(key);
        long bakeStart = PortalPerfStats.begin();
        ChunkMesh baked = bakeChunk(blocks, ghost);
        PortalPerfStats.noteMeshBake(bakeStart >= 0L ? System.nanoTime() - bakeStart : 0L);
        Map<Long, ChunkMesh> byChunk = MESHES.computeIfAbsent(sceneKey, ignored -> new ConcurrentHashMap<>());
        ChunkMesh previous = byChunk.put(key, baked);
        if (previous != null) {
            previous.close();
        }
        if (baked.isEmpty()) {
            byChunk.remove(key, baked);
        }
        markPassBatchesDirty(sceneKey);
    }

    /**
     * Re-bakes every chunk mesh of a scene from its ghost world, keeping the old meshes drawable
     * until each replacement is ready (used when baked-in shading such as cardinal light changes).
     */
    public static void rebakeAll(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return;
        }
        SotoGhostExterior ghost = SotoGhostExterior.get(kind, tardisId);
        Map<Long, ChunkMesh> byChunk = MESHES.get(new PortalSceneStore.SceneKey(kind, tardisId));
        if (ghost == null || byChunk == null) {
            return;
        }
        for (long packed : List.copyOf(byChunk.keySet())) {
            onChunkApplied(kind, tardisId, ChunkPos.getX(packed), ChunkPos.getZ(packed), ghost);
        }
    }

    public static void onChunkUnloaded(PortalStreamKind kind, UUID tardisId, int chunkX, int chunkZ) {
        if (kind == null || tardisId == null) {
            return;
        }
        PortalSceneStore.SceneKey sceneKey = new PortalSceneStore.SceneKey(kind, tardisId);
        GhostBakeScheduler queue = BAKE_QUEUES.get(sceneKey);
        if (queue != null) {
            queue.remove(ChunkPos.pack(chunkX, chunkZ));
        }
        Map<Long, ChunkMesh> byChunk = MESHES.get(sceneKey);
        if (byChunk == null) {
            return;
        }
        ChunkMesh removed = byChunk.remove(ChunkPos.pack(chunkX, chunkZ));
        if (removed != null) {
            removed.close();
        }
        if (byChunk.isEmpty()) {
            MESHES.remove(sceneKey, byChunk);
        }
        markPassBatchesDirty(sceneKey);
    }

    public static void invalidate(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return;
        }
        PortalSceneStore.SceneKey sceneKey = new PortalSceneStore.SceneKey(kind, tardisId);
        GhostBakeScheduler queue = BAKE_QUEUES.remove(sceneKey);
        if (queue != null) {
            queue.clear();
        }
        Map<Long, ChunkMesh> byChunk = MESHES.remove(sceneKey);
        if (byChunk != null) {
            for (ChunkMesh mesh : byChunk.values()) {
                mesh.close();
            }
            byChunk.clear();
        }
        PassBatchState batches = PASS_BATCHES.remove(sceneKey);
        if (batches != null) {
            batches.close();
        }
    }

    public static void invalidateAll() {
        for (PortalSceneStore.SceneKey key : List.copyOf(MESHES.keySet())) {
            invalidate(key.kind(), key.tardisId());
        }
        for (GhostBakeScheduler queue : BAKE_QUEUES.values()) {
            queue.clear();
        }
        BAKE_QUEUES.clear();
        for (PortalSceneStore.SceneKey key : List.copyOf(PASS_BATCHES.keySet())) {
            PassBatchState batches = PASS_BATCHES.remove(key);
            if (batches != null) {
                batches.close();
            }
        }
    }

    /**
     * Test helper: records a chunk as having drawable mesh without requiring a GPU bake.
     */
    public static void markChunkMeshForTest(PortalStreamKind kind, UUID tardisId, int chunkX, int chunkZ) {
        if (kind == null || tardisId == null) {
            return;
        }
        PortalSceneStore.SceneKey sceneKey = new PortalSceneStore.SceneKey(kind, tardisId);
        long key = ChunkPos.pack(chunkX, chunkZ);
        Map<Long, ChunkMesh> byChunk = MESHES.computeIfAbsent(sceneKey, ignored -> new ConcurrentHashMap<>());
        ChunkMesh previous = byChunk.put(key, ChunkMesh.TEST_DRAWABLE);
        if (previous != null && previous != ChunkMesh.TEST_DRAWABLE && previous != ChunkMesh.MARKER) {
            previous.close();
        }
        markPassBatchesDirty(sceneKey);
    }

    /**
     * Test helper: records a non-drawable MARKER entry (must not satisfy {@link #hasMeshes}).
     */
    public static void markChunkMarkerForTest(PortalStreamKind kind, UUID tardisId, int chunkX, int chunkZ) {
        if (kind == null || tardisId == null) {
            return;
        }
        PortalSceneStore.SceneKey sceneKey = new PortalSceneStore.SceneKey(kind, tardisId);
        long key = ChunkPos.pack(chunkX, chunkZ);
        Map<Long, ChunkMesh> byChunk = MESHES.computeIfAbsent(sceneKey, ignored -> new ConcurrentHashMap<>());
        ChunkMesh previous = byChunk.put(key, ChunkMesh.MARKER);
        if (previous != null && previous != ChunkMesh.MARKER && previous != ChunkMesh.TEST_DRAWABLE) {
            previous.close();
        }
        markPassBatchesDirty(sceneKey);
    }

    /** Test helper: whether pass batches need rebuild for this scene. */
    public static boolean arePassBatchesDirtyForTest(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return false;
        }
        PassBatchState state = PASS_BATCHES.get(new PortalSceneStore.SceneKey(kind, tardisId));
        return state == null || state.dirty;
    }

    /**
     * Draws one terrain pass as a single GPU batch (opaque → cutout → translucent order preserved
     * by the caller issuing three draws).
     */
    public static void drawLayer(
            PortalStreamKind kind,
            UUID tardisId,
            Matrix4f viewMatrix,
            TerrainPass pass,
            PortalCameraTransform.Result hitch
    ) {
        if (kind == null || tardisId == null || viewMatrix == null || pass == null) {
            return;
        }
        PortalSceneStore.SceneKey sceneKey = new PortalSceneStore.SceneKey(kind, tardisId);
        Map<Long, ChunkMesh> byChunk = MESHES.get(sceneKey);
        if (byChunk == null || byChunk.isEmpty()) {
            return;
        }
        PassBatchState batches = ensurePassBatches(sceneKey, byChunk, hitch);
        LayerBuffer batch = batches.buffer(pass);
        if (batch == null) {
            return;
        }
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(viewMatrix);
        try {
            batch.draw();
        } finally {
            modelViewStack.popMatrix();
        }
    }

    private static void markPassBatchesDirty(PortalSceneStore.SceneKey sceneKey) {
        PassBatchState state = PASS_BATCHES.get(sceneKey);
        if (state != null) {
            state.dirty = true;
        } else {
            PASS_BATCHES.put(sceneKey, new PassBatchState());
        }
    }

    private static PassBatchState ensurePassBatches(
            PortalSceneStore.SceneKey sceneKey,
            Map<Long, ChunkMesh> byChunk,
            PortalCameraTransform.Result hitch
    ) {
        PassBatchState state = PASS_BATCHES.computeIfAbsent(sceneKey, ignored -> new PassBatchState());
        HitchSignature signature = HitchSignature.from(hitch);
        if (!state.dirty && signature.equals(state.hitchSignature)) {
            return state;
        }
        SotoGhostExterior ghost = SotoGhostExterior.get(sceneKey.kind(), sceneKey.tardisId());
        BlockPos footprintOrigin = ghost != null ? ghost.footprintOrigin() : BlockPos.ZERO;
        long rebuildStart = PortalPerfStats.begin();
        state.rebuild(byChunk, hitch, signature, footprintOrigin);
        PortalPerfStats.end(PortalPerfStats.Stage.PASS_BATCH_REBUILD, rebuildStart);
        return state;
    }

    private static ChunkMesh bakeChunk(Map<BlockPos, BlockState> blocks, SotoGhostExterior ghost) {
        if (blocks == null || blocks.isEmpty()) {
            return ChunkMesh.EMPTY;
        }
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return ChunkMesh.EMPTY;
        }

        boolean anyVisible = false;
        for (BlockState state : blocks.values()) {
            if (state != null && state.getRenderShape() != RenderShape.INVISIBLE) {
                anyVisible = true;
                break;
            }
        }
        if (!anyVisible) {
            return ChunkMesh.EMPTY;
        }

        if (!RenderSystem.isOnRenderThread() || client.getModelManager() == null || client.getBlockColors() == null) {
            return ChunkMesh.MARKER;
        }

        try {
            BlockStateModelSet models = client.getModelManager().getBlockStateModelSet();
            BlockColors blockColors = client.getBlockColors();
            ModelBlockRenderer baker = new ModelBlockRenderer(false, true, blockColors);

            EnumMap<ChunkSectionLayer, ByteBufferBuilder> allocators = new EnumMap<>(ChunkSectionLayer.class);
            EnumMap<ChunkSectionLayer, BufferBuilder> builders = new EnumMap<>(ChunkSectionLayer.class);
            QuadInstance quadInstance = new QuadInstance();
            quadInstance.setOverlayCoords(0);

            for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState state = entry.getValue();
                if (pos == null || state == null || state.getRenderShape() == RenderShape.INVISIBLE) {
                    continue;
                }
                BlockStateModel model = models.get(state);
                if (model == null) {
                    continue;
                }
                baker.tesselateBlock(
                        (x, y, z, quad, instance) -> putQuad(allocators, builders, x, y, z, quad, instance),
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        ghost,
                        pos,
                        state,
                        model,
                        state.getSeed(pos)
                );
            }

            List<CpuLayer> uploaded = new ArrayList<>();
            for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
                BufferBuilder builder = builders.get(layer);
                if (builder == null) {
                    continue;
                }
                MeshData meshData = builder.build();
                if (meshData == null) {
                    continue;
                }
                try {
                    CpuLayer cpuLayer = CpuLayer.fromMesh(layer, meshData);
                    if (cpuLayer != null) {
                        uploaded.add(cpuLayer);
                    }
                } finally {
                    meshData.close();
                }
            }
            for (ByteBufferBuilder allocator : allocators.values()) {
                allocator.close();
            }

            if (uploaded.isEmpty()) {
                return ChunkMesh.MARKER;
            }
            return new ChunkMesh(uploaded);
        } catch (Throwable ignored) {
            return ChunkMesh.MARKER;
        }
    }

    private static void putQuad(
            EnumMap<ChunkSectionLayer, ByteBufferBuilder> allocators,
            EnumMap<ChunkSectionLayer, BufferBuilder> builders,
            float x,
            float y,
            float z,
            BakedQuad quad,
            QuadInstance instance
    ) {
        ChunkSectionLayer layer = quad.materialInfo().layer();
        if (layer == null) {
            layer = ChunkSectionLayer.SOLID;
        }
        BufferBuilder builder = builders.get(layer);
        if (builder == null) {
            ByteBufferBuilder allocator = new ByteBufferBuilder(Math.max(layer.bufferSize(), 256 * 1024));
            allocators.put(layer, allocator);
            builder = new BufferBuilder(allocator, PrimitiveTopology.QUADS, DefaultVertexFormat.BLOCK);
            builders.put(layer, builder);
        }
        builder.putBlockBakedQuad(x, y, z, quad, instance);
    }

    private static byte[] copyBytes(ByteBuffer buffer) {
        ByteBuffer duplicate = buffer.duplicate();
        byte[] bytes = new byte[duplicate.remaining()];
        duplicate.get(bytes);
        return bytes;
    }

    private static void appendSequentialQuadIndices(ByteBuffer dest, IndexType type, int vertexBase, int vertexCount) {
        int quadCount = vertexCount / QUAD_VERTEX_STRIDE;
        for (int q = 0; q < quadCount; q++) {
            int i = vertexBase + q * QUAD_VERTEX_STRIDE;
            putIndex(dest, type, i);
            putIndex(dest, type, i + 1);
            putIndex(dest, type, i + 2);
            putIndex(dest, type, i + 2);
            putIndex(dest, type, i + 3);
            putIndex(dest, type, i);
        }
    }

    private static void appendRemappedIndices(
            ByteBuffer dest,
            IndexType outType,
            byte[] srcIndices,
            IndexType srcType,
            int indexCount,
            int vertexBase
    ) {
        ByteBuffer src = ByteBuffer.wrap(srcIndices).order(ByteOrder.nativeOrder());
        for (int i = 0; i < indexCount; i++) {
            int value = srcType == IndexType.INT ? src.getInt() : Short.toUnsignedInt(src.getShort());
            putIndex(dest, outType, value + vertexBase);
        }
    }

    private static void putIndex(ByteBuffer dest, IndexType type, int value) {
        if (type == IndexType.INT) {
            dest.putInt(value);
        } else {
            dest.putShort((short) value);
        }
    }

    private static LayerBuffer uploadMerged(TerrainPass pass, ChunkSectionLayer sectionLayer, List<CpuLayer> parts) {
        if (parts == null || parts.isEmpty()) {
            return null;
        }
        int totalVertexBytes = 0;
        int totalVertices = 0;
        int totalIndices = 0;
        for (CpuLayer part : parts) {
            totalVertexBytes += part.vertexBytes().length;
            totalVertices += part.vertexCount();
            if (part.indexBytes() != null) {
                totalIndices += part.indexCount();
            } else {
                totalIndices += (part.vertexCount() / QUAD_VERTEX_STRIDE) * QUAD_INDEX_STRIDE;
            }
        }
        if (totalVertices <= 0 || totalIndices <= 0 || totalVertexBytes <= 0) {
            return null;
        }

        IndexType outType = IndexType.least(totalVertices);
        ByteBuffer mergedVertices = ByteBuffer.allocateDirect(totalVertexBytes).order(ByteOrder.nativeOrder());
        ByteBuffer mergedIndices = ByteBuffer.allocateDirect(totalIndices * outType.bytes).order(ByteOrder.nativeOrder());
        int vertexBase = 0;
        for (CpuLayer part : parts) {
            mergedVertices.put(part.vertexBytes());
            if (part.indexBytes() != null) {
                appendRemappedIndices(
                        mergedIndices,
                        outType,
                        part.indexBytes(),
                        part.indexType(),
                        part.indexCount(),
                        vertexBase
                );
            } else {
                appendSequentialQuadIndices(mergedIndices, outType, vertexBase, part.vertexCount());
            }
            vertexBase += part.vertexCount();
        }
        mergedVertices.flip();
        mergedIndices.flip();

        GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(
                () -> "dwm_soto_pass_" + pass.name().toLowerCase(),
                GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                mergedVertices
        );
        GpuBuffer indexBuffer = RenderSystem.getDevice().createBuffer(
                () -> "dwm_soto_pass_idx_" + pass.name().toLowerCase(),
                GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST,
                mergedIndices
        );
        return new LayerBuffer(pass, sectionLayer, vertexBuffer, indexBuffer, outType, totalIndices);
    }

    private record HitchSignature(double eyeX, double eyeY, double eyeZ, double lookX, double lookY, double lookZ) {
        static HitchSignature from(PortalCameraTransform.Result hitch) {
            if (hitch == null) {
                return new HitchSignature(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
            }
            Vec3 eye = hitch.eyeRelative();
            Vec3 look = hitch.lookDirection();
            return new HitchSignature(eye.x, eye.y, eye.z, look.x, look.y, look.z);
        }
    }

    private static final class PassBatchState implements AutoCloseable {
        private final EnumMap<TerrainPass, LayerBuffer> batches = new EnumMap<>(TerrainPass.class);
        private boolean dirty = true;
        private HitchSignature hitchSignature;

        LayerBuffer buffer(TerrainPass pass) {
            return batches.get(pass);
        }

        void rebuild(
                Map<Long, ChunkMesh> byChunk,
                PortalCameraTransform.Result hitch,
                HitchSignature signature,
                BlockPos footprintOrigin
        ) {
            closeBatches();
            EnumMap<TerrainPass, List<CpuLayer>> byPass = new EnumMap<>(TerrainPass.class);
            EnumMap<TerrainPass, ChunkSectionLayer> sectionForPass = new EnumMap<>(TerrainPass.class);

            List<Long> chunkKeys = new ArrayList<>(byChunk.keySet());
            chunkKeys.sort(Long::compare);
            Matrix4f viewMatrix = hitch != null ? hitch.viewMatrix() : null;
            Vec3 eye = hitch != null ? hitch.eyeRelative() : null;
            Vec3 look = hitch != null ? hitch.lookDirection() : null;
            BlockPos origin = footprintOrigin == null ? BlockPos.ZERO : footprintOrigin;

            int kept = 0;
            int culled = 0;
            for (Long chunkKey : chunkKeys) {
                ChunkMesh mesh = byChunk.get(chunkKey);
                if (mesh == null || !mesh.isDrawable() || mesh.layers.isEmpty()) {
                    continue;
                }
                int chunkX = ChunkPos.getX(chunkKey);
                int chunkZ = ChunkPos.getZ(chunkKey);
                if (!SotoGhostHitchCull.isChunkVisibleToHitch(chunkX, chunkZ, origin, eye, look, viewMatrix)) {
                    culled++;
                    continue;
                }
                kept++;
                for (CpuLayer layer : mesh.layers) {
                    TerrainPass pass = layer.pass();
                    byPass.computeIfAbsent(pass, ignored -> new ArrayList<>()).add(layer);
                    sectionForPass.putIfAbsent(pass, layer.sectionLayer());
                }
            }
            PortalPerfStats.setCullCounts(kept, culled);

            for (TerrainPass pass : TerrainPass.values()) {
                List<CpuLayer> parts = byPass.get(pass);
                if (parts == null || parts.isEmpty()) {
                    continue;
                }
                LayerBuffer merged = uploadMerged(pass, sectionForPass.get(pass), parts);
                if (merged != null) {
                    batches.put(pass, merged);
                }
            }
            dirty = false;
            hitchSignature = signature;
        }

        private void closeBatches() {
            for (LayerBuffer buffer : batches.values()) {
                buffer.close();
            }
            batches.clear();
        }

        @Override
        public void close() {
            closeBatches();
            dirty = true;
            hitchSignature = null;
        }
    }

    private static final class ChunkMesh implements AutoCloseable {
        static final ChunkMesh EMPTY = new ChunkMesh(List.of(), Kind.EMPTY);
        static final ChunkMesh MARKER = new ChunkMesh(List.of(), Kind.MARKER);
        static final ChunkMesh TEST_DRAWABLE = new ChunkMesh(List.of(), Kind.TEST_DRAWABLE);

        private enum Kind {
            EMPTY,
            MARKER,
            TEST_DRAWABLE,
            REAL
        }

        private final List<CpuLayer> layers;
        private final Kind kind;
        private boolean closed;

        private ChunkMesh(List<CpuLayer> layers) {
            this(layers, Kind.REAL);
        }

        private ChunkMesh(List<CpuLayer> layers, Kind kind) {
            this.layers = List.copyOf(layers);
            this.kind = kind;
        }

        boolean isEmpty() {
            return kind == Kind.EMPTY || (kind == Kind.REAL && layers.isEmpty());
        }

        boolean isDrawable() {
            if (closed) {
                return false;
            }
            return kind == Kind.TEST_DRAWABLE || (kind == Kind.REAL && !layers.isEmpty());
        }

        @Override
        public void close() {
            if (closed || kind != Kind.REAL) {
                return;
            }
            closed = true;
        }
    }

    private record CpuLayer(
            TerrainPass pass,
            ChunkSectionLayer sectionLayer,
            byte[] vertexBytes,
            byte[] indexBytes,
            IndexType indexType,
            int indexCount,
            int vertexCount
    ) {
        static CpuLayer fromMesh(ChunkSectionLayer layer, MeshData meshData) {
            MeshData.DrawState drawState = meshData.drawState();
            if (drawState.vertexCount() <= 0 || drawState.indexCount() <= 0) {
                return null;
            }
            ByteBuffer vertexBytes = meshData.vertexBuffer();
            if (vertexBytes == null || !vertexBytes.hasRemaining()) {
                return null;
            }
            byte[] vertices = copyBytes(vertexBytes);
            byte[] indices = null;
            ByteBuffer indexBytes = meshData.indexBuffer();
            if (indexBytes != null && indexBytes.hasRemaining()) {
                indices = copyBytes(indexBytes);
            }
            return new CpuLayer(
                    TerrainPass.forSectionLayer(layer),
                    layer,
                    vertices,
                    indices,
                    drawState.indexType(),
                    drawState.indexCount(),
                    drawState.vertexCount()
            );
        }
    }

    private record LayerBuffer(
            TerrainPass pass,
            ChunkSectionLayer sectionLayer,
            GpuBuffer vertexBuffer,
            GpuBuffer indexBuffer,
            IndexType indexType,
            int indexCount
    ) implements AutoCloseable {
        void draw() {
            if (vertexBuffer == null || vertexBuffer.isClosed() || indexCount <= 0) {
                return;
            }
            PreparedRenderType prepared = renderTypeFor(sectionLayer).prepare();
            GpuBuffer ib = indexBuffer;
            IndexType type = indexType;
            if (ib == null || ib.isClosed()) {
                var sequential = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
                ib = sequential.getBuffer(indexCount);
                type = sequential.type();
            }
            prepared.drawFromBuffer(vertexBuffer, ib, type, 0, 0, indexCount);
        }

        private static net.minecraft.client.renderer.rendertype.RenderType renderTypeFor(ChunkSectionLayer layer) {
            if (layer == ChunkSectionLayer.TRANSLUCENT) {
                return RenderTypes.translucentMovingBlock();
            }
            if (layer == ChunkSectionLayer.CUTOUT) {
                return RenderTypes.cutoutMovingBlock();
            }
            return RenderTypes.solidMovingBlock();
        }

        @Override
        public void close() {
            if (vertexBuffer != null && !vertexBuffer.isClosed()) {
                vertexBuffer.close();
            }
            if (indexBuffer != null && !indexBuffer.isClosed()) {
                indexBuffer.close();
            }
        }
    }

    public enum TerrainPass {
        OPAQUE,
        CUTOUT,
        TRANSLUCENT;

        static TerrainPass forSectionLayer(ChunkSectionLayer layer) {
            if (layer == null) {
                return OPAQUE;
            }
            if (layer.translucent()) {
                return TRANSLUCENT;
            }
            if (layer == ChunkSectionLayer.CUTOUT) {
                return CUTOUT;
            }
            return OPAQUE;
        }

        static TerrainPass forBlockState(BlockState state) {
            if (state == null) {
                return OPAQUE;
            }
            if (!state.canOcclude()) {
                return CUTOUT;
            }
            return OPAQUE;
        }
    }
}
