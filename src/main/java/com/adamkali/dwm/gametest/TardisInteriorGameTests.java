package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomPlacer;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisEntryGate;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

public class TardisInteriorGameTests {

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void interiorDoor_StoresTardisIdForExit(GameTestHelper context) {
        context.setBlock(1, 2, 1, DWMBlocks.TARDIS_INTERIOR_DOOR);
        context.assertBlockPresent(DWMBlocks.TARDIS_INTERIOR_DOOR, 1, 2, 1);

        if (!(context.getLevel().getBlockEntity(context.absolutePos(new BlockPos(1, 2, 1))) instanceof TardisInteriorDoorBlockEntity door)) {
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

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void interiorDoor_UseOnNonOriginCell_TogglesBankOpen(GameTestHelper context) {
        Direction facing = Direction.SOUTH;
        BlockPos originRel = new BlockPos(1, 2, 1);
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (int slot = 0; slot < TardisInteriorDoorBlock.BANK_WIDTH; slot++) {
                BlockPos cellRel = TardisInteriorDoorBlock.cellPos(originRel, facing, half, slot);
                context.setBlock(
                        cellRel.getX(), cellRel.getY(), cellRel.getZ(),
                        TardisInteriorDoorBlock.bankCellState(facing, half, slot, true));
            }
        }

        BlockPos originAbs = context.absolutePos(originRel);
        if (!(context.getLevel().getBlockEntity(originAbs) instanceof TardisInteriorDoorBlockEntity originDoor)) {
            throw new AssertionError("Expected origin TardisInteriorDoorBlockEntity");
        }
        BlockPos farRel = TardisInteriorDoorBlock.cellPos(originRel, facing, DoubleBlockHalf.UPPER, 2);
        BlockPos farAbs = context.absolutePos(farRel);
        if (context.getLevel().getBlockEntity(farAbs) != null) {
            throw new AssertionError("Non-origin door cell must not have a block entity");
        }

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        BlockState farState = context.getLevel().getBlockState(farAbs);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(farAbs), Direction.NORTH, farAbs, false);
        farState.useWithoutItem(context.getLevel(), player, hit);

        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (int slot = 0; slot < TardisInteriorDoorBlock.BANK_WIDTH; slot++) {
                BlockPos cellAbs = context.absolutePos(
                        TardisInteriorDoorBlock.cellPos(originRel, facing, half, slot));
                BlockState cellState = context.getLevel().getBlockState(cellAbs);
                if (cellState.getValue(TardisInteriorDoorBlock.OPEN)) {
                    throw new AssertionError("Expected OPEN=false after use on non-origin cell at " + cellAbs);
                }
            }
        }
        if (originDoor.isOpen()) {
            throw new AssertionError("Origin BE open flag should be false after toggle");
        }

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void exteriorBlockEntity_StoresInteriorEntranceFields(GameTestHelper context) {
        context.setBlock(1, 2, 1, DWMBlocks.TARDIS_BLOCK);
        if (!(context.getLevel().getBlockEntity(context.absolutePos(new BlockPos(1, 2, 1))) instanceof TardisBlockEntity exterior)) {
            throw new AssertionError("Expected TardisBlockEntity");
        }

        BlockPos entrance = TardisPlotAllocator.plotOrigin(exterior.getTardisId())
                .offset(FirstDoctorConsoleRoomPlacer.LOCAL_ENTRANCE);
        exterior.setInteriorEntrance(entrance);
        exterior.setInteriorGenerated(true);

        if (!exterior.isInteriorGenerated() || !entrance.equals(exterior.getInteriorEntrance())) {
            throw new AssertionError("TardisBlockEntity did not retain interior entrance state");
        }
        if (!TardisDimensions.DIMENSION_ID.getPath().equals("tardis")) {
            throw new AssertionError("Unexpected dimension id");
        }

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void entryGate_MatchesDoorStateUsedByExterior(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT).resolve("gametest_tardis_data");
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
        context.succeed();
    }
}
