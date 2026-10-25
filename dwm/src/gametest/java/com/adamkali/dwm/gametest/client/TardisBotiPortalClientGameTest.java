package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;

import java.util.UUID;

/**
 * Exterior look-in portal (BOTI): stands outside an open TARDIS door and saves
 * {@code portal_boti_exterior.png}. Fails if the portal never renders instead of saving the placeholder.
 */
public class TardisBotiPortalClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            BlockPos tardisPos = new BlockPos(0, ClientGameTestSupport.PAD_Y, 4);

            ClientGameTestSupport.stage(world.getServer(), "spectator", true);
            UUID id = ClientGameTestSupport.placeTardisWithOpenDoor(world.getServer(), tardisPos);
            ClientGameTestSupport.waitForInteriorReady(world.getServer(), tardisPos, id);

            context.getInput().lookAt(tardisPos.above());
            ClientGameTestSupport.toggleHud(context);
            ClientGameTestSupport.waitForPortalRendered(context, PortalStreamKind.BOTI, id);
            ClientGameTestSupport.assertPortalsStillAvailable(context);
            ClientGameTestSupport.screenshot(context, "portal_boti_exterior");
            ClientGameTestSupport.toggleHud(context);
        }
    }
}
