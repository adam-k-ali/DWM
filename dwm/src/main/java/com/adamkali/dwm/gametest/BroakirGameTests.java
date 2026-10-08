package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.BroakirEntity;
import com.adamkali.dwm.entity.DWMEntityTypes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

public class BroakirGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void spawnBroakirOnGrass(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        BroakirEntity broakir = context.spawn(DWMEntityTypes.BROAKIR, grassRel.above());
        if (broakir == null || !broakir.isAlive()) {
            throw new AssertionError("Expected a living Broakir after spawn");
        }
        context.assertEntityPresent(DWMEntityTypes.BROAKIR);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void broakirMatchesDefinition(GameTestHelper context) {
        BroakirEntity mob = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.BROAKIR);
        var definition = EntityDefinitionGameTestSupport.definitionOf(mob);
        EntityDefinitionGameTestSupport.assertAttributesMatch(mob, definition);
        EntityDefinitionGameTestSupport.assertGoalsMatch(mob, definition);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void broakirPanicsWhenHurt(GameTestHelper context) {
        BroakirEntity mob = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.BROAKIR);
        Player attacker = context.makeMockPlayer(GameType.SURVIVAL);
        mob.hurtServer(context.getLevel(), mob.damageSources().playerAttack(attacker), 1.0F);
        context.succeedWhen(() -> {
            context.assertTrue(EntityDefinitionGameTestSupport.isRunning(mob.goalSelector, PanicGoal.class),
                    "Expected PanicGoal to run after being hurt");
        });
    }
}
