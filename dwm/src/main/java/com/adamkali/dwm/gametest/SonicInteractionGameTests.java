package com.adamkali.dwm.gametest;

import com.adamkali.dwm.item.DWMItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class SonicInteractionGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void sonicFirstSessionSmokeFlow(GameTestHelper context) {
        if (DWMItems.SONIC_SECOND_DOCTOR == null) {
            throw new AssertionError("Expected sonic item to be registered");
        }

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        ItemStack sonicStack = new ItemStack(DWMItems.SONIC_SECOND_DOCTOR);
        player.setItemInHand(InteractionHand.MAIN_HAND, sonicStack);

        BlockPos trapdoorRel = new BlockPos(1, 2, 1);
        BlockPos trapdoorAbs = context.absolutePos(trapdoorRel);
        context.setBlock(trapdoorRel, Blocks.IRON_TRAPDOOR);
        context.assertBlockPresent(Blocks.IRON_TRAPDOOR, trapdoorRel);
        BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(trapdoorAbs), Direction.UP, trapdoorAbs, false);
        UseOnContext itemUsageContext = new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult);
        DWMItems.SONIC_SECOND_DOCTOR.useOn(itemUsageContext);

        if (!context.getLevel().getBlockState(trapdoorAbs).getValue(TrapDoorBlock.OPEN)) {
            throw new AssertionError("Expected sonic interaction to open iron trapdoor");
        }

        Sheep sheep = (Sheep) context.spawn(EntityTypes.SHEEP, 2, 2, 1);
        context.assertEntitiesPresent(EntityTypes.SHEEP, 1);
        DWMItems.SONIC_SECOND_DOCTOR.interactLivingEntity(sonicStack, player, sheep, InteractionHand.MAIN_HAND);
        if (!sheep.isSheared()) {
            throw new AssertionError("Expected sonic interaction to shear sheep");
        }

        context.succeed();
    }
}
