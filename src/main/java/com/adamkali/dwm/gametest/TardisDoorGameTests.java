package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.storage.LevelResource;

public class TardisDoorGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void tardisDoorStateSmokeFlow(GameTestHelper context) {
        context.setBlock(1, 2, 1, DWMBlocks.TARDIS_BLOCK);
        context.assertBlockPresent(DWMBlocks.TARDIS_BLOCK, 1, 2, 1);
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer().getWorldPath(LevelResource.ROOT).resolve("gametest_tardis_data");
        TardisDataModel model = TardisDataLoader.create();
        InteractionResult toggleResult = TardisLogic.toggleDoor(model.uuid);
        if (toggleResult != InteractionResult.SUCCESS) {
            throw new AssertionError("Expected successful door toggle in smoke flow");
        }
        TardisLogic.updateDoorState(model.uuid);

        context.succeed();
    }
}
