package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;

import java.util.UUID;

/** One screenshot of the TARDIS exterior per chameleon variant: {@code tardis_variant_<id>.png}. */
public class TardisVariantsClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        DWMConfig.setBoolean(DWMConfig.ENABLE_DOOR_PORTALS, false);

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            BlockPos tardisPos = new BlockPos(0, ClientGameTestSupport.PAD_Y, 4);

            ClientGameTestSupport.stage(world.getServer(), "spectator");
            UUID tardisId = world.getServer().computeOnServer(server -> {
                var level = server.overworld();
                level.setBlock(tardisPos, DWMBlocks.TARDIS_BLOCK.defaultBlockState(), 3);
                if (!(level.getBlockEntity(tardisPos) instanceof TardisBlockEntity shell)) {
                    throw new AssertionError("Expected a TardisBlockEntity at " + tardisPos);
                }
                return shell.getTardisId();
            });

            context.getInput().lookAt(tardisPos.above());
            ClientGameTestSupport.toggleHud(context);
            for (TardisChameleonVariant variant : TardisChameleonVariant.values()) {
                world.getServer().runOnServer(server -> TardisLogic.setVariant(tardisId, variant, server));
                context.waitTicks(20);
                ClientGameTestSupport.screenshot(context, "tardis_variant_" + variant.getId().getPath());
            }
            ClientGameTestSupport.toggleHud(context);
        }
    }
}
