package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.portal.PortalSampler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import net.minecraft.core.BlockPos;

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
        assertEquals(id, BotiPlotIndex.resolve(origin.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE)));
        assertEquals(id, BotiPlotIndex.resolve(origin.offset(8, 2, 8)));
    }

    @Test
    void resolve_OutsideStreamReturnsNull() {
        UUID id = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        BotiPlotIndex.register(id);
        BlockPos origin = TardisPlotAllocator.plotOrigin(id);

        assertNull(BotiPlotIndex.resolve(origin.offset(TardisPlotAllocator.PLOT_SPACING, 0, 0)));
        int yRadius = PortalSampler.streamYRadiusBlocks(PortalSampler.DEFAULT_STREAM_RADIUS_CHUNKS);
        assertNull(BotiPlotIndex.resolve(origin.offset(0, yRadius + 1, 0)));
    }

    @Test
    void resolve_UnregisteredPlotReturnsNull() {
        UUID id = UUID.fromString("11111111-2222-3333-4444-555555555555");
        BlockPos origin = TardisPlotAllocator.plotOrigin(id);
        assertNull(BotiPlotIndex.resolve(origin.offset(1, 1, 1)));
    }
}
