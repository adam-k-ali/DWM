package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisButtonBlock;
import com.adamkali.dwm.entity.TardisSeatEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Chair sit entity spawn and door-button power pulse.
 */
public class TardisFurnitureGameTests {

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 40)
    public void chairUse_SpawnsSeatAndMountsPlayer(GameTestHelper context) {
        BlockPos chairRel = new BlockPos(2, 2, 2);
        BlockPos chairAbs = context.absolutePos(chairRel);
        context.setBlock(chairRel, DWMBlocks.TARDIS_CHAIR_SMALL.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH));

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        BlockState state = context.getLevel().getBlockState(chairAbs);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(chairAbs), Direction.UP, chairAbs, false);
        state.useWithoutItem(context.getLevel(), player, hit);

        List<TardisSeatEntity> seats = context.getLevel().getEntitiesOfClass(
                TardisSeatEntity.class,
                new AABB(chairAbs),
                seat -> seat.isSeatFor(chairAbs));
        if (seats.isEmpty()) {
            throw new AssertionError("Expected TardisSeatEntity after using chair");
        }
        if (!player.isPassenger()) {
            throw new AssertionError("Expected player to be riding the seat entity");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 60)
    public void doorButton_PowersOnHitThenDepresses(GameTestHelper context) {
        BlockPos buttonRel = new BlockPos(2, 2, 2);
        BlockPos buttonAbs = context.absolutePos(buttonRel);
        context.setBlock(buttonRel, DWMBlocks.TARDIS_DOOR_BUTTON.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                .setValue(TardisButtonBlock.POWERED, false));

        // NORTH facing activates on NORTH_SOUTH_SHAPE_B (z 9-15 in block space).
        Vec3 hitLoc = new Vec3(buttonAbs.getX() + 0.5, buttonAbs.getY() + 0.1, buttonAbs.getZ() + 12.0 / 16.0);
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        BlockState state = context.getLevel().getBlockState(buttonAbs);
        BlockHitResult hit = new BlockHitResult(hitLoc, Direction.UP, buttonAbs, false);
        var result = state.useWithoutItem(context.getLevel(), player, hit);
        if (!result.consumesAction()) {
            throw new AssertionError("Expected button hit to consume action, got " + result);
        }
        if (!context.getLevel().getBlockState(buttonAbs).getValue(TardisButtonBlock.POWERED)) {
            throw new AssertionError("Expected button POWERED=true after activation");
        }

        context.runAtTickTime(context.getTick() + 25, () -> {
            if (context.getLevel().getBlockState(buttonAbs).getValue(TardisButtonBlock.POWERED)) {
                throw new AssertionError("Expected button to depress after scheduled tick");
            }
            context.succeed();
        });
    }
}
