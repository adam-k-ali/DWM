package com.adamkali.dwm.item;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout.DimensionKind;
import com.adamkali.dwm.world.DWMBiomeKeys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RadiationMeterReadoutTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void percent_usesSharedEnvironmentalRadiation() {
        assertEquals(15, RadiationMeterReadout.percent(sample(
                DimensionKind.SKARO,
                false,
                DWMBiomeKeys.SKARO_THAL_PLATEAU.identifier())));
        assertEquals(90, RadiationMeterReadout.percent(sample(DimensionKind.NETHER, false, null)));
        assertEquals(35, RadiationMeterReadout.percent(sample(DimensionKind.OVERWORLD, true, null)));
        assertEquals(0, RadiationMeterReadout.percent(ExteriorEnvironmentReadout.Sample.none()));
    }

    @Test
    void updateReading_clampsAndSkipsUnchangedComponents() {
        assertEquals(0, RadiationMeterItem.clampReading(-5));
        assertEquals(100, RadiationMeterItem.clampReading(120));
        assertTrue(RadiationMeterItem.shouldUpdate(null, 40));
        assertFalse(RadiationMeterItem.shouldUpdate(40, 40));
        assertTrue(RadiationMeterItem.shouldUpdate(40, 41));
    }

    private static ExteriorEnvironmentReadout.Sample sample(
            DimensionKind dimension,
            boolean thundering,
            net.minecraft.resources.Identifier biomeId
    ) {
        return new ExteriorEnvironmentReadout.Sample(
                false,
                dimension,
                64,
                63,
                false,
                true,
                0.8F,
                thundering,
                biomeId);
    }
}
