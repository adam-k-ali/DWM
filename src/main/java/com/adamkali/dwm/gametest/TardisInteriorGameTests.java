package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomPlacer;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisEntryGate;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.WorldSavePath;

import java.util.UUID;

public class TardisInteriorGameTests implements FabricGameTest {

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void interiorDoor_StoresTardisIdForExit(TestContext context) {
        context.setBlockState(1, 2, 1, DWMBlocks.TARDIS_INTERIOR_DOOR);
        context.expectBlock(DWMBlocks.TARDIS_INTERIOR_DOOR, 1, 2, 1);

        if (!(context.getWorld().getBlockEntity(context.getAbsolutePos(new BlockPos(1, 2, 1))) instanceof TardisInteriorDoorBlockEntity door)) {
            throw new AssertionError("Expected TardisInteriorDoorBlockEntity");
        }

        UUID tardisId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        door.setTardisId(tardisId);
        if (!tardisId.equals(door.getTardisId())) {
            throw new AssertionError("Interior door did not retain tardisId");
        }
        if (!door.isOpenEnoughForExit()) {
            throw new AssertionError("Interior door should start open enough for exit");
        }

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void exteriorBlockEntity_StoresInteriorEntranceFields(TestContext context) {
        context.setBlockState(1, 2, 1, DWMBlocks.TARDIS_BLOCK);
        if (!(context.getWorld().getBlockEntity(context.getAbsolutePos(new BlockPos(1, 2, 1))) instanceof TardisBlockEntity exterior)) {
            throw new AssertionError("Expected TardisBlockEntity");
        }

        BlockPos entrance = TardisPlotAllocator.plotOrigin(exterior.getTardisId())
                .add(FirstDoctorConsoleRoomPlacer.LOCAL_ENTRANCE);
        exterior.setInteriorEntrance(entrance);
        exterior.setInteriorGenerated(true);

        if (!exterior.isInteriorGenerated() || !entrance.equals(exterior.getInteriorEntrance())) {
            throw new AssertionError("TardisBlockEntity did not retain interior entrance state");
        }
        if (!TardisDimensions.DIMENSION_ID.getPath().equals("tardis")) {
            throw new AssertionError("Unexpected dimension id");
        }

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void entryGate_MatchesDoorStateUsedByExterior(TestContext context) {
        TardisDataLoader.tardisSaveDirectory = context.getWorld().getServer()
                .getSavePath(WorldSavePath.ROOT).resolve("gametest_tardis_data");
        var model = TardisDataLoader.create();
        TardisDoorState closed = TardisLogic.getDoorState(model.uuid);
        if (TardisEntryGate.canEnter(closed)) {
            throw new AssertionError("Closed door must not allow entry");
        }
        TardisLogic.toggleDoor(model.uuid);
        for (int i = 0; i < 20; i++) {
            TardisLogic.updateDoorState(model.uuid);
        }
        if (!TardisEntryGate.canEnter(TardisLogic.getDoorState(model.uuid))) {
            throw new AssertionError("Fully open door must allow entry");
        }
        context.complete();
    }
}
