package com.adamkali.dwm.scenariotest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaServerProcessTest {
    @Test
    void parsePortDefaultsWhenMissing() {
        assertEquals(25565, VanillaServerProcess.parsePort(null));
    }

    @Test
    void parsePortAcceptsIntegerAndString() {
        assertEquals(25565, VanillaServerProcess.parsePort(25565));
        assertEquals(25566, VanillaServerProcess.parsePort("25566"));
    }

    @Test
    void parsePortRejectsOutOfRangeAndBlank() {
        ScenarioException zero = assertThrows(ScenarioException.class, () -> VanillaServerProcess.parsePort(0));
        ScenarioException high = assertThrows(ScenarioException.class, () -> VanillaServerProcess.parsePort(70000));
        ScenarioException blank = assertThrows(ScenarioException.class, () -> VanillaServerProcess.parsePort("  "));

        assertTrue(zero.getMessage().contains("between 1 and 65535"));
        assertTrue(high.getMessage().contains("between 1 and 65535"));
        assertTrue(blank.getMessage().contains("between 1 and 65535"));
    }

    @Test
    void serverPropertiesUseOfflineLoopbackAndChosenPort() {
        String properties = VanillaServerProcess.serverProperties(25567);

        assertTrue(properties.contains("online-mode=false"));
        assertTrue(properties.contains("server-ip=127.0.0.1"));
        assertTrue(properties.contains("server-port=25567"));
        assertTrue(properties.contains("level-type=minecraft:flat"));
        assertTrue(properties.contains("enforce-secure-profile=false"));
        assertTrue(properties.contains("max-tick-time=-1"));
        assertTrue(properties.contains("difficulty=peaceful"));
    }

    @Test
    void eulaIsAccepted() {
        assertEquals("eula=true\n", VanillaServerProcess.eulaText());
    }
}
