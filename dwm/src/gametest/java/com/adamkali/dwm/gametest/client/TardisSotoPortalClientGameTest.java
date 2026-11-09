package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.UUID;

/**
 * Interior look-out portal (SOTO): stands inside the console room facing the open interior door and saves
 * {@code portal_soto_interior.png}, which should show the coloured wool landmarks outside the TARDIS.
 */
public class TardisSotoPortalClientGameTest implements FabricClientGameTest {
    /** Console room: door bank is at local z = 1 facing south, so the viewer stands behind it and looks north. */
    private static final double VIEWER_LOCAL_X = 5.5;
    private static final double VIEWER_LOCAL_Y = 1.0;
    private static final double VIEWER_LOCAL_Z = 5.5;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            BlockPos tardisPos = new BlockPos(0, ClientGameTestSupport.PAD_Y, 4);

            ClientGameTestSupport.stage(world.getServer(), "spectator", true);
            placeLandmarks(world);
            UUID id = ClientGameTestSupport.placeTardisWithOpenDoor(world.getServer(), tardisPos);
            ClientGameTestSupport.waitForInteriorReady(world.getServer(), tardisPos, id);

            BlockPos plot = TardisPlotAllocator.plotOrigin(id);
            world.getServer().runCommand(String.format(
                    "execute in dwm:tardis run tp @a %.1f %.1f %.1f 180 0",
                    plot.getX() + VIEWER_LOCAL_X, plot.getY() + VIEWER_LOCAL_Y, plot.getZ() + VIEWER_LOCAL_Z));

            ClientGameTestSupport.toggleHud(context);
            ClientGameTestSupport.waitForPortalRendered(context, PortalStreamKind.SOTO, id);
            // The interior door block entity opens on its own (0.05 swing per tick); let it finish.
            context.waitTicks(40);
            ClientGameTestSupport.assertPortalsStillAvailable(context);
            ClientGameTestSupport.screenshot(context, "portal_soto_interior");
            TardisSotoPortalCloudsClientGameTest.run(context, world, id);
            ClientGameTestSupport.toggleHud(context);
        }
    }

    /**
     * Coloured wool in front of the exterior door (it faces north, towards z &lt; 4) so the screenshot visibly
     * proves the portal shows the overworld, with a row of grass blocks beyond it so biome-tinted tops show. Must exist before the exterior is streamed; the chunks are also
     * force-loaded because the viewer leaves the overworld.
     */
    private static void placeLandmarks(TestSingleplayerContext world) {
        world.getServer().runCommand("execute in minecraft:overworld run forceload add -48 -48 48 48");
        world.getServer().runOnServer(server -> {
            var level = server.overworld();
            Block[] wool = new Block[] {
                    Blocks.WOOL.red(), Blocks.WOOL.lime(), Blocks.WOOL.blue(), Blocks.WOOL.yellow(), Blocks.WOOL.magenta()
            };
            for (int x = -2; x <= 2; x++) {
                for (int z = 0; z <= 1; z++) {
                    level.setBlock(new BlockPos(x, ClientGameTestSupport.PAD_Y, z),
                            wool[x + 2].defaultBlockState(), 3);
                }
                // Biome-tinted tops: rendered black when the ghost tint had no alpha.
                level.setBlock(new BlockPos(x, ClientGameTestSupport.PAD_Y, -1),
                        Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            }
            placeLeafShadedStep(level);
        });
    }

    /**
     * A grass wall (z = -6..-4, top at PAD_Y + 2) behind a one-block-lower strip (z = -3) with leaves
     * hanging over the strip. Its south face (towards the viewer) must render in full: a surface skin
     * built from a heightmap that counts leaves dropped all but the top of the wall's interior columns.
     */
    private static void placeLeafShadedStep(net.minecraft.server.level.ServerLevel level) {
        int padY = ClientGameTestSupport.PAD_Y;
        var leaves = Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true);
        for (int x = -5; x <= 5; x++) {
            level.setBlock(new BlockPos(x, padY - 1, -3), Blocks.STONE.defaultBlockState(), 3);
            for (int z = -6; z <= -4; z++) {
                for (int y = padY - 1; y <= padY + 2; y++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                }
            }
            level.setBlock(new BlockPos(x, padY + 6, -3), leaves, 3);
        }
    }
}
