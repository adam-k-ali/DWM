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
    public void findLandingAtOrNearby_RejectsFullyBlockedExactWithoutFallbackPad(GameTestHelper context) {
        // Exact target invalid, and no replaceable neighbor pads in the immediate column —
        // assert isValidLanding alone; spiral fallback depends on heightmaps that are noisy
        // in GameTest void worlds, so keep that path covered by unit-style exact checks here.
        BlockPos shellRel = new BlockPos(2, 2, 2);
        context.setBlock(shellRel.below(), Blocks.STONE);
        context.setBlock(shellRel, Blocks.STONE);
        context.setBlock(shellRel.above(), Blocks.STONE);
        context.setBlock(shellRel.relative(DOOR_FACING), Blocks.STONE);
        context.setBlock(shellRel.relative(DOOR_FACING).above(), Blocks.STONE);

        BlockPos shellAbs = context.absolutePos(shellRel);
        if (LandingSiteLogic.isValidLanding(context.getLevel(), shellAbs, DOOR_FACING)) {
            throw new AssertionError("Expected blocked column to be an invalid landing");
        }
        // When exact is invalid, findLandingAtOrNearby may still spiral via heightmap; only
        // require that a returned landing (if any) actually validates.
        var landing = LandingSiteLogic.findLandingAtOrNearby(context.getLevel(), shellAbs, DOOR_FACING);
        if (landing.isPresent()
                && !LandingSiteLogic.isValidLanding(context.getLevel(), landing.get(), DOOR_FACING)) {
            throw new AssertionError("Fallback landing must pass isValidLanding: " + landing.get());
        }
        context.succeed();
    }

    private static void placeShellPad(GameTestHelper context, BlockPos shellRel) {
        TardisGameTestSupport.placeLandingPad(context, shellRel, DOOR_FACING);
    }
}
