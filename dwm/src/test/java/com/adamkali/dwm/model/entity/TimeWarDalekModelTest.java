package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.model.json.EntityModelJson;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeWarDalekModelTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void bodyLayerKeepsTheDalekPartNamesAndAddsShoulderSlats() {
        ModelPart root = EntityModelJson.loadClasspath(TimeWarDalekModel.LAYER_LOCATION.model()).bakeRoot();
        assertTrue(root.hasChild("skirt"));
        assertTrue(root.getChild("shoulders").hasChild("gun"));
        assertTrue(root.getChild("shoulders").hasChild("plunger"));
        assertTrue(root.getChild("shoulders").hasChild("slats"));
        assertTrue(root.hasChild("neck"));
        assertTrue(root.getChild("head").hasChild("eyestalk"));
        assertTrue(root.getChild("head").hasChild("light_left"));
        assertTrue(root.getChild("head").hasChild("light_right"));
    }

    @Test
    void the1963CasingHasNoShoulderSlats() {
        ModelPart root = EntityModelJson.loadClasspath(DalekModel.LAYER_LOCATION.model()).bakeRoot();
        assertFalse(root.getChild("shoulders").hasChild("slats"));
    }

    @Test
    void layerLocationUsesTimeWarDalekId() {
        assertEquals("time_war_dalek", TimeWarDalekModel.LAYER_LOCATION.model().getPath());
        assertEquals("main", TimeWarDalekModel.LAYER_LOCATION.layer());
    }

    @Test
    void sharesTheClassicHeight() {
        assertEquals(DalekMeshes.height(DalekModel.LAYER_LOCATION), DalekMeshes.height(TimeWarDalekModel.LAYER_LOCATION), 1.0e-4F);
    }
}
