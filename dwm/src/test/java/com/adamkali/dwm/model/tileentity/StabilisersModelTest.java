package com.adamkali.dwm.model.tileentity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.minecraft.client.model.geom.ModelPart;

class StabilisersModelTest {
    @Test
    void texturedModelData_createsRootWithLever() {
        ModelPart root = assertDoesNotThrow(() -> StabilisersModel.getTexturedModelData().bakeRoot());
        assertNotNull(root.getChild("stable_adjust"));
        assertNotNull(root.getChild("stable_adjust").getChild("lever"));
    }
}
