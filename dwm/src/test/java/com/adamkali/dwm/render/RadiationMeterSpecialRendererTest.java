package com.adamkali.dwm.render;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3fc;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RadiationMeterSpecialRendererTest {
    @Test
    void unbakedCodec_isIdentityType() {
        RadiationMeterSpecialRenderer.Unbaked unbaked = new RadiationMeterSpecialRenderer.Unbaked();
        assertSame(RadiationMeterSpecialRenderer.Unbaked.MAP_CODEC, unbaked.type());
    }

    @Test
    void screenText_formatsUnknownAndClampedPercentages() {
        assertEquals("--%", RadiationMeterSpecialRenderer.screenText(-1));
        assertEquals("0%", RadiationMeterSpecialRenderer.screenText(0));
        assertEquals("95%", RadiationMeterSpecialRenderer.screenText(95));
        assertEquals("100%", RadiationMeterSpecialRenderer.screenText(100));
        assertEquals("100%", RadiationMeterSpecialRenderer.screenText(140));
    }

    @Test
    void severityColours_coverFourBands() {
        assertEquals(RadiationMeterSpecialRenderer.UNKNOWN_COLOR, RadiationMeterSpecialRenderer.colorForPercent(-1));
        assertEquals(RadiationMeterSpecialRenderer.SAFE_COLOR, RadiationMeterSpecialRenderer.colorForPercent(24));
        assertEquals(RadiationMeterSpecialRenderer.CAUTION_COLOR, RadiationMeterSpecialRenderer.colorForPercent(25));
        assertEquals(RadiationMeterSpecialRenderer.WARNING_COLOR, RadiationMeterSpecialRenderer.colorForPercent(50));
        assertEquals(RadiationMeterSpecialRenderer.DANGER_COLOR, RadiationMeterSpecialRenderer.colorForPercent(75));
    }

    @Test
    void glyphIndex_mapsDigitsDashAndPercent() {
        assertEquals(0, RadiationMeterSpecialRenderer.glyphIndex('0'));
        assertEquals(9, RadiationMeterSpecialRenderer.glyphIndex('9'));
        assertEquals(10, RadiationMeterSpecialRenderer.glyphIndex('-'));
        assertEquals(11, RadiationMeterSpecialRenderer.glyphIndex('%'));
    }

    @Test
    void glyphQuad_windsCounterClockwiseFromNegativeZ() {
        float[] xs = RadiationMeterSpecialRenderer.glyphQuadXs(0.0F, 0.1F);
        assertEquals(0.0F, xs[0]);
        assertEquals(0.1F, xs[1]);
        assertEquals(0.1F, xs[2]);
        assertEquals(0.0F, xs[3]);
    }

    @Test
    void glyphQuad_flipsUToUndoItemPoseMirror() {
        float[] us = RadiationMeterSpecialRenderer.glyphQuadUs(0.0F, 0.1F);
        assertEquals(0.1F, us[0]);
        assertEquals(0.0F, us[1]);
        assertEquals(0.0F, us[2]);
        assertEquals(0.1F, us[3]);
    }

    @Test
    void firstGlyph_isPlacedOnTheRightToUndoItemPoseMirror() {
        assertTrue(RadiationMeterSpecialRenderer.firstGlyphX(3) > 0.0F);
    }

    @Test
    void extents_coverTheWholeHandheldModel() {
        List<Vector3fc> corners = new ArrayList<>();
        RadiationMeterSpecialRenderer.emitExtents(corners::add);
        assertEquals(8, corners.size());
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        for (Vector3fc corner : corners) {
            minX = Math.min(minX, corner.x());
            minY = Math.min(minY, corner.y());
            maxY = Math.max(maxY, corner.y());
            maxZ = Math.max(maxZ, corner.z());
        }
        assertEquals(0.125F, minX);
        assertEquals(0.0F, minY);
        assertEquals(1.0625F, maxY);
        assertEquals(0.6875F, maxZ);
    }
}
