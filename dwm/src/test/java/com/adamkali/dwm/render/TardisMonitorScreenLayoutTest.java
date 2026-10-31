package com.adamkali.dwm.render;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.ArtronLogic;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.tardis.logic.MonitorPage;
import com.adamkali.dwm.tardis.logic.TardisMonitorReadout;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TardisMonitorScreenLayoutTest {
    @BeforeEach
    void setUp() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void unlinked_showsNoSignalOnEveryPage() {
        TardisMonitorReadout none = TardisMonitorReadout.none();
        for (MonitorPage page : MonitorPage.values()) {
            assertEquals("dwm.monitor.no_signal", key(TardisMonitorScreenLayout.title(page, none)));
            List<TardisMonitorScreenLayout.Line> lines = TardisMonitorScreenLayout.lines(page, none);
            assertEquals(1, lines.size());
            assertEquals("dwm.monitor.no_signal_hint", key(lines.getFirst().text()));
        }
        assertEquals(TardisMonitorScreenLayout.ALERT_COLOR, TardisMonitorScreenLayout.titleColor(none));
    }

    @Test
    void everyPage_staysWithinScreenCapacity() {
        for (TardisTravelPhase phase : TardisTravelPhase.values()) {
            for (DestinationMode mode : DestinationMode.values()) {
                TardisMonitorReadout readout = readout(phase, mode, "x", reading());
                for (MonitorPage page : MonitorPage.values()) {
                    List<TardisMonitorScreenLayout.Line> lines = TardisMonitorScreenLayout.lines(page, readout);
                    assertFalse(lines.isEmpty(), page + " " + phase + " " + mode);
                    assertTrue(lines.size() <= TardisMonitorScreenLayout.MAX_LINES, page + " " + phase + " " + mode);
                }
            }
        }
    }

    @Test
    void location_landedShowsDimensionBiomeCoordsAndLanded() {
        List<TardisMonitorScreenLayout.Line> lines = TardisMonitorScreenLayout.lines(
                MonitorPage.LOCATION, readout(TardisTravelPhase.IDLE, DestinationMode.BIOME, "", reading()));
        assertEquals("dimension.minecraft.overworld", key(lines.get(0).text()));
        assertEquals("biome.minecraft.plains", key(lines.get(1).text()));
        assertEquals("dwm.monitor.coords", key(lines.get(2).text()));
        assertEquals("dwm.monitor.phase.idle", key(lines.get(3).text()));
        assertFalse(lines.get(3).hasBar());
    }

    @Test
    void location_inFlightShowsVortexAndTimedPhasesShowProgressBar() {
        List<TardisMonitorScreenLayout.Line> flight = TardisMonitorScreenLayout.lines(
                MonitorPage.LOCATION, readout(TardisTravelPhase.IN_FLIGHT, DestinationMode.BIOME, "", reading()));
        assertEquals("dwm.monitor.time_vortex", key(flight.getFirst().text()));

        TardisMonitorScreenLayout.Line demat = TardisMonitorScreenLayout.lines(
                MonitorPage.LOCATION,
                readout(TardisTravelPhase.DEMATERIALISING, DestinationMode.BIOME, "", reading())).getLast();
        assertEquals("dwm.monitor.phase.dematerialising", key(demat.text()));
        assertTrue(demat.hasBar());
        assertEquals(0.5F, demat.bar(), 1e-4F);
    }

    @Test
    void destination_targetLineDependsOnMode() {
        assertEquals("biome.minecraft.desert", key(target(DestinationMode.BIOME, "minecraft:desert")));
        assertEquals("Home", target(DestinationMode.WAYPOINT, "Home").getString());
        assertEquals("Susan", target(DestinationMode.PLAYER, "Susan").getString());
        assertEquals("1, 2, 3", target(DestinationMode.FAST_RETURN, "1, 2, 3").getString());
        assertEquals("dwm.monitor.telepathic_target", key(target(DestinationMode.TELEPATHIC, "")));
        assertEquals("dwm.monitor.target_unset", key(target(DestinationMode.PLAYER, "")));
    }

    @Test
    void systems_artronBarAndCircuitCount() {
        List<TardisMonitorScreenLayout.Line> lines = TardisMonitorScreenLayout.lines(
                MonitorPage.SYSTEMS, readout(TardisTravelPhase.IDLE, DestinationMode.BIOME, "", reading()));
        TardisMonitorScreenLayout.Line artron = lines.getFirst();
        assertTrue(artron.hasBar());
        assertEquals(ArtronLogic.needleFrom(250), artron.bar(), 1e-4F);
        assertEquals("dwm.monitor.circuits", key(lines.getLast().text()));
        assertEquals(TardisMonitorScreenLayout.WARN_COLOR, lines.getLast().color());
    }

    @Test
    void atmosphere_noSignalInFlightOtherwiseFourBars() {
        List<TardisMonitorScreenLayout.Line> none = TardisMonitorScreenLayout.lines(
                MonitorPage.ATMOSPHERE,
                readout(TardisTravelPhase.IN_FLIGHT, DestinationMode.BIOME, "", ExteriorEnvironmentReadout.Reading.none()));
        assertEquals(1, none.size());
        assertEquals("dwm.monitor.atmosphere_no_signal", key(none.getFirst().text()));

        List<TardisMonitorScreenLayout.Line> bars = TardisMonitorScreenLayout.lines(
                MonitorPage.ATMOSPHERE, readout(TardisTravelPhase.IDLE, DestinationMode.BIOME, "", reading()));
        assertEquals(4, bars.size());
        assertTrue(bars.stream().allMatch(TardisMonitorScreenLayout.Line::hasBar));
        // Radiation uses the handheld meter's colour bands.
        assertEquals(RadiationMeterSpecialRenderer.colorForPercent(12), bars.getLast().barColor());
    }

    @Test
    void wings_showArtronAndPhase() {
        TardisMonitorReadout readout = readout(TardisTravelPhase.MATERIALISING, DestinationMode.BIOME, "", reading());
        List<TardisMonitorScreenLayout.Line> left = TardisMonitorScreenLayout.leftWing(readout);
        assertEquals("dwm.monitor.wing.artron", key(left.getFirst().text()));
        assertEquals("50%", left.get(1).text().getString());
        List<TardisMonitorScreenLayout.Line> right = TardisMonitorScreenLayout.rightWing(readout);
        assertEquals("dwm.monitor.wing.phase.materialising", key(right.get(1).text()));
        assertEquals(0.5F, right.getLast().bar(), 1e-4F);
    }

    @Test
    void locks_summarisesPinnedAxes() {
        assertEquals("dwm.monitor.locks_none", key(TardisMonitorScreenLayout.locks(TardisMonitorReadout.none())));
    }

    @Test
    void artronColor_bands() {
        assertEquals(TardisMonitorScreenLayout.ALERT_COLOR, TardisMonitorScreenLayout.artronColor(5));
        assertEquals(TardisMonitorScreenLayout.WARN_COLOR, TardisMonitorScreenLayout.artronColor(20));
        assertEquals(TardisMonitorScreenLayout.GOOD_COLOR, TardisMonitorScreenLayout.artronColor(80));
    }

    private static Component target(DestinationMode mode, String detail) {
        List<TardisMonitorScreenLayout.Line> lines = TardisMonitorScreenLayout.lines(
                MonitorPage.DESTINATION, readout(TardisTravelPhase.IDLE, mode, detail, reading()));
        return lines.get(2).text();
    }

    private static ExteriorEnvironmentReadout.Reading reading() {
        return new ExteriorEnvironmentReadout.Reading(false, 1.0F, 0.5F, 0.52F, 0.12F);
    }

    private static TardisMonitorReadout readout(
            TardisTravelPhase phase,
            DestinationMode mode,
            String detail,
            ExteriorEnvironmentReadout.Reading reading
    ) {
        return new TardisMonitorReadout(
                true, "minecraft:overworld", "minecraft:plains", 10, 64, -20,
                phase, phase == TardisTravelPhase.IN_FLIGHT ? 1.0F : phase.isTraveling() ? 0.5F : 0.0F,
                mode, "minecraft:overworld", detail,
                true, false, false, false,
                250, false, false, false,
                TardisChameleonVariant.TT_CAPSULE, TardisMonitorReadout.CIRCUIT_COUNT - 1,
                reading);
    }

    private static String key(Component component) {
        if (component.getContents() instanceof TranslatableContents translatable) {
            return translatable.getKey();
        }
        return component.getString();
    }
}
