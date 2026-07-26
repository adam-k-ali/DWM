package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

/**
 * Caches the First Doctor console-room placement list used for exterior BOTI tessellation.
 * Blocks are drawn via {@link BlockRenderManager#renderBlockAsEntity} each frame (cheap for ~11³).
 */
public final class BotiInteriorMeshCache {
    private static int cachedVersion = -1;
    private static Map<BlockPos, BlockState> visibleBlocks = Map.of();

    private BotiInteriorMeshCache() {
    }

    public static Map<BlockPos, BlockState> getVisibleBlocks() {
        if (cachedVersion != FirstDoctorConsoleRoomLayout.LAYOUT_VERSION || visibleBlocks.isEmpty()) {
            visibleBlocks = Map.copyOf(FirstDoctorConsoleRoomLayout.botiVisiblePlacements());
            cachedVersion = FirstDoctorConsoleRoomLayout.LAYOUT_VERSION;
        }
        return visibleBlocks;
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        for (Map.Entry<BlockPos, BlockState> entry : getVisibleBlocks().entrySet()) {
            BlockPos pos = entry.getKey();
            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderManager.renderBlockAsEntity(entry.getValue(), matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }
    }

    /** Test/helper: clear cached placements (e.g. after resource reload). */
    public static void invalidate() {
        cachedVersion = -1;
        visibleBlocks = Map.of();
    }
}
