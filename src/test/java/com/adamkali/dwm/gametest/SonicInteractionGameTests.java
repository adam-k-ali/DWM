package com.adamkali.dwm.gametest;

import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public class SonicInteractionGameTests implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void sonicFirstSessionSmokeFlow(TestContext context) {
        if (DWMItems.SONIC_SECOND_DOCTOR == null) {
            throw new AssertionError("Expected sonic item to be registered");
        }

        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        ItemStack sonicStack = new ItemStack(DWMItems.SONIC_SECOND_DOCTOR);
        player.setStackInHand(Hand.MAIN_HAND, sonicStack);

        BlockPos trapdoorPos = new BlockPos(1, 2, 1);
        context.setBlockState(1, 2, 1, Blocks.IRON_TRAPDOOR);
        context.expectBlock(Blocks.IRON_TRAPDOOR, 1, 2, 1);
        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(trapdoorPos), Direction.UP, trapdoorPos, false);
        ItemUsageContext itemUsageContext = new ItemUsageContext(player, Hand.MAIN_HAND, hitResult);
        DWMItems.SONIC_SECOND_DOCTOR.useOnBlock(itemUsageContext);

        if (!context.getWorld().getBlockState(trapdoorPos).get(TrapdoorBlock.OPEN)) {
            throw new AssertionError("Expected sonic interaction to open iron trapdoor");
        }

        BlockPos ironDoorPos = new BlockPos(2, 2, 1);
        context.setBlockState(2, 2, 1, Blocks.IRON_DOOR);
        context.setBlockState(2, 3, 1, Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.HALF, net.minecraft.block.enums.DoubleBlockHalf.UPPER));
        BlockHitResult doorHitResult = new BlockHitResult(Vec3d.ofCenter(ironDoorPos), Direction.NORTH, ironDoorPos, false);
        DWMItems.SONIC_SECOND_DOCTOR.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, doorHitResult));
        if (!context.getWorld().getBlockState(ironDoorPos).get(DoorBlock.OPEN)) {
            throw new AssertionError("Expected sonic interaction to open iron door");
        }

        BlockPos glassPos = new BlockPos(3, 2, 1);
        context.setBlockState(3, 2, 1, Blocks.GLASS);
        BlockHitResult glassHitResult = new BlockHitResult(Vec3d.ofCenter(glassPos), Direction.UP, glassPos, false);
        DWMItems.SONIC_SECOND_DOCTOR.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, glassHitResult));
        context.expectBlock(Blocks.AIR, 3, 2, 1);

        SheepEntity sheep = (SheepEntity) context.spawnEntity(EntityType.SHEEP, 2, 2, 1);
        context.expectEntities(EntityType.SHEEP, 1);
        DWMItems.SONIC_SECOND_DOCTOR.useOnEntity(sonicStack, player, sheep, Hand.MAIN_HAND);
        if (!sheep.isSheared()) {
            throw new AssertionError("Expected sonic interaction to shear sheep");
        }

        context.complete();
    }
}
