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
    public void daytimeSkaroAllowsNaturalDalekPlacement(GameTestHelper context) {
        ServerLevel skaro = requireSkaro(context);
        context.setTime(6000);
        BlockPos ground = new BlockPos(8, 80, 8);
        skaro.getChunk(ground);
        skaro.setBlock(ground, Blocks.STONE.defaultBlockState(), 3);
        skaro.setBlock(ground.above(), Blocks.AIR.defaultBlockState(), 3);
        skaro.setBlock(ground.above(2), Blocks.AIR.defaultBlockState(), 3);

        boolean allowed = SpawnPlacements.checkSpawnRules(
                DWMEntityTypes.DALEK,
                skaro,
                EntitySpawnReason.NATURAL,
                ground.above(),
                RandomSource.create()
        );
        if (!allowed) {
            throw new AssertionError("Expected NATURAL Dalek placement on daytime Skaro ground");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void naturalSpawnIsRejectedOutsideSkaro(GameTestHelper context) {
        BlockPos groundRel = new BlockPos(2, 1, 2);
        context.setBlock(groundRel, Blocks.STONE);
        context.setBlock(groundRel.above(), Blocks.AIR);
        context.setBlock(groundRel.above(2), Blocks.AIR);

        boolean allowed = SpawnPlacements.checkSpawnRules(
                DWMEntityTypes.DALEK,
                context.getLevel(),
                EntitySpawnReason.NATURAL,
                context.absolutePos(groundRel.above()),
                RandomSource.create()
        );
        if (allowed) {
            throw new AssertionError("NATURAL Dalek placement must be rejected outside Skaro");
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

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 40)
    public void idleDalekDoesNotSharePlayerTargetBeyondRadius(GameTestHelper context) {
        ServerLevel skaro = requireSkaro(context);
        BlockPos hunterGround = new BlockPos(48, 80, 48);
        BlockPos idleGround = hunterGround.east((int) DalekPatrolLogic.TARGET_SHARE_RADIUS + 4);
        skaro.getChunk(hunterGround);
        skaro.getChunk(idleGround);
        prepareColumn(skaro, hunterGround);
        prepareColumn(skaro, idleGround);
        skaro.setBlock(idleGround.west().above(), Blocks.STONE.defaultBlockState(), 3);
        skaro.setBlock(idleGround.west().above(2), Blocks.STONE.defaultBlockState(), 3);

        DalekEntity hunter = spawnOn(skaro, hunterGround.above());
        DalekEntity idle = spawnOn(skaro, idleGround.above());
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(hunter.getX(), hunter.getY(), hunter.getZ() + 1.5);
        hunter.setTarget(player);

        context.runAtTickTime(10, () -> {
            if (hunter.distanceTo(idle) <= DalekPatrolLogic.TARGET_SHARE_RADIUS) {
                throw new AssertionError("Beyond-radius Daleks must start outside the patrol radius");
            }
            if (idle.getTarget() == player) {
                throw new AssertionError("Idle Dalek must not share a player target beyond the patrol radius");
            }
            context.succeed();
        });
    }

    private static ServerLevel requireSkaro(GameTestHelper context) {
        ServerLevel skaro = context.getLevel().getServer().getLevel(SkaroDimensions.SKARO_WORLD_KEY);
        if (skaro == null) {
            throw new AssertionError("Expected dwm:skaro to be loaded");
        }
        return skaro;
    }

    private static void placeFloor(GameTestHelper context, BlockPos origin, int width, int depth) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                context.setBlock(origin.offset(x, 0, z), Blocks.STONE);
            }
        }
    }

    private static void prepareColumn(ServerLevel level, BlockPos ground) {
        level.setBlock(ground, Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(ground.above(), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(ground.above(2), Blocks.AIR.defaultBlockState(), 3);
    }

    private static DalekEntity spawnOn(ServerLevel level, BlockPos pos) {
        DalekEntity dalek = DWMEntityTypes.DALEK.create(level, EntitySpawnReason.COMMAND);
        if (dalek == null) {
            throw new AssertionError("Failed to create Dalek");
        }
        dalek.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        if (!level.addFreshEntity(dalek)) {
            throw new AssertionError("Failed to add Dalek at " + pos);
        }
        return dalek;
    }
}
