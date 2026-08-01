package com.adamkali.dwm.render.soto.ghost;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gl.GlUsage;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 2: per-chunk GPU meshes baked from {@link SotoGhostExterior} on chunk apply.
 */
public final class SotoGhostMeshCache {
    private static final int FULLBRIGHT = LightmapTextureManager.pack(15, 15);

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
        long key = ChunkPos.toLong(chunkX, chunkZ);
        Map<BlockPos, BlockState> blocks = ghost.blocksInChunk(key);
        ChunkMesh baked = bakeChunk(blocks);
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
        ChunkMesh removed = byChunk.remove(ChunkPos.toLong(chunkX, chunkZ));
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
        long key = ChunkPos.toLong(chunkX, chunkZ);
        Map<Long, ChunkMesh> byChunk = MESHES.computeIfAbsent(tardisId, ignored -> new ConcurrentHashMap<>());
        ChunkMesh previous = byChunk.put(key, ChunkMesh.MARKER);
        if (previous != null && previous != ChunkMesh.MARKER) {
            previous.close();
        }
    }

    public static void draw(UUID tardisId, MatrixStack matrices) {
        if (tardisId == null || matrices == null) {
            return;
        }
        Map<Long, ChunkMesh> byChunk = MESHES.get(tardisId);
        if (byChunk == null || byChunk.isEmpty()) {
            return;
        }
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.mul(matrices.peek().getPositionMatrix());
        try {
            for (ChunkMesh mesh : byChunk.values()) {
                mesh.draw();
            }
        } finally {
            modelViewStack.popMatrix();
        }
    }

    private static ChunkMesh bakeChunk(Map<BlockPos, BlockState> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return ChunkMesh.EMPTY;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return ChunkMesh.EMPTY;
        }
        BlockRenderManager blockRenderManager = client.getBlockRenderManager();
        BlockColors blockColors = client.getBlockColors();
        if (blockRenderManager == null || blockColors == null) {
            return ChunkMesh.EMPTY;
        }

        Map<RenderLayer, List<Map.Entry<BlockPos, BlockState>>> byLayer = new HashMap<>();
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockState state = entry.getValue();
            if (state == null || state.getRenderType() == BlockRenderType.INVISIBLE) {
                continue;
            }
            RenderLayer layer = RenderLayers.getEntityBlockLayer(state);
            byLayer.computeIfAbsent(layer, ignored -> new ArrayList<>()).add(entry);
        }
        if (byLayer.isEmpty()) {
            return ChunkMesh.EMPTY;
        }

        List<LayerBuffer> uploaded = new ArrayList<>(byLayer.size());
        MatrixStack matrices = new MatrixStack();
        try {
            for (Map.Entry<RenderLayer, List<Map.Entry<BlockPos, BlockState>>> layerEntry : byLayer.entrySet()) {
                LayerBuffer layerBuffer = bakeLayer(
                        layerEntry.getKey(),
                        layerEntry.getValue(),
                        blockRenderManager,
                        blockColors,
                        matrices
                );
                if (layerBuffer != null) {
                    uploaded.add(layerBuffer);
                }
            }
        } catch (RuntimeException e) {
            for (LayerBuffer layerBuffer : uploaded) {
                layerBuffer.close();
            }
            return ChunkMesh.EMPTY;
        }
        return uploaded.isEmpty() ? ChunkMesh.EMPTY : new ChunkMesh(uploaded);
    }

    private static LayerBuffer bakeLayer(
            RenderLayer layer,
            List<Map.Entry<BlockPos, BlockState>> entries,
            BlockRenderManager blockRenderManager,
            BlockColors blockColors,
            MatrixStack matrices
    ) {
        BufferAllocator allocator = new BufferAllocator(Math.max(256 * 1024, entries.size() * 512));
        BufferBuilder bufferBuilder = new BufferBuilder(
                allocator,
                VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        );
        boolean wrote = false;
        try {
            for (Map.Entry<BlockPos, BlockState> entry : entries) {
                BlockPos pos = entry.getKey();
                BlockState state = entry.getValue();
                BakedModel model = blockRenderManager.getModel(state);
                int color = blockColors.getColor(state, null, null, 0);
                float red = (float) (color >> 16 & 0xFF) / 255.0F;
                float green = (float) (color >> 8 & 0xFF) / 255.0F;
                float blue = (float) (color & 0xFF) / 255.0F;

                matrices.push();
                matrices.translate(pos.getX(), pos.getY(), pos.getZ());
                blockRenderManager.getModelRenderer().render(
                        matrices.peek(),
                        bufferBuilder,
                        state,
                        model,
                        red,
                        green,
                        blue,
                        FULLBRIGHT,
                        OverlayTexture.DEFAULT_UV
                );
                matrices.pop();
                wrote = true;
            }
            if (!wrote) {
                return null;
            }
            BuiltBuffer built = bufferBuilder.endNullable();
            if (built == null) {
                return null;
            }
            VertexBuffer vertexBuffer = new VertexBuffer(GlUsage.STATIC_WRITE);
            vertexBuffer.bind();
            vertexBuffer.upload(built);
            VertexBuffer.unbind();
            return new LayerBuffer(layer, vertexBuffer);
        } catch (RuntimeException e) {
            return null;
        } finally {
            allocator.close();
        }
    }

    private static final class ChunkMesh implements AutoCloseable {
        static final ChunkMesh EMPTY = new ChunkMesh(List.of(), false);
        /** Non-empty placeholder used by {@link #markChunkMeshForTest}. */
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

        void draw() {
            if (closed || isEmpty() || marker) {
                return;
            }
            for (LayerBuffer layerBuffer : layers) {
                if (!layerBuffer.buffer().isClosed()) {
                    layerBuffer.buffer().draw(layerBuffer.layer());
                }
            }
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

    private record LayerBuffer(RenderLayer layer, VertexBuffer buffer) implements AutoCloseable {
        @Override
        public void close() {
            if (!buffer.isClosed()) {
                buffer.close();
            }
        }
    }
}
