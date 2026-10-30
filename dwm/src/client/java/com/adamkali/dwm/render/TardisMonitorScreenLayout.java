package com.adamkali.dwm.render;

import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.ArtronLogic;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.tardis.logic.MonitorPage;
import com.adamkali.dwm.tardis.logic.TardisMonitorReadout;
import com.adamkali.dwm.text.BiomeNames;
import com.adamkali.dwm.text.DimensionNames;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pure text layout for TARDIS monitor screens: turns a synced {@link TardisMonitorReadout} into
 * coloured lines (optionally with a 0–1 bar). The renderer only measures and draws.
 */
public final class TardisMonitorScreenLayout {
    /** Most content lines a page may produce (title and page dots are extra). */
    public static final int MAX_LINES = 5;

    static final int TITLE_COLOR = 0xFFF2B84B;
    static final int TEXT_COLOR = 0xFFCFF6F2;
    static final int GOOD_COLOR = 0xFF7CFC9A;
    static final int WARN_COLOR = 0xFFF2B84B;
    static final int ALERT_COLOR = 0xFFFF6B5B;
    static final int DIM_COLOR = 0xFF6F8F8C;

    private TardisMonitorScreenLayout() {
    }

    /**
     * One screen row. {@code bar < 0} means text only; otherwise a 0–1 bar is drawn after the label.
     */
    public record Line(Component text, int color, float bar, int barColor) {
        public static final float NO_BAR = -1.0F;

        static Line text(Component text, int color) {
            return new Line(text, color, NO_BAR, 0);
        }

        static Line bar(Component label, float fraction, int barColor) {
            return new Line(label, TEXT_COLOR, Math.max(0.0F, Math.min(1.0F, fraction)), barColor);
        }

        public boolean hasBar() {
            return bar >= 0.0F;
        }
    }

    public static Component title(MonitorPage page, TardisMonitorReadout readout) {
        if (!readout.linked()) {
            return Component.translatable("dwm.monitor.no_signal");
        }
        return Component.translatable(page.titleKey());
    }

    public static int titleColor(TardisMonitorReadout readout) {
        return readout.linked() ? TITLE_COLOR : ALERT_COLOR;
    }

    public static List<Line> lines(MonitorPage page, TardisMonitorReadout readout) {
        if (!readout.linked()) {
            return List.of(Line.text(Component.translatable("dwm.monitor.no_signal_hint"), DIM_COLOR));
        }
        return switch (page) {
            case LOCATION -> location(readout);
            case DESTINATION -> destination(readout);
            case SYSTEMS -> systems(readout);
            case ATMOSPHERE -> atmosphere(readout);
        };
    }

    private static List<Line> location(TardisMonitorReadout readout) {
        List<Line> lines = new ArrayList<>();
        if (readout.phase() == TardisTravelPhase.IN_FLIGHT) {
            lines.add(Line.text(Component.translatable("dwm.monitor.time_vortex"), TEXT_COLOR));
        } else if (!readout.hasExterior()) {
            lines.add(Line.text(Component.translatable("dwm.monitor.no_exterior"), DIM_COLOR));
        } else {
            lines.add(Line.text(DimensionNames.of(readout.exteriorDimension()), TEXT_COLOR));
            if (!readout.exteriorBiome().isEmpty()) {
                lines.add(Line.text(BiomeNames.of(readout.exteriorBiome()), TEXT_COLOR));
            }
            lines.add(Line.text(Component.translatable(
                    "dwm.monitor.coords", readout.exteriorX(), readout.exteriorY(), readout.exteriorZ()), DIM_COLOR));
        }
        lines.add(phaseLine(readout));
        return lines;
    }

    private static Line phaseLine(TardisMonitorReadout readout) {
        TardisTravelPhase phase = readout.phase();
        Component label = phaseLabel(phase);
        return switch (phase) {
            case IDLE -> Line.text(label, GOOD_COLOR);
            case IN_FLIGHT -> Line.text(label, WARN_COLOR);
            case DEMATERIALISING, MATERIALISING -> Line.bar(label, readout.phaseProgress(), WARN_COLOR);
        };
    }

    static Component phaseLabel(TardisTravelPhase phase) {
        return Component.translatable("dwm.monitor.phase." + phase.name().toLowerCase(Locale.ROOT));
    }

    private static List<Line> destination(TardisMonitorReadout readout) {
        List<Line> lines = new ArrayList<>();
        Component mode = Component.translatable(
                "dwm.monitor.mode." + readout.destinationMode().name().toLowerCase(Locale.ROOT));
        lines.add(Line.text(Component.translatable("dwm.monitor.mode", mode), TEXT_COLOR));
        if (!readout.destinationDimension().isEmpty()) {
            lines.add(Line.text(DimensionNames.of(readout.destinationDimension()), TEXT_COLOR));
        }
        lines.add(destinationTarget(readout));
        lines.add(Line.text(Component.translatable(
                        "dwm.monitor.stabilisers", onOff(readout.stabilisersEnabled())),
                readout.stabilisersEnabled() ? GOOD_COLOR : WARN_COLOR));
        lines.add(Line.text(Component.translatable("dwm.monitor.locks", locks(readout)), TEXT_COLOR));
        return lines;
    }

