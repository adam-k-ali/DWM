package com.adamkali.dwm.network;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.interior.ars.ArsService;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Server-side gates of the ARS C2S payloads: owner, not in flight, and standing inside that TARDIS's plot in
 * {@code dwm:tardis}. The service itself is mocked; {@code ArsLayoutLogicTest} covers the rules.
 */
class ArsPayloadHandlerTest {
    private static final Identifier LIBRARY = Identifier.fromNamespaceAndPath("dwm", "library");
    private static final ArsService.Result GROWN = new ArsService.Result(true, ArsService.GROWN_KEY, List.of());

    private UUID owner;
    private TardisDataModel model;
    private BlockPos insidePlot;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @BeforeEach
    void setUp() {
        owner = UUID.randomUUID();
        model = new TardisDataModel();
        model.setOwner(owner);
        insidePlot = TardisPlotAllocator.plotOrigin(model.uuid).offset(20, 1, 12);
    }

    @Test
    void grow_reachesServiceWithRawFieldsForOwnerInsidePlot() {
        ArsGrowC2SPayload payload = new ArsGrowC2SPayload(model.uuid, LIBRARY, 4, 0, 3);
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<ArsService> service = Mockito.mockStatic(ArsService.class)) {
            loader.when(() -> TardisDataLoader.get(model.uuid)).thenReturn(model);
            service.when(() -> ArsService.grow(null, model.uuid, LIBRARY, 4, 0, 3, true)).thenReturn(GROWN);

            ArsService.Result result = grow(payload, owner, TardisDimensions.TARDIS_WORLD_KEY, insidePlot, true);

            assertSame(GROWN, result);
            service.verify(() -> ArsService.grow(null, model.uuid, LIBRARY, 4, 0, 3, true));
        }
    }

    @Test
    void grow_passesAbsurdIntsThroughToTheServiceUnchanged() {
        ArsGrowC2SPayload payload = new ArsGrowC2SPayload(
                model.uuid, LIBRARY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<ArsService> service = Mockito.mockStatic(ArsService.class)) {
            loader.when(() -> TardisDataLoader.get(model.uuid)).thenReturn(model);

            grow(payload, owner, TardisDimensions.TARDIS_WORLD_KEY, insidePlot, false);

            service.verify(() -> ArsService.grow(null, model.uuid, LIBRARY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, false));
        }
    }

    @Test
    void jettison_reachesServiceForOwnerInsidePlot() {
        ArsService.Result jettisoned = new ArsService.Result(true, ArsService.JETTISONED_KEY, List.of());
        ArsJettisonC2SPayload payload = new ArsJettisonC2SPayload(model.uuid, Integer.MAX_VALUE, -7);
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<ArsService> service = Mockito.mockStatic(ArsService.class)) {
            loader.when(() -> TardisDataLoader.get(model.uuid)).thenReturn(model);
            service.when(() -> ArsService.jettison(null, model.uuid, Integer.MAX_VALUE, -7)).thenReturn(jettisoned);

            ArsService.Result result = ServerPayloadTypeRegistry.handleArsJettison(
                    null, payload, owner, TardisDimensions.TARDIS_WORLD_KEY, insidePlot, null);

            assertSame(jettisoned, result);
        }
    }

    @Test
    void grow_refusesUnknownTardis() {
        UUID unknown = UUID.randomUUID();
        ArsGrowC2SPayload payload = new ArsGrowC2SPayload(unknown, LIBRARY, 4, 0, 0);
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<ArsService> service = Mockito.mockStatic(ArsService.class)) {
            loader.when(() -> TardisDataLoader.get(unknown)).thenReturn(null);

            assertNull(grow(payload, owner, TardisDimensions.TARDIS_WORLD_KEY,
                    TardisPlotAllocator.plotOrigin(unknown).offset(20, 1, 12), true));
            service.verifyNoInteractions();
        }
    }

    @Test
    void growAndJettison_refuseNonOwner() {
        UUID visitor = UUID.randomUUID();
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<ArsService> service = Mockito.mockStatic(ArsService.class)) {
            loader.when(() -> TardisDataLoader.get(model.uuid)).thenReturn(model);

            assertNull(grow(new ArsGrowC2SPayload(model.uuid, LIBRARY, 4, 0, 0),
                    visitor, TardisDimensions.TARDIS_WORLD_KEY, insidePlot, true));
            assertNull(ServerPayloadTypeRegistry.handleArsJettison(null,
                    new ArsJettisonC2SPayload(model.uuid, 4, 0), visitor,
                    TardisDimensions.TARDIS_WORLD_KEY, insidePlot, null));
            service.verifyNoInteractions();
        }
    }

    @Test
    void growAndJettison_refuseWhileInFlight() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<TardisTravelService> travel = Mockito.mockStatic(TardisTravelService.class);
             MockedStatic<ArsService> service = Mockito.mockStatic(ArsService.class)) {
            loader.when(() -> TardisDataLoader.get(model.uuid)).thenReturn(model);
            travel.when(() -> TardisTravelService.isTraveling(model.uuid)).thenReturn(true);

            assertNull(grow(new ArsGrowC2SPayload(model.uuid, LIBRARY, 4, 0, 0),
                    owner, TardisDimensions.TARDIS_WORLD_KEY, insidePlot, true));
            assertNull(jettison(owner, TardisDimensions.TARDIS_WORLD_KEY, insidePlot));
            service.verifyNoInteractions();
        }
    }

    @Test
    void growAndJettison_refuseOutsideTheTardisDimension() {
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<ArsService> service = Mockito.mockStatic(ArsService.class)) {
            loader.when(() -> TardisDataLoader.get(model.uuid)).thenReturn(model);

            // Same block coordinates as the plot, but in the overworld.
            assertNull(grow(new ArsGrowC2SPayload(model.uuid, LIBRARY, 4, 0, 0),
                    owner, Level.OVERWORLD, insidePlot, true));
            assertNull(jettison(owner, Level.OVERWORLD, insidePlot));
            assertNull(grow(new ArsGrowC2SPayload(model.uuid, LIBRARY, 4, 0, 0),
                    owner, null, insidePlot, true));
            service.verifyNoInteractions();
        }
    }

    @Test
    void growAndJettison_refuseOutsideThisTardisPlot() {
        BlockPos origin = TardisPlotAllocator.plotOrigin(model.uuid);
        int offset = TardisPlotAllocator.WINDOW_OFFSET;
        List<BlockPos> outside = List.of(
                origin.offset(-offset - 1, 1, 5),
                origin.offset(TardisPlotAllocator.PLOT_SPACING - offset, 1, 5),
                origin.offset(5, 1, TardisPlotAllocator.PLOT_SPACING + 10));
        try (MockedStatic<TardisDataLoader> loader = Mockito.mockStatic(TardisDataLoader.class);
             MockedStatic<ArsService> service = Mockito.mockStatic(ArsService.class)) {
            loader.when(() -> TardisDataLoader.get(model.uuid)).thenReturn(model);

            for (BlockPos pos : outside) {
                assertNull(grow(new ArsGrowC2SPayload(model.uuid, LIBRARY, 4, 0, 0),
                        owner, TardisDimensions.TARDIS_WORLD_KEY, pos, true), pos.toString());
                assertNull(jettison(owner, TardisDimensions.TARDIS_WORLD_KEY, pos), pos.toString());
            }
            assertNull(grow(new ArsGrowC2SPayload(model.uuid, LIBRARY, 4, 0, 0),
                    owner, TardisDimensions.TARDIS_WORLD_KEY, null, true));
            service.verifyNoInteractions();
        }
    }

    @Test
    void requireInsideTardis_acceptsWholeWindowAtAnyHeight() {
        BlockPos window = TardisPlotAllocator.windowOrigin(TardisPlotAllocator.plotOrigin(model.uuid));
        int last = TardisPlotAllocator.PLOT_SPACING - 1;
        for (BlockPos pos : List.of(window, window.offset(last, -30, last), window.offset(0, 40, last))) {
            assertTrue(ServerPayloadTypeRegistry.requireInsideTardis(
                    model.uuid, TardisDimensions.TARDIS_WORLD_KEY, pos, null), pos.toString());
        }
        assertFalse(ServerPayloadTypeRegistry.requireInsideTardis(
                model.uuid, TardisDimensions.TARDIS_WORLD_KEY, window.offset(last + 1, 0, 0), null));
        assertFalse(ServerPayloadTypeRegistry.requireInsideTardis(
                model.uuid, TardisDimensions.TARDIS_WORLD_KEY, window.offset(-1, 0, 0), null));
    }

    @Test
    void growAndJettison_ignoreNullPlayerAndPayload() {
        try (MockedStatic<ArsService> service = Mockito.mockStatic(ArsService.class)) {
            assertFalse(ServerPayloadTypeRegistry.safelyHandleArsGrow(
                    new ArsGrowC2SPayload(model.uuid, LIBRARY, 4, 0, 0), null));
            assertFalse(ServerPayloadTypeRegistry.safelyHandleArsJettison(
                    new ArsJettisonC2SPayload(model.uuid, 4, 0), null));
            assertNull(ServerPayloadTypeRegistry.handleArsGrow(null, null, owner,
                    TardisDimensions.TARDIS_WORLD_KEY, insidePlot, true, null));
            service.verify(() -> ArsService.grow(ArgumentMatchers.any(), ArgumentMatchers.any(),
                    ArgumentMatchers.any(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(),
                    ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean()), Mockito.never());
        }
    }

    private static ArsService.Result grow(
            ArsGrowC2SPayload payload,
            UUID player,
            ResourceKey<Level> dimension,
            BlockPos position,
            boolean instabuild
    ) {
        return ServerPayloadTypeRegistry.handleArsGrow(null, payload, player, dimension, position, instabuild, null);
    }

    private ArsService.Result jettison(UUID player, ResourceKey<Level> dimension, BlockPos position) {
        return ServerPayloadTypeRegistry.handleArsJettison(
                null, new ArsJettisonC2SPayload(model.uuid, 4, 0), player, dimension, position, null);
    }
}
