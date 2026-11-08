package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.model.json.TestModelLoading;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.minecraft.client.model.geom.ModelPart;

class MaterialisationLeverModelTest {
    @Test
    void texturedModelData_createsRootWithPanelAndLever() {
        ModelPart root = assertDoesNotThrow(() -> TestModelLoading.bake(MaterialisationLeverModel.LAYER_LOCATION));
        ModelPart demat = root.getChild("demat");
        assertNotNull(demat);
        assertNotNull(demat.getChild("panel"));
        assertNotNull(demat.getChild("lever"));
    }
}