    private static Line destinationTarget(TardisMonitorReadout readout) {
        String detail = readout.destinationDetail();
        return switch (readout.destinationMode()) {
            case TELEPATHIC -> Line.text(Component.translatable("dwm.monitor.telepathic_target"), TEXT_COLOR);
            case BIOME -> detail.isEmpty()
                    ? unset()
                    : Line.text(BiomeNames.of(detail), TEXT_COLOR);
            case WAYPOINT, PLAYER, FAST_RETURN -> detail.isEmpty()
                    ? unset()
                    : Line.text(Component.literal(detail), TEXT_COLOR);
        };
    }

    private static Line unset() {
        return Line.text(Component.translatable("dwm.monitor.target_unset"), DIM_COLOR);
    }

    private static Component onOff(boolean on) {
        return Component.translatable(on ? "dwm.monitor.on" : "dwm.monitor.off");
    }

    /** {@code "X Y -"} style summary of pinned axes, or "None". */
    static Component locks(TardisMonitorReadout readout) {
        if (!readout.lockX() && !readout.lockY() && !readout.lockZ()) {
            return Component.translatable("dwm.monitor.locks_none");
        }
        return Component.literal((readout.lockX() ? "X" : "-")
                + " " + (readout.lockY() ? "Y" : "-")
                + " " + (readout.lockZ() ? "Z" : "-"));
    }

    private static List<Line> systems(TardisMonitorReadout readout) {
        List<Line> lines = new ArrayList<>();
        lines.add(artronLine(readout));
        String doorsKey = readout.doorsLocked()
                ? "dwm.monitor.doors.locked"
                : readout.doorsOpen() ? "dwm.monitor.doors.open" : "dwm.monitor.doors.closed";
        lines.add(Line.text(Component.translatable(doorsKey), readout.doorsOpen() ? WARN_COLOR : TEXT_COLOR));
        lines.add(Line.text(Component.translatable(
                readout.cloaked() ? "dwm.monitor.cloak.engaged" : "dwm.monitor.cloak.off"), TEXT_COLOR));
        lines.add(Line.text(Component.translatable(
                "dwm.monitor.shell", Component.translatable(readout.variant().getId().toLanguageKey())), TEXT_COLOR));
        boolean allFitted = readout.circuitsFitted() >= TardisMonitorReadout.CIRCUIT_COUNT;
        lines.add(Line.text(Component.translatable(
                        "dwm.monitor.circuits", readout.circuitsFitted(), TardisMonitorReadout.CIRCUIT_COUNT),
                allFitted ? GOOD_COLOR : WARN_COLOR));
        return lines;
    }

    private static Line artronLine(TardisMonitorReadout readout) {
        int percent = ArtronLogic.percent(readout.artron());
        return Line.bar(Component.translatable("dwm.monitor.artron", percent),
                ArtronLogic.needleFrom(readout.artron()), artronColor(percent));
    }

    static int artronColor(int percent) {
        if (percent <= 10) {
            return ALERT_COLOR;
        }
        return percent < 35 ? WARN_COLOR : GOOD_COLOR;
    }

    private static List<Line> atmosphere(TardisMonitorReadout readout) {
        ExteriorEnvironmentReadout.Reading reading = readout.reading();
        if (reading.noSignal()) {
            return List.of(Line.text(Component.translatable("dwm.monitor.atmosphere_no_signal"), DIM_COLOR));
        }
        float radiation = reading.needle(reading.radiation());
        return List.of(
                Line.bar(Component.translatable("dwm.monitor.oxygen"), reading.needle(reading.oxygen()),
                        reading.needle(reading.oxygen()) < 0.5F ? ALERT_COLOR : GOOD_COLOR),
                Line.bar(Component.translatable("dwm.monitor.pressure"), reading.needle(reading.pressure()), TEXT_COLOR),
                Line.bar(Component.translatable("dwm.monitor.temperature"), reading.needle(reading.temperature()), WARN_COLOR),
                Line.bar(Component.translatable("dwm.monitor.radiation"), radiation,
                        RadiationMeterSpecialRenderer.colorForPercent(Math.round(radiation * 100.0F)))
        );
    }

    /** Left wing of the full scanner (viewer's left): artron tank. */
    public static List<Line> leftWing(TardisMonitorReadout readout) {
        if (!readout.linked()) {
            return List.of(Line.text(Component.literal("--"), DIM_COLOR));
        }
        int percent = ArtronLogic.percent(readout.artron());
        return List.of(
                Line.text(Component.translatable("dwm.monitor.wing.artron"), TITLE_COLOR),
                Line.text(Component.literal(percent + "%"), artronColor(percent)),
                Line.bar(Component.empty(), ArtronLogic.needleFrom(readout.artron()), artronColor(percent))
        );
    }

    /** Right wing of the full scanner (viewer's right): travel phase. */
    public static List<Line> rightWing(TardisMonitorReadout readout) {
        if (!readout.linked()) {
            return List.of(Line.text(Component.literal("--"), DIM_COLOR));
        }
        TardisTravelPhase phase = readout.phase();
        int color = phase == TardisTravelPhase.IDLE ? GOOD_COLOR : WARN_COLOR;
        return List.of(
                Line.text(Component.translatable("dwm.monitor.wing.status"), TITLE_COLOR),
                Line.text(Component.translatable(
                        "dwm.monitor.wing.phase." + phase.name().toLowerCase(Locale.ROOT)), color),
                Line.bar(Component.empty(), readout.phaseProgress(), color)
        );
    }
}
