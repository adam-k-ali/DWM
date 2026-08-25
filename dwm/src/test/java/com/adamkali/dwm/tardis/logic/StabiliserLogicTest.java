package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import net.minecraft.util.RandomSource;

import static org.junit.jupiter.api.Assertions.*;

class StabiliserLogicTest {
    private TardisDataModel model;

    @BeforeEach
    void setUp() {
        MinecraftTestBootstrap.ensure();
        model = new TardisDataModel();
        model.uuid = UUID.randomUUID();
    }

    @Test
    void isEnabled_defaultsTrueIncludingNullBoxedField() {
        assertTrue(StabiliserLogic.isEnabled(model));
        model.stabilisersEnabled = null;
        assertTrue(StabiliserLogic.isEnabled(model));
        assertTrue(StabiliserLogic.isEnabled(null));
    }

    @Test
    void toggle_flipsEnabledState() {
        assertTrue(StabiliserLogic.isEnabled(model));
        assertFalse(StabiliserLogic.toggle(model));
        assertFalse(StabiliserLogic.isEnabled(model));
        assertTrue(StabiliserLogic.toggle(model));
        assertTrue(StabiliserLogic.isEnabled(model));
    }

    @Test
    void applyScatter_returnsCenterWhenEnabled() {
        var center = new net.minecraft.core.BlockPos(10, 64, -3);
        assertEquals(
                center,
                StabiliserLogic.applyScatter(null, center, null, model, null).orElseThrow()
        );
    }

    @Test
    void sampleHorizontalOffset_staysInRadiusAndOutsideMinOffset() {
        RandomSource random = RandomSource.create(42L);
        for (int i = 0; i < 200; i++) {
            int[] offset = StabiliserLogic.sampleHorizontalOffset(random);
            int chebyshev = Math.max(Math.abs(offset[0]), Math.abs(offset[1]));
            assertTrue(chebyshev >= StabiliserLogic.MIN_OFFSET, "offset too close: " + chebyshev);
            assertTrue(chebyshev <= StabiliserLogic.SCATTER_RADIUS, "offset too far: " + chebyshev);
        }
    }
}
