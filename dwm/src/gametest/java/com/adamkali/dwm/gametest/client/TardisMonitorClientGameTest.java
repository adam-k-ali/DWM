package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisDecorEntityBlock;
import com.adamkali.dwm.block.entities.TardisMonitorBlockEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.MonitorPage;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * TARDIS monitors: a full scanner (Location page) and a compact scanner (Systems page) linked to a
 * staged TARDIS, screens facing the camera. Saves {@code tardis_monitors.png} and a close-up of the
 * compact screen, {@code tardis_monitor_close.png}, to check mesh, texture and screen text.
 */
public class TardisMonitorClientGameTest implements FabricClientGameTest {
    private static final BlockPos FULL_POS = new BlockPos(2, ClientGameTestSupport.PAD_Y, 4);
    private static final BlockPos COMPACT_POS = new BlockPos(-3, ClientGameTestSupport.PAD_Y, 3);

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            ClientGameTestSupport.stage(world.getServer(), "spectator");
            world.getServer().runOnServer(server -> {
                var level = server.overworld();
                TardisDataModel model = TardisDataLoader.create();
                model.setExteriorLocation(level.dimension().identifier().toString(), 0, ClientGameTestSupport.PAD_Y, 8, 0);
                model.variant = TardisChameleonVariant.FIRST_DOCTOR_BOX;
                model.artron = 320;
                model.selectedBiome = "minecraft:desert";
                model.lockY = true;
                UUID tardisId = model.uuid;
                // Screens face -Z (the camera) when the block faces south.
                link(level, FULL_POS, DWMBlocks.TARDIS_FULL_SCANNER.defaultBlockState()
                        .setValue(TardisDecorEntityBlock.FACING, Direction.SOUTH), tardisId, MonitorPage.LOCATION);
                link(level, COMPACT_POS, DWMBlocks.TARDIS_COMPACT_SCANNER.defaultBlockState()
                        .setValue(TardisDecorEntityBlock.FACING, Direction.SOUTH), tardisId, MonitorPage.SYSTEMS);
            });

            context.getInput().lookAt(new BlockPos(0, ClientGameTestSupport.CAMERA_Y, 4));
            ClientGameTestSupport.toggleHud(context);
            // Monitors refresh once a second; let two refreshes land on the client.
            context.waitTicks(50);
            ClientGameTestSupport.screenshot(context, "tardis_monitors");

            world.getServer().runCommand(String.format("tp @a %d %d %d 0 0",
                    COMPACT_POS.getX(), ClientGameTestSupport.CAMERA_Y - 1, COMPACT_POS.getZ() - 2));
            context.waitTicks(5);
            context.getInput().lookAt(COMPACT_POS.above());
            context.waitTicks(5);
            ClientGameTestSupport.screenshot(context, "tardis_monitor_close");
            ClientGameTestSupport.toggleHud(context);
        }
    }

    private static void link(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            UUID tardisId,
            MonitorPage page
    ) {
        level.setBlock(pos, state, 3);
        if (!(level.getBlockEntity(pos) instanceof TardisMonitorBlockEntity monitor)) {
            throw new AssertionError("Expected a TardisMonitorBlockEntity at " + pos);
        }
        monitor.setTardisId(tardisId);
        while (monitor.page() != page) {
            monitor.cyclePage(false);
        }
    }
}
