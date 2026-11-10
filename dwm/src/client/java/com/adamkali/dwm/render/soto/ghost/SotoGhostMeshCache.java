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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Per-chunk meshes baked from {@link SotoGhostExterior} and uploaded to their own GPU buffers once,
 * right after the bake. Each frame draws the hitch-visible chunks' buffers directly (one draw per
 * chunk per {@link TerrainPass}); editing one chunk never re-uploads another.
 * Keyed by (PortalStreamKind, UUID) so BOTI and SOTO share the same mesh infrastructure.
 */
public final class SotoGhostMeshCache {
    private static final Map<PortalSceneStore.SceneKey, Map<Long, ChunkMesh>> MESHES = new ConcurrentHashMap<>();
    /** GPU upload time inside the current {@link #bakeNow} (render thread only). */
    private static long uploadNsThisBake;
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
        // Release GPU buffers whose meshes were closed off the render thread (e.g. on disconnect).
        LayerBuffer.drainPendingClose();
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
        uploadNsThisBake = 0L;
        ChunkMesh baked = bakeChunk(blocks, ghost);
        // Upload time is reported separately (MESH_UPLOAD); keep MESH_BAKE to tessellation only.
        PortalPerfStats.noteMeshBake(bakeStart >= 0L ? System.nanoTime() - bakeStart - uploadNsThisBake : 0L);
        Map<Long, ChunkMesh> byChunk = MESHES.computeIfAbsent(sceneKey, ignored -> new ConcurrentHashMap<>());
        ChunkMesh previous = byChunk.put(key, baked);
        if (previous != null) {
            previous.close();
        }
        if (baked.isEmpty()) {
            byChunk.remove(key, baked);
        }
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
    }

    public static void invalidateAll() {
        for (PortalSceneStore.SceneKey key : List.copyOf(MESHES.keySet())) {
            invalidate(key.kind(), key.tardisId());
        }
        for (GhostBakeScheduler queue : BAKE_QUEUES.values()) {
            queue.clear();
        }
        BAKE_QUEUES.clear();
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
    }

    /**
     * Test helper: installs a REAL mesh holding one GPU-free {@link LayerBuffer} per pass given,
     * closing whatever mesh it replaces (same lifecycle as a real bake).
     */
    static void markChunkBuffersForTest(
            PortalStreamKind kind, UUID tardisId, int chunkX, int chunkZ, TerrainPass... passes
    ) {
        if (kind == null || tardisId == null) {
            return;
        }
        List<LayerBuffer> buffers = new ArrayList<>();
        for (TerrainPass pass : passes) {
            buffers.add(LayerBuffer.forTest(pass));
        }
        PortalSceneStore.SceneKey sceneKey = new PortalSceneStore.SceneKey(kind, tardisId);
        Map<Long, ChunkMesh> byChunk = MESHES.computeIfAbsent(sceneKey, ignored -> new ConcurrentHashMap<>());
        ChunkMesh previous = byChunk.put(ChunkPos.pack(chunkX, chunkZ), new ChunkMesh(buffers));
        if (previous != null) {
            previous.close();
        }
    }

    /** Test helper: the live buffer objects of one chunk (identity checks), or empty. */
    static List<LayerBuffer> chunkBuffersForTest(PortalStreamKind kind, UUID tardisId, int chunkX, int chunkZ) {
        if (kind == null || tardisId == null) {
            return List.of();
        }
        Map<Long, ChunkMesh> byChunk = MESHES.get(new PortalSceneStore.SceneKey(kind, tardisId));
        ChunkMesh mesh = byChunk == null ? null : byChunk.get(ChunkPos.pack(chunkX, chunkZ));
        return mesh == null ? List.of() : mesh.layers;
    }

    /** GPU buffer sets ever created (one per chunk layer upload). */
    public static long buffersCreated() {
        return LayerBuffer.CREATED.get();
    }

    /** GPU buffer sets closed; equals {@link #buffersCreated()} once every mesh is released. */
    public static long buffersClosed() {
        return LayerBuffer.CLOSED.get();
    }

    /** Vertex + index bytes uploaded to the GPU since the last reset. */
    public static long bytesUploaded() {
        return LayerBuffer.BYTES_UPLOADED.get();
    }

    /** Per-chunk draw calls issued since the last reset. */
    public static long drawCalls() {
        return LayerBuffer.DRAW_CALLS.get();
    }

    /** Resets the buffer/upload/draw counters (perf tests). */
    public static void resetBufferStats() {
        LayerBuffer.CREATED.set(0L);
        LayerBuffer.CLOSED.set(0L);
        LayerBuffer.BYTES_UPLOADED.set(0L);
        LayerBuffer.DRAW_CALLS.set(0L);
    }

    /**
     * Draws one terrain pass from each hitch-visible chunk's own GPU buffers (opaque → cutout →
     * translucent order preserved by the caller issuing three passes). Nothing is merged or uploaded here.
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
        SotoGhostExterior ghost = SotoGhostExterior.get(kind, tardisId);
        BlockPos origin = ghost != null && ghost.footprintOrigin() != null ? ghost.footprintOrigin() : BlockPos.ZERO;
        Vec3 eye = hitch != null ? hitch.eyeRelative() : null;
        Vec3 look = hitch != null ? hitch.lookDirection() : null;
        Matrix4f cullView = hitch != null ? hitch.viewMatrix() : null;

        List<Long> chunkKeys = new ArrayList<>(byChunk.keySet());
        chunkKeys.sort(Long::compare);

        int kept = 0;
        int culled = 0;
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(viewMatrix);
        try {
            // Prepared after the modelview is set (it captures the transform slice); one per section layer.
            EnumMap<ChunkSectionLayer, PreparedRenderType> prepared = new EnumMap<>(ChunkSectionLayer.class);
            for (Long chunkKey : chunkKeys) {
                ChunkMesh mesh = byChunk.get(chunkKey);
                if (mesh == null || !mesh.isDrawable()) {
                    continue;
                }
                if (!SotoGhostHitchCull.isChunkVisibleToHitch(
                        ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey), origin, eye, look, cullView)) {
                    culled++;
                    continue;
                }
                kept++;
                for (LayerBuffer buffer : mesh.layers) {
                    if (buffer.pass() != pass) {
                        continue;
                    }
                    PreparedRenderType type = prepared.computeIfAbsent(
                            buffer.sectionLayer(), layer -> LayerBuffer.renderTypeFor(layer).prepare());
                    buffer.draw(type);
                }
            }
        } finally {
            modelViewStack.popMatrix();
        }
        if (pass == TerrainPass.OPAQUE) {
            PortalPerfStats.setCullCounts(kept, culled);
        }
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

        List<LayerBuffer> uploaded = new ArrayList<>();
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

            try {
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
                            // Upload once, here; later frames only draw (no merge / re-upload).
                            long uploadStart = PortalPerfStats.begin();
                            uploaded.add(LayerBuffer.upload(cpuLayer));
                            PortalPerfStats.end(PortalPerfStats.Stage.MESH_UPLOAD, uploadStart);
                            if (uploadStart >= 0L) {
                                uploadNsThisBake += System.nanoTime() - uploadStart;
                            }
                        }
                    } finally {
                        meshData.close();
                    }
                }
            } finally {
                for (ByteBufferBuilder allocator : allocators.values()) {
                    allocator.close();
                }
            }

            if (uploaded.isEmpty()) {
                return ChunkMesh.MARKER;
            }
            return new ChunkMesh(uploaded);
        } catch (Throwable ignored) {
            for (LayerBuffer buffer : uploaded) {
                buffer.close();
            }
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

        private final List<LayerBuffer> layers;
        private final Kind kind;
        private boolean closed;

        private ChunkMesh(List<LayerBuffer> layers) {
            this(layers, Kind.REAL);
        }

        private ChunkMesh(List<LayerBuffer> layers, Kind kind) {
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
            for (LayerBuffer buffer : layers) {
                buffer.close();
            }
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

    /**
     * One chunk's GPU buffers for a single {@link ChunkSectionLayer}, uploaded once after the bake.
     * {@code close()} is idempotent and null-tolerant; off the render thread the real GPU release is
     * deferred to {@link #drainPendingClose()}.
     */
    static final class LayerBuffer implements AutoCloseable {
        static final AtomicLong CREATED = new AtomicLong();
        static final AtomicLong CLOSED = new AtomicLong();
        static final AtomicLong BYTES_UPLOADED = new AtomicLong();
        static final AtomicLong DRAW_CALLS = new AtomicLong();
        private static final ConcurrentLinkedQueue<GpuBuffer> PENDING_CLOSE = new ConcurrentLinkedQueue<>();

        private final TerrainPass pass;
        private final ChunkSectionLayer sectionLayer;
        private final GpuBuffer vertexBuffer;
        private final GpuBuffer indexBuffer;
        private final IndexType indexType;
        private final int indexCount;
        private final AtomicBoolean closed = new AtomicBoolean();

        LayerBuffer(
                TerrainPass pass,
                ChunkSectionLayer sectionLayer,
                GpuBuffer vertexBuffer,
                GpuBuffer indexBuffer,
                IndexType indexType,
                int indexCount
        ) {
            this.pass = pass;
            this.sectionLayer = sectionLayer;
            this.vertexBuffer = vertexBuffer;
            this.indexBuffer = indexBuffer;
            this.indexType = indexType;
            this.indexCount = indexCount;
            CREATED.incrementAndGet();
        }

        /** Uploads a baked layer to the GPU. Render thread only. */
        static LayerBuffer upload(CpuLayer cpu) {
            ByteBuffer vertices = ByteBuffer.allocateDirect(cpu.vertexBytes().length).order(ByteOrder.nativeOrder());
            vertices.put(cpu.vertexBytes()).flip();
            GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "dwm_soto_chunk_" + cpu.pass().name().toLowerCase(),
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                    vertices
            );
            GpuBuffer indexBuffer = null;
            long bytes = cpu.vertexBytes().length;
            // Null index bytes: draw() falls back to the shared sequential quad index buffer.
            if (cpu.indexBytes() != null) {
                ByteBuffer indices = ByteBuffer.allocateDirect(cpu.indexBytes().length).order(ByteOrder.nativeOrder());
                indices.put(cpu.indexBytes()).flip();
                try {
                    indexBuffer = RenderSystem.getDevice().createBuffer(
                            () -> "dwm_soto_chunk_idx_" + cpu.pass().name().toLowerCase(),
                            GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST,
                            indices
                    );
                } catch (Throwable t) {
                    vertexBuffer.close();
                    throw t;
                }
                bytes += cpu.indexBytes().length;
            }
            BYTES_UPLOADED.addAndGet(bytes);
            return new LayerBuffer(cpu.pass(), cpu.sectionLayer(), vertexBuffer, indexBuffer, cpu.indexType(), cpu.indexCount());
        }

        /** Test seam: a buffer with no GPU resources that still participates in lifecycle counting. */
        static LayerBuffer forTest(TerrainPass pass) {
            return new LayerBuffer(pass, ChunkSectionLayer.SOLID, null, null, IndexType.SHORT, 6);
        }

        TerrainPass pass() {
            return pass;
        }

        ChunkSectionLayer sectionLayer() {
            return sectionLayer;
        }

        boolean isClosed() {
            return closed.get();
        }

        void draw(PreparedRenderType prepared) {
            if (closed.get() || vertexBuffer == null || vertexBuffer.isClosed() || indexCount <= 0) {
                return;
            }
            GpuBuffer ib = indexBuffer;
            IndexType type = indexType;
            if (ib == null || ib.isClosed()) {
                var sequential = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
                ib = sequential.getBuffer(indexCount);
                type = sequential.type();
            }
            prepared.drawFromBuffer(vertexBuffer, ib, type, 0, 0, indexCount);
            DRAW_CALLS.incrementAndGet();
        }

        static net.minecraft.client.renderer.rendertype.RenderType renderTypeFor(ChunkSectionLayer layer) {
            if (layer == ChunkSectionLayer.TRANSLUCENT) {
                return RenderTypes.translucentMovingBlock();
            }
            if (layer == ChunkSectionLayer.CUTOUT) {
                return RenderTypes.cutoutMovingBlock();
            }
            return RenderTypes.solidMovingBlock();
        }

        /** Releases GPU buffers queued by off-render-thread closes. Call on the render thread. */
        static void drainPendingClose() {
            GpuBuffer buffer;
            while ((buffer = PENDING_CLOSE.poll()) != null) {
                if (!buffer.isClosed()) {
                    buffer.close();
                }
            }
        }

        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            CLOSED.incrementAndGet();
            release(vertexBuffer);
            release(indexBuffer);
        }

        private static void release(GpuBuffer buffer) {
            if (buffer == null || buffer.isClosed()) {
                return;
            }
            if (RenderSystem.isOnRenderThread()) {
                buffer.close();
            } else {
                PENDING_CLOSE.add(buffer);
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
