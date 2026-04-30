package com.adamkali.dwm.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DWMConfigTest {
    @BeforeEach
    void resetConfigStatics() throws Exception {
        Field initializedField = DWMConfig.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        initializedField.set(null, false);

        Field configField = DWMConfig.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(null, new HashMap<String, Object>());
    }

    @Test
    void init_whenConfigIsEmpty_marksFirstStartTrueAndKeepsChameleonDisabledByDefault() {
        try (MockedStatic<DWMConfigManager> configManager = Mockito.mockStatic(DWMConfigManager.class)) {
            configManager.when(DWMConfigManager::load).thenReturn(new HashMap<>());

            DWMConfig.init();

            assertTrue(DWMConfig.getBoolean(DWMConfig.IS_FIRST_START));
            assertFalse(DWMConfig.getBoolean(DWMConfig.ENABLE_CHAMELEON_GUI));
        }
    }

    @Test
    void init_whenConfigExists_marksFirstStartFalse() {
        HashMap<String, Object> loaded = new HashMap<>();
        loaded.put(DWMConfig.IS_FIRST_START.getKey(), true);
        loaded.put(DWMConfig.ENABLE_CHAMELEON_GUI.getKey(), true);

        try (MockedStatic<DWMConfigManager> configManager = Mockito.mockStatic(DWMConfigManager.class)) {
            configManager.when(DWMConfigManager::load).thenReturn(loaded);

            DWMConfig.init();

            assertFalse(DWMConfig.getBoolean(DWMConfig.IS_FIRST_START));
            assertTrue(DWMConfig.getBoolean(DWMConfig.ENABLE_CHAMELEON_GUI));
        }
    }

    @Test
    void setBooleanAndSave_persistsUpdatedConfig() {
        try (MockedStatic<DWMConfigManager> configManager = Mockito.mockStatic(DWMConfigManager.class)) {
            configManager.when(DWMConfigManager::load).thenReturn(new HashMap<>());
            DWMConfig.init();

            DWMConfig.setBoolean(DWMConfig.ENABLE_CHAMELEON_GUI, true);
            DWMConfig.save();

            configManager.verify(() -> DWMConfigManager.save(Mockito.argThat(map ->
                    map.containsKey(DWMConfig.ENABLE_CHAMELEON_GUI.getKey())
                            && Boolean.TRUE.equals(map.get(DWMConfig.ENABLE_CHAMELEON_GUI.getKey()))
            )));
        }
    }
}
