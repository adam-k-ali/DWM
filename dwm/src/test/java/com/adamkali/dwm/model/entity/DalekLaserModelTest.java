package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.model.json.EntityModelJson;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekLaserModelTest {
    @Test
    void bodyLayerHasBolt() {
        ModelPart root = EntityModelJson.loadClasspath(DalekLaserModel.LAYER_LOCATION.model()).bakeRoot();
        assertTrue(root.hasChild("bolt"));
    }

    @Test
    void layerLocationUsesDalekLaserId() {
        assertEquals("dalek_laser", DalekLaserModel.LAYER_LOCATION.model().getPath());
        assertEquals("main", DalekLaserModel.LAYER_LOCATION.layer());
    }
}
