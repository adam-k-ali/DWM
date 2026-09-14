package com.adamkali.dwm.gametest;

import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.DalekPatrolLogic;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.world.SkaroDimensions;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public class DalekPopulationGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void daytimeGroundAllowsCommandButNotNaturalDalekPlacement(GameTestHelper context) {
        context.setTime(6000);
        BlockPos groundRel = new BlockPos(2, 1, 2);
        context.setBlock(groundRel, Blocks.STONE);
        context.setBlock(groundRel.above(), Blocks.AIR);
        context.setBlock(groundRel.above(2), Blocks.AIR);
        BlockPos spawnPos = context.absolutePos(groundRel.above());

        boolean natural = SpawnPlacements.checkSpawnRules(
                DWMEntityTypes.DALEK,
                context.getLevel(),
                EntitySpawnReason.NATURAL,
                spawnPos,
                RandomSource.create()
        );
        if (natural) {
            throw new AssertionError("NATURAL Dalek placement must be rejected outside Skaro");
        }

        boolean command = SpawnPlacements.checkSpawnRules(
                DWMEntityTypes.DALEK,
                context.getLevel(),
                EntitySpawnReason.COMMAND,
                spawnPos,
                RandomSource.create()
        );
        if (!command) {
            throw new AssertionError("Expected daylight COMMAND Dalek placement on valid ground");
        }

        ServerLevel skaro = context.getLevel().getServer().getLevel(SkaroDimensions.SKARO_WORLD_KEY);
        if (skaro != null) {
            BlockPos ground = new BlockPos(8, 80, 8);
            skaro.getChunk(ground);
            skaro.setBlock(ground, Blocks.STONE.defaultBlockState(), 3);
            skaro.setBlock(ground.above(), Blocks.AIR.defaultBlockState(), 3);
            skaro.setBlock(ground.above(2), Blocks.AIR.defaultBlockState(), 3);
            boolean skaroNatural = SpawnPlacements.checkSpawnRules(
                    DWMEntityTypes.DALEK,
                    skaro,
                    EntitySpawnReason.NATURAL,
                    ground.above(),
                    RandomSource.create()
            );
            if (!skaroNatural) {
                throw new AssertionError("Expected NATURAL Dalek placement on daytime Skaro ground");
            }
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 40)
    public void idleDalekSharesPlayerTargetWithinRadius(GameTestHelper context) {
        BlockPos floor = new BlockPos(1, 1, 1);
        placeFloor(context, floor, 6, 4);
        context.setBlock(new BlockPos(3, 2, 2), Blocks.STONE);
        context.setBlock(new BlockPos(3, 3, 2), Blocks.STONE);
        context.setBlock(new BlockPos(3, 2, 3), Blocks.STONE);
        context.setBlock(new BlockPos(3, 3, 3), Blocks.STONE);

        DalekEntity hunter = context.spawn(DWMEntityTypes.DALEK, new BlockPos(1, 2, 2));
        DalekEntity idle = context.spawn(DWMEntityTypes.DALEK, new BlockPos(5, 2, 2));
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(hunter.getX(), hunter.getY(), hunter.getZ() + 1.5);
        hunter.setTarget(player);

        context.runAtTickTime(10, () -> {
            if (idle.getTarget() != player) {
                throw new AssertionError("Expected idle Dalek to share the nearby player target");
            }
            if (hunter.distanceTo(idle) > DalekPatrolLogic.TARGET_SHARE_RADIUS) {
                throw new AssertionError("Share test Daleks must start inside the patrol radius");
            }
            context.succeed();
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void idleDalekDoesNotSharePlayerTargetBeyondRadius(GameTestHelper context) {
        BlockPos floor = new BlockPos(1, 1, 1);
        placeFloor(context, floor, 6, 4);
        context.setBlock(new BlockPos(3, 2, 2), Blocks.STONE);
        context.setBlock(new BlockPos(3, 3, 2), Blocks.STONE);
        context.setBlock(new BlockPos(3, 2, 3), Blocks.STONE);
        context.setBlock(new BlockPos(3, 3, 3), Blocks.STONE);

        DalekEntity hunter = context.spawn(DWMEntityTypes.DALEK, new BlockPos(1, 2, 2));
        DalekEntity idle = context.spawn(DWMEntityTypes.DALEK, new BlockPos(5, 2, 2));
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(hunter.getX(), hunter.getY(), hunter.getZ() + 1.5);
        hunter.setTarget(player);

        double originX = idle.getX();
        double originY = idle.getY();
        double originZ = idle.getZ();
        idle.snapTo(originX + DalekPatrolLogic.TARGET_SHARE_RADIUS + 4.0, originY, originZ);
        for (int i = 0; i < 20; i++) {
            idle.tick();
        }
        if (hunter.distanceTo(idle) <= DalekPatrolLogic.TARGET_SHARE_RADIUS) {
            idle.snapTo(originX, originY, originZ);
            throw new AssertionError("Beyond-radius Daleks must start outside the patrol radius");
        }
        if (idle.getTarget() == player) {
            idle.snapTo(originX, originY, originZ);
            throw new AssertionError("Idle Dalek must not share a player target beyond the patrol radius");
        }
        idle.snapTo(originX, originY, originZ);
        context.succeed();
    }

    private static void placeFloor(GameTestHelper context, BlockPos origin, int width, int depth) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                context.setBlock(origin.offset(x, 0, z), Blocks.STONE);
            }
        }
    }
}
