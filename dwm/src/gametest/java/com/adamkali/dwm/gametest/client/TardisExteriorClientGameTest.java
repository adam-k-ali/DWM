package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.config.DWMConfig;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Smoke test for the client render path: boots a real client, places a TARDIS exterior
 * and saves a screenshot to {@code <runDir>/screenshots/tardis_exterior.png}.
 */
public class TardisExteriorClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        // Door portals allocate extra FBOs/pipelines; keep this first scenario to the plain exterior.
        DWMConfig.setBoolean(DWMConfig.ENABLE_DOOR_PORTALS, false);

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            BlockPos tardisPos = new BlockPos(0, 100, 4);

            world.getServer().runCommand("time set noon");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("gamemode spectator @a");
            world.getServer().runCommand("tp @a 0 101 0 0 0");

            world.getServer().runOnServer(server -> {
                var level = server.overworld();
                for (int x = -3; x <= 3; x++) {
                    for (int z = 2; z <= 7; z++) {
                        level.setBlock(new BlockPos(x, tardisPos.getY() - 1, z), Blocks.STONE.defaultBlockState(), 3);
                    }
                }
                level.setBlock(tardisPos, DWMBlocks.TARDIS_BLOCK.defaultBlockState(), 3);
                if (!(level.getBlockEntity(tardisPos) instanceof TardisBlockEntity shell)) {
                    throw new AssertionError("Expected a TardisBlockEntity at " + tardisPos);
                }
                shell.getTardisId();
            });

            context.getInput().lookAt(tardisPos.above());
            context.waitTicks(40);

            Path screenshot = context.takeScreenshot("tardis_exterior");
            try {
                if (!Files.exists(screenshot) || Files.size(screenshot) == 0) {
                    throw new AssertionError("Screenshot missing or empty: " + screenshot);
                }
            } catch (java.io.IOException e) {
                throw new AssertionError("Could not read screenshot " + screenshot, e);
            }
        }
    }
}
