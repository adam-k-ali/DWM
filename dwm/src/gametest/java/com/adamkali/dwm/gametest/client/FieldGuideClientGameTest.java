package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.gui.FieldGuideScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** Opens the Field Guide screen and saves {@code field_guide_cover.png}. */
public class FieldGuideClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            ClientGameTestSupport.stage(world.getServer(), "creative");
            context.setScreen(FieldGuideScreen::new);
            context.waitForScreen(FieldGuideScreen.class);
            context.waitTicks(10);
            ClientGameTestSupport.screenshot(context, "field_guide_cover");
            context.setScreen(() -> null);
        }
    }
}
