package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.model.json.EntityModelJson;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParadigmDalekModelTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void bodyLayerKeepsTheDalekPartNames() {
        ModelPart root = EntityModelJson.loadClasspath(ParadigmDalekModel.LAYER_LOCATION.model()).bakeRoot();
        assertTrue(root.hasChild("skirt"));
        assertTrue(root.hasChild("shoulders"));
        assertTrue(root.getChild("shoulders").hasChild("gun"));
        assertTrue(root.getChild("shoulders").hasChild("plunger"));
        assertTrue(root.hasChild("neck"));
        assertTrue(root.hasChild("head"));
        assertTrue(root.getChild("head").hasChild("eyestalk"));
        assertTrue(root.getChild("head").hasChild("light_left"));
        assertTrue(root.getChild("head").hasChild("light_right"));
    }

    @Test
    void layerLocationUsesParadigmDalekId() {
        assertEquals("paradigm_dalek", ParadigmDalekModel.LAYER_LOCATION.model().getPath());
        assertEquals("main", ParadigmDalekModel.LAYER_LOCATION.layer());
    }

    @Test
    void meshTowersOverBothClassicCasings() {
        float paradigmHeight = DalekMeshes.height(ParadigmDalekModel.LAYER_LOCATION);
        // Matches the 2.4-block hitbox to within a pixel.
        assertEquals(2.4F * 16.0F, paradigmHeight, 1.0F);
        for (ModelLayerLocation classic : new ModelLayerLocation[]{DalekModel.LAYER_LOCATION, TimeWarDalekModel.LAYER_LOCATION}) {
            float classicHeight = DalekMeshes.height(classic);
            assertTrue(paradigmHeight >= 1.35F * classicHeight, paradigmHeight + " vs " + classicHeight + " for " + classic);
            assertTrue(
                    DalekMeshes.width(ParadigmDalekModel.LAYER_LOCATION) > DalekMeshes.width(classic),
                    "Paradigm skirt should be wider than " + classic
            );
        }
    }
}
