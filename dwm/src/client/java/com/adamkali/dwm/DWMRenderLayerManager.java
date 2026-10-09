package com.adamkali.dwm;

import com.adamkali.dwm.model.item.RadiationMeterModel;
import com.adamkali.dwm.model.json.JsonEntityModelLayers;
import com.adamkali.dwm.model.tileentity.*;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

/**
 * Registers entity/block-entity model layers. Terrain cutout/translucent layers are
 * inferred from sprite properties in Minecraft 26.2 (no BlockRenderLayerMap).
 */
public class DWMRenderLayerManager {
    private static void registerEntityRenderLayers() {
        JsonEntityModelLayers.register(TTCapsuleModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(FirstDoctorTardisModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(SecondDoctorTardisModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(ThirdDoctorTardisModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(FourthDoctorTardisModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(FifthDoctorTardisModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(SixthDoctorTardisModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(SeventhDoctorTardisModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(TardisClassicInteriorDoorModel.LAYER_LOCATION);
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
        JsonEntityModelLayers.register(StabilisersModel.LAYER_LOCATION);
        ModelLayerRegistry.registerModelLayer(
                ReaderModel.LAYER_LOCATION,
                ReaderModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                RadiationReaderModel.LAYER_LOCATION,
                RadiationReaderModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                CloakLeverModel.LAYER_LOCATION,
                CloakLeverModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                DoorLockModel.LAYER_LOCATION,
                DoorLockModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                TelepathicCircuitModel.LAYER_LOCATION,
                TelepathicCircuitModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                CoordinateLockModel.LAYER_LOCATION,
                CoordinateLockModel::getTexturedModelData);
        JsonEntityModelLayers.register(TardisGlobeModel.LAYER_LOCATION);
        ModelLayerRegistry.registerModelLayer(
                TardisCompactScannerModel.LAYER_LOCATION,
                TardisCompactScannerModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                TardisFullScannerModel.LAYER_LOCATION,
                TardisFullScannerModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                RadiationMeterModel.LAYER_LOCATION,
                RadiationMeterModel::getTexturedModelData);
    }

    public static void initialize() {
        registerEntityRenderLayers();
    }
}
