package com.adamkali.dwm;

import com.adamkali.dwm.block.DWMBlocks;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class DWMRenderLayerManager {
    public static void initialize() {
        BlockRenderLayerMap.INSTANCE.putBlock(DWMBlocks.WHITE_ROUNDEL_B, RenderLayer.getCutoutMipped());
    }
}
