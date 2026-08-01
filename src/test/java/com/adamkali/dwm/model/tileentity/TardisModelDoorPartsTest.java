package com.adamkali.dwm.model.tileentity;

import net.minecraft.client.model.ModelPart;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisModelDoorPartsTest {

    @Test
    void firstDoctor_resolvesRootLeftAndRightDoors() {
        ModelPart root = FirstDoctorTardisModel.getTexturedModelData().createModel();
        FirstDoctorTardisModel model = new FirstDoctorTardisModel(root);

        List<ModelPart> doors = model.getDoorParts();

        assertEquals(2, doors.size());
        assertTrue(doors.contains(root.getChild("LeftDoor")));
        assertTrue(doors.contains(root.getChild("rightDoor")));
    }

    @Test
    void ttCapsule_resolvesBoneDoor() {
        ModelPart root = TTCapsuleModel.getTexturedModelData().createModel();
        TTCapsuleModel model = new TTCapsuleModel(root);

        List<ModelPart> doors = model.getDoorParts();

        assertEquals(1, doors.size());
        assertTrue(doors.contains(root.getChild("bone").getChild("door")));
    }

    @Test
    void secondDoctor_resolvesMainNestedDoors() {
        ModelPart root = SecondDoctorTardisModel.getTexturedModelData().createModel();
        SecondDoctorTardisModel model = new SecondDoctorTardisModel(root);

        List<ModelPart> doors = model.getDoorParts();
        ModelPart main = root.getChild("Main");

        assertEquals(2, doors.size());
        assertTrue(doors.contains(main.getChild("LeftDoor")));
        assertTrue(doors.contains(main.getChild("Door2")));
    }
}
