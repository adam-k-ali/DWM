package com.adamkali.dwm.model.tileentity;

import net.minecraft.client.model.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BiomeSelectorModelTest {
    @Test
    void texturedModelData_createsRootWithSelectorChild() {
        ModelPart root = assertDoesNotThrow(() -> BiomeSelectorModel.getTexturedModelData().createModel());
        assertNotNull(root.getChild("biome_selector"));
    }
}
