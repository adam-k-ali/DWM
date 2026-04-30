package com.adamkali.dwm.network;

import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerPayloadTypeRegistryTest {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        TardisDataLoader.tardisSaveDirectory = tempDir;
        Field field = TardisDataLoader.class.getDeclaredField("tardisData");
        field.setAccessible(true);
        HashMap<?, ?> cache = (HashMap<?, ?>) field.get(null);
        cache.clear();
    }

    @Test
    void safelyHandleChameleonUpdate_rejectsUnknownTardisId() {
        UUID unknownId = UUID.randomUUID();
        UpdateTardisChameleonC2SPayload payload = new UpdateTardisChameleonC2SPayload(
                TardisChameleonVariant.FIRST_DOCTOR_BOX.getId(),
                unknownId
        );

        try (MockedStatic<DWMConfig> config = Mockito.mockStatic(DWMConfig.class);
             MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<TardisLogic> logic = Mockito.mockStatic(TardisLogic.class)) {
            config.when(() -> DWMConfig.getBoolean(DWMConfig.ENABLE_CHAMELEON_GUI)).thenReturn(true);
            loader.when(() -> TardisDataLoader.get(unknownId)).thenReturn(null);

            boolean accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(payload, "playerA");

            assertFalse(accepted);
            logic.verifyNoInteractions();
        }
    }

    @Test
    void safelyHandleChameleonUpdate_rejectsInvalidVariantId() {
        UUID tardisId = UUID.randomUUID();
        UpdateTardisChameleonC2SPayload payload = new UpdateTardisChameleonC2SPayload(
                Identifier.of("dwm", "not_a_variant"),
                tardisId
        );

        try (MockedStatic<DWMConfig> config = Mockito.mockStatic(DWMConfig.class);
             MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<TardisLogic> logic = Mockito.mockStatic(TardisLogic.class)) {
            config.when(() -> DWMConfig.getBoolean(DWMConfig.ENABLE_CHAMELEON_GUI)).thenReturn(true);
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(new TardisDataModel());

            boolean accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(payload, "playerA");

            assertFalse(accepted);
            logic.verifyNoInteractions();
        }
    }

    @Test
    void safelyHandleChameleonUpdate_appliesValidPayload() {
        UUID tardisId = UUID.randomUUID();
        TardisChameleonVariant variant = TardisChameleonVariant.SEVENTH_DOCTOR_BOX;
        UpdateTardisChameleonC2SPayload payload = new UpdateTardisChameleonC2SPayload(variant.getId(), tardisId);

        try (MockedStatic<DWMConfig> config = Mockito.mockStatic(DWMConfig.class);
             MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<TardisLogic> logic = Mockito.mockStatic(TardisLogic.class)) {
            config.when(() -> DWMConfig.getBoolean(DWMConfig.ENABLE_CHAMELEON_GUI)).thenReturn(true);
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(new TardisDataModel());

            boolean accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(payload, "playerA");

            assertTrue(accepted);
            logic.verify(() -> TardisLogic.setVariant(tardisId, variant));
        }
    }

    @Test
    void safelyHandleChameleonUpdate_persistsUpdatedVariantThroughSaveAndLoad() throws Exception {
        TardisDataModel model = TardisDataLoader.create();
        UUID tardisId = model.uuid;
        UpdateTardisChameleonC2SPayload payload = new UpdateTardisChameleonC2SPayload(
                TardisChameleonVariant.SIXTH_DOCTOR_BOX.getId(),
                tardisId
        );

        boolean accepted;
        try (MockedStatic<DWMConfig> config = Mockito.mockStatic(DWMConfig.class)) {
            config.when(() -> DWMConfig.getBoolean(DWMConfig.ENABLE_CHAMELEON_GUI)).thenReturn(true);
            accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(payload, "playerA");
        }
        TardisDataLoader.save();

        Field field = TardisDataLoader.class.getDeclaredField("tardisData");
        field.setAccessible(true);
        HashMap<?, ?> cache = (HashMap<?, ?>) field.get(null);
        cache.clear();

        TardisDataModel loaded = TardisDataLoader.get(tardisId);

        assertTrue(accepted);
        assertTrue(loaded != null);
        assertTrue(loaded.variant == TardisChameleonVariant.SIXTH_DOCTOR_BOX);
    }

    @Test
    void safelyHandleChameleonUpdate_rejectsWhenExperimentalFeatureDisabled() {
        UUID tardisId = UUID.randomUUID();
        UpdateTardisChameleonC2SPayload payload = new UpdateTardisChameleonC2SPayload(
                TardisChameleonVariant.FOURTH_DOCTOR_BOX.getId(),
                tardisId
        );

        try (MockedStatic<DWMConfig> config = Mockito.mockStatic(DWMConfig.class);
             MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<TardisLogic> logic = Mockito.mockStatic(TardisLogic.class)) {
            config.when(() -> DWMConfig.getBoolean(DWMConfig.ENABLE_CHAMELEON_GUI)).thenReturn(false);
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(new TardisDataModel());

            boolean accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(payload, "playerA");

            assertFalse(accepted);
            logic.verifyNoInteractions();
        }
    }
}
