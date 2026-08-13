package com.adamkali.dwm.gametest;

import com.adamkali.dwm.tardis.logic.LandingSiteLogic;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

public class TardisLandingGameTests {
    private static final Direction DOOR_FACING = Direction.NORTH;

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void isValidLanding_acceptsOpenDoorColumn(GameTestHelper context) {
        BlockPos shellRel = new BlockPos(2, 2, 2);
        placeShellPad(context, shellRel);
        // Door column (north of shell) left as air from empty structure.

        BlockPos shellAbs = context.absolutePos(shellRel);
        if (!LandingSiteLogic.isValidLanding(context.getLevel(), shellAbs, DOOR_FACING)) {
            throw new AssertionError("Expected valid landing with replaceable door column");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void isValidLanding_rejectsBlockedDoorFeet(GameTestHelper context) {
        BlockPos shellRel = new BlockPos(2, 2, 2);
        placeShellPad(context, shellRel);
        BlockPos doorRel = shellRel.relative(DOOR_FACING);
        context.setBlock(doorRel, Blocks.STONE);

        BlockPos shellAbs = context.absolutePos(shellRel);
        if (LandingSiteLogic.isValidLanding(context.getLevel(), shellAbs, DOOR_FACING)) {
            throw new AssertionError("Expected invalid landing when door feet are blocked");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void isValidLanding_rejectsBlockedDoorHead(GameTestHelper context) {
        BlockPos shellRel = new BlockPos(2, 2, 2);
        placeShellPad(context, shellRel);
        BlockPos doorHeadRel = shellRel.relative(DOOR_FACING).above();
        context.setBlock(doorHeadRel, Blocks.STONE);

        BlockPos shellAbs = context.absolutePos(shellRel);
        if (LandingSiteLogic.isValidLanding(context.getLevel(), shellAbs, DOOR_FACING)) {
            throw new AssertionError("Expected invalid landing when door head is blocked");
        }
        context.succeed();
    }

    private static void placeShellPad(GameTestHelper context, BlockPos shellRel) {
        context.setBlock(shellRel.below(), Blocks.STONE);
        context.setBlock(shellRel, Blocks.AIR);
        context.setBlock(shellRel.above(), Blocks.AIR);
        BlockPos doorRel = shellRel.relative(DOOR_FACING);
        context.setBlock(doorRel, Blocks.AIR);
        context.setBlock(doorRel.above(), Blocks.AIR);
    }
}
