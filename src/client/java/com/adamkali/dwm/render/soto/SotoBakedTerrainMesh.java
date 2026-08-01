package com.adamkali.dwm.render.soto;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gl.GlUsage;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
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
import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GPU-baked SOTO terrain mesh. Built once per snapshot, drawn each frame.
 */
public final class SotoBakedTerrainMesh implements AutoCloseable {
    public static final SotoBakedTerrainMesh EMPTY = new SotoBakedTerrainMesh(List.of());

    private final List<LayerBuffer> layers;
    private boolean closed;

    private SotoBakedTerrainMesh(List<LayerBuffer> layers) {
        this.layers = List.copyOf(layers);
    }

    public boolean isEmpty() {
        return layers.isEmpty();
    }

    public int layerCount() {
        return layers.size();
    }

    /**
     * Bakes block models (entity-block layers) at the given packed light.
     * Returns {@link #EMPTY} if the client/render pipeline is unavailable or there is no geometry.
     */
    public static SotoBakedTerrainMesh bake(Map<BlockPos, BlockState> blocks, int light) {
        if (blocks == null || blocks.isEmpty()) {
            return EMPTY;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return EMPTY;
        }
        BlockRenderManager blockRenderManager = client.getBlockRenderManager();
        BlockColors blockColors = client.getBlockColors();
        if (blockRenderManager == null || blockColors == null) {
            return EMPTY;
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
            return EMPTY;
        }

        List<LayerBuffer> uploaded = new ArrayList<>(byLayer.size());
        MatrixStack matrices = new MatrixStack();
        try {
            for (Map.Entry<RenderLayer, List<Map.Entry<BlockPos, BlockState>>> layerEntry : byLayer.entrySet()) {
                RenderLayer layer = layerEntry.getKey();
                LayerBuffer layerBuffer = bakeLayer(
                        layer,
                        layerEntry.getValue(),
                        blockRenderManager,
                        blockColors,
                        light,
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
            return EMPTY;
        }
        return uploaded.isEmpty() ? EMPTY : new SotoBakedTerrainMesh(uploaded);
    }

    public void draw(MatrixStack matrices) {
        if (closed || isEmpty()) {
            return;
        }
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.mul(matrices.peek().getPositionMatrix());
        try {
            for (LayerBuffer layerBuffer : layers) {
                if (!layerBuffer.buffer().isClosed()) {
                    layerBuffer.buffer().draw(layerBuffer.layer());
                }
            }
        } finally {
            modelViewStack.popMatrix();
        }
    }

    @Override
    public void close() {
        if (closed || this == EMPTY) {
            return;
        }
        closed = true;
        for (LayerBuffer layerBuffer : layers) {
            layerBuffer.close();
        }
    }

    private static LayerBuffer bakeLayer(
            RenderLayer layer,
            List<Map.Entry<BlockPos, BlockState>> entries,
            BlockRenderManager blockRenderManager,
            BlockColors blockColors,
            int light,
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
                        light,
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

    private record LayerBuffer(RenderLayer layer, VertexBuffer buffer) implements AutoCloseable {
        @Override
        public void close() {
            if (!buffer.isClosed()) {
                buffer.close();
            }
        }
    }
}
