package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
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
        TardisLogic.updateDoorState(model.uuid);

        context.complete();
    }
}
