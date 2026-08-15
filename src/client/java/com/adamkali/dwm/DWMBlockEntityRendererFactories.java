package com.adamkali.dwm;

import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.render.FirstDoctorConsoleBlockEntityRenderer;
import com.adamkali.dwm.render.TardisBlockEntityRenderer;
import com.adamkali.dwm.render.TardisDecorBlockEntityRenderer;
import com.adamkali.dwm.render.TardisInteriorDoorBlockEntityRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class DWMBlockEntityRendererFactories {
    public static void initialize() {
        BlockEntityRendererRegistry.register(DWMBlockEntities.TARDIS_BLOCK_ENTITY, TardisBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(
                DWMBlockEntities.TARDIS_INTERIOR_DOOR_BLOCK_ENTITY,
                TardisInteriorDoorBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(
                DWMBlockEntities.FIRST_DOCTOR_CONSOLE_BLOCK_ENTITY,
                FirstDoctorConsoleBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(
                DWMBlockEntities.TARDIS_DECOR_BLOCK_ENTITY,
                TardisDecorBlockEntityRenderer::new);
    }
}
