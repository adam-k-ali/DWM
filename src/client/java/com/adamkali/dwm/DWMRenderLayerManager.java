package com.adamkali.dwm;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.model.tileentity.*;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.RenderLayer;

public class DWMRenderLayerManager {
    private static void registerBlockRenderLayers() {
        BlockRenderLayerMap.INSTANCE.putBlock(DWMBlocks.WHITE_ROUNDEL_B, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(DWMBlocks.ASH_LEAVES, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(DWMBlocks.ASH_SAPLING, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DWMBlocks.POTTED_ASH_SAPLING, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DWMBlocks.DARK_ASH_LEAVES, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(DWMBlocks.DARK_ASH_SAPLING, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DWMBlocks.POTTED_DARK_ASH_SAPLING, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DWMBlocks.DARK_ASH_TRAPDOOR, RenderLayer.getCutout());
    }

    private static void registerEntityRenderLayers() {
        EntityModelLayerRegistry.registerModelLayer(TTCapsuleModel.LAYER_LOCATION, TTCapsuleModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(FirstDoctorTardisModel.LAYER_LOCATION, FirstDoctorTardisModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(SecondDoctorTardisModel.LAYER_LOCATION, SecondDoctorTardisModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(ThirdDoctorTardisModel.LAYER_LOCATION, ThirdDoctorTardisModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(FourthDoctorTardisModel.LAYER_LOCATION, FourthDoctorTardisModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(FifthDoctorTardisModel.LAYER_LOCATION, FifthDoctorTardisModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(SixthDoctorTardisModel.LAYER_LOCATION, SixthDoctorTardisModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(SeventhDoctorTardisModel.LAYER_LOCATION, SeventhDoctorTardisModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(
                TardisClassicInteriorDoorModel.LAYER_LOCATION,
                TardisClassicInteriorDoorModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(
                FirstDoctorConsoleModel.LAYER_LOCATION,
                FirstDoctorConsoleModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(
                BiomeSelectorModel.LAYER_LOCATION,
                BiomeSelectorModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(
                PlanetLocatorModel.LAYER_LOCATION,
                PlanetLocatorModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(
                MaterialisationLeverModel.LAYER_LOCATION,
                MaterialisationLeverModel::getTexturedModelData);
    }

    public static void initialize() {
        registerBlockRenderLayers();
        registerEntityRenderLayers();
    }
}
