package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.TimeLordEntity;
import com.adamkali.dwm.entity.TimeLordVariant;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
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
}
