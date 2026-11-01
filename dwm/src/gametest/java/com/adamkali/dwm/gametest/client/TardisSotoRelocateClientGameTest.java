package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BS-02: after the TARDIS relocates, the interior look-out portal (SOTO) must show the new location, not the old
 * one. Covers a short hop inside the overworld (the footprint shifts but its chunk keys overlap) and a move to the
 * Nether at the same X/Z (identical chunk keys, different dimension). After each move the client's ghost origin
 * must equal the new footprint origin and every sampled block must match the server.
 */
public class TardisSotoRelocateClientGameTest implements FabricClientGameTest {
    private static final double VIEWER_LOCAL_X = 5.5;
    private static final double VIEWER_LOCAL_Y = 1.0;
    private static final double VIEWER_LOCAL_Z = 5.5;

    /** Footprint size of the SOTO exterior sample (x, y, z). */
    private static final int FOOTPRINT_X = 11;
    private static final int FOOTPRINT_Y = 7;
    private static final int FOOTPRINT_Z = 11;

    private static final int SYNC_WAIT_TICKS = 400;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            BlockPos start = new BlockPos(0, ClientGameTestSupport.PAD_Y, 4);
            BlockPos hop = start.offset(3, 0, 0);

            ClientGameTestSupport.stage(world.getServer(), "spectator", true);
            world.getServer().runCommand("execute in minecraft:overworld run forceload add -48 -48 48 48");
            world.getServer().runCommand("execute in minecraft:the_nether run forceload add -48 -48 48 48");
            prepareLandingArea(world, Level.OVERWORLD, Blocks.WOOL.red(), Blocks.WOOL.blue());
            prepareLandingArea(world, Level.NETHER, Blocks.GOLD_BLOCK, Blocks.GLOWSTONE);

            UUID id = ClientGameTestSupport.placeTardisWithOpenDoor(world.getServer(), start);
            ClientGameTestSupport.waitForInteriorReady(world.getServer(), start, id);

            BlockPos plot = TardisPlotAllocator.plotOrigin(id);
            world.getServer().runCommand(String.format(
                    "execute in dwm:tardis run tp @a %.1f %.1f %.1f 180 0",
                    plot.getX() + VIEWER_LOCAL_X, plot.getY() + VIEWER_LOCAL_Y, plot.getZ() + VIEWER_LOCAL_Z));

            ClientGameTestSupport.toggleHud(context);
            ClientGameTestSupport.waitForPortalRendered(context, PortalStreamKind.SOTO, id);
            awaitGhostMatchesServer(context, world, id, Level.OVERWORLD, start);

            relocate(world, id, Level.OVERWORLD, hop);
            awaitGhostMatchesServer(context, world, id, Level.OVERWORLD, hop);
            context.waitTicks(10);
            ClientGameTestSupport.screenshot(context, "soto_relocate_hop");

