package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.model.json.TestModelLoading;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisModelDoorPartsTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    private static ModelPart bake(ModelLayerLocation layer) {
        // Registers the JSON animation for the layer, as the model reload does on the client.
        return TestModelLoading.bake(layer);
    }

    @Test
    void firstDoctor_resolvesRootLeftAndRightDoors() {
        ModelLayerLocation layer = TardisModels.layer(TardisChameleonVariant.FIRST_DOCTOR_BOX);
        ModelPart root = bake(layer);
        TardisModel model = new TardisModel(root, layer);

        List<ModelPart> doors = model.getDoorParts();

        assertEquals(2, doors.size());
        assertTrue(doors.contains(root.getChild("LeftDoor")));
        assertTrue(doors.contains(root.getChild("rightDoor")));
    }

    @Test
    void ttCapsule_resolvesBoneDoor() {
        ModelLayerLocation layer = TardisModels.layer(TardisChameleonVariant.TT_CAPSULE);
        ModelPart root = bake(layer);
        TardisModel model = new TardisModel(root, layer);

        List<ModelPart> doors = model.getDoorParts();

        assertEquals(1, doors.size());
        assertTrue(doors.contains(root.getChild("bone").getChild("door")));
    }

    @Test
    void secondDoctor_resolvesMainNestedDoors() {
        ModelLayerLocation layer = TardisModels.layer(TardisChameleonVariant.SECOND_DOCTOR_BOX);
        ModelPart root = bake(layer);
        TardisModel model = new TardisModel(root, layer);

        List<ModelPart> doors = model.getDoorParts();
        ModelPart main = root.getChild("Main");

        assertEquals(2, doors.size());
        assertTrue(doors.contains(main.getChild("LeftDoor")));
        assertTrue(doors.contains(main.getChild("Door2")));
    }

    @Test
    void everyVariantSwingsItsDoorLikeTheOldJavaAnimation() {
        for (TardisChameleonVariant variant : TardisChameleonVariant.values()) {
            ModelLayerLocation layer = TardisModels.layer(variant);
            ModelPart root = bake(layer);
            TardisModel model = new TardisModel(root, layer);
            ModelPart swinging = variant == TardisChameleonVariant.TT_CAPSULE
                    ? root.getChild("bone").getChild("door")
                    : (root.hasChild("LeftDoor") ? root.getChild("LeftDoor") : root.getChild("Main").getChild("LeftDoor"));
            // Old Java: LeftDoor yaw = progress * PI / 3, TT Capsule door yaw = progress * PI / 2.
            float maxYaw = variant == TardisChameleonVariant.TT_CAPSULE ? (float) Math.PI / 2 : (float) Math.PI / 3;
            for (float progress : new float[]{0.0F, 0.25F, 0.5F, 1.0F}) {
                TardisRenderState state = new TardisRenderState();
                state.setDoorSwingProgress(progress);
                model.setupAnim(state);
                assertEquals(progress * maxYaw, swinging.yRot, 1.0e-5F, variant + " at " + progress);
                assertEquals(0.0F, swinging.xRot, 1.0e-6F);
            }
        }
    }

    @Test
    void rightDoorStaysClosedWhenOpening() {
        ModelLayerLocation layer = TardisModels.layer(TardisChameleonVariant.FIRST_DOCTOR_BOX);
        ModelPart root = bake(layer);
        TardisRenderState state = new TardisRenderState();
        state.setDoorSwingProgress(1.0F);
        new TardisModel(root, layer).setupAnim(state);
        assertEquals(0.0F, root.getChild("rightDoor").yRot, 1.0e-6F);
    }
}
