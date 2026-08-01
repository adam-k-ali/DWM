package com.adamkali.dwm.model.tileentity;

import net.minecraft.client.model.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisClassicInteriorDoorModelTest {
    private static final float EPSILON = 1e-4f;

    @Test
    void swingAngles_oppositeSignsAndMatchExteriorMagnitude() {
        assertEquals(0.0f, TardisClassicInteriorDoorModel.door1Yaw(0.0f), EPSILON);
        assertEquals(0.0f, TardisClassicInteriorDoorModel.door2Yaw(0.0f), EPSILON);

        float expected = (float) Math.PI / 3.0f;
        assertEquals(-expected, TardisClassicInteriorDoorModel.door1Yaw(1.0f), EPSILON);
        assertEquals(expected, TardisClassicInteriorDoorModel.door2Yaw(1.0f), EPSILON);
        assertTrue(TardisClassicInteriorDoorModel.door1Yaw(0.5f) < 0.0f);
        assertTrue(TardisClassicInteriorDoorModel.door2Yaw(0.5f) > 0.0f);
    }

    @Test
    void texturedModelData_hasDoorHierarchy() {
        ModelPart root = TardisClassicInteriorDoorModel.getTexturedModelData().createModel();
        assertTrue(root.hasChild("frame"));
        assertTrue(root.getChild("frame").hasChild("Door1"));
        assertTrue(root.hasChild("frame2"));
        assertTrue(root.getChild("frame2").hasChild("Door2"));
        assertTrue(root.getChild("frame2").getChild("Door2").hasChild("bone"));
    }
}
