package com.adamkali.dwm;

import com.adamkali.dwm.model.item.RadiationMeterModel;
import com.adamkali.dwm.model.json.JsonEntityModelLayers;
import com.adamkali.dwm.model.tileentity.*;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;

/**
 * Registers entity/block-entity model layers. Terrain cutout/translucent layers are
 * inferred from sprite properties in Minecraft 26.2 (no BlockRenderLayerMap).
 */
public class DWMRenderLayerManager {
    private static void registerEntityRenderLayers() {
        for (TardisChameleonVariant variant : TardisChameleonVariant.values()) {
            JsonEntityModelLayers.register(TardisModels.layer(variant));
        }
        JsonEntityModelLayers.register(TardisClassicInteriorDoorModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(FirstDoctorConsoleModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(BiomeSelectorModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(PlanetLocatorModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(WaypointSelectorModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(PlayerLocatorModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(ChameleonCircuitModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(MaterialisationLeverModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(FastReturnModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(StabilisersModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(ReaderModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(RadiationReaderModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(CloakLeverModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(DoorLockModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(TelepathicCircuitModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(CoordinateLockModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(TardisGlobeModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(TardisCompactScannerModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(TardisFullScannerModel.LAYER_LOCATION);
        JsonEntityModelLayers.register(RadiationMeterModel.LAYER_LOCATION);
    }

    public static void initialize() {
        registerEntityRenderLayers();
    }
}
