package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.entity.ThalEntity;
import com.adamkali.dwm.gametest.StructureGameTestSupport;
import com.adamkali.dwm.world.structure.ThalFrame;
import com.adamkali.dwm.world.structure.ThalSettlementLayout;
import com.adamkali.dwm.world.structure.ThalSettlementLayout.Kind;
import com.adamkali.dwm.world.structure.ThalSettlementLayout.PlannedPiece;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Screenshots of a Thal plateau settlement built from a fixed seed on a flat dirt plateau:
 * {@code thal_settlement} (high three-quarter aerial of the whole settlement) and
 * {@code thal_settlement_close} (lower three-quarter view of the shelter with a hut, a field and residents).
 *
 * <p>Residents are frozen (NoAI, silent) so the shots are stable. For the close shot the shelter's residents are
 * stood on the path outside its door, since inside the building they would be hidden by the walls.
 *
 * <p>Registered after the portal scenarios on purpose: in a full run, placing it before them made the SOTO portal
 * composite wait time out (each of the portal tests passes alone or with this one; cause not yet isolated).
 */
public class ThalSettlementClientGameTest implements FabricClientGameTest {
    /** Plans a settlement with a shelter, five huts, two fields and a watch post (asserted below). */
    private static final long SEED = 20260725L;
    private static final int FLOOR_Y = ClientGameTestSupport.PAD_Y;
    private static final int CENTRE_X = 0;
    private static final int CENTRE_Z = 0;
    /** Plateau half-extent: the layout bound plus a margin of dirt around it. */
    private static final int RADIUS = ThalSettlementLayout.BOUND + 10;
    private static final int RESIDENTS_VISIBLE_TICKS = 10;
    private static final int RENDER_TICKS = 60;

