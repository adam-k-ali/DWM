package com.adamkali.dwm;

import com.adamkali.dwm.model.tileentity.*;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

/**
 * Registers entity/block-entity model layers. Terrain cutout/translucent layers are
 * inferred from sprite properties in Minecraft 26.2 (no BlockRenderLayerMap).
 */
public class DWMRenderLayerManager {
    private static void registerEntityRenderLayers() {
        ModelLayerRegistry.registerModelLayer(TTCapsuleModel.LAYER_LOCATION, TTCapsuleModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(FirstDoctorTardisModel.LAYER_LOCATION, FirstDoctorTardisModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(SecondDoctorTardisModel.LAYER_LOCATION, SecondDoctorTardisModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(ThirdDoctorTardisModel.LAYER_LOCATION, ThirdDoctorTardisModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(FourthDoctorTardisModel.LAYER_LOCATION, FourthDoctorTardisModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(FifthDoctorTardisModel.LAYER_LOCATION, FifthDoctorTardisModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(SixthDoctorTardisModel.LAYER_LOCATION, SixthDoctorTardisModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(SeventhDoctorTardisModel.LAYER_LOCATION, SeventhDoctorTardisModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                TardisClassicInteriorDoorModel.LAYER_LOCATION,
                TardisClassicInteriorDoorModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                FirstDoctorConsoleModel.LAYER_LOCATION,
                FirstDoctorConsoleModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                BiomeSelectorModel.LAYER_LOCATION,
                BiomeSelectorModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                PlanetLocatorModel.LAYER_LOCATION,
                PlanetLocatorModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                WaypointSelectorModel.LAYER_LOCATION,
                WaypointSelectorModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                PlayerLocatorModel.LAYER_LOCATION,
                PlayerLocatorModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                ChameleonCircuitModel.LAYER_LOCATION,
                ChameleonCircuitModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                MaterialisationLeverModel.LAYER_LOCATION,
                MaterialisationLeverModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                FastReturnModel.LAYER_LOCATION,
                FastReturnModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                StabilisersModel.LAYER_LOCATION,
                StabilisersModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                TardisGlobeModel.LAYER_LOCATION,
                TardisGlobeModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                TardisCompactScannerModel.LAYER_LOCATION,
                TardisCompactScannerModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                TardisFullScannerModel.LAYER_LOCATION,
                TardisFullScannerModel::getTexturedModelData);
    }

    public static void initialize() {
        registerEntityRenderLayers();
    }
}
