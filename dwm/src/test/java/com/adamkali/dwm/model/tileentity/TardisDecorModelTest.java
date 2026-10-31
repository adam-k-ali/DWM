package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.model.json.EntityModelJson;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TardisDecorModelTest {
    @Test
    void globeModel_bakesWithStandAndGlobeGroups() {
        ModelPart root = assertDoesNotThrow(() -> EntityModelJson.loadClasspath(TardisGlobeModel.LAYER_LOCATION.model()).bakeRoot());
        assertTrue(root.hasChild("mesh"));
        assertTrue(root.hasChild("Globe"));
        assertTrue(root.hasChild("Globe2"));
        assertTrue(root.hasChild("Globe3"));
        assertTrue(root.hasChild("arrow"));
        assertTrue(root.hasChild("arrow2"));
    }

    @Test
    void compactScannerModel_bakesMonitorParts() {
        ModelPart root = assertDoesNotThrow(() -> TardisCompactScannerModel.getTexturedModelData().bakeRoot());
        assertMonitorParts(root);
    }

    @Test
    void fullScannerModel_bakesMonitorParts() {
        ModelPart root = assertDoesNotThrow(() -> TardisFullScannerModel.getTexturedModelData().bakeRoot());
        assertMonitorParts(root);
    }

    @Test
    void layersAndTextures_areDistinctWhereExpected() {
        assertNotEquals(TardisGlobeModel.LAYER_LOCATION, TardisFullScannerModel.LAYER_LOCATION);
        assertNotEquals(TardisCompactScannerModel.LAYER_LOCATION, TardisFullScannerModel.LAYER_LOCATION);
        assertNotEquals(TardisCompactScannerModel.TEXTURE_LOCATION, TardisFullScannerModel.TEXTURE_LOCATION);
        assertEquals("tardis_globe", TardisGlobeModel.LAYER_LOCATION.model().getPath());
        assertEquals("tardis_compact_scanner", TardisCompactScannerModel.LAYER_LOCATION.model().getPath());
        assertEquals("tardis_full_scanner", TardisFullScannerModel.LAYER_LOCATION.model().getPath());
    }

    @Test
    void monitorTextures_matchDeclaredSizeAndGlowTexelIsWhite() throws IOException {
        assertGlowTexel(TardisCompactScannerModel.TEXTURE_LOCATION, TardisCompactScannerModel.TEXTURE_WIDTH,
                TardisCompactScannerModel.TEXTURE_HEIGHT, TardisCompactScannerModel.GLOW_U,
                TardisCompactScannerModel.GLOW_V);
        assertGlowTexel(TardisFullScannerModel.TEXTURE_LOCATION, TardisFullScannerModel.TEXTURE_WIDTH,
                TardisFullScannerModel.TEXTURE_HEIGHT, TardisFullScannerModel.GLOW_U,
                TardisFullScannerModel.GLOW_V);
    }

    @Test
    void screens_fitInsideTheirBlockShapes() {
        // Compact: 1x1 footprint (model x/z -8..8), 2 tall.
        assertScreenWithin(TardisCompactScannerModel.SCREEN, 8.0F);
        // Full: 3 wide (model x -24..24), 2 tall.
        assertScreenWithin(TardisFullScannerModel.SCREEN, 24.0F);
        assertScreenWithin(TardisFullScannerModel.LEFT_WING, 24.0F);
        assertScreenWithin(TardisFullScannerModel.RIGHT_WING, 24.0F);
        // Viewer's left wing sits at +X (the viewer looks toward +Z).
        assertTrue(TardisFullScannerModel.LEFT_WING.minX() > TardisFullScannerModel.SCREEN.maxX());
        assertTrue(TardisFullScannerModel.RIGHT_WING.maxX() < TardisFullScannerModel.SCREEN.minX());
    }

    private static void assertMonitorParts(ModelPart root) {
        assertTrue(root.hasChild("monitor"));
        ModelPart monitor = root.getChild("monitor");
        assertEquals((float) Math.PI, monitor.zRot, 1e-6F);
        for (String part : new String[] {"body", "screen", "bezel", "controls"}) {
            assertTrue(monitor.hasChild(part), part);
        }
    }

    private static void assertScreenWithin(MonitorScreenRect rect, float halfWidth) {
        assertTrue(rect.width() > 0.0F && rect.height() > 0.0F);
        assertTrue(rect.minX() >= -halfWidth && rect.maxX() <= halfWidth);
        assertTrue(rect.minY() >= 0.0F && rect.maxY() <= 32.0F);
        assertTrue(rect.frontZ() >= -8.5F && rect.frontZ() <= 8.0F);
    }

    private static void assertGlowTexel(Identifier texture, int width, int height, float u, float v) throws IOException {
        Path path = Path.of("src/client/resources/assets", texture.getNamespace(), texture.getPath());
        try (InputStream in = Files.newInputStream(path)) {
            BufferedImage image = ImageIO.read(in);
            assertEquals(width, image.getWidth(), path.toString());
            assertEquals(height, image.getHeight(), path.toString());
            int argb = image.getRGB((int) (u * width), (int) (v * height));
            assertEquals(0xFFFFFFFF, argb, path.toString());
        }
    }
}
