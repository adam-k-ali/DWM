package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.FlutterwingEntity;
import com.adamkali.dwm.entity.FlutterwingVariant;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import java.util.EnumSet;

public class FlutterwingGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void spawnFlutterwingOnGrass(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        FlutterwingEntity flutterwing = context.spawn(DWMEntityTypes.FLUTTERWING, grassRel.above());
        if (flutterwing == null || !flutterwing.isAlive()) {
            throw new AssertionError("Expected a living Flutterwing after spawn");
        }
        EnumSet<FlutterwingVariant> species = EnumSet.allOf(FlutterwingVariant.class);
        if (!species.contains(flutterwing.getVariant())) {
            throw new AssertionError("Unexpected Flutterwing variant: " + flutterwing.getVariant());
        }
        context.assertEntityPresent(DWMEntityTypes.FLUTTERWING);
        context.succeed();
    }
}
