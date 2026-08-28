package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TardisInteriorUnitTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void plotOrigin_IsStableForSameUuid() {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        BlockPos first = TardisPlotAllocator.plotOrigin(id);
        BlockPos second = TardisPlotAllocator.plotOrigin(id);
        assertEquals(first, second);
        assertEquals(TardisPlotAllocator.PLOT_BASE_Y, first.getY());
    }

    @Test
    void plotOrigin_SeparatesDistinctUuids() {
        UUID a = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID b = UUID.fromString("00000000-0000-0000-0000-000000000002");
        BlockPos originA = TardisPlotAllocator.plotOrigin(a);
        BlockPos originB = TardisPlotAllocator.plotOrigin(b);
        assertNotEquals(originA, originB);
        assertTrue(TardisPlotAllocator.plotsAreSeparated(originA, originB, FirstDoctorConsoleRoomPlacer.SIZE_X));
    }

    @Test
    void plotOrigin_ManyUuidsDoNotCollideOnGridCell() {
        Set<BlockPos> origins = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            origins.add(TardisPlotAllocator.plotOrigin(new UUID(i, i * 31L + 7L)));
        }
        assertEquals(200, origins.size());
    }

    @Test
    void entryGate_RequiresOpenDoorNearFullySwung() {
        TardisDoorState closed = new TardisDoorState();
        closed.isOpen = false;
        closed.doorSwing = 0f;
        assertFalse(TardisEntryGate.canEnter(closed));

        TardisDoorState opening = new TardisDoorState();
        opening.isOpen = true;
        opening.doorSwing = 0.5f;
        assertFalse(TardisEntryGate.canEnter(opening));

        TardisDoorState ready = new TardisDoorState();
        ready.isOpen = true;
        ready.doorSwing = TardisDimensions.ENTRY_DOOR_SWING_THRESHOLD;
        assertTrue(TardisEntryGate.canEnter(ready));

        assertFalse(TardisEntryGate.canEnter(null));
    }

    @Test
    void botiGate_ShowsEarlierThanEntry() {
        TardisDoorState closed = new TardisDoorState();
        closed.doorSwing = 0f;
        assertFalse(TardisPortalGate.shouldShow(closed));

        TardisDoorState ajar = new TardisDoorState();
        ajar.isOpen = true;
        ajar.doorSwing = TardisDimensions.BOTI_DOOR_SWING_THRESHOLD;
        assertTrue(TardisPortalGate.shouldShow(ajar));
        assertFalse(TardisEntryGate.canEnter(ajar));

        assertFalse(TardisPortalGate.shouldShow(null));
        assertTrue(TardisDimensions.BOTI_DOOR_SWING_THRESHOLD < TardisDimensions.ENTRY_DOOR_SWING_THRESHOLD);
    }

    @Test
    void consoleRoomLayout_ConstantsAlignWithPlacer() {
        assertEquals(FirstDoctorConsoleRoomLayout.SIZE_X, FirstDoctorConsoleRoomPlacer.SIZE_X);
        assertEquals(FirstDoctorConsoleRoomLayout.SIZE_Y, FirstDoctorConsoleRoomPlacer.SIZE_Y);
        assertEquals(FirstDoctorConsoleRoomLayout.SIZE_Z, FirstDoctorConsoleRoomPlacer.SIZE_Z);
        assertEquals(FirstDoctorConsoleRoomLayout.LOCAL_ENTRANCE, FirstDoctorConsoleRoomPlacer.LOCAL_ENTRANCE);
        assertEquals(11, FirstDoctorConsoleRoomLayout.SIZE_X);
        assertEquals(7, FirstDoctorConsoleRoomLayout.SIZE_Y);
        assertEquals(17, FirstDoctorConsoleRoomLayout.SIZE_Z);
        assertEquals(new BlockPos(5, 1, 2), FirstDoctorConsoleRoomLayout.LOCAL_ENTRANCE);
        assertEquals(new BlockPos(4, 1, 1), FirstDoctorConsoleRoomLayout.LOCAL_DOOR_ORIGIN);
        assertEquals(new BlockPos(5, 1, 7), FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE);
        assertEquals(4, FirstDoctorConsoleRoomLayout.LAYOUT_VERSION);
        assertTrue(FirstDoctorConsoleRoomLayout.SIZE_Z < TardisPlotAllocator.PLOT_SPACING);
    }

    @Test
    void roomChunkBounds_coversFullRoomIncludingChunkBoundary() {
        BlockPos origin = new BlockPos(15, 64, 15);
        int[] bounds = FirstDoctorConsoleRoomPlacer.roomChunkBounds(origin);
        assertEquals(0, bounds[0]);
        assertEquals(1, bounds[1]);
        assertEquals(0, bounds[2]);
        assertEquals(1, bounds[3]);
    }

    @Test
    void consoleRoomLayout_PlacesFirstDoctorConsoleWithoutStackedRoundel() {
        Map<BlockPos, BlockState> placements = FirstDoctorConsoleRoomLayout.placements();
        BlockPos consolePos = FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE;
        assertEquals(DWMBlocks.FIRST_DOCTOR_CONSOLE, placements.get(consolePos).getBlock());
        assertTrue(placements.get(consolePos.above()).isAir(), "no stacked roundel above console");

        Map<BlockPos, BlockState> boti = FirstDoctorConsoleRoomLayout.botiVisiblePlacements();
        assertEquals(DWMBlocks.FIRST_DOCTOR_CONSOLE, boti.get(consolePos).getBlock(),
                "console must be included in BOTI so its BER can draw");
    }

    @Test
    void consoleRoomPlacer_StampsFullStrengthInvisibleLightAboveConsole() {
        ServerLevel world = mock(ServerLevel.class);
        BlockPos origin = new BlockPos(100, 64, 200);

        FirstDoctorConsoleRoomPlacer.placeInteriorLight(world, origin);

        verify(world).setBlock(
                eq(origin.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE.above(3))),
                argThat(state -> state.is(Blocks.LIGHT) && state.getLightEmission() == 15),
                eq(Block.UPDATE_CLIENTS)
        );
    }

    @Test
    void dimensionConstants_AreStable() {
        assertEquals("dwm", TardisDimensions.DIMENSION_ID.getNamespace());
        assertEquals("tardis", TardisDimensions.DIMENSION_ID.getPath());
        assertEquals("first_doctor_console_room", TardisDimensions.CONSOLE_ROOM_STRUCTURE_ID.getPath());
        assertTrue(TardisDimensions.isTardisWorld(TardisDimensions.TARDIS_WORLD_KEY));
        assertFalse(TardisDimensions.isTardisWorld(net.minecraft.world.level.Level.OVERWORLD));
        assertFalse(TardisDimensions.isTardisWorld((net.minecraft.world.level.Level) null));
    }

    @Test
    void structureNbtResource_ExistsAndIsNonEmpty() {
        var stream = getClass().getClassLoader().getResourceAsStream("data/dwm/structure/first_doctor_console_room.nbt");
        assertNotNull(stream, "first_doctor_console_room.nbt must be on the classpath");
        try (stream) {
            assertTrue(stream.readAllBytes().length > 100);
        } catch (Exception e) {
            fail(e);
        }
    }
}
