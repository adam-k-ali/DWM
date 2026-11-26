package com.adamkali.dwm.tardis.interior.migration;

import com.adamkali.dwm.tardis.interior.PlotSlot;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Which region files and chunk columns a plot window touches, and where it ends. */
class TardisPlotPreparerTest {
    @Test
    void windowBoxIsTheWholeWindow() {
        BlockPos anchor = TardisPlotAllocator.plotOrigin(new PlotSlot(0, 0));
        InteriorBlockOps.Box box = TardisPlotPreparer.windowBox(anchor);
        assertEquals(new InteriorBlockOps.Box(-4995, 5004, -4995, 5004), box);
        assertTrue(box.contains(new BlockPos(-4995, -64, 5004)));
        assertFalse(box.contains(new BlockPos(-4996, 64, 0)));
        assertFalse(box.contains(new BlockPos(0, 64, 5005)));
    }

    @Test
    void regionBoundsCoverEveryChunkOfTheWindowIncludingNegativeOnes() {
        InteriorBlockOps.Box box = TardisPlotPreparer.windowBox(TardisPlotAllocator.plotOrigin(new PlotSlot(0, 0)));
        // x -4995 is chunk -313 (region -10); x 5004 is chunk 312 (region 9).
        assertArrayEquals(new int[]{-10, 9, -10, 9}, TardisPlotPreparer.regionBounds(box));
        Set<ChunkPos> all = new HashSet<>();
        int[] regions = TardisPlotPreparer.regionBounds(box);
        for (int rx = regions[0]; rx <= regions[1]; rx++) {
            for (int rz = regions[2]; rz <= regions[3]; rz++) {
                for (ChunkPos chunk : TardisPlotPreparer.chunksInRegion(box, rx, rz)) {
                    assertTrue(all.add(chunk), "chunk listed twice: " + chunk);
                    assertEquals(rx, Math.floorDiv(chunk.x(), 32));
                    assertEquals(rz, Math.floorDiv(chunk.z(), 32));
                }
            }
        }
        int chunksPerEdge = (5004 >> 4) - (-4995 >> 4) + 1;
        assertEquals(626, chunksPerEdge);
        assertEquals(chunksPerEdge * chunksPerEdge, all.size());
        assertTrue(all.contains(new ChunkPos(-313, -313)));
        assertTrue(all.contains(new ChunkPos(312, 312)));
        assertFalse(all.contains(new ChunkPos(313, 0)));
    }

    @Test
    void chunksInARegionOnTheWindowEdgeAreClipped() {
        BlockPos anchor = TardisPlotAllocator.plotOrigin(new PlotSlot(3, -2));
        InteriorBlockOps.Box box = TardisPlotPreparer.windowBox(anchor);
        int[] regions = TardisPlotPreparer.regionBounds(box);
        List<ChunkPos> corner = TardisPlotPreparer.chunksInRegion(box, regions[0], regions[2]);
        for (ChunkPos chunk : corner) {
            assertTrue(chunk.getMaxBlockX() >= box.minX() && chunk.getMinBlockX() <= box.maxX(), chunk.toString());
            assertTrue(chunk.getMaxBlockZ() >= box.minZ() && chunk.getMinBlockZ() <= box.maxZ(), chunk.toString());
        }
        assertFalse(corner.isEmpty());
        assertTrue(corner.size() <= 32 * 32);
        // A region entirely outside the window has no chunks to scan.
        assertTrue(TardisPlotPreparer.chunksInRegion(box, regions[1] + 1, regions[2]).isEmpty());
    }

    @Test
    void onlyFullyGeneratedSavedChunksCountAsExisting() {
        com.adamkali.dwm.MinecraftTestBootstrap.ensure();
        assertTrue(TardisPlotPreparer.isFull("minecraft:full"));
        assertTrue(TardisPlotPreparer.isFull("full"));
        assertFalse(TardisPlotPreparer.isFull("minecraft:noise"));
        assertFalse(TardisPlotPreparer.isFull("minecraft:features"));
        assertFalse(TardisPlotPreparer.isFull(""));
        assertFalse(TardisPlotPreparer.isFull("Not A Status!"));
    }

    @Test
    void legacyWindowBoxMatchesTheLegacyAllocator() {
        BlockPos legacy = new BlockPos(640, 64, 1280);
        InteriorBlockOps.Box box = InteriorBlockOps.legacyWindowBox(legacy);
        assertEquals(new InteriorBlockOps.Box(613, 676, 1253, 1316), box);
        for (BlockPos pos : List.of(new BlockPos(613, 0, 1253), new BlockPos(676, 300, 1316), new BlockPos(612, 64, 1300))) {
            assertEquals(TardisPlotAllocator.isInsideLegacyWindow(pos, legacy), box.contains(pos), pos.toString());
        }
    }
}