            BlockPos nether = new BlockPos(hop.getX(), hop.getY(), hop.getZ());
            relocate(world, id, Level.NETHER, nether);
            awaitGhostMatchesServer(context, world, id, Level.NETHER, nether);
            int wool = context.computeOnClient(client -> countWool(SotoGhostExterior.get(PortalStreamKind.SOTO, id)));
            if (wool != 0) {
                throw new AssertionError(wool + " overworld wool blocks are still visible after moving to the Nether");
            }
            context.waitTicks(10);
            ClientGameTestSupport.screenshot(context, "soto_relocate_nether");
            ClientGameTestSupport.toggleHud(context);
        }
    }

    /**
     * Flat stone floor one block below {@link ClientGameTestSupport#PAD_Y}, cleared air above it and two landmark
     * rows, so the overworld and Nether footprints differ everywhere the client could keep stale data.
     */
    private static void prepareLandingArea(
            TestSingleplayerContext world, ResourceKey<Level> dimension, Block rowA, Block rowB) {
        world.getServer().runOnServer(server -> {
            ServerLevel level = server.getLevel(dimension);
            int floorY = ClientGameTestSupport.PAD_Y - 1;
            for (int x = -12; x <= 14; x++) {
                for (int z = -8; z <= 16; z++) {
                    level.setBlock(new BlockPos(x, floorY, z), Blocks.STONE.defaultBlockState(), 3);
                    for (int y = floorY + 1; y <= floorY + 8; y++) {
                        level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
            for (int x = -2; x <= 8; x++) {
                level.setBlock(new BlockPos(x, ClientGameTestSupport.PAD_Y, 0), rowA.defaultBlockState(), 3);
                level.setBlock(new BlockPos(x, ClientGameTestSupport.PAD_Y, 8), rowB.defaultBlockState(), 3);
            }
        });
    }

    /** Lands the TARDIS at {@code landing} in {@code dimension} via the real materialise path. */
    private static void relocate(
            TestSingleplayerContext world, UUID id, ResourceKey<Level> dimension, BlockPos landing) {
        world.getServer().runOnServer(server -> {
            TardisDataModel model = TardisDataLoader.get(id);
            if (model == null) {
                throw new AssertionError("Missing TARDIS data for " + id);
            }
            model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);
            TardisTravelService.putFlightShellForTests(id, id);
            InteractionResult result =
                    TardisTravelService.materialiseAt(id, server, server.getLevel(dimension), landing, 0);
            if (result != InteractionResult.SUCCESS) {
                throw new AssertionError("Expected materialise at " + landing + " in " + dimension.identifier()
                        + " to succeed, got " + result);
            }
        });
    }

    /**
     * Waits for the client ghost to adopt the new footprint origin, then for every block of the 11x7x11 footprint
     * to equal what the server has. Fails with the remaining mismatch count if it never converges.
     */
    private static void awaitGhostMatchesServer(
            ClientGameTestContext context,
            TestSingleplayerContext world,
            UUID id,
            ResourceKey<Level> dimension,
            BlockPos exteriorPos) {
        BlockPos expectedOrigin = SotoExteriorSampler.footprintOrigin(exteriorPos);
        Map<BlockPos, BlockState> expected = world.getServer().computeOnServer(server -> {
            ServerLevel level = server.getLevel(dimension);
            Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
            for (int dx = 0; dx < FOOTPRINT_X; dx++) {
                for (int dy = 0; dy < FOOTPRINT_Y; dy++) {
                    for (int dz = 0; dz < FOOTPRINT_Z; dz++) {
                        BlockState state = level.getBlockState(expectedOrigin.offset(dx, dy, dz));
                        blocks.put(new BlockPos(dx, dy, dz),
                                SotoExteriorSampler.isSotoVisible(state) ? state : Blocks.AIR.defaultBlockState());
                    }
                }
            }
            return blocks;
        });

        String last = "ghost never received any chunks";
        for (int waited = 0; waited < SYNC_WAIT_TICKS; waited += 10) {
            String problem = context.computeOnClient(client -> describeProblem(id, expectedOrigin, expected));
            if (problem == null) {
                return;
            }
            last = problem;
            context.waitTicks(10);
        }
        throw new AssertionError("SOTO ghost for TARDIS " + id + " did not match the server in "
                + dimension.identifier() + " around " + exteriorPos + ": " + last);
    }

    /** Null when the ghost matches; otherwise a short description of the first problem. */
    private static String describeProblem(UUID id, BlockPos expectedOrigin, Map<BlockPos, BlockState> expected) {
        SotoGhostExterior ghost = SotoGhostExterior.get(PortalStreamKind.SOTO, id);
        if (ghost == null) {
            return "no ghost on the client";
        }
        if (!expectedOrigin.equals(ghost.footprintOrigin())) {
            return "client origin " + ghost.footprintOrigin() + " but expected " + expectedOrigin;
        }
        int mismatches = 0;
        for (Map.Entry<BlockPos, BlockState> entry : expected.entrySet()) {
            if (!entry.getValue().equals(ghost.getBlockState(entry.getKey()))) {
                mismatches++;
            }
        }
        return mismatches == 0 ? null : mismatches + " of " + expected.size() + " sampled blocks differ";
    }

    private static int countWool(SotoGhostExterior ghost) {
        if (ghost == null) {
            return 0;
        }
        int wool = 0;
        for (int dx = 0; dx < FOOTPRINT_X; dx++) {
            for (int dy = 0; dy < FOOTPRINT_Y; dy++) {
                for (int dz = 0; dz < FOOTPRINT_Z; dz++) {
                    if (ghost.getBlockState(new BlockPos(dx, dy, dz)).is(net.minecraft.tags.BlockTags.WOOL)) {
                        wool++;
                    }
                }
            }
        }
        return wool;
    }
}
