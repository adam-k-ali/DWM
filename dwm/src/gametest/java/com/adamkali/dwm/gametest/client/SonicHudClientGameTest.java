package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.render.SonicFieldModeHudController;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Sonic screwdriver HUD: {@code sonic_hud_indicator} (held, carousel closed) and
 * {@code sonic_hud_carousel} (field-mode carousel open).
 */
public class SonicHudClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            ClientGameTestSupport.stage(world.getServer(), "creative");
            world.getServer().runCommand("give @a dwm:sonic_fifth_doctor");
            context.waitTicks(20);
            ClientGameTestSupport.clearToasts(context);
            context.waitTicks(2);
            ClientGameTestSupport.screenshot(context, "sonic_hud_indicator");

            // The carousel closes on any tick where the player is not sneaking.
            context.getInput().holdKey(options -> options.keyShift);
            context.waitTicks(2);
            context.runOnClient(client -> SonicFieldModeHudController.open(client.player.getMainHandItem()));
            context.waitTicks(20);
            ClientGameTestSupport.screenshot(context, "sonic_hud_carousel");
            context.getInput().releaseKey(options -> options.keyShift);
        }
    }
}
