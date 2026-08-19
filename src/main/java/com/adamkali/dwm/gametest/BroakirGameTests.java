package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.BroakirEntity;
import com.adamkali.dwm.entity.DWMEntityTypes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
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
}
