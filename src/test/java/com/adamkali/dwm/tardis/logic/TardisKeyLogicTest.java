package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TardisKeyLogicTest {
    @Test
    void ownerBindsUnboundKeyToTardisId() {
        TardisDataModel model = new TardisDataModel();
        UUID owner = UUID.randomUUID();
        model.setOwner(owner);

        TardisKeyLogic.UseResult result = TardisKeyLogic.useOnTardis(null, owner, model.uuid, model);

        assertEquals(TardisKeyLogic.UseResult.BOUND, result);
        assertFalse(model.doorsLocked);
    }

    @Test
    void nonOwnerAndUnownedTardisCannotBindKey() {
        TardisDataModel owned = new TardisDataModel();
        owned.setOwner(UUID.randomUUID());
        TardisDataModel unowned = new TardisDataModel();

        assertEquals(
                TardisKeyLogic.UseResult.NOT_OWNER,
                TardisKeyLogic.useOnTardis(null, UUID.randomUUID(), owned.uuid, owned)
        );
        assertEquals(
                TardisKeyLogic.UseResult.NOT_OWNER,
                TardisKeyLogic.useOnTardis(null, UUID.randomUUID(), unowned.uuid, unowned)
        );
    }

    @Test
    void matchingBoundKeyCanToggleEvenAfterOwnershipChanges() {
        TardisDataModel model = new TardisDataModel();
        UUID originalOwner = UUID.randomUUID();
        model.setOwner(originalOwner);

        assertEquals(
                TardisKeyLogic.UseResult.BOUND,
                TardisKeyLogic.useOnTardis(null, originalOwner, model.uuid, model)
        );
        model.setOwner(UUID.randomUUID());

        assertEquals(
                TardisKeyLogic.UseResult.TOGGLE_READY,
                TardisKeyLogic.useOnTardis(model.uuid, UUID.randomUUID(), model.uuid, model)
        );
        DoorLockLogic.toggle(model);
        assertTrue(model.doorsLocked);
    }

    @Test
    void toggleLock_refusedWhenDoorsAreOpen() {
        TardisDataModel model = new TardisDataModel();
        model.doorState.isOpen = true;

        assertFalse(DoorLockLogic.canToggleLock(model));
        assertFalse(DoorLockLogic.toggle(model));
        assertFalse(model.doorsLocked);
    }

    @Test
    void boundKeyRefusesDifferentTardis() {
        TardisDataModel boundModel = new TardisDataModel();
        TardisDataModel differentModel = new TardisDataModel();

        assertEquals(
                TardisKeyLogic.UseResult.WRONG_TARDIS,
                TardisKeyLogic.useOnTardis(
                        boundModel.uuid,
                        UUID.randomUUID(),
                        differentModel.uuid,
                        differentModel
                )
        );
        assertFalse(differentModel.doorsLocked);
    }
}
