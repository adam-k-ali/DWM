package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleDisplayStateTest {
    @BeforeEach
    void setUp() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void from_copiesModelFlagsAndReading() {
        TardisDataModel model = new TardisDataModel();
        model.variant = TardisChameleonVariant.FIRST_DOCTOR_BOX;
        model.stabilisersEnabled = Boolean.FALSE;
        model.cloaked = true;
        model.doorsLocked = true;
        model.lockX = true;
        model.lockY = false;
        model.lockZ = true;

        ExteriorEnvironmentReadout.Reading reading =
                new ExteriorEnvironmentReadout.Reading(false, 0.25F, 0.5F, 0.75F, 1.0F);

        ConsoleDisplayState state = ConsoleDisplayState.from(model, reading);

        assertEquals(TardisChameleonVariant.FIRST_DOCTOR_BOX, state.variant());
        assertFalse(state.stabilisersEnabled());
        assertTrue(state.cloaked());
        assertTrue(state.doorsLocked());
        assertTrue(state.lockX());
        assertFalse(state.lockY());
        assertTrue(state.lockZ());
        assertEquals(reading, state.reading());
    }

    @Test
    void from_nullModel_usesDefaultsWithReading() {
        ExteriorEnvironmentReadout.Reading reading =
                new ExteriorEnvironmentReadout.Reading(false, 0.1F, 0.2F, 0.3F, 0.4F);
        ConsoleDisplayState state = ConsoleDisplayState.from(null, reading);

        assertEquals(TardisChameleonVariant.TT_CAPSULE, state.variant());
        assertTrue(state.stabilisersEnabled());
        assertFalse(state.cloaked());
        assertEquals(reading, state.reading());
    }

    @Test
    void withReading_preservesFlags() {
        ConsoleDisplayState base = ConsoleDisplayState.from(null, ExteriorEnvironmentReadout.Reading.none())
                .withReading(new ExteriorEnvironmentReadout.Reading(false, 0.9F, 0.8F, 0.7F, 0.6F));

        assertEquals(0.9F, base.reading().oxygen(), 1e-4);
        assertTrue(base.stabilisersEnabled());
        assertEquals(TardisChameleonVariant.TT_CAPSULE, base.variant());
    }

    @Test
    void defaults_matchSafeConsoleStartup() {
        ConsoleDisplayState defaults = ConsoleDisplayState.defaults();
        assertEquals(TardisChameleonVariant.TT_CAPSULE, defaults.variant());
        assertTrue(defaults.stabilisersEnabled());
        assertFalse(defaults.cloaked());
        assertTrue(defaults.reading().noSignal());
    }
}
