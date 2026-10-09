package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.model.json.EntityModelJson;
import org.junit.jupiter.api.Test;

import java.util.List;
import net.minecraft.client.model.geom.ModelPart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisModelDoorPartsTest {

    @Test
    void firstDoctor_resolvesRootLeftAndRightDoors() {
        ModelPart root = EntityModelJson.loadClasspath(FirstDoctorTardisModel.LAYER_LOCATION.model()).bakeRoot();
        FirstDoctorTardisModel model = new FirstDoctorTardisModel(root);

        List<ModelPart> doors = model.getDoorParts();

        assertEquals(2, doors.size());
        assertTrue(doors.contains(root.getChild("LeftDoor")));
        assertTrue(doors.contains(root.getChild("rightDoor")));
    }

    @Test
    void ttCapsule_resolvesBoneDoor() {
        ModelPart root = EntityModelJson.loadClasspath(TTCapsuleModel.LAYER_LOCATION.model()).bakeRoot();
        TTCapsuleModel model = new TTCapsuleModel(root);

        List<ModelPart> doors = model.getDoorParts();

        assertEquals(1, doors.size());
        assertTrue(doors.contains(root.getChild("bone").getChild("door")));
    }

    @Test
    void secondDoctor_resolvesMainNestedDoors() {
        ModelPart root = EntityModelJson.loadClasspath(SecondDoctorTardisModel.LAYER_LOCATION.model()).bakeRoot();
        SecondDoctorTardisModel model = new SecondDoctorTardisModel(root);

        List<ModelPart> doors = model.getDoorParts();
        ModelPart main = root.getChild("Main");

        assertEquals(2, doors.size());
        assertTrue(doors.contains(main.getChild("LeftDoor")));
        assertTrue(doors.contains(main.getChild("Door2")));
    }
}
