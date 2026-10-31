package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisExteriorLocation;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Client-facing snapshot of ship status for TARDIS monitors (compact / full scanner).
 * Holds ids and primitives only; the client turns them into text. Built server-side from
 * {@link TardisDataModel} plus an exterior {@link ExteriorEnvironmentReadout.Sample}.
 *
 * <p>{@code destinationDetail} depends on {@code destinationMode}: biome id (BIOME), waypoint name
 * (WAYPOINT), player name (PLAYER), {@code "x, y, z"} (FAST_RETURN), empty (TELEPATHIC).
 */
public record TardisMonitorReadout(
        boolean linked,
        String exteriorDimension,
        String exteriorBiome,
        int exteriorX,
        int exteriorY,
        int exteriorZ,
        TardisTravelPhase phase,
        float phaseProgress,
        DestinationMode destinationMode,
        String destinationDimension,
        String destinationDetail,
        boolean stabilisersEnabled,
        boolean lockX,
        boolean lockY,
        boolean lockZ,
        int artron,
        boolean doorsOpen,
        boolean doorsLocked,
        boolean cloaked,
        TardisChameleonVariant variant,
        int circuitsFitted,
        ExteriorEnvironmentReadout.Reading reading
) {
    public static final int CIRCUIT_COUNT = TardisCircuit.values().length;

    public TardisMonitorReadout {
        exteriorDimension = exteriorDimension == null ? "" : exteriorDimension;
        exteriorBiome = exteriorBiome == null ? "" : exteriorBiome;
        phase = phase == null ? TardisTravelPhase.IDLE : phase;
        phaseProgress = Mth.clamp(phaseProgress, 0.0F, 1.0F);
        destinationMode = destinationMode == null ? DestinationMode.BIOME : destinationMode;
        destinationDimension = destinationDimension == null ? "" : destinationDimension;
        destinationDetail = destinationDetail == null ? "" : destinationDetail;
        artron = Mth.clamp(artron, 0, ArtronLogic.CAPACITY);
        variant = variant == null ? TardisChameleonVariant.TT_CAPSULE : variant;
        circuitsFitted = Mth.clamp(circuitsFitted, 0, CIRCUIT_COUNT);
        reading = reading == null ? ExteriorEnvironmentReadout.Reading.none() : reading;
    }

    /** Unlinked monitor (outside an interior, or no TARDIS data): screen shows no signal. */
    public static TardisMonitorReadout none() {
        return new TardisMonitorReadout(
                false, "", "", 0, 0, 0,
                TardisTravelPhase.IDLE, 0.0F,
                DestinationMode.BIOME, "", "",
                true, false, false, false,
                0, false, false, false,
                TardisChameleonVariant.TT_CAPSULE, 0,
                ExteriorEnvironmentReadout.Reading.none()
        );
    }

    public boolean hasExterior() {
        return !exteriorDimension.isEmpty();
    }

    /**
     * Builds a readout from the TARDIS model and an exterior sample (in flight or no exterior →
     * {@link ExteriorEnvironmentReadout.Sample#none()}). {@code playerNames} resolves a player
     * locator target to a display name, or {@code null} when unknown.
     */
    public static TardisMonitorReadout from(
            @Nullable TardisDataModel model,
            @Nullable ExteriorEnvironmentReadout.Sample sample,
            Function<UUID, @Nullable String> playerNames
    ) {
        if (model == null) {
            return none();
        }
        ExteriorEnvironmentReadout.Sample safeSample =
                sample == null ? ExteriorEnvironmentReadout.Sample.none() : sample;
        TardisTravelPhase phase = model.getTravelPhase();
        boolean hasExterior = model.hasExteriorLocation && model.exteriorDimension != null;
        Identifier biomeId = safeSample.noSignal() ? null : safeSample.biomeId();
        Destination destination = destination(model, phase, playerNames);
        return new TardisMonitorReadout(
                true,
                hasExterior ? model.exteriorDimension : "",
                biomeId == null ? "" : biomeId.toString(),
                model.exteriorX,
                model.exteriorY,
                model.exteriorZ,
                phase,
                phaseProgress(phase, model.travelPhaseTicks),
                destination.mode(),
                destination.dimension(),
                destination.detail(),
                StabiliserLogic.isEnabled(model),
                model.lockX,
                model.lockY,
                model.lockZ,
                ArtronLogic.read(model),
                model.doorState != null && model.doorState.isOpen,
                model.doorsLocked,
                model.cloaked,
                model.variant,
                circuitsFitted(model),
                ExteriorEnvironmentReadout.fromSample(safeSample)
        );
    }

    /**
     * 0–1 progress through a timed travel phase ({@code ticksRemaining} counts down). Flight is
     * open-ended (full bar); idle is empty.
     */
    public static float phaseProgress(TardisTravelPhase phase, int ticksRemaining) {
        return switch (phase) {
            case DEMATERIALISING -> elapsedFraction(ticksRemaining, TardisTravelService.DEMATERIALISING_DURATION_TICKS);
            case MATERIALISING -> elapsedFraction(ticksRemaining, TardisTravelService.MATERIALISING_DURATION_TICKS);
            case IN_FLIGHT -> 1.0F;
            case IDLE -> 0.0F;
        };
    }

    private static float elapsedFraction(int ticksRemaining, int duration) {
        if (duration <= 0) {
            return 1.0F;
        }
        return Mth.clamp((duration - ticksRemaining) / (float) duration, 0.0F, 1.0F);
    }

    public static int circuitsFitted(@Nullable TardisDataModel model) {
        int fitted = 0;
        for (TardisCircuit circuit : TardisCircuit.values()) {
            if (CircuitFittedLogic.isFitted(model, circuit)) {
                fitted++;
            }
        }
        return fitted;
    }

    private record Destination(DestinationMode mode, String dimension, String detail) {
    }

    /**
     * While travelling, prefers the course snapshot taken at dematerialisation; otherwise shows the
     * currently selected destination.
     */
    private static Destination destination(
            TardisDataModel model,
            TardisTravelPhase phase,
            Function<UUID, @Nullable String> playerNames
    ) {
        boolean course = phase.isTraveling() && model.travelDestinationMode != null;
        DestinationMode mode = course ? model.travelDestinationMode : model.getDestinationMode();
        return switch (mode) {
            case BIOME -> course
                    ? new Destination(mode, model.travelDestinationDimension, model.travelDestinationBiome)
                    : new Destination(mode, TardisLogic.effectiveDestinationDimension(model), model.selectedBiome);
            case WAYPOINT -> {
                Optional<TardisWaypoint> waypoint = WaypointLogic.find(model, model.selectedWaypointId);
                yield waypoint
                        .map(w -> new Destination(mode, w.dimension, w.name))
                        .orElseGet(() -> new Destination(mode, "", ""));
            }
            case PLAYER -> {
                UUID target = course && model.travelTargetPlayerUuid != null
                        ? model.travelTargetPlayerUuid
                        : model.selectedPlayerUuid;
                String name = target == null ? null : playerNames.apply(target);
                yield new Destination(mode, "", name);
            }
            case FAST_RETURN -> {
                if (course) {
                    yield new Destination(mode, model.travelDestinationDimension, coords(
                            model.travelDestinationX, model.travelDestinationY, model.travelDestinationZ));
                }
                Optional<TardisExteriorLocation> selected = FastReturnLogic.selected(model);
                yield selected
                        .map(l -> new Destination(mode, l.dimension, coords(l.x, l.y, l.z)))
                        .orElseGet(() -> new Destination(mode, "", ""));
            }
            case TELEPATHIC -> new Destination(mode, "", "");
        };
    }

    private static String coords(int x, int y, int z) {
        return x + ", " + y + ", " + z;
    }

    public void write(ValueOutput output) {
        output.putBoolean("monitorLinked", linked);
        output.putString("monitorExteriorDimension", exteriorDimension);
        output.putString("monitorExteriorBiome", exteriorBiome);
        output.putInt("monitorExteriorX", exteriorX);
        output.putInt("monitorExteriorY", exteriorY);
        output.putInt("monitorExteriorZ", exteriorZ);
        output.putString("monitorPhase", phase.name());
        output.putFloat("monitorPhaseProgress", phaseProgress);
        output.putString("monitorDestinationMode", destinationMode.name());
        output.putString("monitorDestinationDimension", destinationDimension);
        output.putString("monitorDestinationDetail", destinationDetail);
        output.putBoolean("monitorStabilisers", stabilisersEnabled);
        output.putBoolean("monitorLockX", lockX);
        output.putBoolean("monitorLockY", lockY);
        output.putBoolean("monitorLockZ", lockZ);
        output.putInt("monitorArtron", artron);
        output.putBoolean("monitorDoorsOpen", doorsOpen);
        output.putBoolean("monitorDoorsLocked", doorsLocked);
        output.putBoolean("monitorCloaked", cloaked);
        output.putString("monitorVariant", variant.getId().toString());
        output.putInt("monitorCircuits", circuitsFitted);
        boolean noSignal = reading.noSignal();
        output.putBoolean("monitorNoSignal", noSignal);
        output.putFloat("monitorOxygen", noSignal ? 0.0F : reading.oxygen());
        output.putFloat("monitorPressure", noSignal ? 0.0F : reading.pressure());
        output.putFloat("monitorTemperature", noSignal ? 0.0F : reading.temperature());
        output.putFloat("monitorRadiation", noSignal ? 0.0F : reading.radiation());
    }

    public static TardisMonitorReadout read(ValueInput input) {
        if (!input.getBooleanOr("monitorLinked", false)) {
            return none();
        }
        boolean noSignal = input.getBooleanOr("monitorNoSignal", true);
        ExteriorEnvironmentReadout.Reading reading = noSignal
                ? ExteriorEnvironmentReadout.Reading.none()
                : new ExteriorEnvironmentReadout.Reading(
                        false,
                        input.getFloatOr("monitorOxygen", 0.0F),
                        input.getFloatOr("monitorPressure", 0.0F),
                        input.getFloatOr("monitorTemperature", 0.0F),
                        input.getFloatOr("monitorRadiation", 0.0F)
                );
        return new TardisMonitorReadout(
                true,
                input.getStringOr("monitorExteriorDimension", ""),
                input.getStringOr("monitorExteriorBiome", ""),
                input.getIntOr("monitorExteriorX", 0),
                input.getIntOr("monitorExteriorY", 0),
                input.getIntOr("monitorExteriorZ", 0),
                TardisTravelPhase.fromString(input.getStringOr("monitorPhase", "")),
                input.getFloatOr("monitorPhaseProgress", 0.0F),
                DestinationMode.fromString(input.getStringOr("monitorDestinationMode", "")),
                input.getStringOr("monitorDestinationDimension", ""),
                input.getStringOr("monitorDestinationDetail", ""),
                input.getBooleanOr("monitorStabilisers", true),
                input.getBooleanOr("monitorLockX", false),
                input.getBooleanOr("monitorLockY", false),
                input.getBooleanOr("monitorLockZ", false),
                input.getIntOr("monitorArtron", 0),
                input.getBooleanOr("monitorDoorsOpen", false),
                input.getBooleanOr("monitorDoorsLocked", false),
                input.getBooleanOr("monitorCloaked", false),
                parseVariant(input.getStringOr("monitorVariant", "")),
                input.getIntOr("monitorCircuits", 0),
                reading
        );
    }

    private static TardisChameleonVariant parseVariant(String id) {
        Identifier identifier = id == null || id.isBlank() ? null : Identifier.tryParse(id);
        if (identifier == null) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
        try {
            return TardisChameleonVariant.fromId(identifier);
        } catch (IllegalArgumentException ignored) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
    }
}
