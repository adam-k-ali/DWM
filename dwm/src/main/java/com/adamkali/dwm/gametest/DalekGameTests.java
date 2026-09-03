package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.DalekVariant;
import com.adamkali.dwm.entity.DWMEntityTypes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class DalekGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void spawnDalekOnGrass(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        DalekEntity dalek = context.spawn(DWMEntityTypes.DALEK, grassRel.above());
        if (dalek == null || !dalek.isAlive()) {
            throw new AssertionError("Expected a living Dalek after spawn");
        }
        if (dalek.getVariant() != DalekVariant.CLASSIC_1963) {
            throw new AssertionError("Unexpected Dalek variant: " + dalek.getVariant());
        }
        context.assertEntityPresent(DWMEntityTypes.DALEK);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void rangedAttackSpawnsLaser(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        DalekEntity dalek = context.spawn(DWMEntityTypes.DALEK, grassRel.above());
        if (dalek == null || !dalek.isAlive()) {
            throw new AssertionError("Expected a living Dalek after spawn");
        }

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(dalek.getX() + 4.0, dalek.getY(), dalek.getZ());
        dalek.performRangedAttack(player, 1.0F);
        context.assertEntityPresent(DWMEntityTypes.DALEK_LASER);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void fliesWhenTargetIsAbove(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        DalekEntity dalek = context.spawn(DWMEntityTypes.DALEK, grassRel.above());
        if (dalek == null || !dalek.isAlive()) {
            throw new AssertionError("Expected a living Dalek after spawn");
        }

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(dalek.getX(), dalek.getY() + 6.0, dalek.getZ());
        dalek.setTarget(player);
        dalek.refreshFlightState();
        if (!dalek.isFlying()) {
            throw new AssertionError("Expected Dalek to fly when the target is well above ground pathing");
        }
        context.succeed();
    }
}
