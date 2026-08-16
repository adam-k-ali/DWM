package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Exterior door interaction path via {@link TardisBlock#useWithoutItem}.
 */
public class TardisDoorGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void tardisDoorStateSmokeFlow(GameTestHelper context) {
        context.setBlock(1, 2, 1, DWMBlocks.TARDIS_BLOCK);
        context.assertBlockPresent(DWMBlocks.TARDIS_BLOCK, 1, 2, 1);
        TardisGameTestSupport.configureSaveDirectory(context);
        TardisDataModel model = TardisDataLoader.create();
        var toggleResult = TardisLogic.toggleDoor(model.uuid);
        if (toggleResult != net.minecraft.world.InteractionResult.SUCCESS) {
            throw new AssertionError("Expected successful door toggle in smoke flow");
        }
        TardisLogic.updateDoorState(model.uuid);

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void exteriorUseWithoutItem_TogglesDoorViaBlockEntity(GameTestHelper context) {
        BlockPos shellRel = new BlockPos(2, 2, 2);
        TardisBlockEntity exterior = TardisGameTestSupport.placeExteriorShell(context, shellRel);
        BlockPos shellAbs = context.absolutePos(shellRel);
        TardisGameTestSupport.forceDoorsClosed(exterior.getTardisId());

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        var state = context.getLevel().getBlockState(shellAbs);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(shellAbs), Direction.NORTH, shellAbs, false);
        state.useWithoutItem(context.getLevel(), player, hit);

        var door = TardisLogic.getDoorState(exterior.getTardisId());
        if (door == null || !door.isOpen) {
            throw new AssertionError("Expected exterior useWithoutItem to open doors via BE toggle");
        }
        context.succeed();
    }
}
