package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;

/**
 * Smoke test for the client render path: boots a real client, places a TARDIS exterior
 * and saves a screenshot to {@code <runDir>/screenshots/tardis_exterior.png}.
 */
public class TardisExteriorClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            BlockPos tardisPos = new BlockPos(0, ClientGameTestSupport.PAD_Y, 4);

            ClientGameTestSupport.stage(world.getServer(), "spectator");
            world.getServer().runOnServer(server -> {
                var level = server.overworld();
                level.setBlock(tardisPos, DWMBlocks.TARDIS_BLOCK.defaultBlockState(), 3);
                if (!(level.getBlockEntity(tardisPos) instanceof TardisBlockEntity shell)) {
                    throw new AssertionError("Expected a TardisBlockEntity at " + tardisPos);
                }
                shell.getTardisId();
            });

            context.getInput().lookAt(tardisPos.above());
            ClientGameTestSupport.toggleHud(context);
            context.waitTicks(40);
            ClientGameTestSupport.screenshot(context, "tardis_exterior");
            ClientGameTestSupport.toggleHud(context);
        }
    }
}
