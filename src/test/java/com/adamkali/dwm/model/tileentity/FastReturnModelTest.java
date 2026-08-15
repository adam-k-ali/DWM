package com.adamkali.dwm.model.tileentity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.minecraft.client.model.geom.ModelPart;

class FastReturnModelTest {
    @Test
    void texturedModelData_createsRootWithSwitch() {
        ModelPart root = assertDoesNotThrow(() -> FastReturnModel.getTexturedModelData().bakeRoot());
        assertNotNull(root.getChild("switch"));
    }
}
