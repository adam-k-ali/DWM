package com.adamkali.dwm;

import com.adamkali.dwm.block.entities.DWMBlockEntities;
import com.adamkali.dwm.platform.DwmClientPlatform;
import com.adamkali.dwm.platform.DwmClientServices;
import com.adamkali.dwm.render.FirstDoctorConsoleBlockEntityRenderer;
import com.adamkali.dwm.render.TardisBlockEntityRenderer;
import com.adamkali.dwm.render.TardisDecorBlockEntityRenderer;
import com.adamkali.dwm.render.TardisInteriorDoorBlockEntityRenderer;

public class DWMBlockEntityRendererFactories {
    public static void initialize() {
        DwmClientPlatform platform = DwmClientServices.get();
        platform.registerBlockEntityRenderer(DWMBlockEntities.TARDIS_BLOCK_ENTITY, TardisBlockEntityRenderer::new);
        platform.registerBlockEntityRenderer(
                DWMBlockEntities.TARDIS_INTERIOR_DOOR_BLOCK_ENTITY,
                TardisInteriorDoorBlockEntityRenderer::new);
        platform.registerBlockEntityRenderer(
                DWMBlockEntities.FIRST_DOCTOR_CONSOLE_BLOCK_ENTITY,
                FirstDoctorConsoleBlockEntityRenderer::new);
        platform.registerBlockEntityRenderer(
                DWMBlockEntities.TARDIS_DECOR_BLOCK_ENTITY,
                TardisDecorBlockEntityRenderer::new);
    }
}
