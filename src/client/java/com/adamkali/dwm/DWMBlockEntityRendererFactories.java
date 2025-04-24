package com.adamkali.dwm;

import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.render.TardisBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class DWMBlockEntityRendererFactories {
    public static void initialize() {
        BlockEntityRendererFactories.register(DWMBlockEntities.TARDIS_BLOCK_ENTITY, TardisBlockEntityRenderer::new);
    }
}
