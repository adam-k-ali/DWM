package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisInteriorService;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.UUID;

/**
 * Enter/exit teleport paths and travel-gated blocking for {@link TardisInteriorService}.
 *
 * <p>Note: Fabric {@code GameTestServer} currently loads only the vanilla dimensions, so
 * {@code dwm:tardis} is often unavailable. Enter success is asserted when the dimension is
 * present; otherwise the null-dimension failure path is checked. Exit is exercised entirely
 * in the GameTest overworld.
 */
public class TardisTeleportGameTests {

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 100)
    public void enterFromExterior_UsesTardisDimensionWhenLoaded(GameTestHelper context) {
        TardisTravelService.clearActiveForTests();
        BlockPos shellRel = new BlockPos(2, 2, 2);
        TardisBlockEntity exterior = TardisGameTestSupport.placeExteriorShell(context, shellRel);
        UUID tardisId = exterior.getTardisId();
        TardisGameTestSupport.forceDoorsFullyOpen(tardisId);

        ServerLevel tardisWorld = TardisGameTestSupport.tardisDimensionOrNull(context);
        ServerPlayer player = TardisGameTestSupport.mockServerPlayer(context);

        boolean entered = TardisInteriorService.tryEnterFromExterior(player, context.getLevel(), exterior);
        if (tardisWorld == null) {
            if (entered) {
                throw new AssertionError("Enter must fail when dwm:tardis is not loaded");
            }
            context.succeed();
            return;
        }
        if (!entered) {
            throw new AssertionError("Expected tryEnterFromExterior to succeed with open doors");
        }
        if (!TardisDimensions.isTardisWorld(player.level())) {
            throw new AssertionError("Player should be in dwm:tardis after enter, was "
                    + player.level().dimension().identifier());
        }
        if (!exterior.isInteriorGenerated() || exterior.getInteriorEntrance() == null) {
            throw new AssertionError("Exterior should record generated interior entrance after enter");
        }
        BlockPos entrance = exterior.getInteriorEntrance();
        if (player.blockPosition().distManhattan(entrance) > 2) {
            throw new AssertionError("Player should land near interior entrance " + entrance
                    + " but was at " + player.blockPosition());
        }
        if (tardisWorld.getBlockState(entrance.below()).isAir()) {
            throw new AssertionError("Expected solid floor under interior entrance");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 100)
    public void exitToExterior_TeleportsBesideShellFromOverworldDoor(GameTestHelper context) {
        TardisTravelService.clearActiveForTests();
        BlockPos shellRel = new BlockPos(2, 2, 2);
        TardisBlockEntity exterior = TardisGameTestSupport.placeExteriorShell(context, shellRel);
        UUID tardisId = exterior.getTardisId();

        // Place an interior door bank in the GameTest overworld (not dwm:tardis) and stamp the id.
        Direction facing = Direction.SOUTH;
        BlockPos doorOriginRel = new BlockPos(4, 2, 4);
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (int slot = 0; slot < TardisInteriorDoorBlock.BANK_WIDTH; slot++) {
                BlockPos cellRel = TardisInteriorDoorBlock.cellPos(doorOriginRel, facing, half, slot);
                context.setBlock(
                        cellRel.getX(), cellRel.getY(), cellRel.getZ(),
                        TardisInteriorDoorBlock.bankCellState(facing, half, slot, true));
            }
        }
        BlockPos doorOriginAbs = context.absolutePos(doorOriginRel);
        if (!(context.getLevel().getBlockEntity(doorOriginAbs) instanceof TardisInteriorDoorBlockEntity door)) {
            throw new AssertionError("Expected interior door BE at " + doorOriginAbs);
        }
        door.setTardisId(tardisId);

        ServerPlayer player = TardisGameTestSupport.mockServerPlayer(context);
        // Move the mock player onto the door so exit teleport has a defined starting point.
        player.snapTo(doorOriginAbs.getX() + 0.5, doorOriginAbs.getY(), doorOriginAbs.getZ() + 0.5);

        boolean exited = TardisInteriorService.tryExitToExterior(player, door);
        if (!exited) {
            throw new AssertionError("Expected tryExitToExterior to succeed with exterior location set");
        }

        BlockPos shellAbs = context.absolutePos(shellRel);
        BlockPos expectedExit = shellAbs.relative(Direction.NORTH);
        if (player.blockPosition().distManhattan(expectedExit) > 2) {
            throw new AssertionError("Expected exit near door column " + expectedExit
                    + " but player at " + player.blockPosition());
        }
        if (!player.level().dimension().equals(context.getLevel().dimension())) {
            throw new AssertionError("Exit should remain in the exterior dimension");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 40)
    public void enterFromExterior_BlockedWhileTraveling(GameTestHelper context) {
        TardisTravelService.clearActiveForTests();
        BlockPos shellRel = new BlockPos(2, 2, 2);
        TardisBlockEntity exterior = TardisGameTestSupport.placeExteriorShell(context, shellRel);
        UUID tardisId = exterior.getTardisId();
        TardisGameTestSupport.forceDoorsClosed(tardisId);

        TardisDataModel model = TardisDataLoader.get(tardisId);
        BlockPos targetRel = new BlockPos(4, 2, 2);
        TardisGameTestSupport.placeLandingPad(context, targetRel, Direction.NORTH);
        TardisGameTestSupport.selectWaypointDestination(model, context.absolutePos(targetRel), 0);

        InteractionResult started = TardisTravelService.startTravel(tardisId, context.getLevel().getServer());
        if (started != InteractionResult.SUCCESS) {
            throw new AssertionError("Expected travel to start for gate test, got " + started);
        }
        if (!TardisTravelService.isTraveling(tardisId)) {
            throw new AssertionError("Expected isTraveling after startTravel");
        }

        TardisGameTestSupport.forceDoorsFullyOpen(tardisId);
        ServerPlayer player = TardisGameTestSupport.mockServerPlayer(context);
        boolean entered = TardisInteriorService.tryEnterFromExterior(player, context.getLevel(), exterior);
        if (entered) {
            throw new AssertionError("Enter must fail while TARDIS is traveling");
        }

        TardisTravelService.clearActiveForTests();
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 40)
    public void enterFromExterior_BlockedWhenDoorsClosed(GameTestHelper context) {
        TardisTravelService.clearActiveForTests();
        BlockPos shellRel = new BlockPos(2, 2, 2);
        TardisBlockEntity exterior = TardisGameTestSupport.placeExteriorShell(context, shellRel);
        TardisGameTestSupport.forceDoorsClosed(exterior.getTardisId());

        ServerPlayer player = TardisGameTestSupport.mockServerPlayer(context);
        boolean entered = TardisInteriorService.tryEnterFromExterior(player, context.getLevel(), exterior);
        if (entered) {
            throw new AssertionError("Enter must fail when doors are closed");
        }
        context.succeed();
    }
}
