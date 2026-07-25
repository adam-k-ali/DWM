package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TardisInteriorUnitTest {

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
    void dimensionConstants_AreStable() {
        assertEquals("dwm", TardisDimensions.DIMENSION_ID.getNamespace());
        assertEquals("tardis", TardisDimensions.DIMENSION_ID.getPath());
        assertEquals("first_doctor_console_room", TardisDimensions.CONSOLE_ROOM_STRUCTURE_ID.getPath());
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
