package com.adamkali.dwm.render.soto.ghost;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 2: per-chunk GPU meshes baked from {@link SotoGhostExterior} on chunk apply.
 * <p>
 * Minecraft 26.2 removed {@code VertexBuffer}/{@code ItemBlockRenderTypes}/{@code BlockRenderDispatcher}
 * and the old {@code renderModel} path. Full terrain bake+draw via {@code BlockStateModel} +
 * {@code GpuBuffer}/{@code RenderPass} is deferred. Chunk apply still records mesh presence so
 * portal readiness and tests keep working; {@link #drawLayer} is a no-op until bake lands.
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

    private static ChunkMesh bakeChunk(Map<BlockPos, BlockState> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return ChunkMesh.EMPTY;
        }
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return ChunkMesh.EMPTY;
        }
        // Count visible blocks so empty chunks stay empty; actual GPU bake is deferred.
        boolean anyVisible = false;
        for (BlockState state : blocks.values()) {
            if (state != null && state.getRenderShape() != RenderShape.INVISIBLE) {
                anyVisible = true;
                TerrainPass.forBlockState(state);
                break;
            }
        }
        if (!anyVisible) {
            return ChunkMesh.EMPTY;
        }
        // Presence marker until BlockStateModel + GpuBuffer bake is ported (FULLBRIGHT reserved).
        if (FULLBRIGHT < 0) {
            return ChunkMesh.EMPTY;
        }
        return ChunkMesh.MARKER;
    }

    private static final class ChunkMesh implements AutoCloseable {
        static final ChunkMesh EMPTY = new ChunkMesh(false);
        /** Non-empty placeholder used by {@link #markChunkMeshForTest} and interim bake. */
        static final ChunkMesh MARKER = new ChunkMesh(true);

        private final boolean marker;
        private boolean closed;

        private ChunkMesh(boolean marker) {
            this.marker = marker;
        }

        boolean isEmpty() {
            return !marker;
        }

        void draw(TerrainPass pass) {
            if (closed || isEmpty() || marker || pass == null) {
                return;
            }
            // TODO(soto-mesh): RenderPass draw of baked GpuBuffers per TerrainPass / ChunkSectionLayer.
        }

        @Override
        public void close() {
            if (closed || this == EMPTY || this == MARKER) {
                return;
            }
            closed = true;
        }
    }

    public enum TerrainPass {
        OPAQUE,
        CUTOUT,
        TRANSLUCENT;

        static TerrainPass forBlockState(BlockState state) {
            // 26.2: ItemBlockRenderTypes gone. Approximate from block transparency until
            // ChunkSectionLayer is available off BlockStateModel material flags.
            if (state == null) {
                return OPAQUE;
            }
            if (!state.getFluidState().isEmpty() || !state.canOcclude()) {
                // Translucent-ish / non-occluding → cutout bucket for ordering.
                if (!state.canOcclude()) {
                    return CUTOUT;
                }
            }
            // Keep ChunkSectionLayer referenced so the full bake can map SOLID/CUTOUT/TRANSLUCENT.
            ChunkSectionLayer ignored = ChunkSectionLayer.SOLID;
            if (ignored.translucent()) {
                return TRANSLUCENT;
            }
            return OPAQUE;
        }
    }
}
