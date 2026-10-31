package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisExteriorLocation;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TardisMonitorReadoutTest {
    private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000042");

    @BeforeEach
    void setUp() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void from_nullModel_isUnlinked() {
        TardisMonitorReadout readout = TardisMonitorReadout.from(null, null, uuid -> null);
        assertFalse(readout.linked());
        assertEquals(TardisMonitorReadout.none(), readout);
    }

    @Test
    void from_landedShip_copiesLocationSystemsAndReading() {
        TardisDataModel model = landedModel();
        model.cloaked = true;
        model.doorsLocked = true;
        model.doorState.isOpen = false;
        model.variant = TardisChameleonVariant.FIRST_DOCTOR_BOX;
        model.artron = 125;
        model.lockY = true;
        CircuitFittedLogic.setFitted(model, TardisCircuit.CLOAK, false);
        CircuitFittedLogic.setFitted(model, TardisCircuit.TELEPATHIC, false);

        TardisMonitorReadout readout = TardisMonitorReadout.from(model, plainsSample(), uuid -> null);

        assertTrue(readout.linked());
        assertTrue(readout.hasExterior());
        assertEquals("minecraft:overworld", readout.exteriorDimension());
        assertEquals("minecraft:plains", readout.exteriorBiome());
        assertEquals(10, readout.exteriorX());
        assertEquals(64, readout.exteriorY());
        assertEquals(-20, readout.exteriorZ());
        assertEquals(TardisTravelPhase.IDLE, readout.phase());
        assertEquals(0.0F, readout.phaseProgress());
        assertTrue(readout.cloaked());
        assertTrue(readout.doorsLocked());
        assertFalse(readout.doorsOpen());
        assertEquals(TardisChameleonVariant.FIRST_DOCTOR_BOX, readout.variant());
        assertEquals(125, readout.artron());
        assertTrue(readout.lockY());
        assertFalse(readout.lockX());
        assertEquals(TardisMonitorReadout.CIRCUIT_COUNT - 2, readout.circuitsFitted());
        assertFalse(readout.reading().noSignal());
        assertEquals(1.0F, readout.reading().oxygen());
    }

    @Test
    void from_biomeMode_showsSelectedBiomeInEffectiveDimension() {
        TardisDataModel model = landedModel();
        model.selectedDimension = "dwm:gallifrey";
        model.selectedBiome = "dwm:gallifrey_plains";

        TardisMonitorReadout readout = TardisMonitorReadout.from(model, plainsSample(), uuid -> null);

        assertEquals(DestinationMode.BIOME, readout.destinationMode());
        assertEquals("dwm:gallifrey", readout.destinationDimension());
        assertEquals("dwm:gallifrey_plains", readout.destinationDetail());
    }

    @Test
    void from_waypointMode_showsWaypointName() {
        TardisDataModel model = landedModel();
        TardisWaypoint home = new TardisWaypoint(UUID.randomUUID(), "Home", "minecraft:the_nether", 1, 2, 3, 0);
        model.getWaypoints().add(home);
        model.selectedWaypointId = home.id;
        model.setDestinationMode(DestinationMode.WAYPOINT);

        TardisMonitorReadout readout = TardisMonitorReadout.from(model, plainsSample(), uuid -> null);

        assertEquals(DestinationMode.WAYPOINT, readout.destinationMode());
        assertEquals("minecraft:the_nether", readout.destinationDimension());
        assertEquals("Home", readout.destinationDetail());
    }

    @Test
    void from_playerMode_resolvesNameOrLeavesBlankWhenOffline() {
        TardisDataModel model = landedModel();
        model.selectedPlayerUuid = PLAYER;
        model.setDestinationMode(DestinationMode.PLAYER);

        assertEquals("Susan", TardisMonitorReadout.from(model, plainsSample(),
                uuid -> uuid.equals(PLAYER) ? "Susan" : null).destinationDetail());
        assertEquals("", TardisMonitorReadout.from(model, plainsSample(), uuid -> null).destinationDetail());
    }

    @Test
    void from_fastReturnMode_showsSelectedHistoryCoordinates() {
        TardisDataModel model = landedModel();
        model.getLocationHistory().add(new TardisExteriorLocation("minecraft:overworld", 100, 70, -5, 0));
        model.selectedFastReturnIndex = 0;
        model.setDestinationMode(DestinationMode.FAST_RETURN);

        TardisMonitorReadout readout = TardisMonitorReadout.from(model, plainsSample(), uuid -> null);

        assertEquals("minecraft:overworld", readout.destinationDimension());
        assertEquals("100, 70, -5", readout.destinationDetail());
    }

    @Test
    void from_inFlight_prefersCourseSnapshotAndHasNoSignal() {
        TardisDataModel model = landedModel();
        model.selectedBiome = "minecraft:desert";
        model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);
        model.travelDestinationMode = DestinationMode.BIOME;
        model.travelDestinationDimension = "minecraft:the_end";
        model.travelDestinationBiome = "minecraft:end_highlands";

        TardisMonitorReadout readout = TardisMonitorReadout.from(
                model, ExteriorEnvironmentReadout.Sample.none(), uuid -> null);

        assertEquals(TardisTravelPhase.IN_FLIGHT, readout.phase());
        assertEquals(1.0F, readout.phaseProgress());
        assertEquals("minecraft:the_end", readout.destinationDimension());
        assertEquals("minecraft:end_highlands", readout.destinationDetail());
        assertTrue(readout.reading().noSignal());
        assertEquals("", readout.exteriorBiome());
    }

    @Test
    void phaseProgress_countsUpAsTicksRemainingCountDown() {
        int demat = TardisTravelService.DEMATERIALISING_DURATION_TICKS;
        assertEquals(0.0F, TardisMonitorReadout.phaseProgress(TardisTravelPhase.DEMATERIALISING, demat));
        assertEquals(0.5F, TardisMonitorReadout.phaseProgress(TardisTravelPhase.DEMATERIALISING, demat / 2), 1e-4F);
        assertEquals(1.0F, TardisMonitorReadout.phaseProgress(TardisTravelPhase.DEMATERIALISING, 0));
        int mat = TardisTravelService.MATERIALISING_DURATION_TICKS;
        assertEquals(0.25F, TardisMonitorReadout.phaseProgress(TardisTravelPhase.MATERIALISING, mat * 3 / 4), 1e-4F);
        assertEquals(0.0F, TardisMonitorReadout.phaseProgress(TardisTravelPhase.IDLE, 50));
    }

    @Test
    void writeRead_roundTrips() {
        TardisDataModel model = landedModel();
        model.doorState.isOpen = true;
        model.lockX = true;
        model.lockZ = true;
        model.stabilisersEnabled = Boolean.FALSE;
        model.selectedBiome = "minecraft:desert";
        TardisMonitorReadout original = TardisMonitorReadout.from(model, plainsSample(), uuid -> null);

        assertEquals(original, roundTrip(original));
        assertEquals(TardisMonitorReadout.none(), roundTrip(TardisMonitorReadout.none()));
    }

    @Test
    void equality_ignoresNothingThatChangesTheScreen() {
        TardisDataModel model = landedModel();
        TardisMonitorReadout before = TardisMonitorReadout.from(model, plainsSample(), uuid -> null);
        model.artron = 400;
        TardisMonitorReadout after = TardisMonitorReadout.from(model, plainsSample(), uuid -> null);
        assertNotEquals(before, after);
        assertEquals(after, TardisMonitorReadout.from(model, plainsSample(), uuid -> null));
    }

    private static TardisMonitorReadout roundTrip(TardisMonitorReadout readout) {
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        readout.write(output);
        CompoundTag tag = output.buildResult();
        return TardisMonitorReadout.read(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, tag));
    }

    private static TardisDataModel landedModel() {
        TardisDataModel model = new TardisDataModel();
        model.setExteriorLocation("minecraft:overworld", 10, 64, -20, 0);
        return model;
    }

    private static ExteriorEnvironmentReadout.Sample plainsSample() {
        return new ExteriorEnvironmentReadout.Sample(
                false,
                ExteriorEnvironmentReadout.DimensionKind.OVERWORLD,
                64,
                63,
                false,
                true,
                0.8F,
                false,
                Identifier.withDefaultNamespace("plains"));
    }
}
