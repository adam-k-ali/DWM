package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.resources.Identifier;

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
        UUID playerUuid = UUID.randomUUID();
        UpdateTardisChameleonC2SPayload payload = new UpdateTardisChameleonC2SPayload(
                TardisChameleonVariant.FIRST_DOCTOR_BOX.getId(),
                unknownId
        );

        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<TardisLogic> logic = Mockito.mockStatic(TardisLogic.class)) {
            loader.when(() -> TardisDataLoader.get(unknownId)).thenReturn(null);

            boolean accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(payload, playerUuid, null);

            assertFalse(accepted);
            logic.verifyNoInteractions();
        }
    }

    @Test
    void safelyHandleChameleonUpdate_rejectsInvalidVariantId() {
        UUID owner = UUID.randomUUID();
        TardisDataModel model = new TardisDataModel();
        model.setOwner(owner);
        UpdateTardisChameleonC2SPayload payload = new UpdateTardisChameleonC2SPayload(
                Identifier.fromNamespaceAndPath("dwm", "not_a_variant"),
                model.uuid
        );

        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<TardisLogic> logic = Mockito.mockStatic(TardisLogic.class)) {
            loader.when(() -> TardisDataLoader.get(model.uuid)).thenReturn(model);

            boolean accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(payload, owner, null);

            assertFalse(accepted);
            logic.verifyNoInteractions();
        }
    }

    @Test
    void safelyHandleChameleonUpdate_appliesValidPayloadForOwner() {
        UUID owner = UUID.randomUUID();
        TardisDataModel model = new TardisDataModel();
        model.setOwner(owner);
        TardisChameleonVariant variant = TardisChameleonVariant.SEVENTH_DOCTOR_BOX;
        UpdateTardisChameleonC2SPayload payload = new UpdateTardisChameleonC2SPayload(variant.getId(), model.uuid);

        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<TardisLogic> logic = Mockito.mockStatic(TardisLogic.class)) {
            loader.when(() -> TardisDataLoader.get(model.uuid)).thenReturn(model);

            boolean accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(payload, owner, null);

            assertTrue(accepted);
            logic.verify(() -> TardisLogic.setVariant(model.uuid, variant));
        }
    }

    @Test
    void safelyHandleChameleonUpdate_rejectsNonOwner() {
        UUID owner = UUID.randomUUID();
        UUID visitor = UUID.randomUUID();
        TardisDataModel model = new TardisDataModel();
        model.setOwner(owner);
        UpdateTardisChameleonC2SPayload payload = new UpdateTardisChameleonC2SPayload(
                TardisChameleonVariant.FIFTH_DOCTOR_BOX.getId(),
                model.uuid
        );

        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<TardisLogic> logic = Mockito.mockStatic(TardisLogic.class)) {
            loader.when(() -> TardisDataLoader.get(model.uuid)).thenReturn(model);

            boolean accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(payload, visitor, null);

            assertFalse(accepted);
            logic.verifyNoInteractions();
        }
    }

    @Test
    void safelyHandleChameleonUpdate_persistsUpdatedVariantThroughSaveAndLoad() throws Exception {
        TardisDataModel model = TardisDataLoader.create();
        UUID owner = UUID.randomUUID();
        model.setOwner(owner);
        UUID tardisId = model.uuid;
        UpdateTardisChameleonC2SPayload payload = new UpdateTardisChameleonC2SPayload(
                TardisChameleonVariant.SIXTH_DOCTOR_BOX.getId(),
                tardisId
        );

        boolean accepted = ServerPayloadTypeRegistry.safelyHandleChameleonUpdate(payload, owner, null);
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
    void safelyHandleSelectWaypoint_rejectsNonOwner() {
        TardisDataModel model = TardisDataLoader.create();
        model.setOwner(UUID.randomUUID());
        UUID visitor = UUID.randomUUID();

        boolean accepted = ServerPayloadTypeRegistry.safelyHandleSelectWaypoint(
                new SelectWaypointC2SPayload(model.uuid, null),
                visitor,
                null
        );

        assertFalse(accepted);
    }

    @Test
    void safelyHandleSelectWaypoint_acceptsOwner() {
        TardisDataModel model = TardisDataLoader.create();
        UUID owner = UUID.randomUUID();
        model.setOwner(owner);

        boolean accepted = ServerPayloadTypeRegistry.safelyHandleSelectWaypoint(
                new SelectWaypointC2SPayload(model.uuid, null),
                owner,
                null
        );

        assertTrue(accepted);
    }

    @Test
    void safelyHandlePortalStreamRequest_rejectsNullPayloadFieldsOrPlayer() {
        assertFalse(ServerPayloadTypeRegistry.safelyHandlePortalStreamRequest(
                new RequestPortalStreamC2SPayload(com.adamkali.dwm.tardis.portal.PortalStreamKind.SOTO, null),
                null
        ));
        assertFalse(ServerPayloadTypeRegistry.safelyHandlePortalStreamRequest(
                new RequestPortalStreamC2SPayload(com.adamkali.dwm.tardis.portal.PortalStreamKind.SOTO, UUID.randomUUID()),
                null
        ));
    }
}
