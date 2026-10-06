package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.TimeLordEntity;
import com.adamkali.dwm.entity.TimeLordVariant;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import java.util.EnumSet;

public class TimeLordGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void spawnTimeLordOnGrass(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        TimeLordEntity timeLord = context.spawn(DWMEntityTypes.TIME_LORD, grassRel.above());
        if (timeLord == null || !timeLord.isAlive()) {
            throw new AssertionError("Expected a living Time Lord after spawn");
        }
        EnumSet<TimeLordVariant> variants = EnumSet.allOf(TimeLordVariant.class);
        if (!variants.contains(timeLord.getVariant())) {
            throw new AssertionError("Unexpected Time Lord variant: " + timeLord.getVariant());
        }
        context.assertEntityPresent(DWMEntityTypes.TIME_LORD);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void timeLordMatchesDefinition(GameTestHelper context) {
        TimeLordEntity mob = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.TIME_LORD);
        var definition = EntityDefinitionGameTestSupport.definitionOf(mob);
        EntityDefinitionGameTestSupport.assertAttributesMatch(mob, definition);
        EntityDefinitionGameTestSupport.assertGoalsMatch(mob, definition);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void timeLordPanicsWhenHurt(GameTestHelper context) {
        TimeLordEntity mob = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.TIME_LORD);
        Player attacker = context.makeMockPlayer(GameType.SURVIVAL);
        mob.hurtServer(context.getLevel(), mob.damageSources().playerAttack(attacker), 1.0F);
        context.succeedWhen(() -> {
            context.assertTrue(EntityDefinitionGameTestSupport.isRunning(mob.goalSelector, PanicGoal.class),
                    "Expected PanicGoal to run after being hurt");
        });
    }
}
