package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BotiPlotIndexTest {

    @AfterEach
    void tearDown() {
        BotiPlotIndex.clear();
    }

    @Test
    void registerAndResolve_InsideFootprint() {
        UUID id = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        BotiPlotIndex.register(id);
        BlockPos origin = TardisPlotAllocator.plotOrigin(id);

        assertTrue(BotiPlotIndex.isRegistered(id));
        assertEquals(origin, BotiPlotIndex.getOrigin(id));
        assertEquals(id, BotiPlotIndex.resolve(origin.add(5, 1, 5)));
    }

    @Test
    void resolve_OutsideFootprintReturnsNull() {
        UUID id = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        BotiPlotIndex.register(id);
        BlockPos origin = TardisPlotAllocator.plotOrigin(id);

        assertNull(BotiPlotIndex.resolve(origin.add(TardisPlotAllocator.PLOT_SPACING / 2, 0, 0)));
        assertNull(BotiPlotIndex.resolve(origin.add(0, BotiInteriorSampler.SIZE_Y + 2, 0)));
    }

    @Test
    void resolve_UnregisteredPlotReturnsNull() {
        UUID id = UUID.fromString("11111111-2222-3333-4444-555555555555");
        BlockPos origin = TardisPlotAllocator.plotOrigin(id);
        assertNull(BotiPlotIndex.resolve(origin.add(1, 1, 1)));
    }
}
