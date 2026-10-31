package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisDecorEntityBlock;
import com.adamkali.dwm.block.entities.TardisMonitorBlockEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomPlacer;
import com.adamkali.dwm.tardis.logic.MonitorPage;
import com.adamkali.dwm.tardis.logic.TardisMonitorReadout;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelResource;

import java.util.UUID;

public class TardisMonitorGameTests {
    /** Local position of the compact scanner in {@code first_doctor_console_room.nbt}. */
    private static final BlockPos LOCAL_COMPACT_SCANNER = new BlockPos(7, 1, 15);

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void consoleRoomPlacer_StampsTardisIdOnMonitor(GameTestHelper context) {
        redirectSaves(context);
        UUID tardisId = TardisDataLoader.create().uuid;
        BlockPos originAbs = context.absolutePos(new BlockPos(0, 2, 0));
        FirstDoctorConsoleRoomPlacer.place(context.getLevel(), originAbs, tardisId);

        BlockPos monitorAbs = originAbs.offset(LOCAL_COMPACT_SCANNER);
        if (!context.getLevel().getBlockState(monitorAbs).is(DWMBlocks.TARDIS_COMPACT_SCANNER)) {
            throw new AssertionError("Expected the console room's compact scanner at " + monitorAbs
                    + " (layout size " + FirstDoctorConsoleRoomLayout.SIZE_X + "x" + FirstDoctorConsoleRoomLayout.SIZE_Z + ")");
        }
        if (!(context.getLevel().getBlockEntity(monitorAbs) instanceof TardisMonitorBlockEntity monitor)) {
            throw new AssertionError("Expected TardisMonitorBlockEntity at " + monitorAbs);
        }
        if (!tardisId.equals(monitor.getTardisId())) {
            throw new AssertionError("Monitor tardisId not stamped: " + monitor.getTardisId());
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 80)
    public void linkedMonitor_syncsReadoutFromModel(GameTestHelper context) {
        redirectSaves(context);
        TardisDataModel model = TardisDataLoader.create();
        model.artron = 123;
        model.cloaked = true;
        BlockPos monitorRel = new BlockPos(2, 2, 2);
        TardisMonitorBlockEntity monitor = placeMonitor(context, monitorRel);
        monitor.setTardisId(model.uuid);

        context.succeedWhen(() -> {
            TardisMonitorReadout readout = monitor.readout();
            context.assertTrue(readout.linked(), "Monitor readout never linked");
            context.assertTrue(readout.artron() == 123, "Expected artron 123, got " + readout.artron());
            context.assertTrue(readout.cloaked(), "Expected cloak flag on the readout");
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void emptyHandUse_cyclesPage(GameTestHelper context) {
        redirectSaves(context);
        BlockPos monitorRel = new BlockPos(2, 2, 2);
        TardisMonitorBlockEntity monitor = placeMonitor(context, monitorRel);
        Player player = context.makeMockPlayer(GameType.SURVIVAL);

        context.useBlock(monitorRel, player);
        if (monitor.page() != MonitorPage.DESTINATION) {
            throw new AssertionError("Expected DESTINATION after one use, got " + monitor.page());
        }
        // A full cycle of uses lands back on the same page.
        for (int i = 0; i < MonitorPage.values().length; i++) {
            context.useBlock(monitorRel, player);
        }
        if (monitor.page() != MonitorPage.DESTINATION) {
            throw new AssertionError("Expected a full cycle to wrap back to DESTINATION, got " + monitor.page());
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 160)
    public void monitorOutsideInterior_staysUnlinked(GameTestHelper context) {
        redirectSaves(context);
        TardisMonitorBlockEntity monitor = placeMonitor(context, new BlockPos(2, 2, 2));
        // Two link retries: the GameTest level is not the TARDIS dimension, so nothing links.
        context.runAfterDelay(130, () -> {
            context.assertTrue(monitor.getTardisId() == null, "Monitor outside an interior must not link");
            context.assertFalse(monitor.readout().linked(), "Unlinked monitor must show no signal");
            context.succeed();
        });
    }

    private static TardisMonitorBlockEntity placeMonitor(GameTestHelper context, BlockPos rel) {
        context.setBlock(rel, DWMBlocks.TARDIS_COMPACT_SCANNER.defaultBlockState()
                .setValue(TardisDecorEntityBlock.FACING, Direction.NORTH));
        if (!(context.getLevel().getBlockEntity(context.absolutePos(rel)) instanceof TardisMonitorBlockEntity monitor)) {
            throw new AssertionError("Expected TardisMonitorBlockEntity at " + rel);
        }
        return monitor;
    }

    private static void redirectSaves(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("gametest_tardis_data");
    }
}
