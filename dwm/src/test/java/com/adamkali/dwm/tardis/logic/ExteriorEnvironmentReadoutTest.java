package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout.DimensionKind;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout.Reading;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout.Sample;
import com.adamkali.dwm.world.DWMBiomeKeys;
import com.adamkali.dwm.world.radiation.RadiationExposureLogic;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExteriorEnvironmentReadoutTest {
    @Test
    void noSignal_propagatesThroughFromSampleAndNeedle() {
        Reading reading = ExteriorEnvironmentReadout.fromSample(Sample.none());
        assertTrue(reading.noSignal());
        assertTrue(ExteriorEnvironmentReadout.isNoSignal(reading.oxygen()));
        assertEquals(0.0F, reading.needle(reading.oxygen()), 0.001F);
        assertTrue(ExteriorEnvironmentReadout.fromSample(null).noSignal());
        assertTrue(ExteriorEnvironmentReadout.sample(null, null, true).noSignal());
    }

    @Test
    void oxygen_isZeroWhenWaterloggedOrNoAir() {
        Sample waterlogged = overworld(false, true, true, 0.8F, false);
        Sample sealed = overworld(false, false, false, 0.8F, false);
        assertEquals(0.0F, ExteriorEnvironmentReadout.oxygen(waterlogged));
        assertEquals(0.0F, ExteriorEnvironmentReadout.oxygen(sealed));
    }

    @Test
    void oxygen_isReducedInNetherAndEnd() {
        assertEquals(1.0F, ExteriorEnvironmentReadout.oxygen(overworld(false, false, true, 0.8F, false)));
        assertEquals(0.35F, ExteriorEnvironmentReadout.oxygen(nether(false, false, true, 2.0F, false)));
        assertEquals(0.45F, ExteriorEnvironmentReadout.oxygen(end(false, false, true, 0.5F, false)));
    }

    @Test
    void pressure_scalesWithYRelativeToSeaLevel() {
        float seaLevel = ExteriorEnvironmentReadout.pressure(overworldAtY(63));
        float high = ExteriorEnvironmentReadout.pressure(overworldAtY(127));
        float low = ExteriorEnvironmentReadout.pressure(overworldAtY(0));
        assertTrue(high < seaLevel);
        assertTrue(low > seaLevel);
        assertTrue(ExteriorEnvironmentReadout.pressure(nether(false, false, true, 2.0F, false)) > 0.5F);
        assertTrue(ExteriorEnvironmentReadout.pressure(end(false, false, true, 0.5F, false)) < 0.5F);
    }

    @Test
    void temperature_mapsBiomeRangeIntoUnitInterval() {
        assertEquals(0.0F, ExteriorEnvironmentReadout.temperature(overworld(false, false, true, -0.5F, false)));
        assertEquals(1.0F, ExteriorEnvironmentReadout.temperature(overworld(false, false, true, 2.0F, false)));
        float mid = ExteriorEnvironmentReadout.temperature(overworld(false, false, true, 0.75F, false));
        assertTrue(mid > 0.4F && mid < 0.6F);
    }

    @Test
    void radiation_isHighInNetherMediumInEndAndBumpsOnThunder() {
        assertEquals(0.9F, ExteriorEnvironmentReadout.radiation(nether(false, false, true, 2.0F, false)));
        assertEquals(0.55F, ExteriorEnvironmentReadout.radiation(end(false, false, true, 0.5F, false)));
        assertEquals(0.12F, ExteriorEnvironmentReadout.radiation(overworld(false, false, true, 0.8F, false)));
        assertEquals(0.35F, ExteriorEnvironmentReadout.radiation(overworld(false, false, true, 0.8F, true)));
    }

    @Test
    void radiation_usesSkaroBiomeTable() {
        assertEquals(
                RadiationExposureLogic.THAL_PLATEAU,
                ExteriorEnvironmentReadout.radiation(skaro(DWMBiomeKeys.SKARO_THAL_PLATEAU.identifier()))
        );
        assertEquals(
                RadiationExposureLogic.IRRADIATED_WASTES,
                ExteriorEnvironmentReadout.radiation(skaro(DWMBiomeKeys.SKARO_IRRADIATED_WASTES.identifier()))
        );
        assertEquals(
                RadiationExposureLogic.DRAMMANKIN_MIRE,
                ExteriorEnvironmentReadout.radiation(skaro(DWMBiomeKeys.SKARO_DRAMMANKIN_MIRE.identifier()))
        );
    }

    @Test
    void kindOf_parsesDimensionIds() {
        assertEquals(DimensionKind.OVERWORLD, ExteriorEnvironmentReadout.kindOf("minecraft:overworld"));
        assertEquals(DimensionKind.NETHER, ExteriorEnvironmentReadout.kindOf("minecraft:the_nether"));
        assertEquals(DimensionKind.END, ExteriorEnvironmentReadout.kindOf("minecraft:the_end"));
        assertEquals(DimensionKind.SKARO, ExteriorEnvironmentReadout.kindOf("dwm:skaro"));
        assertEquals(DimensionKind.OTHER, ExteriorEnvironmentReadout.kindOf("dwm:gallifrey"));
        assertEquals(DimensionKind.OTHER, ExteriorEnvironmentReadout.kindOf((String) null));
    }

    private static Sample overworldAtY(int y) {
        return new Sample(false, DimensionKind.OVERWORLD, y, 63, false, true, 0.8F, false, null);
    }

    private static Sample overworld(
            boolean noSignal,
            boolean waterlogged,
            boolean hasAir,
            float biomeTemperature,
            boolean thundering
    ) {
        return new Sample(noSignal, DimensionKind.OVERWORLD, 64, 63, waterlogged, hasAir, biomeTemperature, thundering, null);
    }

    private static Sample nether(
            boolean noSignal,
            boolean waterlogged,
            boolean hasAir,
            float biomeTemperature,
            boolean thundering
    ) {
        return new Sample(noSignal, DimensionKind.NETHER, 64, 32, waterlogged, hasAir, biomeTemperature, thundering, null);
    }

    private static Sample end(
            boolean noSignal,
            boolean waterlogged,
            boolean hasAir,
            float biomeTemperature,
            boolean thundering
    ) {
        return new Sample(noSignal, DimensionKind.END, 64, 63, waterlogged, hasAir, biomeTemperature, thundering, null);
    }

    private static Sample skaro(Identifier biomeId) {
        return new Sample(false, DimensionKind.SKARO, 64, 63, false, true, 0.8F, false, biomeId);
    }
}
