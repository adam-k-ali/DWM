package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.UUID;

/**
 * One screenshot of the TARDIS exterior per chameleon variant ({@code tardis_variant_<id>.png}), plus an
 * open-door shot of each generated modern exterior ({@code tardis_variant_<id>_open.png}).
 */
public class TardisVariantsClientGameTest implements FabricClientGameTest {
    private static final List<TardisChameleonVariant> GENERATED_EXTERIORS = List.of(
            TardisChameleonVariant.TARDIS_2005,
            TardisChameleonVariant.TARDIS_2010,
            TardisChameleonVariant.TARDIS_2014,
            TardisChameleonVariant.TARDIS_2018);

    @Override
    public void runTest(ClientGameTestContext context) {
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

            // Door swung open on the generated exteriors, to check the hinge side and the leaf/aperture fit.
            ClientGameTestSupport.setDoorOpen(world.getServer(), tardisId, true);
            for (TardisChameleonVariant variant : GENERATED_EXTERIORS) {
                world.getServer().runOnServer(server -> TardisLogic.setVariant(tardisId, variant, server));
                context.waitTicks(20);
                ClientGameTestSupport.screenshot(context, "tardis_variant_" + variant.getId().getPath() + "_open");
            }
            ClientGameTestSupport.setDoorOpen(world.getServer(), tardisId, false);
            ClientGameTestSupport.toggleHud(context);
        }
    }
}
