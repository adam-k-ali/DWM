package com.adamkali.dwm.gametest;

import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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
        useSonicOn(player, sonicStack, trapdoorAbs);

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

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void sonicOpensIronDoor(GameTestHelper context) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        ItemStack sonicStack = new ItemStack(DWMItems.SONIC_SECOND_DOCTOR);
        player.setItemInHand(InteractionHand.MAIN_HAND, sonicStack);

        BlockPos lowerRel = new BlockPos(2, 2, 2);
        BlockPos lowerAbs = context.absolutePos(lowerRel);
        context.setBlock(lowerRel, Blocks.IRON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.OPEN, false));
        context.setBlock(lowerRel.above(), Blocks.IRON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(DoorBlock.OPEN, false));

        useSonicOn(player, sonicStack, lowerAbs);
        if (!context.getLevel().getBlockState(lowerAbs).getValue(DoorBlock.OPEN)) {
            throw new AssertionError("Expected sonic to open iron door");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void sonicBreaksGlass(GameTestHelper context) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        ItemStack sonicStack = new ItemStack(DWMItems.SONIC_SECOND_DOCTOR);
        player.setItemInHand(InteractionHand.MAIN_HAND, sonicStack);

        BlockPos glassRel = new BlockPos(2, 2, 2);
        BlockPos glassAbs = context.absolutePos(glassRel);
        context.setBlock(glassRel, Blocks.GLASS);
        useSonicOn(player, sonicStack, glassAbs);
        context.assertBlockPresent(Blocks.AIR, glassRel);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void sonicDamagesSlime(GameTestHelper context) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        ItemStack sonicStack = new ItemStack(DWMItems.SONIC_SECOND_DOCTOR);
        player.setItemInHand(InteractionHand.MAIN_HAND, sonicStack);

        // Size 1 slime has little health; one sonic hit should apply damage.
        Slime slime = (Slime) context.spawn(EntityTypes.SLIME, 2, 2, 1);
        slime.setSize(1, true);
        float before = slime.getHealth();
        DWMItems.SONIC_SECOND_DOCTOR.interactLivingEntity(sonicStack, player, slime, InteractionHand.MAIN_HAND);
        if (slime.isAlive() && !(slime.getHealth() < before)) {
            throw new AssertionError("Expected sonic to damage slime (health was " + before + ")");
        }
        context.succeed();
    }

    private static void useSonicOn(Player player, ItemStack sonicStack, BlockPos abs) {
        BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
        UseOnContext itemUsageContext = new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult);
        DWMItems.SONIC_SECOND_DOCTOR.useOn(itemUsageContext);
    }
}
