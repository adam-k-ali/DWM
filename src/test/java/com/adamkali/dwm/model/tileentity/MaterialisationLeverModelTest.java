package com.adamkali.dwm.model.tileentity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.minecraft.client.model.geom.ModelPart;

class MaterialisationLeverModelTest {
    @Test
    void texturedModelData_createsRootWithPanelAndLever() {
        ModelPart root = assertDoesNotThrow(() -> MaterialisationLeverModel.getTexturedModelData().bakeRoot());
        ModelPart demat = root.getChild("demat");
        assertNotNull(demat);
        assertNotNull(demat.getChild("panel"));
        assertNotNull(demat.getChild("lever"));
    }
}
