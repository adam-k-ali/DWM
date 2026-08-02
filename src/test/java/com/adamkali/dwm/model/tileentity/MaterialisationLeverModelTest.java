package com.adamkali.dwm.model.tileentity;

import net.minecraft.client.model.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MaterialisationLeverModelTest {
    @Test
    void texturedModelData_createsRootWithPanelAndLever() {
        ModelPart root = assertDoesNotThrow(() -> MaterialisationLeverModel.getTexturedModelData().createModel());
        ModelPart demat = root.getChild("demat");
        assertNotNull(demat);
        assertNotNull(demat.getChild("panel"));
        assertNotNull(demat.getChild("lever"));
    }
}
