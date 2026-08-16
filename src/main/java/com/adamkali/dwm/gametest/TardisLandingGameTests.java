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

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void findLandingAtOrNearby_ReturnsExactWhenValid(GameTestHelper context) {
        BlockPos shellRel = new BlockPos(2, 2, 2);
        placeShellPad(context, shellRel);
        BlockPos shellAbs = context.absolutePos(shellRel);

        var landing = LandingSiteLogic.findLandingAtOrNearby(context.getLevel(), shellAbs, DOOR_FACING);
        if (landing.isEmpty() || !shellAbs.equals(landing.get())) {
            throw new AssertionError("Expected exact landing at " + shellAbs + " but got " + landing);
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void findLandingAtOrNearby_SpiralsWhenExactBlocked(GameTestHelper context) {
        BlockPos blockedRel = new BlockPos(2, 2, 2);
        BlockPos nearbyRel = new BlockPos(3, 2, 2);
        // Exact target has solid feet (invalid).
        context.setBlock(blockedRel.below(), Blocks.STONE);
        context.setBlock(blockedRel, Blocks.STONE);
        context.setBlock(blockedRel.above(), Blocks.AIR);
        // Nearby column is a valid pad.
        placeShellPad(context, nearbyRel);

        BlockPos blockedAbs = context.absolutePos(blockedRel);
        BlockPos nearbyAbs = context.absolutePos(nearbyRel);
        var landing = LandingSiteLogic.findLandingAtOrNearby(context.getLevel(), blockedAbs, DOOR_FACING);
        if (landing.isEmpty()) {
            throw new AssertionError("Expected nearby valid landing when exact target blocked");
        }
        if (!nearbyAbs.equals(landing.get())) {
            // Spiral may pick another valid cell; require it be close and valid.
            if (landing.get().distManhattan(blockedAbs) > 8
                    || !LandingSiteLogic.isValidLanding(context.getLevel(), landing.get(), DOOR_FACING)) {
                throw new AssertionError("Expected spiral landing near blocked target, got " + landing.get());
            }
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void findSurfaceLanding_ReturnsValidNearOrigin(GameTestHelper context) {
        BlockPos shellRel = new BlockPos(2, 2, 2);
        placeShellPad(context, shellRel);
        BlockPos shellAbs = context.absolutePos(shellRel);

        var landing = LandingSiteLogic.findSurfaceLanding(context.getLevel(), shellAbs, DOOR_FACING);
        if (landing.isEmpty()) {
            throw new AssertionError("Expected findSurfaceLanding to find a valid pad near " + shellAbs);
        }
        if (!LandingSiteLogic.isValidLanding(context.getLevel(), landing.get(), DOOR_FACING)) {
            throw new AssertionError("Surface landing " + landing.get() + " failed isValidLanding");
        }
        context.succeed();
    }

    private static void placeShellPad(GameTestHelper context, BlockPos shellRel) {
        TardisGameTestSupport.placeLandingPad(context, shellRel, DOOR_FACING);
    }
}
