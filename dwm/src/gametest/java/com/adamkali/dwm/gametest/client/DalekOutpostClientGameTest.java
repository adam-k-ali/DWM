package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.gametest.StructureGameTestSupport;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

/**
 * Screenshots of the assembled Skaro Dalek outpost from an elevated three-quarter view:
 * {@code dalek_outpost.png} (wastes/mountains) and {@code dalek_outpost_ruined.png} (petrified jungle).
 */
public class DalekOutpostClientGameTest implements FabricClientGameTest {
    /** Sand ground around the outpost; the core floor replaces its top layer like worldgen would. */
    private static final int GROUND_Y = ClientGameTestSupport.PAD_Y - 1;
    private static final int MIN_X = -24;
    private static final int MAX_X = 24;
    private static final int MIN_Z = 2;
    private static final int MAX_Z = 42;
    /** Minimum corner of the 11×11 core, so the core is centred on x = 0, z = 22. */
    private static final BlockPos CORE_ANCHOR = new BlockPos(-5, GROUND_Y, 17);
    private static final BlockPos CAMERA = new BlockPos(-14, GROUND_Y + 14, 6);

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            ClientGameTestSupport.stage(world.getServer(), "spectator");
            // Elevated three-quarter view from the north-west; let the teleport land before aiming,
            // since the server's position packet would otherwise reset the camera rotation.
            world.getServer().runCommand("tp @a " + CAMERA.getX() + " " + CAMERA.getY() + " " + CAMERA.getZ());
            context.waitTicks(5);
            context.getInput().lookAt(CORE_ANCHOR.offset(5, 2, 5));
            ClientGameTestSupport.toggleHud(context);

            for (String structure : new String[] {"dalek_outpost", "dalek_outpost_ruined"}) {
                world.getServer().runOnServer(server -> {
                    var level = server.overworld();
                    for (int x = MIN_X; x <= MAX_X; x++) {
                        for (int z = MIN_Z; z <= MAX_Z; z++) {
                            for (int y = GROUND_Y - 1; y <= GROUND_Y + 14; y++) {
                                var state = y < GROUND_Y ? Blocks.STONE : y == GROUND_Y ? Blocks.SAND : Blocks.AIR;
                                level.setBlock(new BlockPos(x, y, z), state.defaultBlockState(), 2);
                            }
                        }
                    }
                    StructureGameTestSupport.placeJigsaw(
                            level, Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, structure), CORE_ANCHOR, 3);
                });
                context.waitTicks(40);
                ClientGameTestSupport.screenshot(context, structure);
            }
            ClientGameTestSupport.toggleHud(context);
        }
    }
}
