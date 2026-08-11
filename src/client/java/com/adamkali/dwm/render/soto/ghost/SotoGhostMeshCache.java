package com.adamkali.dwm.render.soto.ghost;

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
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 2: per-chunk GPU meshes baked from {@link SotoGhostExterior} on chunk apply.
 * <p>
 * Minecraft 26.2: bake via {@link ModelBlockRenderer#tesselateBlock} into {@link MeshData},
 * upload {@link GpuBuffer}s, draw with moving-block {@link PreparedRenderType}s while the portal
 * output overrides are active.
 */
public final class SotoGhostMeshCache {
    private static final int FULLBRIGHT = LightCoordsUtil.FULL_BRIGHT;

    private static final Map<UUID, Map<Long, ChunkMesh>> MESHES = new ConcurrentHashMap<>();

    private SotoGhostMeshCache() {
    }

    public static boolean hasMeshes(UUID tardisId) {
        if (tardisId == null) {
            return false;
        }
        Map<Long, ChunkMesh> byChunk = MESHES.get(tardisId);
        return byChunk != null && !byChunk.isEmpty();
    }

    public static int meshChunkCount(UUID tardisId) {
        if (tardisId == null) {
            return 0;
        }
        Map<Long, ChunkMesh> byChunk = MESHES.get(tardisId);
        return byChunk == null ? 0 : byChunk.size();
    }

    public static void onChunkApplied(UUID tardisId, int chunkX, int chunkZ, SotoGhostExterior ghost) {
        if (tardisId == null || ghost == null) {
            return;
        }
        long key = ChunkPos.pack(chunkX, chunkZ);
        Map<BlockPos, BlockState> blocks = ghost.blocksInChunk(key);
        ChunkMesh baked = bakeChunk(blocks, ghost);
        Map<Long, ChunkMesh> byChunk = MESHES.computeIfAbsent(tardisId, ignored -> new ConcurrentHashMap<>());
        ChunkMesh previous = byChunk.put(key, baked);
        if (previous != null) {
            previous.close();
        }
        if (baked.isEmpty()) {
            byChunk.remove(key, baked);
        }
    }

    public static void onChunkUnloaded(UUID tardisId, int chunkX, int chunkZ) {
        if (tardisId == null) {
            return;
        }
        Map<Long, ChunkMesh> byChunk = MESHES.get(tardisId);
        if (byChunk == null) {
            return;
        }
        ChunkMesh removed = byChunk.remove(ChunkPos.pack(chunkX, chunkZ));
        if (removed != null) {
            removed.close();
        }
        if (byChunk.isEmpty()) {
            MESHES.remove(tardisId, byChunk);
        }
    }

    public static void invalidate(UUID tardisId) {
        if (tardisId == null) {
            return;
        }
        Map<Long, ChunkMesh> byChunk = MESHES.remove(tardisId);
        if (byChunk == null) {
            return;
        }
        for (ChunkMesh mesh : byChunk.values()) {
            mesh.close();
        }
        byChunk.clear();
    }

    public static void invalidateAll() {
        for (UUID id : List.copyOf(MESHES.keySet())) {
            invalidate(id);
        }
    }

    /**
     * Test helper: records a chunk as having a drawable mesh without requiring a GPU bake.
     */
    public static void markChunkMeshForTest(UUID tardisId, int chunkX, int chunkZ) {
        if (tardisId == null) {
            return;
        }
        long key = ChunkPos.pack(chunkX, chunkZ);
        Map<Long, ChunkMesh> byChunk = MESHES.computeIfAbsent(tardisId, ignored -> new ConcurrentHashMap<>());
        ChunkMesh previous = byChunk.put(key, ChunkMesh.MARKER);
        if (previous != null && previous != ChunkMesh.MARKER) {
            previous.close();
        }
    }

    /**
     * Draws one terrain pass across every chunk, preserving opaque → cutout → translucent order.
     */
    public static void drawLayer(UUID tardisId, Matrix4f viewMatrix, TerrainPass pass) {
        if (tardisId == null || viewMatrix == null || pass == null) {
            return;
        }
        Map<Long, ChunkMesh> byChunk = MESHES.get(tardisId);
        if (byChunk == null || byChunk.isEmpty()) {
            return;
        }
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(viewMatrix);
        try {
            List<Long> chunkKeys = new ArrayList<>(byChunk.keySet());
            chunkKeys.sort(Long::compare);
            for (Long chunkKey : chunkKeys) {
                ChunkMesh mesh = byChunk.get(chunkKey);
                if (mesh != null) {
                    mesh.draw(pass);
                }
            }
        } finally {
            modelViewStack.popMatrix();
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

        try {
            BlockStateModelSet models = client.getModelManager().getBlockStateModelSet();
            BlockColors blockColors = client.getBlockColors();
            ModelBlockRenderer baker = new ModelBlockRenderer(false, true, blockColors);

            EnumMap<ChunkSectionLayer, ByteBufferBuilder> allocators = new EnumMap<>(ChunkSectionLayer.class);
            EnumMap<ChunkSectionLayer, BufferBuilder> builders = new EnumMap<>(ChunkSectionLayer.class);
            QuadInstance quadInstance = new QuadInstance();
            quadInstance.setLightCoords(FULLBRIGHT);
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

            List<LayerBuffer> uploaded = new ArrayList<>();
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
                    LayerBuffer layerBuffer = uploadLayer(layer, meshData);
                    if (layerBuffer != null) {
                        uploaded.add(layerBuffer);
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

    private static LayerBuffer uploadLayer(ChunkSectionLayer layer, MeshData meshData) {
        MeshData.DrawState drawState = meshData.drawState();
        if (drawState.vertexCount() <= 0 || drawState.indexCount() <= 0) {
            return null;
        }
        ByteBuffer vertexBytes = meshData.vertexBuffer();
        if (vertexBytes == null || !vertexBytes.hasRemaining()) {
            return null;
        }
        GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(
                () -> "dwm_soto_" + layer.label(),
                GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                vertexBytes
        );
        GpuBuffer indexBuffer = null;
        ByteBuffer indexBytes = meshData.indexBuffer();
        if (indexBytes != null && indexBytes.hasRemaining()) {
            indexBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "dwm_soto_idx_" + layer.label(),
                    GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST,
                    indexBytes
            );
        }
        return new LayerBuffer(
                TerrainPass.forSectionLayer(layer),
                layer,
                vertexBuffer,
                indexBuffer,
                drawState.indexType(),
                drawState.indexCount()
        );
    }

    private static final class ChunkMesh implements AutoCloseable {
        static final ChunkMesh EMPTY = new ChunkMesh(List.of(), false);
        /** Non-empty placeholder used by {@link #markChunkMeshForTest} and interim bake. */
        static final ChunkMesh MARKER = new ChunkMesh(List.of(), true);

        private final List<LayerBuffer> layers;
        private final boolean marker;
        private boolean closed;

        private ChunkMesh(List<LayerBuffer> layers) {
            this(layers, false);
        }

        private ChunkMesh(List<LayerBuffer> layers, boolean marker) {
            this.layers = List.copyOf(layers);
            this.marker = marker;
        }

        boolean isEmpty() {
            return !marker && layers.isEmpty();
        }

        int draw(TerrainPass pass) {
            if (closed || isEmpty() || marker || pass == null) {
                return 0;
            }
            int drawn = 0;
            for (LayerBuffer layerBuffer : layers) {
                if (layerBuffer.pass() == pass) {
                    layerBuffer.draw();
                    drawn++;
                }
            }
            return drawn;
        }

        @Override
        public void close() {
            if (closed || this == EMPTY || this == MARKER) {
                return;
            }
            closed = true;
            for (LayerBuffer layerBuffer : layers) {
                layerBuffer.close();
            }
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
