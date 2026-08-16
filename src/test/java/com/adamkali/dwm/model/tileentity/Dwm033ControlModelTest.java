package com.adamkali.dwm.model.tileentity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.minecraft.client.model.geom.ModelPart;

class Dwm033ControlModelTest {
    @Test
    void reader_bakesNeedle() {
        ModelPart root = assertDoesNotThrow(() -> ReaderModel.getTexturedModelData().bakeRoot());
        assertNotNull(root.getChild("needle"));
        assertNotNull(root.getChild("reader"));
    }

    @Test
    void radiationReader_bakesNeedle() {
        ModelPart root = assertDoesNotThrow(() -> RadiationReaderModel.getTexturedModelData().bakeRoot());
        assertNotNull(root.getChild("needle"));
    }

    @Test
    void cloakLever_bakesControl() {
        ModelPart root = assertDoesNotThrow(() -> CloakLeverModel.getTexturedModelData().bakeRoot());
        assertNotNull(root.getChild("lever").getChild("lever_control2"));
    }

    @Test
    void doorLock_bakesLever() {
        ModelPart root = assertDoesNotThrow(() -> DoorLockModel.getTexturedModelData().bakeRoot());
        assertNotNull(root.getChild("lever"));
    }
}
