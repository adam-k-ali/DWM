package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class TardisOwnershipLogicTest {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        TardisDataLoader.tardisSaveDirectory = tempDir;
        clearCache();
    }

    @AfterEach
    void tearDown() throws Exception {
        clearCache();
        TardisDataLoader.tardisSaveDirectory = null;
    }

    @Test
    void tryClaimOnEnter_claimsUnownedWhenPlayerOwnsNone() {
        TardisDataModel model = TardisDataLoader.create();
        UUID player = UUID.randomUUID();

        assertTrue(TardisOwnershipLogic.tryClaimOnEnter(model.uuid, player));
        assertEquals(player, model.ownerUuid);
        assertTrue(TardisOwnershipLogic.isOwner(model, player));
    }

    @Test
    void tryClaimOnEnter_noopWhenAlreadyOwned() {
        TardisDataModel model = TardisDataLoader.create();
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        model.setOwner(owner);

        assertFalse(TardisOwnershipLogic.tryClaimOnEnter(model.uuid, other));
        assertEquals(owner, model.ownerUuid);
    }

    @Test
    void tryClaimOnEnter_noopWhenPlayerAlreadyOwnsAnother() {
        UUID player = UUID.randomUUID();
        TardisDataModel first = TardisDataLoader.create();
        first.setOwner(player);
        TardisDataModel second = TardisDataLoader.create();

        assertFalse(TardisOwnershipLogic.tryClaimOnEnter(second.uuid, player));
        assertNull(second.ownerUuid);
    }

    @Test
    void tryClaimOnEnter_rejectsNulls() {
        TardisDataModel model = TardisDataLoader.create();
        assertFalse(TardisOwnershipLogic.tryClaimOnEnter(null, UUID.randomUUID()));
        assertFalse(TardisOwnershipLogic.tryClaimOnEnter(model.uuid, null));
    }

    @Test
    void tryForceClaim_overwritesExistingOwnerWhenPlayerOwnsNone() {
        TardisDataModel model = TardisDataLoader.create();
        UUID previous = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        model.setOwner(previous);

        assertEquals(
                TardisOwnershipLogic.ForceClaimResult.CLAIMED,
                TardisOwnershipLogic.tryForceClaim(model.uuid, player)
        );
        assertEquals(player, model.ownerUuid);
    }

    @Test
    void tryForceClaim_claimsUnownedWhenPlayerOwnsNone() {
        TardisDataModel model = TardisDataLoader.create();
        UUID player = UUID.randomUUID();

        assertEquals(
                TardisOwnershipLogic.ForceClaimResult.CLAIMED,
                TardisOwnershipLogic.tryForceClaim(model.uuid, player)
        );
        assertEquals(player, model.ownerUuid);
    }

    @Test
    void tryForceClaim_alreadyOwnerWhenPlayerOwnsThisTardis() {
        TardisDataModel model = TardisDataLoader.create();
        UUID player = UUID.randomUUID();
        model.setOwner(player);

        assertEquals(
                TardisOwnershipLogic.ForceClaimResult.ALREADY_OWNER,
                TardisOwnershipLogic.tryForceClaim(model.uuid, player)
        );
        assertEquals(player, model.ownerUuid);
    }

    @Test
    void tryForceClaim_refusesWhenPlayerOwnsAnother() {
        UUID player = UUID.randomUUID();
        TardisDataModel first = TardisDataLoader.create();
        first.setOwner(player);
        TardisDataModel second = TardisDataLoader.create();
        UUID previous = UUID.randomUUID();
        second.setOwner(previous);

        assertEquals(
                TardisOwnershipLogic.ForceClaimResult.PLAYER_OWNS_ANOTHER,
                TardisOwnershipLogic.tryForceClaim(second.uuid, player)
        );
        assertEquals(previous, second.ownerUuid);
        assertEquals(player, first.ownerUuid);
    }

    @Test
    void tryForceClaim_unknownWhenTardisMissing() {
        assertEquals(
                TardisOwnershipLogic.ForceClaimResult.UNKNOWN,
                TardisOwnershipLogic.tryForceClaim(UUID.randomUUID(), UUID.randomUUID())
        );
    }

    @Test
    void tryForceClaim_invalidForNulls() {
        TardisDataModel model = TardisDataLoader.create();
        assertEquals(
                TardisOwnershipLogic.ForceClaimResult.INVALID,
                TardisOwnershipLogic.tryForceClaim(null, UUID.randomUUID())
        );
        assertEquals(
                TardisOwnershipLogic.ForceClaimResult.INVALID,
                TardisOwnershipLogic.tryForceClaim(model.uuid, null)
        );
        assertNull(model.ownerUuid);
    }

    @SuppressWarnings("unchecked")
    private static void clearCache() throws Exception {
        Field field = TardisDataLoader.class.getDeclaredField("tardisData");
        field.setAccessible(true);
        ((HashMap<?, ?>) field.get(null)).clear();
    }
}
