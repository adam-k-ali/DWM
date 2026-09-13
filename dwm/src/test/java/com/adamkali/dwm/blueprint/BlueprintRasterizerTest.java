package com.adamkali.dwm.blueprint;

import com.adamkali.dwm.blueprint.model.Blueprint;
import com.adamkali.dwm.blueprint.model.BlueprintBlock;
import com.adamkali.dwm.blueprint.model.BlueprintLocation;
import com.adamkali.dwm.blueprint.model.CircleShape;
import com.adamkali.dwm.blueprint.model.DomeShape;
import com.adamkali.dwm.blueprint.model.PolygonShape;
import com.adamkali.dwm.blueprint.model.RectangleShape;
import com.adamkali.dwm.blueprint.model.SphereShape;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlueprintRasterizerTest {
    private static final BlueprintBlock STONE = new BlueprintBlock("minecraft:stone");
    private static final BlueprintBlock DIRT = new BlueprintBlock("minecraft:dirt");

    @Test
    void filledCircle_includesCenterAndExcludesOutside() {
        Blueprint bp = new Blueprint(List.of(
                new CircleShape(new BlueprintLocation(0, 0, 0), STONE, true, 2)
        ));
        Map<BlockPos, BlueprintBlock> voxels = BlueprintRasterizer.rasterize(bp);
        assertEquals(STONE, voxels.get(new BlockPos(0, 0, 0)));
        assertEquals(STONE, voxels.get(new BlockPos(2, 0, 0)));
        assertFalse(voxels.containsKey(new BlockPos(3, 0, 0)));
        assertFalse(voxels.containsKey(new BlockPos(0, 1, 0)));
    }

    @Test
    void hollowCircle_isRingOnly() {
        Blueprint bp = new Blueprint(List.of(
                new CircleShape(new BlueprintLocation(0, 0, 0), STONE, false, 3)
        ));
        Map<BlockPos, BlueprintBlock> voxels = BlueprintRasterizer.rasterize(bp);
        assertFalse(voxels.containsKey(new BlockPos(0, 0, 0)), "center should be hollow");
        assertTrue(voxels.containsKey(new BlockPos(3, 0, 0)) || voxels.containsKey(new BlockPos(2, 0, 2)));
    }

    @Test
    void filledRectangle_isInclusiveCuboid() {
        Blueprint bp = new Blueprint(List.of(
                new RectangleShape(
                        new BlueprintLocation(0, 0, 0),
                        new BlueprintLocation(1, 0, 1),
                        STONE,
                        true
                )
        ));
        Map<BlockPos, BlueprintBlock> voxels = BlueprintRasterizer.rasterize(bp);
        assertEquals(4, voxels.size());
        assertTrue(voxels.containsKey(new BlockPos(0, 0, 0)));
        assertTrue(voxels.containsKey(new BlockPos(1, 0, 1)));
    }

    @Test
    void hollowRectangle_isShellNotWireframe() {
        Blueprint bp = new Blueprint(List.of(
                new RectangleShape(
                        new BlueprintLocation(0, 0, 0),
                        new BlueprintLocation(2, 2, 2),
                        STONE,
                        false
                )
        ));
        Map<BlockPos, BlueprintBlock> voxels = BlueprintRasterizer.rasterize(bp);
        assertFalse(voxels.containsKey(new BlockPos(1, 1, 1)), "interior should be empty");
        assertTrue(voxels.containsKey(new BlockPos(0, 0, 0)));
        assertTrue(voxels.containsKey(new BlockPos(1, 0, 1)), "face center should be present");
        assertTrue(voxels.containsKey(new BlockPos(2, 2, 2)));
    }

    @Test
    void filledSphere_includesCenter() {
        Blueprint bp = new Blueprint(List.of(
                new SphereShape(new BlueprintLocation(0, 0, 0), STONE, true, 2)
        ));
        Map<BlockPos, BlueprintBlock> voxels = BlueprintRasterizer.rasterize(bp);
        assertEquals(STONE, voxels.get(new BlockPos(0, 0, 0)));
        assertTrue(voxels.containsKey(new BlockPos(0, 2, 0)));
        assertFalse(voxels.containsKey(new BlockPos(0, 3, 0)));
    }

    @Test
    void hollowSphere_excludesDeepInterior() {
        Blueprint bp = new Blueprint(List.of(
                new SphereShape(new BlueprintLocation(0, 0, 0), STONE, false, 4)
        ));
        Map<BlockPos, BlueprintBlock> voxels = BlueprintRasterizer.rasterize(bp);
        assertFalse(voxels.containsKey(new BlockPos(0, 0, 0)));
        assertTrue(voxels.containsKey(new BlockPos(0, 4, 0)) || voxels.containsKey(new BlockPos(4, 0, 0)));
    }

    @Test
    void dome_onlyUpperHemisphere() {
        Blueprint bp = new Blueprint(List.of(
                new DomeShape(new BlueprintLocation(0, 0, 0), STONE, true, 3)
        ));
        Map<BlockPos, BlueprintBlock> voxels = BlueprintRasterizer.rasterize(bp);
        assertTrue(voxels.containsKey(new BlockPos(0, 0, 0)));
        assertTrue(voxels.containsKey(new BlockPos(0, 3, 0)));
        assertFalse(voxels.containsKey(new BlockPos(0, -1, 0)));
    }

    @Test
    void filledPentagon_includesCenter() {
        Blueprint bp = new Blueprint(List.of(
                new PolygonShape(5, new BlueprintLocation(0, 0, 0), STONE, true, 4)
        ));
        Map<BlockPos, BlueprintBlock> voxels = BlueprintRasterizer.rasterize(bp);
        assertTrue(voxels.containsKey(new BlockPos(0, 0, 0)));
        assertFalse(voxels.containsKey(new BlockPos(0, 1, 0)));
    }

    @Test
    void laterShape_overwritesEarlierAtSameVoxel() {
        Blueprint bp = new Blueprint(List.of(
                new RectangleShape(
                        new BlueprintLocation(0, 0, 0),
                        new BlueprintLocation(0, 0, 0),
                        STONE,
                        true
                ),
                new RectangleShape(
                        new BlueprintLocation(0, 0, 0),
                        new BlueprintLocation(0, 0, 0),
                        DIRT,
                        true
                )
        ));
        Map<BlockPos, BlueprintBlock> voxels = BlueprintRasterizer.rasterize(bp);
        assertEquals(DIRT, voxels.get(new BlockPos(0, 0, 0)));
    }

    @Test
    void radiusZero_placesSingleBlockAtOrigin() {
        Blueprint bp = new Blueprint(List.of(
                new CircleShape(new BlueprintLocation(1, 2, 3), STONE, true, 0)
        ));
        Map<BlockPos, BlueprintBlock> voxels = BlueprintRasterizer.rasterize(bp);
        assertEquals(1, voxels.size());
        assertEquals(STONE, voxels.get(new BlockPos(1, 2, 3)));
    }

    @Test
    void relativeOffsets_preservedFromShapeOrigin() {
        Blueprint bp = new Blueprint(List.of(
                new RectangleShape(
                        new BlueprintLocation(5, 10, 15),
                        new BlueprintLocation(5, 10, 15),
                        STONE,
                        true
                )
        ));
        Map<BlockPos, BlueprintBlock> voxels = BlueprintRasterizer.rasterize(bp);
        assertTrue(voxels.containsKey(new BlockPos(5, 10, 15)));
    }
}
