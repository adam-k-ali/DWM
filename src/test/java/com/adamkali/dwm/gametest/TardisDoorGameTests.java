package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.WorldSavePath;

public class TardisDoorGameTests implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void tardisDoorStateSmokeFlow(TestContext context) {
        context.setBlockState(1, 2, 1, DWMBlocks.TARDIS_BLOCK);
        context.expectBlock(DWMBlocks.TARDIS_BLOCK, 1, 2, 1);
        TardisDataLoader.tardisSaveDirectory = context.getWorld().getServer().getSavePath(WorldSavePath.ROOT).resolve("gametest_tardis_data");
        TardisDataModel model = TardisDataLoader.create();
        ActionResult toggleResult = TardisLogic.toggleDoor(model.uuid);
        if (toggleResult != ActionResult.SUCCESS) {
            throw new AssertionError("Expected successful door toggle in smoke flow");
        }

        TardisDoorState initialState = TardisLogic.getDoorState(model.uuid);
        if (initialState == null || !initialState.isOpen) {
            throw new AssertionError("Expected door to be marked open after first toggle");
        }

        TardisLogic.updateDoorState(model.uuid);
        TardisDoorState updatedState = TardisLogic.getDoorState(model.uuid);
        if (updatedState == null || updatedState.doorSwing <= 0.0f) {
            throw new AssertionError("Expected door swing to advance after update");
        }

        ActionResult toggledWhileMoving = TardisLogic.toggleDoor(model.uuid);
        if (toggledWhileMoving != ActionResult.PASS) {
            throw new AssertionError("Expected toggle to pass while door is mid-swing");
        }

        context.complete();
    }
}
