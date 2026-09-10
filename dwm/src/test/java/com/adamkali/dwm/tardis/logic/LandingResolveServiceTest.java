package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LandingResolveServiceTest {
    private UUID tardisId;
    private TardisDataModel model;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @BeforeEach
    void setUp() {
        tardisId = UUID.randomUUID();
        model = new TardisDataModel();
        model.uuid = tardisId;
        TardisTravelService.clearActiveForTests();
    }

    @AfterEach
    void tearDown() {
        TardisTravelService.clearActiveForTests();
    }

    @Test
    void enqueueForTest_tracksPhaseAndCommitFlag() {
        LandingResolveService.enqueueForTest(tardisId, false, LandingResolveService.Phase.LOCATE);
        assertEquals(1, LandingResolveService.pendingJobCount());
        assertTrue(LandingResolveService.hasJob(tardisId));
        assertFalse(LandingResolveService.isCommitted(tardisId));
        assertTrue(LandingResolveService.isWaitingForLanding(tardisId));
        assertEquals(LandingResolveService.Phase.LOCATE, LandingResolveService.getPhase(tardisId));
    }

    @Test
    void readyJob_isNotWaitingForLanding() {
        LandingResolveService.enqueueForTest(tardisId, false, LandingResolveService.Phase.READY);
        assertTrue(LandingResolveService.hasJob(tardisId));
        assertFalse(LandingResolveService.isWaitingForLanding(tardisId));
        assertEquals(LandingResolveService.Phase.READY, LandingResolveService.getPhase(tardisId));
    }

    @Test
    void commit_marksExistingLoadJobCommittedWithoutTickingReady() {
        LandingResolveService.enqueueForTest(tardisId, false, LandingResolveService.Phase.LOAD);
        MinecraftServer server = mock(MinecraftServer.class);

        assertTrue(LandingResolveService.commit(server, tardisId, tardisId));
        assertTrue(LandingResolveService.isCommitted(tardisId));
        assertEquals(LandingResolveService.Phase.LOAD, LandingResolveService.getPhase(tardisId));
    }

    @Test
    void cancel_removesJob() {
        LandingResolveService.enqueueForTest(tardisId, true, LandingResolveService.Phase.LOCATE);
        LandingResolveService.cancel(tardisId);
        assertFalse(LandingResolveService.hasJob(tardisId));
        assertEquals(0, LandingResolveService.pendingJobCount());
    }

    @Test
    void prefetch_noOpsWithoutServer() {
        LandingResolveService.prefetch(null, tardisId);
        assertFalse(LandingResolveService.hasJob(tardisId));
    }

    @Test
    void prefetch_startsUncommittedBiomeLocate() {
        model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
        model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);
        model.travelDestinationMode = DestinationMode.BIOME;
        model.travelDestinationDimension = "minecraft:overworld";
        model.travelDestinationBiome = "minecraft:plains";

        MinecraftServer server = mockServerWithOverworld();
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            LandingResolveService.prefetch(server, tardisId);
            LandingResolveService.prefetch(server, tardisId);

            assertEquals(1, LandingResolveService.pendingJobCount());
            assertTrue(LandingResolveService.hasJob(tardisId));
            assertFalse(LandingResolveService.isCommitted(tardisId));
            assertEquals(LandingResolveService.Phase.LOCATE, LandingResolveService.getPhase(tardisId));
        }
    }

    @Test
    void prefetch_skipsPlayerMode() {
        model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
        model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);
        model.travelDestinationMode = DestinationMode.PLAYER;
        model.travelTargetPlayerUuid = tardisId;

        MinecraftServer server = mockServerWithOverworld();
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);
            LandingResolveService.prefetch(server, tardisId);
            assertFalse(LandingResolveService.hasJob(tardisId));
        }
    }

    @Test
    void requestMaterialise_queuesBiomeSearchAndSecondPullIsPass() {
        model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
        model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);
        model.travelDestinationMode = DestinationMode.BIOME;
        model.travelDestinationDimension = "minecraft:overworld";
        model.travelDestinationBiome = "minecraft:plains";
        TardisTravelService.putFlightShellForTests(tardisId, tardisId);

        MinecraftServer server = mockServerWithOverworld();
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class)) {
            loader.when(() -> TardisDataLoader.get(tardisId)).thenReturn(model);

            assertEquals(
                    InteractionResult.SUCCESS,
                    TardisTravelService.requestMaterialise(tardisId, server, tardisId));
            assertEquals(TardisTravelPhase.IN_FLIGHT, model.getTravelPhase());
            assertTrue(LandingResolveService.isCommitted(tardisId));
            assertTrue(LandingResolveService.isWaitingForLanding(tardisId));
            assertEquals(LandingResolveService.Phase.LOCATE, LandingResolveService.getPhase(tardisId));

            assertEquals(
                    InteractionResult.PASS,
                    TardisTravelService.requestMaterialise(tardisId, server, tardisId));
            assertEquals(TardisTravelPhase.IN_FLIGHT, model.getTravelPhase());
        }
    }

    @Test
    void clearActiveForTests_clearsLandingJobs() {
        LandingResolveService.enqueueForTest(tardisId, true, LandingResolveService.Phase.LOCATE);
        TardisTravelService.clearActiveForTests();
        assertFalse(LandingResolveService.hasJob(tardisId));
        assertEquals(0, LandingResolveService.pendingJobCount());
    }

    private static MinecraftServer mockServerWithOverworld() {
        MinecraftServer server = mock(MinecraftServer.class);
        ServerLevel world = mock(ServerLevel.class);
        ResourceKey<Level> overworld = ResourceKey.create(
                Registries.DIMENSION, Identifier.parse("minecraft:overworld"));
        when(server.getLevel(any())).thenReturn(world);
        when(world.dimension()).thenReturn(overworld);
        return server;
    }
}
