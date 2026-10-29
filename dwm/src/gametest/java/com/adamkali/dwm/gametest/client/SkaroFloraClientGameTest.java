package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.plant.ThalCropBlock;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/**
 * Skaro flora close-ups: {@code skaro_flora} (noon) and {@code skaro_flora_night} (midnight, so the radiation
 * fungus' emissive gills show). On screen-right a 3 x 2 radiation fungus patch on dirt; on screen-left a farmland
 * plot with the Thal crop at ages 0, 2, 4 and 7 (one column per visual stage). The camera sits low and close so
 * the toadstool undersides are visible.
 *
 * <p>Random ticks are off so the crop cannot grow or the farmland dry out between placement and the shots.
 */
public class SkaroFloraClientGameTest implements FabricClientGameTest {
    private static final int GROUND_Y = ClientGameTestSupport.PAD_Y - 1;
    private static final int PLANT_Y = ClientGameTestSupport.PAD_Y;
    private static final int[] CROP_AGES = {0, 2, 4, ThalCropBlock.MAX_AGE};
    private static final int RENDER_TICKS = 40;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            ClientGameTestSupport.stage(world.getServer(), "spectator");
            world.getServer().runCommand("gamerule random_tick_speed 0");
            ClientGameTestSupport.toggleHud(context);
            world.getServer().runOnServer(server -> placeFlora(server.overworld()));

            world.getServer().runCommand("tp @a 0 " + PLANT_Y + " 0");
            context.waitTicks(5);
            context.getInput().lookAt(new BlockPos(0, PLANT_Y, 5));
            context.waitTicks(RENDER_TICKS);
            ClientGameTestSupport.screenshot(context, "skaro_flora");

            world.getServer().runCommand("time set midnight");
            context.waitTicks(RENDER_TICKS);
            ClientGameTestSupport.screenshot(context, "skaro_flora_night");
            ClientGameTestSupport.toggleHud(context);
        }
    }

    private static void placeFlora(ServerLevel level) {
        for (int x = -4; x <= -2; x++) {
            for (int z = 4; z <= 5; z++) {
                level.setBlock(new BlockPos(x, GROUND_Y, z), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(new BlockPos(x, PLANT_Y, z), DWMBlocks.RADIATION_FUNGUS.defaultBlockState(), 2);
            }
        }
        for (int i = 0; i < CROP_AGES.length; i++) {
            int x = 1 + i;
            for (int z = 4; z <= 5; z++) {
                level.setBlock(new BlockPos(x, GROUND_Y, z), Blocks.FARMLAND.defaultBlockState(), 2);
                level.setBlock(new BlockPos(x, PLANT_Y, z),
                        DWMBlocks.THAL_CROP.defaultBlockState().setValue(ThalCropBlock.AGE, CROP_AGES[i]), 2);
            }
        }
    }
}
