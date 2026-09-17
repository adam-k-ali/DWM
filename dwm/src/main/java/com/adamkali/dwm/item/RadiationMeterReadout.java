package com.adamkali.dwm.item;

import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.world.radiation.RadiationExposureLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Shared environmental reading used by the handheld radiation meter.
 */
public final class RadiationMeterReadout {
    private RadiationMeterReadout() {
    }

    public static int percent(Level level, BlockPos pos) {
        return percent(ExteriorEnvironmentReadout.sampleFacts(level, pos));
    }

    static int percent(ExteriorEnvironmentReadout.Sample sample) {
        ExteriorEnvironmentReadout.Reading reading = ExteriorEnvironmentReadout.fromSample(sample);
        if (reading.noSignal() || ExteriorEnvironmentReadout.isNoSignal(reading.radiation())) {
            return 0;
        }
        return RadiationExposureLogic.meterPercent(reading.radiation());
    }
}
