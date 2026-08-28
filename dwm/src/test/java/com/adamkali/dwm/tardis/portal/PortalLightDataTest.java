package com.adamkali.dwm.tardis.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
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
    void sample_fromDataLayersPacksNibbleValues() {
        DataLayer blockLayer = new DataLayer();
        DataLayer skyLayer = new DataLayer();
        blockLayer.set(1, 2, 3, 7);
        skyLayer.set(1, 2, 3, 12);
        LayerLightEventListener blockLight = Mockito.mock(LayerLightEventListener.class);
        LayerLightEventListener skyLight = Mockito.mock(LayerLightEventListener.class);
        Mockito.when(blockLight.getDataLayerData(any())).thenReturn(blockLayer);
        Mockito.when(skyLight.getDataLayerData(any())).thenReturn(skyLayer);

        PortalLightData data = PortalLightData.sample(
                blockLight, skyLight, new BlockPos(1, 2, 3), new BlockPos(1, 2, 3));

        assertEquals(7, data.brightness(LightLayer.BLOCK, new BlockPos(1, 2, 3), -1));
        assertEquals(12, data.brightness(LightLayer.SKY, new BlockPos(1, 2, 3), -1));
    }

    @Test
    void sample_nullLayersReadAsZero() {
        LayerLightEventListener blockLight = Mockito.mock(LayerLightEventListener.class);
        LayerLightEventListener skyLight = Mockito.mock(LayerLightEventListener.class);
        Mockito.when(blockLight.getDataLayerData(any())).thenReturn(null);
        Mockito.when(skyLight.getDataLayerData(any())).thenReturn(null);

        PortalLightData data = PortalLightData.sample(
                blockLight, skyLight, BlockPos.ZERO, BlockPos.ZERO);

        assertEquals(0, data.brightness(LightLayer.BLOCK, BlockPos.ZERO, -1));
        assertEquals(0, data.brightness(LightLayer.SKY, BlockPos.ZERO, -1));
    }

    @Test
    void sample_fromDataLayersCopiesAcrossSectionBoundary() {
        DataLayer blockLow = new DataLayer();
        DataLayer skyLow = new DataLayer();
        DataLayer blockHigh = new DataLayer();
        DataLayer skyHigh = new DataLayer();
        blockLow.set(15, 15, 15, 3);
        skyLow.set(15, 15, 15, 4);
        blockHigh.set(0, 0, 0, 9);
        skyHigh.set(0, 0, 0, 10);
        LayerLightEventListener blockLight = Mockito.mock(LayerLightEventListener.class);
        LayerLightEventListener skyLight = Mockito.mock(LayerLightEventListener.class);
        Mockito.when(blockLight.getDataLayerData(any())).thenAnswer(invocation -> {
            net.minecraft.core.SectionPos section = invocation.getArgument(0);
            return section.y() < 1 ? blockLow : blockHigh;
        });
        Mockito.when(skyLight.getDataLayerData(any())).thenAnswer(invocation -> {
            net.minecraft.core.SectionPos section = invocation.getArgument(0);
            return section.y() < 1 ? skyLow : skyHigh;
        });

        PortalLightData data = PortalLightData.sample(
                blockLight, skyLight, new BlockPos(15, 15, 15), new BlockPos(16, 16, 16));

        assertEquals(3, data.brightness(LightLayer.BLOCK, new BlockPos(15, 15, 15), -1));
        assertEquals(4, data.brightness(LightLayer.SKY, new BlockPos(15, 15, 15), -1));
        assertEquals(9, data.brightness(LightLayer.BLOCK, new BlockPos(16, 16, 16), -1));
        assertEquals(10, data.brightness(LightLayer.SKY, new BlockPos(16, 16, 16), -1));
        assertEquals(0, data.brightness(LightLayer.BLOCK, new BlockPos(16, 15, 15), -1));
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
