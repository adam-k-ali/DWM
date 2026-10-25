package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.model.json.EntityModelJson;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekMutantModelTest {
    private static final int TENTACLES = 6;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void bodyLayerHasBodyEyeBrainAndTentacles() {
        ModelPart root = EntityModelJson.loadClasspath(DalekMutantModel.LAYER_LOCATION.model()).bakeRoot();
        ModelPart body = root.getChild("body");
        assertTrue(body.hasChild("eye"));
        assertTrue(body.hasChild("brain"));
        for (int i = 1; i <= TENTACLES; i++) {
            assertTrue(root.hasChild("tentacle_" + i), "tentacle_" + i);
            assertTrue(root.getChild("tentacle_" + i).hasChild("tip"), "tentacle_" + i + "/tip");
        }
        assertTrue(!root.hasChild("tentacle_" + (TENTACLES + 1)));
    }

    @Test
    void layerLocationUsesDalekMutantId() {
        assertEquals("dalek_mutant", DalekMutantModel.LAYER_LOCATION.model().getPath());
        assertEquals("main", DalekMutantModel.LAYER_LOCATION.layer());
    }
}
