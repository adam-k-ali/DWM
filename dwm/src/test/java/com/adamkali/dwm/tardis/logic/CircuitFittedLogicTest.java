package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
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

class CircuitFittedLogicTest {
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
    }

    @AfterEach
    void tearDown() throws Exception {
        clearCache();
        TardisDataLoader.tardisSaveDirectory = null;
    }

    @Test
    void create_isFullyFittedWithStabilisersOn() {
        TardisDataModel model = TardisDataLoader.create();
        for (TardisCircuit circuit : TardisCircuit.values()) {
            assertTrue(CircuitFittedLogic.isFitted(model, circuit), circuit.name());
        }
        assertTrue(StabiliserLogic.isEnabled(model));
    }

    @Test
    void createFoundUnfinished_breaksListedCircuitsAndDisablesStabilisers() {
        TardisDataModel model = TardisDataLoader.createFoundUnfinished();
        for (TardisCircuit circuit : TardisCircuit.values()) {
            assertFalse(CircuitFittedLogic.isFitted(model, circuit), circuit.name());
            assertTrue(CircuitFittedLogic.isBroken(model, circuit), circuit.name());
        }
        assertFalse(StabiliserLogic.isEnabled(model));
        assertEquals(DestinationMode.BIOME, model.getDestinationMode());
    }

    @Test
    void isFitted_nullFlagMeansWorking() {
        TardisDataModel model = new TardisDataModel();
        model.planetLocatorFitted = null;
        assertTrue(CircuitFittedLogic.isFitted(model, TardisCircuit.PLANET_LOCATOR));
    }

    @Test
    void setFitted_repairsBrokenCircuit() {
        TardisDataModel model = TardisDataLoader.createFoundUnfinished();
        CircuitFittedLogic.setFitted(model, TardisCircuit.WAYPOINTS, true);
        assertTrue(CircuitFittedLogic.isFitted(model, TardisCircuit.WAYPOINTS));
        assertFalse(CircuitFittedLogic.isFitted(model, TardisCircuit.PLANET_LOCATOR));
    }

    @Test
    void circuitFor_mapsConsoleTargets() {
        assertEquals(
                TardisCircuit.PLANET_LOCATOR,
                CircuitFittedLogic.circuitFor(LookTarget.PLANET_LOCATOR).orElseThrow()
        );
        assertEquals(
                TardisCircuit.COORDINATE_LOCKS,
                CircuitFittedLogic.circuitFor(LookTarget.COORDINATE_LOCK_Y).orElseThrow()
        );
        assertTrue(CircuitFittedLogic.circuitFor(LookTarget.BIOME_SELECTOR).isEmpty());
        assertTrue(CircuitFittedLogic.circuitFor(LookTarget.MATERIALISATION_LEVER).isEmpty());
        assertTrue(CircuitFittedLogic.circuitFor(LookTarget.DOOR_LOCK).isEmpty());
    }

    @Test
    void smokeOriginForControl_isControlBoxCenter() {
        BlockPos pos = new BlockPos(10, 64, -3);
        Vec3 origin = CircuitFittedLogic.smokeOriginForControl(
                LookTarget.PLANET_LOCATOR,
                pos,
                Direction.NORTH
        );
        assertEquals(
                com.adamkali.dwm.block.FirstDoctorConsoleControls
                        .worldBoxForTarget(LookTarget.PLANET_LOCATOR, pos, Direction.NORTH)
                        .getCenter(),
                origin
        );
    }

    @Test
    void claimDoesNotUpgradeFoundUnfinished() {
        TardisDataModel model = TardisDataLoader.createFoundUnfinished();
        UUID player = UUID.randomUUID();
        assertTrue(TardisOwnershipLogic.tryClaimOnEnter(model.uuid, player));
        assertEquals(player, model.ownerUuid);
        assertFalse(CircuitFittedLogic.isFitted(model, TardisCircuit.PLANET_LOCATOR));
        assertFalse(StabiliserLogic.isEnabled(model));
    }

    @Test
    void hasValidDestination_blocksBrokenModes() {
        TardisDataModel model = TardisDataLoader.createFoundUnfinished();
        model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
        model.selectedBiome = "minecraft:plains";
        model.setDestinationMode(DestinationMode.BIOME);
        assertTrue(TardisTravelService.hasValidDestinationSelection(model));

        model.setDestinationMode(DestinationMode.WAYPOINT);
        model.selectedWaypointId = UUID.randomUUID();
        model.getWaypoints().add(new TardisWaypoint(
                model.selectedWaypointId, "Pad", "minecraft:overworld", 1, 64, 1, 0
        ));
        assertFalse(TardisTravelService.hasValidDestinationSelection(model));
    }

    @Test
    void effectiveDestination_ignoresSelectedDimensionWhenPlanetLocatorBroken() {
        TardisDataModel model = TardisDataLoader.createFoundUnfinished();
        model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
        model.selectedDimension = "minecraft:the_nether";
        assertEquals("minecraft:overworld", TardisLogic.effectiveDestinationDimension(model));
    }

    @Test
    void summonPreview_circuitBrokenWhenRemoteBroken() {
        TardisDataModel model = TardisDataLoader.createFoundUnfinished();
        model.setExteriorLocation("minecraft:overworld", 0, 64, 0, 0);
        assertEquals(TardisSummonLogic.Result.CIRCUIT_BROKEN, TardisSummonLogic.preview(model));
        assertEquals(
                CircuitFittedLogic.CIRCUIT_BROKEN_KEY,
                TardisSummonLogic.overlayKey(TardisSummonLogic.Result.CIRCUIT_BROKEN)
        );
    }

    @SuppressWarnings("unchecked")
    private static void clearCache() throws Exception {
        Field field = TardisDataLoader.class.getDeclaredField("tardisData");
        field.setAccessible(true);
        ((HashMap<?, ?>) field.get(null)).clear();
    }
}
