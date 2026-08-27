package com.adamkali.dwm.tardis.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.LevelReader;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class PortalLightDataTest {
    @Test
    void sample_packsBothLayersAcrossBoundedVolume() {
        LevelReader level = Mockito.mock(LevelReader.class);
        Mockito.when(level.getBrightness(eq(LightLayer.BLOCK), any(BlockPos.class)))
                .thenAnswer(invocation -> invocation.<BlockPos>getArgument(1).getX() & 15);
        Mockito.when(level.getBrightness(eq(LightLayer.SKY), any(BlockPos.class)))
                .thenAnswer(invocation -> invocation.<BlockPos>getArgument(1).getY() & 15);

        PortalLightData data = PortalLightData.sample(
                level, new BlockPos(3, 5, 7), new BlockPos(4, 6, 8)
        );

        assertEquals(2, data.sizeX());
        assertEquals(2, data.sizeY());
        assertEquals(2, data.sizeZ());
        assertEquals(4, data.brightness(LightLayer.BLOCK, new BlockPos(4, 6, 8), -1));
        assertEquals(6, data.brightness(LightLayer.SKY, new BlockPos(4, 6, 8), -1));
        assertEquals(-1, data.brightness(LightLayer.SKY, new BlockPos(5, 6, 8), -1));
    }

    @Test
    void translated_preservesValuesInNewCoordinateSpace() {
        PortalLightData data = new PortalLightData(
                new BlockPos(10, 20, 30), 1, 1, 1, new byte[]{PortalLightData.pack(4, 12)}
        ).translated(new BlockPos(-10, -20, -30));

        assertEquals(BlockPos.ZERO, data.min());
        assertEquals(4, data.brightness(LightLayer.BLOCK, BlockPos.ZERO, -1));
        assertEquals(12, data.brightness(LightLayer.SKY, BlockPos.ZERO, -1));
    }

    @Test
    void pack_rejectsValuesOutsideNibbleRange() {
        assertThrows(IllegalArgumentException.class, () -> PortalLightData.pack(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> PortalLightData.pack(0, 16));
    }
}
