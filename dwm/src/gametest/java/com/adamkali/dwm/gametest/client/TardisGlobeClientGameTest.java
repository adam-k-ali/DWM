package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisDecorEntityBlock;
import com.adamkali.dwm.block.entities.TardisDecorBlockEntity;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * TARDIS globes on the pad: {@code tardis_globe} shows three in their ambient poses (each globe has
 * its own phase), {@code tardis_globe_spin} the middle one part-way through the spin started on use.
 */
public class TardisGlobeClientGameTest implements FabricClientGameTest {
    private static final int Z = 4;
    private static final Direction[] FACINGS = {Direction.EAST, Direction.SOUTH, Direction.WEST};

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            var server = world.getServer();
            ClientGameTestSupport.stage(server, "spectator");
            server.runOnServer(minecraft -> {
                var level = minecraft.overworld();
                for (int i = 0; i < FACINGS.length; i++) {
                    BlockPos pos = new BlockPos(-2 + 2 * i, ClientGameTestSupport.PAD_Y, Z);
                    level.setBlock(pos, DWMBlocks.TARDIS_GLOBE.defaultBlockState()
                            .setValue(TardisDecorEntityBlock.FACING, FACINGS[i]), 3);
                }
            });

            BlockPos middle = new BlockPos(0, ClientGameTestSupport.PAD_Y, Z);
            context.getInput().lookAt(middle.above());
            ClientGameTestSupport.toggleHud(context);
            context.waitTicks(40);
            ClientGameTestSupport.screenshot(context, "tardis_globe");

            server.runOnServer(minecraft -> minecraft.overworld()
                    .blockEvent(middle, DWMBlocks.TARDIS_GLOBE, TardisDecorBlockEntity.EVENT_SPIN, 0));
            context.waitTicks(12);
            ClientGameTestSupport.screenshot(context, "tardis_globe_spin");
            ClientGameTestSupport.toggleHud(context);
        }
    }
}
