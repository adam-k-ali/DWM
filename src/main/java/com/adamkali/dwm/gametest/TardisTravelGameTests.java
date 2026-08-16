package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionResult;

import java.util.UUID;

/**
 * Dematerialise / materialise shell lifecycle against a real {@link net.minecraft.server.level.ServerLevel}.
 */
public class TardisTravelGameTests {
    /** Demat prelude (1) + demat duration + buffer for server tick scheduling. */
    private static final int TICKS_TO_IN_FLIGHT = TardisTravelService.DEMATERIALISING_DURATION_TICKS + 5;

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 280)
    public void waypointTravel_RemovesShellThenMaterialisesAtPad(GameTestHelper context) {
        TardisTravelService.clearActiveForTests();
        BlockPos shellRel = new BlockPos(1, 2, 1);
        BlockPos targetRel = new BlockPos(5, 2, 1);

        TardisBlockEntity exterior = TardisGameTestSupport.placeExteriorShell(context, shellRel);
        UUID tardisId = exterior.getTardisId();
        TardisGameTestSupport.forceDoorsClosed(tardisId);

        TardisGameTestSupport.placeLandingPad(context, targetRel, Direction.NORTH);
        BlockPos shellAbs = context.absolutePos(shellRel);
        BlockPos targetAbs = context.absolutePos(targetRel);

        TardisDataModel model = TardisDataLoader.get(tardisId);
        TardisGameTestSupport.selectWaypointDestination(model, targetAbs, 0);

        InteractionResult started = TardisTravelService.startTravel(tardisId, context.getLevel().getServer());
        if (started != InteractionResult.SUCCESS) {
            throw new AssertionError("Expected startTravel SUCCESS, got " + started);
        }

        context.runAtTickTime(context.getTick() + TICKS_TO_IN_FLIGHT, () -> {
            TardisDataModel afterDemat = TardisDataLoader.get(tardisId);
            if (afterDemat == null || afterDemat.getTravelPhase() != TardisTravelPhase.IN_FLIGHT) {
                throw new AssertionError("Expected IN_FLIGHT after demat, phase="
                        + (afterDemat == null ? "null" : afterDemat.getTravelPhase()));
            }
            if (!context.getLevel().getBlockState(shellAbs).isAir()) {
                throw new AssertionError("Expected exterior shell removed during demat at " + shellAbs);
            }

            InteractionResult matured = TardisTravelService.requestMaterialise(
                    tardisId, context.getLevel().getServer());
            if (matured != InteractionResult.SUCCESS) {
                throw new AssertionError("Expected requestMaterialise SUCCESS, got " + matured
                        + " reason=" + TardisTravelService.peekLastMaterialiseFailureReason());
            }

            BlockPos landed = new BlockPos(
                    afterDemat.exteriorX, afterDemat.exteriorY, afterDemat.exteriorZ);
            if (!(context.getLevel().getBlockEntity(landed) instanceof TardisBlockEntity landedBe)) {
                throw new AssertionError("Expected TARDIS shell block entity at landing " + landed);
            }
            if (!tardisId.equals(landedBe.getTardisId())) {
                throw new AssertionError("Landed shell tardisId mismatch");
            }
            if (landed.distManhattan(targetAbs) > 8) {
                throw new AssertionError("Landing " + landed + " too far from waypoint target " + targetAbs);
            }
            if (!context.getLevel().getBlockState(landed).is(DWMBlocks.TARDIS_BLOCK)) {
                throw new AssertionError("Expected TARDIS_BLOCK at landing " + landed);
            }

            TardisTravelService.clearActiveForTests();
            context.succeed();
        });
    }
}
