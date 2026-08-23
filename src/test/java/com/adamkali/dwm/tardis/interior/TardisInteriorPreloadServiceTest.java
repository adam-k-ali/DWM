package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.config.DWMConfigManager;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.boti.BotiPlotIndex;
import com.adamkali.dwm.tardis.portal.PortalStreamSyncService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TardisInteriorPreloadServiceTest {
    private static final UUID TARDIS_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    @BeforeEach
    void setUp() throws Exception {
        TardisInteriorPreloadService.clear();
        BotiPlotIndex.clear();
        PortalStreamSyncService.clear();
        resetConfig();
    }

    @AfterEach
    void tearDown() throws Exception {
        TardisInteriorPreloadService.clear();
        BotiPlotIndex.clear();
        PortalStreamSyncService.clear();
        resetConfig();
    }

    @Test
    void requestPreload_EnqueuesIdempotently() {
        ServerLevel world = mockExteriorWorld();
        TardisBlockEntity exterior = mockExterior(world, false);

        try (MockedStatic<DWMConfigManager> ignored = mockConfig(true)) {
            TardisInteriorPreloadService.requestPreload(world, exterior);
            TardisInteriorPreloadService.requestPreload(world, exterior);
        }

        assertEquals(1, TardisInteriorPreloadService.pendingJobCount());
        assertTrue(TardisInteriorPreloadService.hasJob(TARDIS_ID));
        assertEquals(TardisInteriorPreloadService.Phase.LOADING, TardisInteriorPreloadService.getPhase(TARDIS_ID));
    }

    @Test
    void requestPreload_NoOpsWhenDoorPortalsDisabled() {
        ServerLevel world = mockExteriorWorld();
        TardisBlockEntity exterior = mockExterior(world, false);

        try (MockedStatic<DWMConfigManager> ignored = mockConfig(false)) {
            TardisInteriorPreloadService.requestPreload(world, exterior);
        }

        assertEquals(0, TardisInteriorPreloadService.pendingJobCount());
    }

    @Test
    void requestPreload_RegistersPlotWhenAlreadyGenerated() {
        ServerLevel world = mockExteriorWorld();
        TardisBlockEntity exterior = mockExterior(world, true);
        when(exterior.getInteriorEntrance()).thenReturn(BlockPos.ZERO);

        try (MockedStatic<DWMConfigManager> ignored = mockConfig(true)) {
            TardisInteriorPreloadService.requestPreload(world, exterior);
        }

        assertEquals(0, TardisInteriorPreloadService.pendingJobCount());
        assertTrue(BotiPlotIndex.isRegistered(TARDIS_ID));
    }

    @Test
    void cancel_RemovesPendingJob() {
        ResourceKey<Level> dim = mock(ResourceKey.class);
        TardisInteriorPreloadService.enqueueForTest(TARDIS_ID, dim, BlockPos.ZERO);
        assertTrue(TardisInteriorPreloadService.hasJob(TARDIS_ID));

        TardisInteriorPreloadService.cancel(TARDIS_ID);

        assertFalse(TardisInteriorPreloadService.hasJob(TARDIS_ID));
        assertEquals(0, TardisInteriorPreloadService.pendingJobCount());
    }

    @Test
    void tick_StaysLoadingUntilChunksPresent() {
        ResourceKey<Level> dim = mock(ResourceKey.class);
        TardisInteriorPreloadService.enqueueForTest(TARDIS_ID, dim, new BlockPos(10, 64, 10));

        MinecraftServer server = mock(MinecraftServer.class);
        ServerLevel interior = mock(ServerLevel.class);
        ServerChunkCache chunkSource = mock(ServerChunkCache.class);
        when(server.getLevel(TardisDimensions.TARDIS_WORLD_KEY)).thenReturn(interior);
        when(interior.getChunkSource()).thenReturn(chunkSource);
        when(chunkSource.hasChunk(anyInt(), anyInt())).thenReturn(false);

        TardisInteriorPreloadService.tick(server);

        assertEquals(TardisInteriorPreloadService.Phase.LOADING, TardisInteriorPreloadService.getPhase(TARDIS_ID));
        assertEquals(1, TardisInteriorPreloadService.pendingJobCount());
        verify(chunkSource, never()).getChunk(anyInt(), anyInt());
    }

    @Test
    void tick_AdvancesToReadyWhenChunksLoaded_ThenDropsWithoutExterior() {
        ResourceKey<Level> dim = mock(ResourceKey.class);
        TardisInteriorPreloadService.enqueueForTest(TARDIS_ID, dim, new BlockPos(10, 64, 10));

        MinecraftServer server = mock(MinecraftServer.class);
        ServerLevel interior = mock(ServerLevel.class);
        ServerChunkCache chunkSource = mock(ServerChunkCache.class);
        when(server.getLevel(TardisDimensions.TARDIS_WORLD_KEY)).thenReturn(interior);
        when(server.getLevel(dim)).thenReturn(null);
        when(interior.getChunkSource()).thenReturn(chunkSource);
        when(chunkSource.hasChunk(anyInt(), anyInt())).thenReturn(true);

        TardisInteriorPreloadService.tick(server);

        // Ready-to-place attempted, exterior missing → job removed.
        assertFalse(TardisInteriorPreloadService.hasJob(TARDIS_ID));
        assertEquals(0, TardisInteriorPreloadService.pendingJobCount());
    }

    @Test
    void markBotiFootprintDirty_MarksExpectedColumnCount() {
        PortalStreamSyncService.markBotiFootprintDirty(TARDIS_ID);
        BlockPos origin = TardisPlotAllocator.plotOrigin(TARDIS_ID);
        int[] bounds = BotiInteriorSampler.footprintChunkBounds(origin);
        int expected = (bounds[1] - bounds[0] + 1) * (bounds[3] - bounds[2] + 1);
        assertEquals(expected, PortalStreamSyncService.botiDirtyChunkCountForTest(TARDIS_ID));
        assertTrue(expected > 0);
    }

    private static ServerLevel mockExteriorWorld() {
        ServerLevel world = mock(ServerLevel.class);
        @SuppressWarnings("unchecked")
        ResourceKey<Level> dim = mock(ResourceKey.class);
        when(world.isClientSide()).thenReturn(false);
        when(world.dimension()).thenReturn(dim);
        return world;
    }

    private static TardisBlockEntity mockExterior(ServerLevel world, boolean interiorGenerated) {
        TardisBlockEntity exterior = mock(TardisBlockEntity.class);
        when(exterior.getTardisIdOrNull()).thenReturn(TARDIS_ID);
        when(exterior.getTardisId()).thenReturn(TARDIS_ID);
        when(exterior.isInteriorGenerated()).thenReturn(interiorGenerated);
        when(exterior.getBlockPos()).thenReturn(new BlockPos(0, 64, 0));
        when(exterior.getLevel()).thenReturn(world);
        return exterior;
    }

    private static MockedStatic<DWMConfigManager> mockConfig(boolean doorPortals) {
        MockedStatic<DWMConfigManager> configManager = Mockito.mockStatic(DWMConfigManager.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put(DWMConfig.ENABLE_DOOR_PORTALS.getKey(), doorPortals);
        configManager.when(DWMConfigManager::load).thenReturn(map);
        DWMConfig.init();
        return configManager;
    }

    private static void resetConfig() throws Exception {
        Field initializedField = DWMConfig.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        initializedField.setBoolean(null, false);
        Field configField = DWMConfig.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(null, new HashMap<>());
    }
}
