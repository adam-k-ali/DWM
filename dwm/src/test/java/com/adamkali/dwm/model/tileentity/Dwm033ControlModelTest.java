package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.model.json.TestModelLoading;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.minecraft.client.model.geom.ModelPart;

class Dwm033ControlModelTest {
    @Test
    void reader_bakesNeedle() {
        ModelPart root = assertDoesNotThrow(() -> TestModelLoading.bake(ReaderModel.LAYER_LOCATION));
        assertNotNull(root.getChild("needle"));
        assertNotNull(root.getChild("reader"));
    }

    @Test
    void radiationReader_bakesNeedle() {
        ModelPart root = assertDoesNotThrow(() -> TestModelLoading.bake(RadiationReaderModel.LAYER_LOCATION));
        assertNotNull(root.getChild("needle"));
    }

    @Test
    void cloakLever_bakesControl() {
        ModelPart root = assertDoesNotThrow(() -> TestModelLoading.bake(CloakLeverModel.LAYER_LOCATION));
        assertNotNull(root.getChild("lever").getChild("lever_control2"));
    }

    @Test
    void doorLock_bakesLever() {
        ModelPart root = assertDoesNotThrow(() -> TestModelLoading.bake(DoorLockModel.LAYER_LOCATION));
        assertNotNull(root.getChild("lever"));
    }

    @Test
    void telepathicCircuit_bakesLights() {
        ModelPart root = assertDoesNotThrow(() -> TestModelLoading.bake(TelepathicCircuitModel.LAYER_LOCATION));
        assertNotNull(root.getChild("light"));
        assertNotNull(root.getChild("light2"));
    }

    @Test
    void coordinateLock_bakesAxisSwitches() {
        ModelPart root = assertDoesNotThrow(() -> TestModelLoading.bake(CoordinateLockModel.LAYER_LOCATION));
        assertNotNull(root.getChild("coord_button_x").getChild("switch"));
        assertNotNull(root.getChild("coord_button_y").getChild("switch3"));
        assertNotNull(root.getChild("coord_button_z").getChild("switch2"));
    }
}
