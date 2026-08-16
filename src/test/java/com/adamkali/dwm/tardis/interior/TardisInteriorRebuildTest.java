package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class TardisInteriorRebuildTest {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        TardisDataLoader.tardisSaveDirectory = tempDir;
        clearCache();
        TardisTravelService.clearActiveForTests();
    }

    @AfterEach
    void tearDown() throws Exception {
        TardisTravelService.clearActiveForTests();
        clearCache();
        TardisDataLoader.tardisSaveDirectory = null;
    }

    @Test
    void regenerateInterior_refusesWhileTraveling() {
        TardisDataModel model = TardisDataLoader.create();
        model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);
        ServerLevel world = Mockito.mock(ServerLevel.class);

        assertNull(TardisInteriorService.regenerateInterior(world, BlockPos.ZERO, model.uuid));
        Mockito.verifyNoInteractions(world);
    }

    @Test
    void regenerateInterior_refusesNullArgs() {
        assertNull(TardisInteriorService.regenerateInterior((ServerLevel) null, BlockPos.ZERO, UUID.randomUUID()));
        assertNull(TardisInteriorService.regenerateInterior(Mockito.mock(ServerLevel.class), null, UUID.randomUUID()));
        assertNull(TardisInteriorService.regenerateInterior(Mockito.mock(ServerLevel.class), BlockPos.ZERO, null));
    }

    @SuppressWarnings("unchecked")
    private static void clearCache() throws Exception {
        Field field = TardisDataLoader.class.getDeclaredField("tardisData");
        field.setAccessible(true);
        ((HashMap<?, ?>) field.get(null)).clear();
    }
}
