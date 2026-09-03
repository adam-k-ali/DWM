package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.entity.DalekFlightFx;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekModelTest {
    @Test
    void bodyLayerHasChassisHierarchy() {
        ModelPart root = DalekModel.createBodyLayer().bakeRoot();
        assertTrue(root.hasChild("skirt"));
        assertTrue(root.hasChild("shoulders"));
        assertTrue(root.getChild("shoulders").hasChild("gun"));
        assertTrue(root.hasChild("head"));
        assertTrue(root.getChild("head").hasChild("eyestalk"));
    }

    @Test
    void layerLocationUsesDalekId() {
        assertEquals("dalek", DalekModel.LAYER_LOCATION.model().getPath());
        assertEquals("main", DalekModel.LAYER_LOCATION.layer());
    }

    @Test
    void bobOffsetIsZeroWhenGrounded() {
        assertEquals(0.0F, DalekModel.bobOffset(12.5F, false), 1.0e-4F);
    }

    @Test
    void bobOffsetOscillatesWithinAmplitudeWhenFlying() {
        float amplitude = DalekFlightFx.BOB_AMPLITUDE;
        for (int i = 0; i <= 40; i++) {
            float offset = DalekModel.bobOffset(i, true);
            assertTrue(offset <= amplitude + 1.0e-3F, "offset above amplitude: " + offset);
            assertTrue(offset >= -amplitude - 1.0e-3F, "offset below amplitude: " + offset);
        }
        float trough = DalekModel.bobOffset((float) ((3.0 * Math.PI / 2.0) / DalekFlightFx.BOB_SPEED), true);
        assertEquals(-DalekFlightFx.BOB_AMPLITUDE, trough, 0.02F);
    }
}
