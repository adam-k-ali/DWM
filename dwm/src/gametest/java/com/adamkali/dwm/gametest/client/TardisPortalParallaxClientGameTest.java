package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;

import java.util.UUID;

/**
 * Experimental parallax door portals ({@code portalParallax}): the same doorway seen from several
 * positions, so the screenshots show whether the portal view moves with the viewer and lines up with
 * the door frame. Saves {@code portal_parallax_boti_*} and {@code portal_parallax_soto_*}.
 * <p>
 * Screenshots prove alignment, not latency: turning the camera quickly must still be checked by eye in
 * {@code runClient}.
 */
public class TardisPortalParallaxClientGameTest implements FabricClientGameTest {
    /** Frames for the viewer-relative portal to re-render after the camera moves. */
    private static final int SETTLE_TICKS = 5;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            BlockPos tardisPos = new BlockPos(0, ClientGameTestSupport.PAD_Y, 4);

            ClientGameTestSupport.stage(world.getServer(), "spectator", true);
            DWMConfig.setBoolean(DWMConfig.PORTAL_PARALLAX, true);
            try {
                TardisSotoPortalClientGameTest.placeLandmarks(world);
                UUID id = ClientGameTestSupport.placeTardisWithOpenDoor(world.getServer(), tardisPos);
                ClientGameTestSupport.waitForInteriorReady(world.getServer(), tardisPos, id);
                ClientGameTestSupport.toggleHud(context);

                // Exterior look-in: straight on, from the side, and close up.
                context.getInput().lookAt(tardisPos.above());
                ClientGameTestSupport.waitForPortalRendered(context, PortalStreamKind.BOTI, id);
                shot(context, "portal_parallax_boti_front");
                viewFrom(context, world, -2.5, ClientGameTestSupport.CAMERA_Y, 1.5, tardisPos.above());
                shot(context, "portal_parallax_boti_left");
                viewFrom(context, world, 0.5, ClientGameTestSupport.CAMERA_Y, 2.2, tardisPos.above());
                shot(context, "portal_parallax_boti_close");

                // Interior look-out: centred behind the console, then off to one side.
                BlockPos plot = TardisPlotAllocator.plotOrigin(id);
                world.getServer().runCommand(String.format(
                        "execute in dwm:tardis run tp @a %.1f %.1f %.1f 180 0",
                        plot.getX() + 5.5, plot.getY() + 1.0, plot.getZ() + 5.5));
                ClientGameTestSupport.waitForPortalRendered(context, PortalStreamKind.SOTO, id);
                // The interior door block entity opens on its own (0.05 swing per tick); let it finish.
                context.waitTicks(40);
                shot(context, "portal_parallax_soto_centre");
                world.getServer().runCommand(String.format(
                        "execute in dwm:tardis run tp @a %.1f %.1f %.1f 150 5",
                        plot.getX() + 8.5, plot.getY() + 1.0, plot.getZ() + 6.5));
                context.waitTicks(SETTLE_TICKS);
                shot(context, "portal_parallax_soto_offaxis");

                ClientGameTestSupport.toggleHud(context);
            } finally {
                DWMConfig.setBoolean(DWMConfig.PORTAL_PARALLAX, false);
            }
        }
    }

    private static void viewFrom(
            ClientGameTestContext context,
            TestSingleplayerContext world,
            double x,
            double y,
            double z,
            BlockPos target
    ) {
        world.getServer().runCommand(String.format("execute in minecraft:overworld run tp @a %.2f %.2f %.2f", x, y, z));
        context.waitTicks(2);
        context.getInput().lookAt(target);
        context.waitTicks(SETTLE_TICKS);
    }

    private static void shot(ClientGameTestContext context, String name) {
        ClientGameTestSupport.assertPortalsStillAvailable(context);
        ClientGameTestSupport.screenshot(context, name);
    }
}