    @Override
    public void runTest(ClientGameTestContext context) {
        ThalSettlementLayout.Layout layout = ThalSettlementLayout.plan(SEED);
        assertEveryKind(layout);

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            ClientGameTestSupport.stage(world.getServer(), "spectator");
            ClientGameTestSupport.toggleHud(context);
            try {
                world.getServer().runOnServer(server -> {
                    ServerLevel level = server.overworld();
                    forceChunks(level, true);
                    layPlateau(level);
                    StructureGameTestSupport.placeThalSettlement(level, layout, CENTRE_X, CENTRE_Z, FLOOR_Y);
                });
                // Freshly spawned residents only become queryable once their chunk is entity-accessible.
                context.waitTicks(RESIDENTS_VISIBLE_TICKS);
                world.getServer().runOnServer(server -> freezeResidents(server.overworld()));

                // Aerial: from the south-west, high above the settlement, looking at its middle.
                aim(context, world, new BlockPos(-22, FLOOR_Y + 27, 28), new BlockPos(3, FLOOR_Y, -4));
                context.waitTicks(RENDER_TICKS);
                ClientGameTestSupport.screenshot(context, "thal_settlement");

                // Close: in front of the shelter, a little off-axis, looking at its door side.
                PlannedPiece shelter = layout.shelter();
                ThalFrame frame = shelter.frame().translated(CENTRE_X, CENTRE_Z);
                world.getServer().runOnServer(server -> bringShelterResidentsOutside(server.overworld(), frame));
                BlockPos door = new BlockPos(frame.worldX(frame.doorU(), 0), FLOOR_Y, frame.worldZ(frame.doorU(), 0));
                int outX = frame.facing().getStepX();
                int outZ = frame.facing().getStepZ();
                int sideX = -outZ;
                int sideZ = outX;
                BlockPos camera = door.offset(outX * 12 + sideX * 6, 7, outZ * 12 + sideZ * 6);
                aim(context, world, camera, door.offset(-outX * 2, 2, -outZ * 2));
                context.waitTicks(RENDER_TICKS);
                ClientGameTestSupport.screenshot(context, "thal_settlement_close");
            } finally {
                world.getServer().runOnServer(server -> {
                    ServerLevel level = server.overworld();
                    for (ThalEntity thal : residents(level)) {
                        thal.discard();
                    }
                    forceChunks(level, false);
                });
            }
            ClientGameTestSupport.toggleHud(context);
        }
    }

    private static void assertEveryKind(ThalSettlementLayout.Layout layout) {
        if (layout.ofKind(Kind.SHELTER).size() != 1
                || layout.ofKind(Kind.HUT).size() < ThalSettlementLayout.HUT_MIN
                || layout.ofKind(Kind.FIELD).size() != ThalSettlementLayout.FIELD_COUNT
                || layout.ofKind(Kind.WATCH_POST).size() != 1) {
            throw new AssertionError("Seed " + SEED + " no longer plans a shelter, huts, two fields and a watch post");
        }
    }

    /** Teleports, waits for the position packet to land, then aims; the packet would otherwise reset the rotation. */
    private static void aim(ClientGameTestContext context, TestSingleplayerContext world, BlockPos camera, BlockPos target) {
        world.getServer().runCommand("tp @a " + camera.getX() + " " + camera.getY() + " " + camera.getZ());
        context.waitTicks(5);
        context.getInput().lookAt(target);
    }

    private static void forceChunks(ServerLevel level, boolean forced) {
        for (int cx = (CENTRE_X - RADIUS) >> 4; cx <= (CENTRE_X + RADIUS) >> 4; cx++) {
            for (int cz = (CENTRE_Z - RADIUS) >> 4; cz <= (CENTRE_Z + RADIUS) >> 4; cz++) {
                level.setChunkForced(cx, cz, forced);
                if (forced) {
                    level.getChunk(cx, cz);
                }
            }
        }
    }

    /**
     * Stone base, two dirt layers and a dirt surface at the floor level with a deterministic coarse-dirt
     * checker, plus clear air above. The stage pad's stone is replaced where it overlaps.
     */
    private static void layPlateau(ServerLevel level) {
        for (int x = CENTRE_X - RADIUS; x <= CENTRE_X + RADIUS; x++) {
            for (int z = CENTRE_Z - RADIUS; z <= CENTRE_Z + RADIUS; z++) {
                level.setBlock(new BlockPos(x, FLOOR_Y - 3, z), Blocks.STONE.defaultBlockState(), 2);
                level.setBlock(new BlockPos(x, FLOOR_Y - 2, z), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(new BlockPos(x, FLOOR_Y - 1, z), Blocks.DIRT.defaultBlockState(), 2);
                boolean coarse = (((x >> 1) + (z >> 1)) & 1) == 0;
                level.setBlock(new BlockPos(x, FLOOR_Y, z),
                        (coarse ? Blocks.COARSE_DIRT : Blocks.DIRT).defaultBlockState(), 2);
                for (int y = 1; y <= 20; y++) {
                    level.setBlock(new BlockPos(x, FLOOR_Y + y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private static List<ThalEntity> residents(ServerLevel level) {
        return level.getEntitiesOfClass(ThalEntity.class, new AABB(
                CENTRE_X - RADIUS, FLOOR_Y - 4, CENTRE_Z - RADIUS,
                CENTRE_X + RADIUS + 1, FLOOR_Y + 20, CENTRE_Z + RADIUS + 1));
    }

    private static void freezeResidents(ServerLevel level) {
        List<ThalEntity> thals = residents(level);
        if (thals.isEmpty()) {
            throw new AssertionError("The settlement spawned no Thal residents");
        }
        for (ThalEntity thal : thals) {
            thal.setNoAi(true);
            thal.setSilent(true);
        }
    }

    /** Stands the shelter's residents on the path in front of its door, side by side. */
    private static void bringShelterResidentsOutside(ServerLevel level, ThalFrame frame) {
        List<ThalEntity> inside = level.getEntitiesOfClass(ThalEntity.class, new AABB(
                frame.minX(), FLOOR_Y, frame.minZ(), frame.maxX() + 1, FLOOR_Y + 5, frame.maxZ() + 1));
        int outX = frame.facing().getStepX();
        int outZ = frame.facing().getStepZ();
        int index = 0;
        for (ThalEntity thal : inside) {
            int side = index++ % 2 == 0 ? 1 : -1;
            int step = (index + 1) / 2;
            double x = frame.worldX(frame.doorU(), 0) + 0.5 + outX * 2.0 - outZ * side * step;
            double z = frame.worldZ(frame.doorU(), 0) + 0.5 + outZ * 2.0 + outX * side * step;
            thal.snapTo(x, FLOOR_Y + 1, z, thal.getYRot(), 0.0f);
        }
    }
}
