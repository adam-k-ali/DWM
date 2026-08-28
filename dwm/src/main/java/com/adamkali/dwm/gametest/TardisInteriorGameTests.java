package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomPlacer;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisEntryGate;
import com.adamkali.dwm.tardis.interior.TardisInteriorService;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.adamkali.dwm.tardis.logic.WaypointLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
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

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void consoleRoomPlacer_StampsTardisIdOnDoorAndConsole(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT).resolve("gametest_tardis_data");
        UUID tardisId = TardisDataLoader.create().uuid;

        // Place relative to the empty GameTest structure so chunks are loaded and assertions
        // stay within the harness world (not the shared dwm:tardis dimension).
        BlockPos originRel = new BlockPos(0, 2, 0);
        BlockPos originAbs = context.absolutePos(originRel);
        BlockPos entrance = FirstDoctorConsoleRoomPlacer.place(context.getLevel(), originAbs, tardisId);

        BlockPos expectedEntrance = originAbs.offset(FirstDoctorConsoleRoomLayout.LOCAL_ENTRANCE);
        if (!expectedEntrance.equals(entrance)) {
            throw new AssertionError("Expected entrance " + expectedEntrance + " but got " + entrance);
        }

        BlockPos consoleAbs = originAbs.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE);
        if (!context.getLevel().getBlockState(consoleAbs).is(DWMBlocks.FIRST_DOCTOR_CONSOLE)) {
            throw new AssertionError("Expected First Doctor console at " + consoleAbs);
        }
        if (!(context.getLevel().getBlockEntity(consoleAbs) instanceof FirstDoctorConsoleBlockEntity console)) {
            throw new AssertionError("Expected FirstDoctorConsoleBlockEntity at " + consoleAbs);
        }
        if (!tardisId.equals(console.getTardisId())) {
            throw new AssertionError("Console tardisId not stamped: " + console.getTardisId());
        }

        BlockPos doorOriginAbs = originAbs.offset(FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN);
        Direction facing = Direction.SOUTH;
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (int slot = 0; slot < TardisInteriorDoorBlock.BANK_WIDTH; slot++) {
                BlockPos cell = TardisInteriorDoorBlock.cellPos(doorOriginAbs, facing, half, slot);
                if (!context.getLevel().getBlockState(cell).is(DWMBlocks.TARDIS_INTERIOR_DOOR)) {
                    throw new AssertionError("Expected interior door bank cell at " + cell);
                }
            }
        }
        if (!(context.getLevel().getBlockEntity(doorOriginAbs) instanceof TardisInteriorDoorBlockEntity door)) {
            throw new AssertionError("Expected TardisInteriorDoorBlockEntity at door origin " + doorOriginAbs);
        }
        if (!tardisId.equals(door.getTardisId())) {
            throw new AssertionError("Door tardisId not stamped: " + door.getTardisId());
        }
        if (door.isOpen() || context.getLevel().getBlockState(doorOriginAbs).getValue(TardisInteriorDoorBlock.OPEN)) {
            throw new AssertionError("Placed interior doors should start closed to match the exterior");
        }

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 80)
    public void consoleRoomPlacer_EnablesAndCalculatesLightWithThreadedEngine(GameTestHelper context) {
        ServerLevel interior = context.getLevel();
        UUID tardisId = UUID.randomUUID();
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        FirstDoctorConsoleRoomPlacer.place(interior, origin, tardisId);
        BlockPos sourcePos = origin.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE.above(3));

        context.runAfterDelay(40, () -> {
            int brightness = interior.getBrightness(LightLayer.BLOCK, sourcePos);
            if (brightness != 15) {
                throw new AssertionError("Expected propagated block light 15 at " + sourcePos + " but got " + brightness);
            }
            var sample = BotiInteriorSampler.sampleStreamChunk(
                    interior, tardisId, sourcePos.getX() >> 4, sourcePos.getZ() >> 4);
            if (sample == null) {
                throw new AssertionError("Expected BOTI stream sample after interior place (chunk should be FULL)");
            }
            if (sample.lightData().brightness(LightLayer.BLOCK, sourcePos, -1) != 15) {
                throw new AssertionError("Expected BOTI sample to retain propagated block light");
            }
            context.succeed();
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void regenerateInterior_ClearsDirtAndKeepsLinkedData(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT).resolve("gametest_tardis_data");
        var model = TardisDataLoader.create();
        UUID tardisId = model.uuid;
        UUID owner = UUID.fromString("11111111-2222-3333-4444-555555555555");
        model.setOwner(owner);
        model.setExteriorLocation("minecraft:overworld", 10, 64, 20, 0);
        if (WaypointLogic.add(model, "Rebuild Keep").isEmpty()) {
            throw new AssertionError("Expected waypoint to be saved before rebuild");
        }
        UUID waypointId = model.getWaypoints().getFirst().id;

        BlockPos originRel = new BlockPos(0, 2, 0);
        BlockPos originAbs = context.absolutePos(originRel);
        FirstDoctorConsoleRoomPlacer.place(context.getLevel(), originAbs, tardisId);

        BlockPos dirtyRel = new BlockPos(3, 3, 5);
        BlockPos dirtyAbs = context.absolutePos(dirtyRel);
        context.setBlock(dirtyRel.getX(), dirtyRel.getY(), dirtyRel.getZ(), Blocks.GOLD_BLOCK);

        BlockPos entrance = TardisInteriorService.regenerateInterior(context.getLevel(), originAbs, tardisId);
        if (entrance == null) {
            throw new AssertionError("regenerateInterior returned null");
        }

        if (context.getLevel().getBlockState(dirtyAbs).is(Blocks.GOLD_BLOCK)) {
            throw new AssertionError("Dirty gold block should be cleared by rebuild");
        }

        BlockPos consoleAbs = originAbs.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE);
        if (!context.getLevel().getBlockState(consoleAbs).is(DWMBlocks.FIRST_DOCTOR_CONSOLE)) {
            throw new AssertionError("Expected console after rebuild at " + consoleAbs);
        }
        if (!(context.getLevel().getBlockEntity(consoleAbs) instanceof FirstDoctorConsoleBlockEntity console)
                || !tardisId.equals(console.getTardisId())) {
            throw new AssertionError("Console must keep same tardisId after rebuild");
        }

        BlockPos doorOriginAbs = originAbs.offset(FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN);
        if (!(context.getLevel().getBlockEntity(doorOriginAbs) instanceof TardisInteriorDoorBlockEntity door)
                || !tardisId.equals(door.getTardisId())) {
            throw new AssertionError("Door must keep same tardisId after rebuild");
        }

        TardisDataModel after = TardisDataLoader.get(tardisId);
        if (after == null) {
            throw new AssertionError("TARDIS data missing after rebuild");
        }
        if (!owner.equals(after.ownerUuid)) {
            throw new AssertionError("Owner must be preserved after rebuild");
        }
        if (after.getWaypoints().stream().noneMatch(w -> waypointId.equals(w.id))) {
            throw new AssertionError("Waypoint must be preserved after rebuild");
        }
        if (!tardisId.equals(after.uuid)) {
            throw new AssertionError("TARDIS uuid must not change on rebuild");
        }

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void interiorDoor_LinkedUse_TogglesSharedDoorState(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT).resolve("gametest_tardis_data");
        TardisDataModel model = TardisDataLoader.create();

        Direction facing = Direction.SOUTH;
        BlockPos originRel = new BlockPos(1, 2, 1);
        placeClosedDoorBank(context, originRel, facing);

        BlockPos originAbs = context.absolutePos(originRel);
        if (!(context.getLevel().getBlockEntity(originAbs) instanceof TardisInteriorDoorBlockEntity originDoor)) {
            throw new AssertionError("Expected origin TardisInteriorDoorBlockEntity");
        }
        originDoor.setTardisId(model.uuid);

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        BlockState originState = context.getLevel().getBlockState(originAbs);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(originAbs), Direction.NORTH, originAbs, false);
        originState.useWithoutItem(context.getLevel(), player, hit);

        if (!model.doorState.isOpen) {
            throw new AssertionError("Interior use should open shared TardisDataModel door state");
        }
        assertBankOpen(context, originRel, facing, true);
        if (!originDoor.isOpen()) {
            throw new AssertionError("Origin BE should be open after linked toggle");
        }

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void interiorDoor_MirrorsExteriorToggleOnTick(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT).resolve("gametest_tardis_data");
        TardisDataModel model = TardisDataLoader.create();

        Direction facing = Direction.SOUTH;
        BlockPos originRel = new BlockPos(1, 2, 1);
        placeClosedDoorBank(context, originRel, facing);

        BlockPos originAbs = context.absolutePos(originRel);
        if (!(context.getLevel().getBlockEntity(originAbs) instanceof TardisInteriorDoorBlockEntity originDoor)) {
            throw new AssertionError("Expected origin TardisInteriorDoorBlockEntity");
        }
        originDoor.setTardisId(model.uuid);

        TardisLogic.toggleDoor(model.uuid);
        if (!model.doorState.isOpen) {
            throw new AssertionError("Expected exterior toggle to open shared door state");
        }
        if (context.getLevel().getBlockState(originAbs).getValue(TardisInteriorDoorBlock.OPEN)) {
            throw new AssertionError("Interior OPEN should still be closed before origin tick");
        }

        originDoor.tick(context.getLevel(), originAbs, context.getLevel().getBlockState(originAbs), originDoor);
        assertBankOpen(context, originRel, facing, true);

        context.succeed();
    }

    private static void placeClosedDoorBank(GameTestHelper context, BlockPos originRel, Direction facing) {
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (int slot = 0; slot < TardisInteriorDoorBlock.BANK_WIDTH; slot++) {
                BlockPos cellRel = TardisInteriorDoorBlock.cellPos(originRel, facing, half, slot);
                context.setBlock(
                        cellRel.getX(), cellRel.getY(), cellRel.getZ(),
                        TardisInteriorDoorBlock.bankCellState(facing, half, slot, false));
            }
        }
    }

    private static void assertBankOpen(
            GameTestHelper context,
            BlockPos originRel,
            Direction facing,
            boolean expectedOpen
    ) {
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (int slot = 0; slot < TardisInteriorDoorBlock.BANK_WIDTH; slot++) {
                BlockPos cellAbs = context.absolutePos(
                        TardisInteriorDoorBlock.cellPos(originRel, facing, half, slot));
                BlockState cellState = context.getLevel().getBlockState(cellAbs);
                if (cellState.getValue(TardisInteriorDoorBlock.OPEN) != expectedOpen) {
                    throw new AssertionError(
                            "Expected OPEN=" + expectedOpen + " at " + cellAbs
                                    + " but was " + cellState.getValue(TardisInteriorDoorBlock.OPEN));
                }
            }
        }
    }
}
