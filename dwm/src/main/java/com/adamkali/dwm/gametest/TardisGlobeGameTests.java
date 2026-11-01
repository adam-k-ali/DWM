package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisDecorBlockEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TardisGlobeGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void globe_UseStartsSpin(GameTestHelper context) {
        BlockPos rel = new BlockPos(1, 2, 1);
        context.setBlock(rel, DWMBlocks.TARDIS_GLOBE);
        BlockPos abs = context.absolutePos(rel);
        if (!(context.getLevel().getBlockEntity(abs) instanceof TardisDecorBlockEntity globe)) {
            throw new AssertionError("Expected TardisDecorBlockEntity");
        }
        long placedAt = context.getLevel().getGameTime();
        context.assertFalse(globe.spin().isSpinning(placedAt, 0.0F), "Globe must be idle before use");

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        BlockState state = context.getLevel().getBlockState(abs);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.NORTH, abs, false);
        InteractionResult result = state.useWithoutItem(context.getLevel(), player, hit);
        context.assertTrue(result.consumesAction(), "Using the globe should consume the click, got " + result);

        // Block events run at the end of the level tick.
        context.succeedWhen(() -> {
            long now = context.getLevel().getGameTime();
            context.assertTrue(globe.spin().isSpinning(now, 0.5F), "Globe should spin after use");
            context.assertTrue(globe.spin().energy(now, 0.5F) > 0.0F, "Spin energy should be above zero");
        });
    }
}
