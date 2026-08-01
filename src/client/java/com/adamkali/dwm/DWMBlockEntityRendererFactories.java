package com.adamkali.dwm;

import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.render.TardisBlockEntityRenderer;
import com.adamkali.dwm.render.TardisInteriorDoorBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class DWMBlockEntityRendererFactories {
    public static void initialize() {
        BlockEntityRendererFactories.register(DWMBlockEntities.TARDIS_BLOCK_ENTITY, TardisBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(
                DWMBlockEntities.TARDIS_INTERIOR_DOOR_BLOCK_ENTITY,
                TardisInteriorDoorBlockEntityRenderer::new);
    }
}
