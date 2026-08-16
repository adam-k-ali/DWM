package com.adamkali.dwm.gametest;

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

import java.util.UUID;

/**
 * Enter/exit teleport paths and travel-gated blocking for {@link TardisInteriorService}.
 */
public class TardisTeleportGameTests {

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 100)
    public void enterFromExterior_TeleportsIntoTardisDimension(GameTestHelper context) {
        TardisTravelService.clearActiveForTests();
        BlockPos shellRel = new BlockPos(2, 2, 2);
        TardisBlockEntity exterior = TardisGameTestSupport.placeExteriorShell(context, shellRel);
        UUID tardisId = exterior.getTardisId();
        TardisGameTestSupport.forceDoorsFullyOpen(tardisId);

        ServerLevel tardisWorld = TardisGameTestSupport.requireTardisDimension(context);
        ServerPlayer player = TardisGameTestSupport.mockServerPlayer(context);

        boolean entered = TardisInteriorService.tryEnterFromExterior(player, context.getLevel(), exterior);
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
        // Interior plot should have floor under entrance from the placer.
        if (tardisWorld.getBlockState(entrance.below()).isAir()) {
            throw new AssertionError("Expected solid floor under interior entrance");
        }

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 100)
    public void exitToExterior_TeleportsBesideShell(GameTestHelper context) {
        TardisTravelService.clearActiveForTests();
        BlockPos shellRel = new BlockPos(2, 2, 2);
        TardisBlockEntity exterior = TardisGameTestSupport.placeExteriorShell(context, shellRel);
        UUID tardisId = exterior.getTardisId();
        TardisGameTestSupport.forceDoorsFullyOpen(tardisId);

        ServerPlayer player = TardisGameTestSupport.mockServerPlayer(context);
        if (!TardisInteriorService.tryEnterFromExterior(player, context.getLevel(), exterior)) {
            throw new AssertionError("Enter failed before exit test");
        }

        BlockPos doorOrigin = exterior.getInteriorEntrance() == null
                ? null
                : findInteriorDoorOrigin(player.level().getServer().getLevel(TardisDimensions.TARDIS_WORLD_KEY), tardisId);
        if (doorOrigin == null) {
            // Fall back: scan near entrance for a door BE stamped with this id.
            doorOrigin = findInteriorDoorNearEntrance(
                    player.level().getServer().getLevel(TardisDimensions.TARDIS_WORLD_KEY),
                    exterior.getInteriorEntrance(),
                    tardisId);
        }
        if (doorOrigin == null) {
            throw new AssertionError("Could not locate interior door block entity for exit");
        }
        ServerLevel interior = player.level().getServer().getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (!(interior.getBlockEntity(doorOrigin) instanceof TardisInteriorDoorBlockEntity door)) {
            throw new AssertionError("Expected TardisInteriorDoorBlockEntity at " + doorOrigin);
        }

        boolean exited = TardisInteriorService.tryExitToExterior(player, door);
        if (!exited) {
            throw new AssertionError("Expected tryExitToExterior to succeed");
        }
        if (TardisDimensions.isTardisWorld(player.level())) {
            throw new AssertionError("Player should leave dwm:tardis after exit");
        }

        BlockPos shellAbs = context.absolutePos(shellRel);
        BlockPos expectedExit = shellAbs.relative(Direction.NORTH);
        if (player.blockPosition().distManhattan(expectedExit) > 2) {
            throw new AssertionError("Expected exit near door column " + expectedExit
                    + " but player at " + player.blockPosition());
        }

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 40)
    public void enterFromExterior_BlockedWhileTraveling(GameTestHelper context) {
        TardisTravelService.clearActiveForTests();
        BlockPos shellRel = new BlockPos(2, 2, 2);
        TardisBlockEntity exterior = TardisGameTestSupport.placeExteriorShell(context, shellRel);
        UUID tardisId = exterior.getTardisId();
        TardisGameTestSupport.forceDoorsFullyOpen(tardisId);
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

        // Re-open doors for the enter attempt; travel gate must still block.
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

    private static BlockPos findInteriorDoorOrigin(ServerLevel interior, UUID tardisId) {
        if (interior == null) {
            return null;
        }
        // Console room door origin is stamped during place; scan a bounded box around the plot.
        BlockPos plot = com.adamkali.dwm.tardis.interior.TardisPlotAllocator.plotOrigin(tardisId);
        for (int dy = 0; dy < 12; dy++) {
            for (int dx = 0; dx < 24; dx++) {
                for (int dz = 0; dz < 24; dz++) {
                    BlockPos pos = plot.offset(dx, dy, dz);
                    if (interior.getBlockEntity(pos) instanceof TardisInteriorDoorBlockEntity door
                            && tardisId.equals(door.getTardisId())) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private static BlockPos findInteriorDoorNearEntrance(ServerLevel interior, BlockPos entrance, UUID tardisId) {
        if (interior == null || entrance == null) {
            return null;
        }
        for (BlockPos pos : BlockPos.betweenClosed(entrance.offset(-8, -2, -8), entrance.offset(8, 6, 8))) {
            if (interior.getBlockEntity(pos) instanceof TardisInteriorDoorBlockEntity door
                    && tardisId.equals(door.getTardisId())) {
                return pos.immutable();
            }
        }
        return null;
    }
}
