package com.adamkali.dwm;

import com.adamkali.dwm.model.tileentity.*;
import com.adamkali.dwm.platform.DwmClientPlatform;
import com.adamkali.dwm.platform.DwmClientServices;

/**
 * Registers entity/block-entity model layers. Terrain cutout/translucent layers are
 * inferred from sprite properties in Minecraft 26.2 (no BlockRenderLayerMap).
 */
public class DWMRenderLayerManager {
    private static void registerEntityRenderLayers() {
        DwmClientPlatform platform = DwmClientServices.get();
        platform.registerModelLayer(TTCapsuleModel.LAYER_LOCATION, TTCapsuleModel::getTexturedModelData);
        platform.registerModelLayer(FirstDoctorTardisModel.LAYER_LOCATION, FirstDoctorTardisModel::getTexturedModelData);
        platform.registerModelLayer(SecondDoctorTardisModel.LAYER_LOCATION, SecondDoctorTardisModel::getTexturedModelData);
        platform.registerModelLayer(ThirdDoctorTardisModel.LAYER_LOCATION, ThirdDoctorTardisModel::getTexturedModelData);
        platform.registerModelLayer(FourthDoctorTardisModel.LAYER_LOCATION, FourthDoctorTardisModel::getTexturedModelData);
        platform.registerModelLayer(FifthDoctorTardisModel.LAYER_LOCATION, FifthDoctorTardisModel::getTexturedModelData);
        platform.registerModelLayer(SixthDoctorTardisModel.LAYER_LOCATION, SixthDoctorTardisModel::getTexturedModelData);
        platform.registerModelLayer(SeventhDoctorTardisModel.LAYER_LOCATION, SeventhDoctorTardisModel::getTexturedModelData);
        platform.registerModelLayer(
                TardisClassicInteriorDoorModel.LAYER_LOCATION,
                TardisClassicInteriorDoorModel::getTexturedModelData);
        platform.registerModelLayer(
                FirstDoctorConsoleModel.LAYER_LOCATION,
                FirstDoctorConsoleModel::getTexturedModelData);
        platform.registerModelLayer(
                BiomeSelectorModel.LAYER_LOCATION,
                BiomeSelectorModel::getTexturedModelData);
        platform.registerModelLayer(
                PlanetLocatorModel.LAYER_LOCATION,
                PlanetLocatorModel::getTexturedModelData);
        platform.registerModelLayer(
                WaypointSelectorModel.LAYER_LOCATION,
                WaypointSelectorModel::getTexturedModelData);
        platform.registerModelLayer(
                PlayerLocatorModel.LAYER_LOCATION,
                PlayerLocatorModel::getTexturedModelData);
        platform.registerModelLayer(
                ChameleonCircuitModel.LAYER_LOCATION,
                ChameleonCircuitModel::getTexturedModelData);
        platform.registerModelLayer(
                MaterialisationLeverModel.LAYER_LOCATION,
                MaterialisationLeverModel::getTexturedModelData);
        platform.registerModelLayer(
                FastReturnModel.LAYER_LOCATION,
                FastReturnModel::getTexturedModelData);
        platform.registerModelLayer(
                StabilisersModel.LAYER_LOCATION,
                StabilisersModel::getTexturedModelData);
        platform.registerModelLayer(
                ReaderModel.LAYER_LOCATION,
                ReaderModel::getTexturedModelData);
        platform.registerModelLayer(
                RadiationReaderModel.LAYER_LOCATION,
                RadiationReaderModel::getTexturedModelData);
        platform.registerModelLayer(
                CloakLeverModel.LAYER_LOCATION,
                CloakLeverModel::getTexturedModelData);
        platform.registerModelLayer(
                DoorLockModel.LAYER_LOCATION,
                DoorLockModel::getTexturedModelData);
        platform.registerModelLayer(
                TelepathicCircuitModel.LAYER_LOCATION,
                TelepathicCircuitModel::getTexturedModelData);
        platform.registerModelLayer(
                CoordinateLockModel.LAYER_LOCATION,
                CoordinateLockModel::getTexturedModelData);
        platform.registerModelLayer(
                TardisGlobeModel.LAYER_LOCATION,
                TardisGlobeModel::getTexturedModelData);
        platform.registerModelLayer(
                TardisCompactScannerModel.LAYER_LOCATION,
                TardisCompactScannerModel::getTexturedModelData);
        platform.registerModelLayer(
                TardisFullScannerModel.LAYER_LOCATION,
                TardisFullScannerModel::getTexturedModelData);
    }

    public static void initialize() {
        registerEntityRenderLayers();
    }
}
