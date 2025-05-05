package com.adamkali.dwm;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.model.tileentity.*;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.RenderLayer;

public class DWMRenderLayerManager {
    private static void registerBlockRenderLayers() {
        BlockRenderLayerMap.INSTANCE.putBlock(DWMBlocks.WHITE_ROUNDEL_B, RenderLayer.getCutoutMipped());
    }

    private static void registerEntityRenderLayers() {
        EntityModelLayerRegistry.registerModelLayer(TTCapsuleModel.LAYER_LOCATION, TTCapsuleModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(FirstDoctorTardisModel.LAYER_LOCATION, FirstDoctorTardisModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(SecondDoctorTardisModel.LAYER_LOCATION, SecondDoctorTardisModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(ThirdDoctorTardisModel.LAYER_LOCATION, ThirdDoctorTardisModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(FourthDoctorTardisModel.LAYER_LOCATION, FourthDoctorTardisModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(FifthDoctorTardisModel.LAYER_LOCATION, FifthDoctorTardisModel::getTexturedModelData);
    }

    public static void initialize() {
        registerBlockRenderLayers();
        registerEntityRenderLayers();
    }
}
