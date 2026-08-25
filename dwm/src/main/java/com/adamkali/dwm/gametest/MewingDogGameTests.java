package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.MewingDogEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

public class MewingDogGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void spawnMewingDogOnGrass(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        MewingDogEntity dog = context.spawn(DWMEntityTypes.MEWING_DOG, grassRel.above());
        if (dog == null || !dog.isAlive()) {
            throw new AssertionError("Expected a living Mewing Dog after spawn");
        }
        context.assertEntityPresent(DWMEntityTypes.MEWING_DOG);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void boneTamesMewingDog(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        MewingDogEntity dog = context.spawn(DWMEntityTypes.MEWING_DOG, grassRel.above());
        if (dog == null || !dog.isAlive()) {
            throw new AssertionError("Expected a living Mewing Dog after spawn");
        }

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        // Tame chance is 1/3 per bone; retry enough times to make failure vanishingly unlikely.
        boolean tamed = false;
        for (int attempt = 0; attempt < 40 && !tamed; attempt++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE));
            dog.mobInteract(player, InteractionHand.MAIN_HAND);
            tamed = dog.isTame();
        }
        if (!tamed) {
            throw new AssertionError("Expected bone interaction to tame Mewing Dog within 40 attempts");
        }
        if (!dog.isOwnedBy(player)) {
            throw new AssertionError("Expected tamed Mewing Dog to be owned by the interacting player");
        }
        context.succeed();
    }
}
