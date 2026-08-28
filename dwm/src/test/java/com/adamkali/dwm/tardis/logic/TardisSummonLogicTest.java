package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TardisSummonLogicTest {
    @TempDir
    Path tempDir;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

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
    void landingOrigin_isCellOnClickedFace() {
        BlockPos clicked = new BlockPos(4, 10, 8);
        assertEquals(new BlockPos(4, 11, 8), TardisSummonLogic.landingOrigin(clicked, Direction.UP));
        assertEquals(new BlockPos(4, 10, 7), TardisSummonLogic.landingOrigin(clicked, Direction.NORTH));
    }

    @Test
    void doorFacingToward_pointsFromLandingToPlayer() {
        BlockPos landing = new BlockPos(0, 64, 0);
        assertEquals(
                Direction.SOUTH,
                TardisSummonLogic.doorFacingToward(landing, 0.5, 4.5, Direction.NORTH)
        );
        assertEquals(
                Direction.EAST,
                TardisSummonLogic.doorFacingToward(landing, 5.5, 0.5, Direction.NORTH)
        );
        assertEquals(
                Direction.WEST,
                TardisSummonLogic.doorFacingToward(landing, 0.5, 0.5, Direction.WEST)
        );
    }

    @Test
    void preview_noTardisWhenModelMissing() {
        assertEquals(TardisSummonLogic.Result.NO_TARDIS, TardisSummonLogic.preview(null));
    }

    @Test
    void preview_inProgressDuringDematOrMat() {
        TardisDataModel model = TardisDataLoader.create();
        model.setTravelPhase(TardisTravelPhase.DEMATERIALISING);
        assertEquals(TardisSummonLogic.Result.IN_PROGRESS, TardisSummonLogic.preview(model));
        model.setTravelPhase(TardisTravelPhase.MATERIALISING);
        assertEquals(TardisSummonLogic.Result.IN_PROGRESS, TardisSummonLogic.preview(model));
    }

    @Test
    void preview_unavailableWhenIdleWithoutExterior() {
        TardisDataModel model = TardisDataLoader.create();
        assertEquals(TardisSummonLogic.Result.UNAVAILABLE, TardisSummonLogic.preview(model));
    }

    @Test
    void preview_readyWhenIdleWithExteriorOrInFlight() {
        TardisDataModel model = TardisDataLoader.create();
        model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
        assertEquals(TardisSummonLogic.Result.SUMMONED, TardisSummonLogic.preview(model));
        model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);
        assertEquals(TardisSummonLogic.Result.SUMMONED, TardisSummonLogic.preview(model));
    }

    @Test
    void overlayKey_mapsEachResult() {
        assertEquals("dwm.stattenheim.no_tardis", TardisSummonLogic.overlayKey(TardisSummonLogic.Result.NO_TARDIS));
        assertEquals("dwm.stattenheim.summoned", TardisSummonLogic.overlayKey(TardisSummonLogic.Result.SUMMONED));
        assertEquals(
                CircuitFittedLogic.CIRCUIT_BROKEN_KEY,
                TardisSummonLogic.overlayKey(TardisSummonLogic.Result.CIRCUIT_BROKEN)
        );
        assertEquals(
                ArtronLogic.NOT_ENOUGH_KEY,
                TardisSummonLogic.overlayKey(TardisSummonLogic.Result.EMPTY_TANK)
        );
        TardisDataModel empty = TardisDataLoader.create();
        empty.artron = 0;
        assertEquals(
                ArtronLogic.ARTRON_EMPTY_KEY,
                TardisSummonLogic.overlayKey(TardisSummonLogic.Result.EMPTY_TANK, empty)
        );
    }

    @Test
    void preview_circuitBrokenWhenRemoteSummonBroken() {
        TardisDataModel model = TardisDataLoader.create();
        model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
        CircuitFittedLogic.setFitted(model, TardisCircuit.REMOTE_SUMMON, false);
        assertEquals(TardisSummonLogic.Result.CIRCUIT_BROKEN, TardisSummonLogic.preview(model));
    }

    @Test
    void summon_noTardisWhenPlayerOwnsNone() {
        // World-less path: findOwnedBy empty → NO_TARDIS. ServerLevel is required so skip full summon.
        UUID player = UUID.randomUUID();
        assertTrue(TardisDataLoader.findOwnedBy(player).isEmpty());
        assertEquals(TardisSummonLogic.Result.NO_TARDIS, TardisSummonLogic.preview(null));
    }

    @SuppressWarnings("unchecked")
    private static void clearCache() throws Exception {
        Field field = TardisDataLoader.class.getDeclaredField("tardisData");
        field.setAccessible(true);
        ((HashMap<?, ?>) field.get(null)).clear();
    }
}
