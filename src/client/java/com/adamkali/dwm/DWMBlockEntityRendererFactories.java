package com.adamkali.dwm;

import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.render.FirstDoctorConsoleBlockEntityRenderer;
import com.adamkali.dwm.render.TardisBlockEntityRenderer;
import com.adamkali.dwm.render.TardisInteriorDoorBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class DWMBlockEntityRendererFactories {
    public static void initialize() {
        BlockEntityRenderers.register(DWMBlockEntities.TARDIS_BLOCK_ENTITY, TardisBlockEntityRenderer::new);
        BlockEntityRenderers.register(
                DWMBlockEntities.TARDIS_INTERIOR_DOOR_BLOCK_ENTITY,
                TardisInteriorDoorBlockEntityRenderer::new);
        BlockEntityRenderers.register(
                DWMBlockEntities.FIRST_DOCTOR_CONSOLE_BLOCK_ENTITY,
                FirstDoctorConsoleBlockEntityRenderer::new);
    }
}
