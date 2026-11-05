package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.model.json.TestModelLoading;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.minecraft.client.model.geom.ModelPart;

class FastReturnModelTest {
    @Test
    void texturedModelData_createsRootWithSwitch() {
        ModelPart root = assertDoesNotThrow(() -> TestModelLoading.bake(FastReturnModel.LAYER_LOCATION));
        assertNotNull(root.getChild("switch"));
    }
}
