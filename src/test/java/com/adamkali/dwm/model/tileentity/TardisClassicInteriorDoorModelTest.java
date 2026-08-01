package com.adamkali.dwm.model.tileentity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisClassicInteriorDoorModelTest {
    private static final float EPSILON = 1e-4f;

    @Test
    void swingAngles_sameLocalSignAndMatchExteriorMagnitude() {
        assertEquals(0.0f, TardisClassicInteriorDoorModel.door1Yaw(0.0f), EPSILON);
        assertEquals(0.0f, TardisClassicInteriorDoorModel.door2Yaw(0.0f), EPSILON);

        float expected = (float) (3.0 * Math.PI / 4.0);
        assertEquals(-expected, TardisClassicInteriorDoorModel.door1Yaw(1.0f), EPSILON);
        assertEquals(-expected, TardisClassicInteriorDoorModel.door2Yaw(1.0f), EPSILON);
        assertTrue(TardisClassicInteriorDoorModel.door1Yaw(0.5f) < 0.0f);
        assertTrue(TardisClassicInteriorDoorModel.door2Yaw(0.5f) < 0.0f);
    }

    @Test
    void texturedModelData_hasDoorHierarchy() {
        ModelPart root = TardisClassicInteriorDoorModel.getTexturedModelData().createModel();
        assertTrue(root.hasChild("frame"));
        assertTrue(root.getChild("frame").hasChild("Door1"));
        assertTrue(root.hasChild("frame2"));
        assertTrue(root.getChild("frame2").hasChild("Door2"));
        assertTrue(root.getChild("frame2").getChild("Door2").hasChild("bone"));
        assertTrue(root.hasChild("jambs"));
    }

    @Test
    void renderShell_hidesDoorsDuringRenderThenRestoresVisibility() {
        ModelPart root = TardisClassicInteriorDoorModel.getTexturedModelData().createModel();
        TardisClassicInteriorDoorModel model = new TardisClassicInteriorDoorModel(root);
        List<ModelPart> doors = model.getDoorParts();
        assertEquals(2, doors.size());

        boolean[] sawHidden = {false};
        VertexConsumer vertices = Mockito.mock(VertexConsumer.class, invocation -> {
            for (ModelPart door : doors) {
                assertFalse(door.visible, "doors must be hidden while shell renders");
            }
            sawHidden[0] = true;
            Class<?> returnType = invocation.getMethod().getReturnType();
            if (returnType.isInstance(invocation.getMock()) || returnType.equals(VertexConsumer.class)) {
                return invocation.getMock();
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        });

        model.renderShell(new MatrixStack(), vertices, 0, 0);

        assertTrue(sawHidden[0], "shell render should write geometry with doors hidden");
        for (ModelPart door : doors) {
            assertTrue(door.visible, "renderShell must restore door visibility in finally");
        }
    }

    @Test
    void getDoorParts_areNestedUnderFrames() {
        ModelPart root = TardisClassicInteriorDoorModel.getTexturedModelData().createModel();
        TardisClassicInteriorDoorModel model = new TardisClassicInteriorDoorModel(root);
        List<ModelPart> doors = model.getDoorParts();
        assertEquals(root.getChild("frame").getChild("Door1"), doors.get(0));
        assertEquals(root.getChild("frame2").getChild("Door2"), doors.get(1));
        assertFalse(doors.contains(root.getChild("jambs")));
    }
}
