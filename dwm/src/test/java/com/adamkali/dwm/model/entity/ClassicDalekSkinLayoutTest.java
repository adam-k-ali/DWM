package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.model.json.EntityModelCube;
import com.adamkali.dwm.model.json.EntityModelFile;
import net.minecraft.client.model.geom.ModelLayerLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The classic casings are painted per cube (panel seams, shaded tiers, dome-shaded hemispheres), so
 * every distinct cube needs its own UV island. Identical repeats may share one; a cube may never
 * paint over another's island, which is how the old flat-swatch skin worked.
 */
class ClassicDalekSkinLayoutTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    private static final List<ModelLayerLocation> CLASSIC_LAYERS =
            List.of(DalekModel.LAYER_LOCATION, TimeWarDalekModel.LAYER_LOCATION);

    @Test
    void atlasIs128By64() {
        for (ModelLayerLocation layer : CLASSIC_LAYERS) {
            EntityModelFile file = DalekMeshes.file(layer);
            assertEquals(128, file.textureWidth().orElseThrow(), layer + " width");
            assertEquals(64, file.textureHeight().orElseThrow(), layer + " height");
        }
    }

    @Test
    void cubesOnlyShareAnIslandWithIdenticalCubes() {
        for (ModelLayerLocation layer : CLASSIC_LAYERS) {
            assertIslandsDistinct(layer);
        }
    }

    private static void assertIslandsDistinct(ModelLayerLocation layer) {
        List<EntityModelCube> cubes = DalekMeshes.cubes(layer);
        for (EntityModelCube cube : cubes) {
            int[] r = island(cube);
            assertTrue(r[0] >= 0 && r[1] >= 0 && r[2] <= 128 && r[3] <= 64, "island off the atlas at " + cube.uv());
        }
        for (int i = 0; i < cubes.size(); i++) {
            for (int j = i + 1; j < cubes.size(); j++) {
                EntityModelCube a = cubes.get(i);
                EntityModelCube b = cubes.get(j);
                if (a.uv().equals(b.uv()) && a.size().equals(b.size())) {
                    continue;
                }
                int[] ra = island(a);
                int[] rb = island(b);
                if (ra[0] < rb[2] && rb[0] < ra[2] && ra[1] < rb[3] && rb[1] < ra[3]) {
                    fail(layer.model() + ": cube at " + a.uv() + " overlaps the island of the cube at " + b.uv());
                }
            }
        }
    }

    /** Box-UV footprint {@code [u0, v0, u1, v1]} of a cube. */
    private static int[] island(EntityModelCube cube) {
        int w = (int) Math.ceil(cube.size().x());
        int h = (int) Math.ceil(cube.size().y());
        int d = (int) Math.ceil(cube.size().z());
        int u = cube.uv().u();
        int v = cube.uv().v();
        return new int[]{u, v, u + 2 * (w + d), v + d + h};
    }
}
