package com.adamkali.dwm.gametest;

import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.DalekGarrisonLogic;
import com.adamkali.dwm.entity.DalekJob;
import com.adamkali.dwm.entity.ThalEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class DalekGarrisonGameTests {
    private static final float NO_YAW = 0.0F;

    /** Invulnerable targets fail vanilla's {@code canBeSeenAsEnemy}, which drops them; a huge health pool does not. */
    private static void durable(ThalEntity thal) {
        thal.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        thal.setHealth(1000.0F);
    }

    /** Other tests share the level: keep their mock players from pulling idle jobs into combat. */
    private static void ignoreStrangers(GameTestHelper context, DalekEntity dalek) {
        context.onEachTick(() -> dalek.setTarget(null));
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 300)
    public void sentryReturnsToPostAndGoesOnDuty(GameTestHelper context) {
        ThalEntity floor = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.THAL);
        floor.discard();
        BlockPos post = context.absolutePos(new BlockPos(1, 2, 1));
        DalekEntity sentry = context.spawn(DWMEntityTypes.DALEK, new BlockPos(1, 2, 1));
        sentry.assignJob(DalekJob.SENTRY, post, NO_YAW, 0, List.of());
        ignoreStrangers(context, sentry);
        sentry.teleportTo(post.getX() + 5.5, post.getY(), post.getZ() + 5.5);
        context.succeedWhen(() -> {
            context.assertTrue(sentry.isOnDuty(), "Sentry should be on duty at its post");
            context.assertTrue(
                    sentry.distanceToSqr(post.getX() + 0.5, post.getY(), post.getZ() + 0.5) <= 4.0,
                    "Sentry should be back at its post");
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 400)
    public void lookoutFliesBackToPlatform(GameTestHelper context) {
        ThalEntity floor = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.THAL);
        floor.discard();
        context.setBlock(new BlockPos(1, 5, 1), Blocks.STONE.defaultBlockState());
        BlockPos post = context.absolutePos(new BlockPos(1, 6, 1));
        DalekEntity lookout = context.spawn(DWMEntityTypes.DALEK, new BlockPos(6, 2, 6));
        lookout.assignJob(DalekJob.LOOKOUT, post, NO_YAW, 0, List.of());
        ignoreStrangers(context, lookout);
        context.succeedWhen(() -> {
            context.assertTrue(lookout.isOnDuty(), "Lookout should be on duty");
            context.assertTrue(
                    lookout.getY() >= post.getY() - 0.5,
                    "Lookout should be back on the platform");
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 600)
    public void patrollerWalksItsRing(GameTestHelper context) {
        ThalEntity floor = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.THAL);
        floor.discard();
        BlockPos post = context.absolutePos(new BlockPos(4, 2, 4));
        DalekEntity patroller = context.spawn(DWMEntityTypes.DALEK, new BlockPos(4, 2, 4));
        patroller.assignJob(DalekJob.PATROL, post, NO_YAW, 3, List.of());
        ignoreStrangers(context, patroller);
        List<BlockPos> ring = DalekGarrisonLogic.patrolWaypoints(post, 3, DalekGarrisonLogic.PATROL_WAYPOINT_COUNT);
        Set<BlockPos> visited = new HashSet<>();
        context.succeedWhen(() -> {
            for (BlockPos waypoint : ring) {
                double dx = patroller.getX() - (waypoint.getX() + 0.5);
                double dz = patroller.getZ() - (waypoint.getZ() + 0.5);
                if (dx * dx + dz * dz <= 7.0) {
                    visited.add(waypoint);
                }
            }
            context.assertTrue(visited.size() >= 2, "Patroller should reach at least two waypoints, reached " + visited.size());
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 900)
    public void courierRunsToTheFarEndOfItsRouteAndBack(GameTestHelper context) {
        ThalEntity floor = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.THAL);
        floor.discard();
        BlockPos post = context.absolutePos(new BlockPos(1, 2, 1));
        BlockPos far = context.absolutePos(new BlockPos(1, 2, 8));
        DalekEntity courier = context.spawn(DWMEntityTypes.DALEK, new BlockPos(1, 2, 1));
        courier.assignJob(DalekJob.COURIER, post, NO_YAW, 0, List.of(far));
        ignoreStrangers(context, courier);
        AtomicInteger phase = new AtomicInteger();
        context.succeedWhen(() -> {
            double toFar = courier.distanceToSqr(far.getX() + 0.5, far.getY(), far.getZ() + 0.5);
            double toPost = courier.distanceToSqr(post.getX() + 0.5, post.getY(), post.getZ() + 0.5);
            if (phase.get() == 0 && toFar <= 6.25) {
                phase.set(1);
            }
            context.assertTrue(phase.get() == 1 && toPost <= 6.25, "Courier should reach the far end and come back");
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 300)
    public void gunnerHoldsItsPostLikeASentry(GameTestHelper context) {
        ThalEntity floor = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.THAL);
        floor.discard();
        BlockPos post = context.absolutePos(new BlockPos(1, 2, 1));
        DalekEntity gunner = context.spawn(DWMEntityTypes.DALEK, new BlockPos(1, 2, 1));
        gunner.assignJob(DalekJob.GUNNER, post, NO_YAW, 0, List.of());
        ignoreStrangers(context, gunner);
        gunner.teleportTo(post.getX() + 5.5, post.getY(), post.getZ() + 5.5);
        context.succeedWhen(() -> {
            context.assertTrue(gunner.isOnDuty(), "Gunner should be on duty at its post");
            context.assertTrue(
                    gunner.distanceToSqr(post.getX() + 0.5, post.getY(), post.getZ() + 0.5) <= 4.0,
                    "Gunner should be back at its post");
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 600)
    public void technicianVisitsWorkSpot(GameTestHelper context) {
        ThalEntity floor = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.THAL);
        floor.discard();
        BlockPos post = context.absolutePos(new BlockPos(1, 2, 1));
        BlockPos spot = context.absolutePos(new BlockPos(7, 2, 7));
        DalekEntity technician = context.spawn(DWMEntityTypes.DALEK, new BlockPos(1, 2, 1));
        technician.assignJob(DalekJob.TECHNICIAN, post, NO_YAW, 0, List.of(spot));
        ignoreStrangers(context, technician);
        context.succeedWhen(() -> context.assertTrue(
                technician.distanceToSqr(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5) <= 6.25,
                "Technician should walk to its work spot"));
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 400)
    public void lookoutAlarmReachesGarrisonButNotWildDaleks(GameTestHelper context) {
        ThalEntity intruder = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.THAL);
        intruder.setNoAi(true);
        durable(intruder);
        BlockPos lookoutPos = context.absolutePos(new BlockPos(4, 2, 4));
        DalekEntity lookout = context.spawn(DWMEntityTypes.DALEK, new BlockPos(4, 2, 4));
        lookout.assignJob(DalekJob.LOOKOUT, lookoutPos, NO_YAW, 0, List.of());
        DalekEntity farGarrison = context.spawn(DWMEntityTypes.DALEK, new BlockPos(4, 2, 4));
        farGarrison.assignJob(DalekJob.SENTRY, lookoutPos, NO_YAW, 0, List.of());
        farGarrison.setNoAi(true);
        farGarrison.setNoGravity(true);
        farGarrison.teleportTo(lookoutPos.getX() + 30.5, lookoutPos.getY(), lookoutPos.getZ() + 0.5);
        DalekEntity wild = context.spawn(DWMEntityTypes.DALEK, new BlockPos(5, 2, 5));
        wild.setNoAi(true);
        context.onEachTick(() -> lookout.setTarget(intruder));
        context.succeedWhen(() -> {
            context.assertTrue(farGarrison.getTarget() != null, "Lookout alarm should reach a garrison Dalek 30 blocks away");
            context.assertTrue(wild.getTarget() == null, "Alarm must not recruit a wild Dalek");
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 100)
    public void leashDropsTargetBeyondFortyBlocks(GameTestHelper context) {
        ThalEntity intruder = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.THAL);
        intruder.setNoAi(true);
        durable(intruder);
        BlockPos farPost = context.absolutePos(new BlockPos(4, 2, 4)).offset(50, 0, 0);
        DalekEntity strayed = context.spawn(DWMEntityTypes.DALEK, new BlockPos(3, 2, 3));
        strayed.assignJob(DalekJob.SENTRY, farPost, NO_YAW, 0, List.of());
        DalekEntity local = context.spawn(DWMEntityTypes.DALEK, new BlockPos(5, 2, 5));
        local.assignJob(DalekJob.SENTRY, context.absolutePos(new BlockPos(5, 2, 5)), NO_YAW, 0, List.of());
        AtomicInteger strayedLost = new AtomicInteger();
        AtomicInteger localLost = new AtomicInteger();
        strayed.setTarget(intruder);
        local.setTarget(intruder);
        // Re-pin every tick so the only difference between the two is the leash.
        context.onEachTick(() -> {
            if (strayed.getTarget() == null) {
                strayedLost.incrementAndGet();
            }
            if (local.getTarget() == null) {
                localLost.incrementAndGet();
            }
            strayed.setTarget(intruder);
            local.setTarget(intruder);
        });
        context.runAfterDelay(30, () -> {
            context.assertTrue(strayedLost.get() > 0, "A Dalek beyond the leash should drop its target");
            context.assertTrue(localLost.get() == 0, "A Dalek inside the leash should keep its target");
            context.succeed();
        });
    }
}
